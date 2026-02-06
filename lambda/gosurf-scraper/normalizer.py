"""
Data normalizer for GoSurf forecast data.

Converts GoSurf-specific data format to the unified schema used across all providers.
Maps to the database schema defined in docs/data-schema.md
"""

from datetime import datetime, timezone
from typing import Dict, List, Any, Optional
import uuid


class GoSurfNormalizer:
    """Normalizes GoSurf forecast data to unified schema."""

    # Hebrew direction mappings to degrees (approximate)
    DIRECTION_MAP = {
        'צפון': 0,
        'צפון מזרחי': 45,
        'מזרח': 90,
        'דרום מזרחי': 135,
        'דרום': 180,
        'דרום מערבי': 225,
        'מערב': 270,
        'צפון מערבי': 315,
    }

    def __init__(self):
        pass

    def normalize(self, gosurf_data: Dict[str, Any], location: Dict[str, str]) -> List[Dict[str, Any]]:
        """
        Normalize GoSurf data to unified forecast schema.

        Args:
            gosurf_data: Raw data from GoSurf scraper
            location: Location metadata (name, lat, lon)

        Returns:
            List of normalized forecast records (one per hour)
        """
        normalized_records = []

        beach_name = location.get('name', gosurf_data.get('beach_name', 'Unknown'))
        latitude = location.get('latitude')
        longitude = location.get('longitude')
        fetched_at = gosurf_data.get('fetched_at', datetime.now(timezone.utc).isoformat())

        # Process hourly forecasts
        for hourly_forecast in gosurf_data.get('hourly_forecasts', []):
            record = self._normalize_hourly_forecast(
                hourly_forecast,
                gosurf_data,
                beach_name,
                latitude,
                longitude,
                fetched_at
            )
            if record:
                normalized_records.append(record)

        return normalized_records

    def _normalize_hourly_forecast(
        self,
        hourly_forecast: Dict[str, Any],
        gosurf_data: Dict[str, Any],
        beach_name: str,
        latitude: Optional[float],
        longitude: Optional[float],
        fetched_at: str
    ) -> Optional[Dict[str, Any]]:
        """Normalize a single hourly forecast to unified schema."""

        # Parse time (this is simplified - in production, parse date properly)
        time_str = hourly_forecast.get('time', '00:00')

        # Build normalized record matching database schema
        record = {
            # Metadata
            'id': str(uuid.uuid4()),
            'provider': 'gosurf',
            'data_type': 'forecast',

            # Location
            'location_name': beach_name,
            'latitude': latitude,
            'longitude': longitude,
            'station_id': str(gosurf_data.get('spot_id')),

            # Timestamps
            'fetched_at': fetched_at,
            'forecast_time': self._parse_forecast_time(time_str, fetched_at),
            'issued_at': fetched_at,  # GoSurf doesn't provide issue time

            # Marine metrics
            'wave_height': self._extract_wave_height(hourly_forecast),
            'wave_direction': self._extract_wave_direction(hourly_forecast),
            'wave_period': self._extract_wave_period(hourly_forecast),
            'swell_height': None,  # GoSurf doesn't separate swell from total wave
            'swell_direction': self._extract_swell_direction(hourly_forecast),
            'swell_period': self._extract_swell_period(hourly_forecast),
            'sea_surface_temp': self._extract_water_temp(gosurf_data),

            # Weather metrics
            'temperature': hourly_forecast.get('temperature'),
            'temperature_max': None,
            'temperature_min': None,
            'ground_temperature': None,
            'humidity': None,
            'pressure': None,
            'wind_speed': self._extract_wind_speed(hourly_forecast),
            'wind_direction': self._extract_wind_direction(hourly_forecast),
            'wind_gust_max': None,
            'cloud_cover': None,
            'uv_index': None,
            'rainfall': None,

            # Solar radiation (not available from GoSurf)
            'global_radiation': None,
            'diffuse_radiation': None,
            'direct_radiation': None,

            # Data quality
            'completeness': self._calculate_completeness(hourly_forecast),
            'is_valid': True,
            'channel_status': 1,  # Assume valid

            # Raw data for debugging
            'raw_data': hourly_forecast,
        }

        return record

    def _parse_forecast_time(self, time_str: str, base_date_str: str) -> str:
        """
        Parse forecast time string to ISO 8601.

        Args:
            time_str: Time string (e.g., "15:00" or "15")
            base_date_str: Base date/time in ISO format

        Returns:
            ISO 8601 timestamp
        """
        # Simplified - in production, properly parse date from page
        # and combine with time
        base_date = datetime.fromisoformat(base_date_str.replace('Z', '+00:00'))

        # Parse hour from time string
        try:
            if ':' in time_str:
                hour = int(time_str.split(':')[0])
            else:
                hour = int(time_str)

            # Create forecast time (same date, specified hour)
            forecast_time = base_date.replace(hour=hour, minute=0, second=0, microsecond=0)

            return forecast_time.isoformat()
        except (ValueError, AttributeError):
            return base_date.isoformat()

    def _extract_wave_height(self, forecast: Dict[str, Any]) -> Optional[float]:
        """Extract average wave height in meters."""
        wave_data = forecast.get('wave_height')
        if wave_data:
            return wave_data.get('avg_m')
        return None

    def _extract_wave_direction(self, forecast: Dict[str, Any]) -> Optional[int]:
        """Extract wave direction in degrees."""
        swell_data = forecast.get('swell')
        if swell_data:
            direction_str = swell_data.get('direction', '')
            return self.DIRECTION_MAP.get(direction_str)
        return None

    def _extract_wave_period(self, forecast: Dict[str, Any]) -> Optional[float]:
        """Extract wave period in seconds."""
        swell_data = forecast.get('swell')
        if swell_data:
            return swell_data.get('period_seconds')
        return None

    def _extract_swell_direction(self, forecast: Dict[str, Any]) -> Optional[int]:
        """Extract swell direction in degrees (same as wave direction for GoSurf)."""
        return self._extract_wave_direction(forecast)

    def _extract_swell_period(self, forecast: Dict[str, Any]) -> Optional[float]:
        """Extract swell period in seconds (same as wave period for GoSurf)."""
        return self._extract_wave_period(forecast)

    def _extract_water_temp(self, gosurf_data: Dict[str, Any]) -> Optional[float]:
        """Extract water temperature from metadata."""
        metadata = gosurf_data.get('metadata', {})
        return metadata.get('water_temperature_c')

    def _extract_wind_speed(self, forecast: Dict[str, Any]) -> Optional[float]:
        """Extract wind speed in km/h."""
        wind_data = forecast.get('wind')
        if wind_data:
            return float(wind_data.get('speed_kmh', 0))
        return None

    def _extract_wind_direction(self, forecast: Dict[str, Any]) -> Optional[int]:
        """Extract wind direction in degrees."""
        wind_data = forecast.get('wind')
        if wind_data:
            direction_str = wind_data.get('direction', '')
            return self.DIRECTION_MAP.get(direction_str)
        return None

    def _calculate_completeness(self, forecast: Dict[str, Any]) -> float:
        """
        Calculate data completeness score (0.0 to 1.0).

        Checks how many fields are populated vs total expected fields.
        """
        total_fields = 13  # Expected marine + weather fields
        filled_fields = 0

        # Check which fields are present
        if forecast.get('wave_height'):
            filled_fields += 1
        if forecast.get('swell'):
            filled_fields += 2  # period and direction
        if forecast.get('temperature'):
            filled_fields += 1
        if forecast.get('wind'):
            filled_fields += 2  # speed and direction

        return round(filled_fields / total_fields, 2)


# Example usage
if __name__ == '__main__':
    import json

    # Sample GoSurf data
    sample_gosurf_data = {
        'beach_slug': 'betset',
        'beach_name': 'בצת',
        'spot_id': 7,
        'fetched_at': '2025-12-06T10:00:00Z',
        'hourly_forecasts': [
            {
                'time': '00:00',
                'wave_height': {
                    'min_cm': 30,
                    'max_cm': 50,
                    'avg_cm': 40,
                    'avg_m': 0.4
                },
                'wind': {
                    'speed_kmh': 16,
                    'speed_ms': 4.44,
                    'direction': 'דרום מזרחית'
                },
                'temperature': 20.0,
                'swell': {
                    'period_seconds': 5.2,
                    'direction': 'מערבי'
                }
            }
        ],
        'metadata': {
            'water_temperature_c': 22.6,
            'sunrise': '06:28',
            'sunset': '16:32'
        }
    }

    location = {
        'name': 'Betzet',
        'latitude': 33.0892,
        'longitude': 35.1064
    }

    normalizer = GoSurfNormalizer()
    normalized = normalizer.normalize(sample_gosurf_data, location)

    print("Normalized GoSurf Data:")
    print(json.dumps(normalized, indent=2, ensure_ascii=False))
