"""
Sea Level Widget Lambda handler.

Proxies and caches Open-Meteo marine + weather API calls.
Returns both responses merged into a single JSON object so the
Android app only needs one network round-trip.
"""

import asyncio
import json
import os
import time
from decimal import Decimal
from typing import Any

import boto3
import httpx
from boto3.dynamodb.conditions import Key

CACHE_TABLE = os.environ.get("CACHE_TABLE", "sea-level-cache")
CACHE_TTL_SECONDS = 600  # 10 minutes

MARINE_BASE_URL = "https://marine-api.open-meteo.com/v1/marine"
WEATHER_BASE_URL = "https://api.open-meteo.com/v1/forecast"

MARINE_HOURLY_PARAMS = ",".join([
    "wave_height",
    "wave_direction",
    "wave_period",
    "swell_wave_height",
    "swell_wave_direction",
    "swell_wave_period",
    "sea_surface_temperature",
])

WEATHER_HOURLY_PARAMS = ",".join([
    "temperature_2m",
    "cloud_cover",
    "wind_speed_10m",
    "wind_direction_10m",
    "uv_index",
])

dynamodb = boto3.resource("dynamodb")
table = dynamodb.Table(CACHE_TABLE)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_cache_key(lat: float, lon: float, days: int) -> str:
    """Round coords to 2 decimal places (~1.1 km grid) for cache grouping."""
    return f"{round(lat, 2)}:{round(lon, 2)}:{days}"


def _error_response(status: int, message: str) -> dict:
    return {
        "statusCode": status,
        "headers": {"Content-Type": "application/json"},
        "body": json.dumps({"error": message}),
    }


def _success_response(data: dict) -> dict:
    return {
        "statusCode": 200,
        "headers": {"Content-Type": "application/json"},
        "body": json.dumps(data),
    }


def _decimal_to_native(obj: Any) -> Any:
    """Convert DynamoDB Decimal types back to int/float for JSON serialisation."""
    if isinstance(obj, Decimal):
        if obj == int(obj):
            return int(obj)
        return float(obj)
    if isinstance(obj, dict):
        return {k: _decimal_to_native(v) for k, v in obj.items()}
    if isinstance(obj, list):
        return [_decimal_to_native(i) for i in obj]
    return obj


def _float_to_decimal(obj: Any) -> Any:
    """Convert floats to Decimal for DynamoDB storage."""
    if isinstance(obj, float):
        return Decimal(str(obj))
    if isinstance(obj, dict):
        return {k: _float_to_decimal(v) for k, v in obj.items()}
    if isinstance(obj, list):
        return [_float_to_decimal(i) for i in obj]
    return obj


# ---------------------------------------------------------------------------
# Cache
# ---------------------------------------------------------------------------

def _get_cached(cache_key: str) -> dict | None:
    """Return cached data if it exists and has not expired."""
    try:
        resp = table.get_item(Key={"pk": cache_key})
        item = resp.get("Item")
        if item and int(item.get("ttl", 0)) > int(time.time()):
            return _decimal_to_native(item["data"])
    except Exception:
        pass  # cache miss on error — just refetch
    return None


def _put_cached(cache_key: str, data: dict) -> None:
    """Store data in DynamoDB with a TTL."""
    try:
        table.put_item(
            Item={
                "pk": cache_key,
                "data": _float_to_decimal(data),
                "ttl": int(time.time()) + CACHE_TTL_SECONDS,
            }
        )
    except Exception:
        pass  # caching failure is non-fatal


# ---------------------------------------------------------------------------
# Open-Meteo fetchers
# ---------------------------------------------------------------------------

async def _fetch_marine(
    client: httpx.AsyncClient, lat: float, lon: float, days: int
) -> dict:
    resp = await client.get(
        MARINE_BASE_URL,
        params={
            "latitude": lat,
            "longitude": lon,
            "hourly": MARINE_HOURLY_PARAMS,
            "forecast_days": days,
        },
        timeout=10.0,
    )
    resp.raise_for_status()
    return resp.json()


async def _fetch_weather(
    client: httpx.AsyncClient, lat: float, lon: float, days: int
) -> dict:
    resp = await client.get(
        WEATHER_BASE_URL,
        params={
            "latitude": lat,
            "longitude": lon,
            "hourly": WEATHER_HOURLY_PARAMS,
            "forecast_days": days,
        },
        timeout=10.0,
    )
    resp.raise_for_status()
    return resp.json()


async def _fetch_both(lat: float, lon: float, days: int) -> dict:
    """Fetch marine and weather data in parallel."""
    async with httpx.AsyncClient() as client:
        marine_task = _fetch_marine(client, lat, lon, days)
        weather_task = _fetch_weather(client, lat, lon, days)
        marine_data, weather_data = await asyncio.gather(
            marine_task, weather_task
        )
    return {"marine": marine_data, "weather": weather_data}


# ---------------------------------------------------------------------------
# Lambda entry point
# ---------------------------------------------------------------------------

def handler(event: dict, context: Any) -> dict:
    """AWS Lambda handler for GET /weather."""
    params = event.get("queryStringParameters") or {}

    # --- Parse & validate query params ---
    try:
        lat = float(params["lat"])
        lon = float(params["lon"])
    except (KeyError, ValueError, TypeError):
        return _error_response(400, "Missing or invalid 'lat'/'lon' query parameters")

    if not (-90.0 <= lat <= 90.0 and -180.0 <= lon <= 180.0):
        return _error_response(400, "Coordinates out of range")

    days = int(params.get("days", 7))
    if days < 1 or days > 16:
        return _error_response(400, "'days' must be between 1 and 16")

    # --- Check cache ---
    cache_key = _make_cache_key(lat, lon, days)
    cached = _get_cached(cache_key)
    if cached is not None:
        return _success_response(cached)

    # --- Fetch from Open-Meteo ---
    try:
        combined = asyncio.get_event_loop().run_until_complete(
            _fetch_both(lat, lon, days)
        )
    except httpx.HTTPStatusError as exc:
        return _error_response(502, f"Open-Meteo API error: {exc.response.status_code}")
    except httpx.RequestError as exc:
        return _error_response(502, f"Failed to reach Open-Meteo: {exc}")
    except Exception as exc:
        return _error_response(500, f"Internal error: {exc}")

    # --- Cache & return ---
    _put_cached(cache_key, combined)
    return _success_response(combined)
