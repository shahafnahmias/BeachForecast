"""
Unit tests for GoSurf scraper Lambda function.

Tests against real data from gosurf.co.il to ensure accurate parsing.

Run tests:
    python -m pytest test_scraper.py -v
    python -m pytest test_scraper.py -v -s  # with print output
"""

import unittest
from unittest.mock import Mock, patch
import json
from lambda_function import GoSurfScraper, lambda_handler
from normalizer import GoSurfNormalizer


class TestGoSurfScraperTelBaruch(unittest.TestCase):
    """Tests for Tel Baruch beach forecast parsing."""

    @classmethod
    def setUpClass(cls):
        """Fetch real data from Tel Baruch once for all tests."""
        cls.scraper = GoSurfScraper()
        try:
            cls.forecast_data = cls.scraper.fetch_forecast('tel-baruch')
            cls.fetch_succeeded = True
        except Exception as e:
            print(f"Warning: Could not fetch real data: {e}")
            cls.fetch_succeeded = False
            cls.forecast_data = None

    def test_beach_metadata(self):
        """Test that beach metadata is correctly extracted."""
        if not self.fetch_succeeded:
            self.skipTest("Could not fetch real data")

        self.assertIsNotNone(self.forecast_data)
        self.assertEqual(self.forecast_data['beach_slug'], 'tel-baruch')
        self.assertIn('beach_name', self.forecast_data)
        self.assertIsNotNone(self.forecast_data.get('spot_id'))

        print(f"\n✓ Beach: {self.forecast_data['beach_name']}")
        print(f"✓ Spot ID: {self.forecast_data['spot_id']}")

    def test_water_temperature(self):
        """Test water temperature extraction - should be 23.1°C if available."""
        if not self.fetch_succeeded:
            self.skipTest("Could not fetch real data")

        metadata = self.forecast_data.get('metadata', {})
        water_temp = metadata.get('water_temperature_c')

        # Water temperature extraction is optional (not always available on page)
        if water_temp is not None:
            self.assertAlmostEqual(water_temp, 23.1, delta=2.0,
                                   msg=f"Water temp should be ~23.1°C, got {water_temp}°C")
            print(f"\n✓ Water temperature: {water_temp}°C (expected: 23.1°C)")
        else:
            # Just log that it wasn't found - not a hard failure
            print("\nNote: Water temperature not found on page (extraction may need update)")

    def test_sunrise_sunset(self):
        """Test sunrise/sunset time extraction."""
        if not self.fetch_succeeded:
            self.skipTest("Could not fetch real data")

        metadata = self.forecast_data.get('metadata', {})

        # Should have sunrise around 06:27
        if 'sunrise' in metadata:
            sunrise = metadata['sunrise']
            self.assertRegex(sunrise, r'\d{2}:\d{2}', "Sunrise should be HH:MM format")
            print(f"\n✓ Sunrise: {sunrise}")

        # Should have sunset around 16:36
        if 'sunset' in metadata:
            sunset = metadata['sunset']
            self.assertRegex(sunset, r'\d{2}:\d{2}', "Sunset should be HH:MM format")
            print(f"✓ Sunset: {sunset}")

    def test_hourly_forecasts_exist(self):
        """Test that hourly forecasts are extracted."""
        if not self.fetch_succeeded:
            self.skipTest("Could not fetch real data")

        hourly = self.forecast_data.get('hourly_forecasts', [])
        self.assertGreater(len(hourly), 0, "Should have at least one hourly forecast")
        self.assertLessEqual(len(hourly), 60, "Should not have more than ~60 hours")

        print(f"\n✓ Extracted {len(hourly)} hourly forecasts")

    def test_first_hour_wave_height(self):
        """Test first hour (00:00) wave height - should be 10-20 cm."""
        if not self.fetch_succeeded:
            self.skipTest("Could not fetch real data")

        hourly = self.forecast_data.get('hourly_forecasts', [])
        self.assertGreater(len(hourly), 0, "Need at least one forecast")

        # Find the 00:00 forecast
        first_forecast = None
        for forecast in hourly[:5]:  # Check first few hours
            if forecast.get('time', '').startswith('00'):
                first_forecast = forecast
                break

        if first_forecast:
            wave_data = first_forecast.get('wave_height')
            self.assertIsNotNone(wave_data, "Wave height should be present")

            min_cm = wave_data.get('min_cm')
            max_cm = wave_data.get('max_cm')
            avg_m = wave_data.get('avg_m')

            # Expected: 10-20 cm
            self.assertIsNotNone(min_cm, "Min wave height should be extracted")
            self.assertIsNotNone(max_cm, "Max wave height should be extracted")
            self.assertAlmostEqual(min_cm, 10, delta=5, msg=f"Min should be ~10cm, got {min_cm}cm")
            self.assertAlmostEqual(max_cm, 20, delta=10, msg=f"Max should be ~20cm, got {max_cm}cm")

            # Average should be 0.15m (15cm)
            expected_avg_m = 0.15
            self.assertIsNotNone(avg_m, "Average wave height in meters should be calculated")
            self.assertAlmostEqual(avg_m, expected_avg_m, delta=0.1,
                                   msg=f"Avg should be ~{expected_avg_m}m, got {avg_m}m")

            print(f"\n✓ Wave height (00:00): {min_cm}-{max_cm} cm (avg: {avg_m}m)")
        else:
            print("\nWarning: Could not find 00:00 forecast")

    def test_first_hour_wind(self):
        """Test first hour wind - should be 10 km/h Southwest."""
        if not self.fetch_succeeded:
            self.skipTest("Could not fetch real data")

        hourly = self.forecast_data.get('hourly_forecasts', [])

        # Find the 00:00 forecast
        first_forecast = None
        for forecast in hourly[:5]:
            if forecast.get('time', '').startswith('00'):
                first_forecast = forecast
                break

        if first_forecast:
            wind_data = first_forecast.get('wind')
            self.assertIsNotNone(wind_data, "Wind data should be present")

            speed_kmh = wind_data.get('speed_kmh')
            direction = wind_data.get('direction')

            # Expected: 10 km/h
            self.assertIsNotNone(speed_kmh, "Wind speed should be extracted")
            self.assertAlmostEqual(speed_kmh, 10, delta=5,
                                   msg=f"Wind speed should be ~10 km/h, got {speed_kmh}")

            # Expected: Southwest (דרום מערבי)
            self.assertIsNotNone(direction, "Wind direction should be extracted")
            # Direction should contain either דרום (south) or מערב (west)
            if direction:
                self.assertTrue('דרום' in direction or 'מערב' in direction,
                                f"Direction should be Southwest-ish, got {direction}")

            print(f"\n✓ Wind (00:00): {speed_kmh} km/h {direction}")
        else:
            print("\nWarning: Could not find 00:00 forecast")

    def test_first_hour_swell(self):
        """Test first hour swell - should be 6 seconds from North."""
        if not self.fetch_succeeded:
            self.skipTest("Could not fetch real data")

        hourly = self.forecast_data.get('hourly_forecasts', [])

        # Find the 00:00 forecast
        first_forecast = None
        for forecast in hourly[:5]:
            if forecast.get('time', '').startswith('00'):
                first_forecast = forecast
                break

        if first_forecast:
            swell_data = first_forecast.get('swell')

            if swell_data:
                period = swell_data.get('period_seconds')
                direction = swell_data.get('direction')

                # Expected: 5.5 seconds (close to 6)
                if period:
                    self.assertAlmostEqual(period, 5.5, delta=1.0,
                                           msg=f"Swell period should be ~5.5s, got {period}s")

                # Expected: North (צפון or צפוני - both are acceptable)
                if direction:
                    self.assertTrue('צפון' in direction or 'צפוני' in direction,
                                  f"Swell direction should contain North (צפון/צפוני), got {direction}")

                print(f"\n✓ Swell (00:00): {period}s {direction}")
            else:
                print("\nWarning: Swell data not found for 00:00")

    def test_first_hour_temperature(self):
        """Test first hour air temperature - should be 23°C."""
        if not self.fetch_succeeded:
            self.skipTest("Could not fetch real data")

        hourly = self.forecast_data.get('hourly_forecasts', [])

        # Find the 00:00 forecast
        first_forecast = None
        for forecast in hourly[:5]:
            if forecast.get('time', '').startswith('00'):
                first_forecast = forecast
                break

        if first_forecast:
            temperature = first_forecast.get('temperature')

            self.assertIsNotNone(temperature, "Air temperature should be present")
            self.assertAlmostEqual(temperature, 23, delta=2,
                                   msg=f"Temperature should be ~23°C, got {temperature}°C")

            print(f"\n✓ Air temperature (00:00): {temperature}°C")

    def test_wave_progression(self):
        """Test that waves increase throughout the day (10-20cm → 60-100cm)."""
        if not self.fetch_succeeded:
            self.skipTest("Could not fetch real data")

        hourly = self.forecast_data.get('hourly_forecasts', [])
        self.assertGreater(len(hourly), 0)

        wave_heights = []
        for forecast in hourly[:8]:  # First 24 hours (every 3 hours)
            wave_data = forecast.get('wave_height')
            if wave_data:
                avg_cm = wave_data.get('avg_cm')
                if avg_cm:
                    wave_heights.append(avg_cm)
                    time = forecast.get('time', 'N/A')
                    print(f"  {time}: {avg_cm:.0f} cm")

        # Waves should generally increase
        if len(wave_heights) >= 3:
            # Last wave should be significantly higher than first
            first_wave = wave_heights[0]
            last_wave = wave_heights[-1]

            self.assertGreater(last_wave, first_wave * 1.5,
                               msg=f"Waves should increase: {first_wave}cm → {last_wave}cm")

            print(f"\n✓ Wave progression: {first_wave:.0f}cm → {last_wave:.0f}cm")

    def test_weekly_wave_data(self):
        """Test that weekly wave data array is extracted."""
        if not self.fetch_succeeded:
            self.skipTest("Could not fetch real data")

        weekly_data = self.forecast_data.get('weekly_wave_data')

        if weekly_data:
            self.assertIsInstance(weekly_data, list)
            self.assertGreater(len(weekly_data), 0)
            self.assertLess(len(weekly_data), 200)  # Reasonable upper bound

            # All values should be numeric
            for value in weekly_data[:5]:  # Check first few
                self.assertIsInstance(value, (int, float))

            print(f"\n✓ Weekly wave data: {len(weekly_data)} points")
            print(f"  Range: {min(weekly_data):.1f} - {max(weekly_data):.1f} cm")
        else:
            print("\nWarning: No weekly wave data extracted")


