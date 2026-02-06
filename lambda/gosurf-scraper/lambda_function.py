"""
GoSurf Scraper Lambda Function

Scrapes surf forecast data from gosurf.co.il for Israeli beaches.
Extracts wave height, wind, swell, temperature, and other marine/weather data.

Author: Claude
Date: 2025-12-06
"""

import json
import logging
import re
from datetime import datetime, timezone
from typing import Dict, List, Optional, Any
from urllib.parse import urljoin

import requests
from bs4 import BeautifulSoup

# Configure logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

# Constants
GOSURF_BASE_URL = "https://gosurf.co.il"
USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"
TIMEOUT_SECONDS = 10


class GoSurfScraper:
    """Scraper for GoSurf forecast data."""

    def __init__(self, base_url: str = GOSURF_BASE_URL):
        self.base_url = base_url
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': USER_AGENT,
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'he-IL,he;q=0.9,en-US;q=0.8,en;q=0.7',
        })

    def fetch_forecast(self, beach_slug: str) -> Dict[str, Any]:
        """
        Fetch forecast data for a specific beach.

        Args:
            beach_slug: Beach identifier (e.g., 'betset', 'tel-aviv')

        Returns:
            Dictionary containing parsed forecast data

        Raises:
            requests.RequestException: If HTTP request fails
            ValueError: If parsing fails
        """
        url = urljoin(self.base_url, f"/forecast/{beach_slug}")
        logger.info(f"Fetching forecast from: {url}")

        try:
            response = self.session.get(url, timeout=TIMEOUT_SECONDS)
            response.raise_for_status()
            response.encoding = 'utf-8'  # Ensure proper Hebrew text encoding

            logger.info(f"Successfully fetched page (status {response.status_code})")

            # Parse HTML
            soup = BeautifulSoup(response.text, 'html.parser')

            # Extract data
            forecast_data = self._parse_forecast(soup, beach_slug)

            return forecast_data

        except requests.RequestException as e:
            logger.error(f"HTTP request failed: {e}")
            raise
        except Exception as e:
            logger.error(f"Parsing failed: {e}")
            raise ValueError(f"Failed to parse forecast data: {e}")

    def _parse_forecast(self, soup: BeautifulSoup, beach_slug: str) -> Dict[str, Any]:
        """
        Parse forecast data from HTML soup.

        Args:
            soup: BeautifulSoup parsed HTML
            beach_slug: Beach identifier

        Returns:
            Structured forecast data
        """
        # Extract spot ID from JavaScript
        spot_id = self._extract_spot_id(soup)

        # Extract beach name
        beach_name = self._extract_beach_name(soup)

        # Extract weekly wave data from JavaScript
        weekly_wave_data = self._extract_weekly_data(soup)

        # Extract hourly forecast table data
        hourly_forecasts = self._extract_hourly_forecasts(soup)

        # Extract metadata
        metadata = self._extract_metadata(soup)

        return {
            'beach_slug': beach_slug,
            'beach_name': beach_name,
            'spot_id': spot_id,
            'fetched_at': datetime.now(timezone.utc).isoformat(),
            'weekly_wave_data': weekly_wave_data,
            'hourly_forecasts': hourly_forecasts,
            'metadata': metadata,
        }

    def _extract_spot_id(self, soup: BeautifulSoup) -> Optional[int]:
        """Extract spot ID from JavaScript variables."""
        script_tags = soup.find_all('script')
        for script in script_tags:
            if script.string and '_js_spot_id' in script.string:
                match = re.search(r'var\s+_js_spot_id\s*=\s*(\d+)', script.string)
                if match:
                    return int(match.group(1))
        return None

    def _extract_beach_name(self, soup: BeautifulSoup) -> str:
        """Extract beach name from page title or header."""
        # Try to get from title
        title = soup.find('title')
        if title:
            # Remove "GoSurf -" prefix if present
            name = title.get_text().replace('GoSurf -', '').strip()
            return name

        # Fallback to h1 or header
        h1 = soup.find('h1')
        if h1:
            return h1.get_text().strip()

        return "Unknown Beach"

    def _extract_weekly_data(self, soup: BeautifulSoup) -> Optional[List[float]]:
        """Extract weekly wave data array from JavaScript."""
        script_tags = soup.find_all('script')
        for script in script_tags:
            if script.string and 'weeklyData' in script.string:
                match = re.search(r'var\s+weeklyData\s*=\s*\[([\d.,\s]+)\]', script.string)
                if match:
                    data_str = match.group(1)
                    # Parse comma-separated numbers
                    try:
                        values = [float(x.strip()) for x in data_str.split(',') if x.strip()]
                        logger.info(f"Extracted {len(values)} weekly wave data points")
                        return values
                    except ValueError as e:
                        logger.warning(f"Failed to parse weekly data: {e}")
        return None

    def _extract_hourly_forecasts(self, soup: BeautifulSoup) -> List[Dict[str, Any]]:
        """
        Extract hourly forecast data from HTML table.

        Returns:
            List of hourly forecast dictionaries
        """
        forecasts = []

        # Find ALL forecast tables
        # GoSurf uses multiple tables (one per day, starting from table index 3)
        all_tables = soup.find_all('table')
        logger.info(f"Found {len(all_tables)} tables total")

        # Process tables starting from index 3 (tables 0-2 are header/current conditions)
        for table_idx, table in enumerate(all_tables):
            rows = table.find_all('tr')

            # Skip empty tables and small tables (header tables)
            if len(rows) < 2:
                continue

            # Check if this looks like a forecast table (should have 9-column rows)
            first_row_cells = rows[0].find_all('td')
            if len(first_row_cells) != 9:
                continue

            logger.debug(f"Processing forecast table {table_idx} with {len(rows)} rows")

            # Skip row 0 (header row), process rows 1+
            for row in rows[1:]:
                try:
                    forecast_item = self._parse_forecast_row(row)
                    if forecast_item:
                        forecasts.append(forecast_item)
                except Exception as e:
                    logger.debug(f"Failed to parse row: {e}")
                    continue

        logger.info(f"Successfully parsed {len(forecasts)} hourly forecasts")
        return forecasts

    def _parse_forecast_row(self, row) -> Optional[Dict[str, Any]]:
        """
        Parse a single forecast row.

        Table columns (9 cells):
        0: Time (00, 03, 06, etc.)
        1: Temperature (23°)
        2: Wave height (10 - 20ס״מ)
        3: Sea condition text (ים נוח)
        4: Wind speed (10קמ״ש)
        5: Wind direction (דרום מערבית)
        6: Swell height (30ס״מ)
        7: Swell period (5.5שניות)
        8: Swell direction (צפוני)

        Returns:
            Dictionary with forecast data or None if parsing fails
        """
        # Get all table cells
        cells = row.find_all('td')

        # Skip header rows or rows with wrong number of columns
        if len(cells) != 9:
            return None

        try:
            # Column 0: Time
            time_text = cells[0].get_text(strip=True)
            if not time_text.isdigit():
                return None
            time_str = f"{time_text.zfill(2)}:00"

            # Column 1: Temperature
            temp_text = cells[1].get_text(strip=True)
            temperature = self._parse_temperature(temp_text)

            # Column 2: Wave height
            wave_text = cells[2].get_text(strip=True)
            wave_height = self._parse_wave_height(wave_text)

            # Column 4 & 5: Wind
            wind_speed_text = cells[4].get_text(strip=True)
            wind_dir_text = cells[5].get_text(strip=True)
            wind_data = self._parse_wind(wind_speed_text, wind_dir_text)

            # Column 6, 7, 8: Swell
            swell_height_text = cells[6].get_text(strip=True)
            swell_period_text = cells[7].get_text(strip=True)
            swell_dir_text = cells[8].get_text(strip=True)
            swell_data = self._parse_swell(swell_height_text, swell_period_text, swell_dir_text)

            return {
                'time': time_str,
                'wave_height': wave_height,
                'wind': wind_data,
                'temperature': temperature,
                'swell': swell_data,
            }
        except (IndexError, ValueError) as e:
            logger.debug(f"Failed to parse row: {e}")
            return None

    def _parse_temperature(self, text: str) -> Optional[float]:
        """Parse temperature from text like '23°'."""
        match = re.search(r'(\d+)°?', text)
        if match:
            return float(match.group(1))
        return None

    def _parse_wave_height(self, text: str) -> Optional[Dict[str, Any]]:
        """Parse wave height from text like '10 - 20 ס״מ' or '40 ס״מ'."""
        # Parse range (e.g., "10 - 20 ס״מ") or single value (e.g., "40 ס״מ")
        match = re.search(r'(\d+)\s*-?\s*(\d*)\s*ס״מ', text)
        if match:
            min_height = int(match.group(1))
            max_height = int(match.group(2)) if match.group(2) else min_height

            return {
                'min_cm': min_height,
                'max_cm': max_height,
                'avg_cm': (min_height + max_height) / 2,
                'avg_m': (min_height + max_height) / 200,  # Convert to meters
            }

        return None

    def _parse_wind(self, speed_text: str, direction_text: str) -> Optional[Dict[str, Any]]:
        """Parse wind speed and direction from text."""
        # Parse speed like "10 קמ״ש"
        speed_match = re.search(r'(\d+)\s*קמ״ש', speed_text)
        if not speed_match:
            return None

        speed_kmh = int(speed_match.group(1))

        # Clean up direction text
        direction = direction_text.strip() if direction_text else None

        return {
            'speed_kmh': speed_kmh,
            'speed_ms': round(speed_kmh / 3.6, 2),  # Convert to m/s
            'direction': direction,
        }

    def _parse_swell(self, height_text: str, period_text: str, direction_text: str) -> Optional[Dict[str, Any]]:
        """Parse swell data from text."""
        swell_data = {}

        # Parse swell period like "5.5 שניות"
        period_match = re.search(r'(\d+\.?\d*)\s*שניות?', period_text)
        if period_match:
            swell_data['period_seconds'] = float(period_match.group(1))

        # Parse swell direction
        dir_clean = direction_text.strip()
        if dir_clean:
            swell_data['direction'] = dir_clean

        return swell_data if swell_data else None

    def _extract_metadata(self, soup: BeautifulSoup) -> Dict[str, Any]:
        """Extract additional metadata from page."""
        metadata = {}

        # Try to find water temperature using string parameter (text is deprecated)
        water_temp_elem = soup.find(string=re.compile(r'מים.*\d+\.?\d*°'))
        if water_temp_elem:
            match = re.search(r'(\d+\.?\d*)°', water_temp_elem)
            if match:
                metadata['water_temperature_c'] = float(match.group(1))

        # Try to find sunrise/sunset - format: "אור 06:27 - 16:36 זריחה - שקיעה"
        sun_times_elem = soup.find(string=re.compile(r'אור\s+\d{2}:\d{2}\s*-\s*\d{2}:\d{2}'))
        if sun_times_elem:
            # Extract both times: sunrise (first) and sunset (second)
            times = re.findall(r'(\d{2}:\d{2})', sun_times_elem)
            if len(times) >= 2:
                metadata['sunrise'] = times[0]
                metadata['sunset'] = times[1]

        return metadata


