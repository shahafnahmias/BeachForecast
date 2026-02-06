# Open-Meteo Marine & Weather API Documentation

## Overview
- **Provider:** Open-Meteo (open-source weather API)
- **Type:** Forecast data (predictions)
- **Authentication:** None required (public API)
- **Rate Limits:** Reasonable fair use (no strict limits documented)
- **Cost:** Free
- **Update Frequency:** Hourly forecasts updated every ~15 minutes

## API Endpoints

### 1. Marine API
**Endpoint:** `https://marine-api.open-meteo.com/v1/marine`

**Parameters:**
- `latitude` (required): Decimal degrees (e.g., 32.0853)
- `longitude` (required): Decimal degrees (e.g., 34.7818)
- `hourly` (required): Comma-separated list of variables
- `forecast_days` (optional): Number of days (default 7, max 7)

**Hourly Variables Available:**
- `wave_height` - Significant wave height (meters)
- `wave_direction` - Wave direction (degrees, 0-360)
- `wave_period` - Wave period (seconds)
- `swell_wave_height` - Swell wave height (meters)
- `swell_wave_direction` - Swell direction (degrees)
- `swell_wave_period` - Swell period (seconds)
- `sea_surface_temperature` - SST (Celsius)

**Example Request:**
```
GET https://marine-api.open-meteo.com/v1/marine?latitude=32.0853&longitude=34.7818&hourly=wave_height,wave_direction,wave_period,swell_wave_height,swell_wave_direction,swell_wave_period,sea_surface_temperature&forecast_days=7
```

**Response Structure:**
```json
{
  "latitude": 32.0853,
  "longitude": 34.7818,
  "generationtime_ms": 0.123,
  "utc_offset_seconds": 0,
  "timezone": "GMT",
  "timezone_abbreviation": "GMT",
  "elevation": 0.0,
  "hourly_units": {
    "time": "iso8601",
    "wave_height": "m",
    "wave_direction": "°",
    "wave_period": "s",
    "swell_wave_height": "m",
    "swell_wave_direction": "°",
    "swell_wave_period": "s",
    "sea_surface_temperature": "°C"
  },
  "hourly": {
    "time": [
      "2025-12-05T00:00",
      "2025-12-05T01:00",
      ...168 timestamps...
      "2025-12-12T00:00"
    ],
    "wave_height": [1.2, 1.25, 1.18, ...168 values...],
    "wave_direction": [210, 215, 208, ...168 values...],
    "wave_period": [8.5, 8.6, 8.4, ...168 values...],
    "swell_wave_height": [0.8, 0.82, 0.79, ...168 values...],
    "swell_wave_direction": [200, 205, 198, ...168 values...],
    "swell_wave_period": [12.0, 12.1, 11.9, ...168 values...],
    "sea_surface_temperature": [18.5, 18.4, 18.3, ...168 values...]
  }
}
```

**Data Size:** ~9 KB per request

### 2. Weather Forecast API
**Endpoint:** `https://api.open-meteo.com/v1/forecast`

**Parameters:**
- `latitude` (required): Decimal degrees
- `longitude` (required): Decimal degrees
- `hourly` (required): Comma-separated variables
- `forecast_days` (optional): Default 7, max 16

**Hourly Variables Available:**
- `temperature_2m` - Temperature at 2m height (Celsius)
- `cloud_cover` - Total cloud cover (percentage)
- `wind_speed_10m` - Wind speed at 10m (km/h)
- `wind_direction_10m` - Wind direction (degrees)
- `uv_index` - UV index (dimensionless)

**Example Request:**
```
GET https://api.open-meteo.com/v1/forecast?latitude=32.0853&longitude=34.7818&hourly=temperature_2m,cloud_cover,wind_speed_10m,wind_direction_10m,uv_index&forecast_days=7
```

**Response Structure:**
```json
{
  "latitude": 32.0853,
  "longitude": 34.7818,
  "generationtime_ms": 0.089,
  "utc_offset_seconds": 0,
  "timezone": "GMT",
  "timezone_abbreviation": "GMT",
  "elevation": 10.0,
  "hourly_units": {
    "time": "iso8601",
    "temperature_2m": "°C",
    "cloud_cover": "%",
    "wind_speed_10m": "km/h",
    "wind_direction_10m": "°",
    "uv_index": ""
  },
  "hourly": {
    "time": ["2025-12-05T00:00", ...168 timestamps...],
    "temperature_2m": [22.5, 21.8, ...168 values...],
    "cloud_cover": [45, 50, 40, ...168 values...],
    "wind_speed_10m": [12.5, 13.2, ...168 values...],
    "wind_direction_10m": [180, 185, ...168 values...],
    "uv_index": [0.0, 0.0, 1.2, ...168 values...]
  }
}
```

**Data Size:** ~8.5 KB per request

## Data Characteristics
- **Temporal Resolution:** Hourly
- **Forecast Horizon:** 7 days (168 hours)
- **Missing Values:** Represented as `null`
- **Timestamp Format:** ISO 8601 (e.g., "2025-12-05T14:00")
- **Time Zone:** All timestamps in GMT (need to convert to local time)
- **Consistency:** All arrays have same length (168 elements)

## Error Handling
**HTTP Status Codes:**
- `200 OK` - Success
- `400 Bad Request` - Invalid parameters
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Server error

**Error Response:**
```json
{
  "error": true,
  "reason": "Cannot find station with coordinates 200.0, 300.0"
}
```

## Storage Requirements (Open-Meteo)
**Per Location, Per Fetch:**
- Marine API: ~9 KB
- Weather API: ~8.5 KB
- **Total per fetch:** ~18 KB

**Phase 1 Estimates (1 location, hourly):**
- Per day: 18 KB × 24 = 432 KB
- Per month: 432 KB × 30 = 12.96 MB
- **3 months: ~40 MB**