class TestGoSurfScraperBetset(unittest.TestCase):
    """Tests for Betset beach to verify multi-beach support."""

    def test_betset_fetch(self):
        """Test that Betset beach data can be fetched."""
        scraper = GoSurfScraper()

        try:
            forecast_data = scraper.fetch_forecast('betset')
            self.assertEqual(forecast_data['beach_slug'], 'betset')
            self.assertIsNotNone(forecast_data.get('spot_id'))
            self.assertIn('hourly_forecasts', forecast_data)

            print(f"\n✓ Betset beach: {forecast_data['beach_name']}")
            print(f"✓ Forecasts: {len(forecast_data.get('hourly_forecasts', []))} hours")

        except Exception as e:
            self.skipTest(f"Could not fetch Betset data: {e}")


class TestGoSurfNormalizer(unittest.TestCase):
    """Tests for data normalization to unified schema."""

    def setUp(self):
        """Set up test data."""
        self.normalizer = GoSurfNormalizer()

        # Sample GoSurf data matching Tel Baruch format
        self.sample_data = {
            'beach_slug': 'tel-baruch',
            'beach_name': 'תל ברוך',
            'spot_id': 123,
            'fetched_at': '2025-12-06T00:57:00Z',
            'hourly_forecasts': [
                {
                    'time': '00:00',
                    'wave_height': {
                        'min_cm': 10,
                        'max_cm': 20,
                        'avg_cm': 15,
                        'avg_m': 0.15
                    },
                    'wind': {
                        'speed_kmh': 10,
                        'speed_ms': 2.78,
                        'direction': 'דרום מערבי'
                    },
                    'temperature': 23.0,
                    'swell': {
                        'period_seconds': 5.5,
                        'direction': 'צפון'
                    }
                }
            ],
            'metadata': {
                'water_temperature_c': 23.1,
                'sunrise': '06:27',
                'sunset': '16:36'
            }
        }

        self.location = {
            'name': 'Tel Baruch',
            'latitude': 32.1192,
            'longitude': 34.8007
        }

    def test_normalize_structure(self):
        """Test that normalized data has correct structure."""
        normalized = self.normalizer.normalize(self.sample_data, self.location)

        self.assertIsInstance(normalized, list)
        self.assertEqual(len(normalized), 1)  # One hourly forecast

        record = normalized[0]

        # Check required fields
        self.assertEqual(record['provider'], 'gosurf')
        self.assertEqual(record['data_type'], 'forecast')
        self.assertEqual(record['location_name'], 'Tel Baruch')
        self.assertEqual(record['latitude'], 32.1192)
        self.assertEqual(record['longitude'], 34.8007)

        print("\n✓ Normalized structure is correct")

    def test_normalize_wave_height(self):
        """Test wave height normalization."""
        normalized = self.normalizer.normalize(self.sample_data, self.location)
        record = normalized[0]

        # Should extract average wave height in meters
        self.assertEqual(record['wave_height'], 0.15)

        print(f"\n✓ Wave height: {record['wave_height']}m")

    def test_normalize_wind(self):
        """Test wind data normalization."""
        normalized = self.normalizer.normalize(self.sample_data, self.location)
        record = normalized[0]

        # Wind speed should be in km/h
        self.assertEqual(record['wind_speed'], 10.0)

        # Wind direction should be converted to degrees (SW = 225°)
        self.assertIsNotNone(record['wind_direction'])
        self.assertEqual(record['wind_direction'], 225)  # SW in degrees

        print(f"\n✓ Wind: {record['wind_speed']} km/h at {record['wind_direction']}°")

    def test_normalize_swell(self):
        """Test swell data normalization."""
        normalized = self.normalizer.normalize(self.sample_data, self.location)
        record = normalized[0]

        # Swell period
        self.assertEqual(record['swell_period'], 5.5)

        # Swell direction (North = 0°)
        self.assertIsNotNone(record['swell_direction'])
        self.assertEqual(record['swell_direction'], 0)  # North in degrees

        print(f"\n✓ Swell: {record['swell_period']}s at {record['swell_direction']}°")

    def test_normalize_temperature(self):
        """Test temperature normalization."""
        normalized = self.normalizer.normalize(self.sample_data, self.location)
        record = normalized[0]

        self.assertEqual(record['temperature'], 23.0)
        self.assertEqual(record['sea_surface_temp'], 23.1)

        print(f"\n✓ Air temp: {record['temperature']}°C, Water: {record['sea_surface_temp']}°C")

    def test_completeness_score(self):
        """Test completeness score calculation."""
        normalized = self.normalizer.normalize(self.sample_data, self.location)
        record = normalized[0]

        completeness = record['completeness']

        self.assertIsNotNone(completeness)
        self.assertGreaterEqual(completeness, 0.0)
        self.assertLessEqual(completeness, 1.0)
        # Should have decent completeness with wave, wind, temp, swell
        self.assertGreater(completeness, 0.3)

        print(f"\n✓ Completeness: {completeness:.2%}")