def lambda_handler(event, context):
    """
    AWS Lambda handler function.

    Expected event structure:
    {
        "beach_slug": "betset",  # or "tel-aviv", "haifa", etc.
        "beach_name": "Betzet"   # Optional, will be extracted from page if not provided
    }

    Returns:
        Response with statusCode, body containing forecast data, and metadata
    """
    try:
        # Parse input
        beach_slug = event.get('beach_slug')
        if not beach_slug:
            return {
                'statusCode': 400,
                'body': json.dumps({
                    'error': 'Missing required parameter: beach_slug',
                    'example': {'beach_slug': 'betset'}
                })
            }

        logger.info(f"Processing request for beach: {beach_slug}")

        # Create scraper and fetch data
        scraper = GoSurfScraper()
        forecast_data = scraper.fetch_forecast(beach_slug)

        # Add provider metadata
        forecast_data['provider'] = 'gosurf'
        forecast_data['provider_url'] = f"{GOSURF_BASE_URL}/forecast/{beach_slug}"

        logger.info(f"Successfully scraped forecast for {beach_slug}")

        return {
            'statusCode': 200,
            'headers': {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*',
            },
            'body': json.dumps(forecast_data, ensure_ascii=False, indent=2)
        }

    except requests.RequestException as e:
        logger.error(f"HTTP request error: {e}")
        return {
            'statusCode': 502,
            'body': json.dumps({
                'error': 'Failed to fetch data from GoSurf',
                'details': str(e)
            })
        }

    except ValueError as e:
        logger.error(f"Parsing error: {e}")
        return {
            'statusCode': 500,
            'body': json.dumps({
                'error': 'Failed to parse forecast data',
                'details': str(e)
            })
        }

    except Exception as e:
        logger.error(f"Unexpected error: {e}", exc_info=True)
        return {
            'statusCode': 500,
            'body': json.dumps({
                'error': 'Internal server error',
                'details': str(e)
            })
        }


# For local testing
if __name__ == '__main__':
    # Test with Betzet beach
    test_event = {
        'beach_slug': 'betset'
    }

    result = lambda_handler(test_event, None)
    print(json.dumps(json.loads(result['body']), indent=2, ensure_ascii=False))