class TestLambdaHandler(unittest.TestCase):
    """Tests for Lambda handler function."""

    def test_missing_beach_slug(self):
        """Test error handling for missing beach_slug."""
        event = {}
        result = lambda_handler(event, None)

        self.assertEqual(result['statusCode'], 400)
        body = json.loads(result['body'])
        self.assertIn('error', body)

        print("\n✓ Correctly handles missing beach_slug")

    def test_valid_request(self):
        """Test valid request returns 200."""
        event = {'beach_slug': 'tel-baruch'}

        try:
            result = lambda_handler(event, None)

            self.assertEqual(result['statusCode'], 200)
            self.assertIn('Content-Type', result['headers'])

            body = json.loads(result['body'])
            self.assertEqual(body['provider'], 'gosurf')
            self.assertEqual(body['beach_slug'], 'tel-baruch')

            print(f"\n✓ Lambda handler works correctly")
            print(f"  Beach: {body.get('beach_name')}")
            print(f"  Forecasts: {len(body.get('hourly_forecasts', []))}")

        except Exception as e:
            self.skipTest(f"Could not test valid request: {e}")


def run_test_summary():
    """Run tests and show summary."""
    import sys

    # Run tests
    loader = unittest.TestLoader()
    suite = loader.loadTestsFromModule(sys.modules[__name__])
    runner = unittest.TextTestRunner(verbosity=2)
    result = runner.run(suite)

    # Summary
    print("\n" + "=" * 70)
    print("TEST SUMMARY")
    print("=" * 70)
    print(f"Tests run: {result.testsRun}")
    print(f"Successes: {result.testsRun - len(result.failures) - len(result.errors)}")
    print(f"Failures: {len(result.failures)}")
    print(f"Errors: {len(result.errors)}")
    print(f"Skipped: {len(result.skipped)}")

    if result.wasSuccessful():
        print("\n✅ ALL TESTS PASSED!")
    else:
        print("\n❌ SOME TESTS FAILED")

    return 0 if result.wasSuccessful() else 1


if __name__ == '__main__':
    import sys
    sys.exit(run_test_summary())
