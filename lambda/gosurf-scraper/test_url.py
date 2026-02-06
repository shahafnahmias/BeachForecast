#!/usr/bin/env python3
"""
Test GoSurf Lambda with a URL.

Usage:
    python test_url.py https://gosurf.co.il/forecast/tel-baruch
    python test_url.py https://gosurf.co.il/forecast/betset
"""

import sys
import json
from lambda_function import lambda_handler


def extract_beach_slug(url: str) -> str:
    """Extract beach slug from GoSurf URL."""
    # Remove trailing slash if present
    url = url.rstrip('/')

    # Extract the last part of the URL (the beach slug)
    parts = url.split('/')
    beach_slug = parts[-1]

    return beach_slug


def test_with_url(url: str):
    """Test Lambda function with a GoSurf URL."""
    print(f"🌊 Testing GoSurf scraper with URL: {url}")
    print("=" * 60)

    # Extract beach slug from URL
    beach_slug = extract_beach_slug(url)
    print(f"📍 Extracted beach slug: {beach_slug}\n")

    # Create Lambda event
    event = {
        'beach_slug': beach_slug
    }

    # Call Lambda handler
    result = lambda_handler(event, None)

    # Display results
    print(f"📊 Status Code: {result['statusCode']}")

    if result['statusCode'] == 200:
        body = json.loads(result['body'])
        print(f"\n✅ Successfully scraped forecast!")
        print(f"Beach: {body['beach_name']}")
        print(f"Spot ID: {body['spot_id']}")
        print(f"Fetched at: {body['fetched_at']}")

        # Show CURRENT forecast first (first hour)
        hourly = body.get('hourly_forecasts', [])
        if hourly:
            current = hourly[0]
            print(f"\n🌊 CURRENT FORECAST ({current.get('time', 'N/A')}):")
            print("=" * 60)

            wave = current.get('wave_height')
            if wave:
                print(f"  🌊 Waves: {wave['avg_m']:.2f}m ({wave['min_cm']}-{wave['max_cm']} cm)")

            wind = current.get('wind')
            if wind:
                print(f"  💨 Wind: {wind['speed_kmh']} km/h {wind.get('direction', '')}")

            temp = current.get('temperature')
            if temp:
                print(f"  🌡️  Air Temp: {temp}°C")

            swell = current.get('swell')
            if swell:
                period = swell.get('period_seconds')
                direction = swell.get('direction', '')
                if period:
                    print(f"  🌀 Swell: {period}s {direction}")

            print("=" * 60)

        # Show weekly wave data
        if body.get('weekly_wave_data'):
            weekly_data = body['weekly_wave_data']
            print(f"\n📈 Weekly Wave Data ({len(weekly_data)} points):")
            print(f"  Min: {min(weekly_data):.2f} cm")
            print(f"  Max: {max(weekly_data):.2f} cm")
            print(f"  Avg: {sum(weekly_data)/len(weekly_data):.2f} cm")
        print(f"\n⏰ Hourly Forecasts ({len(hourly)} hours):")
        for i, forecast in enumerate(hourly[:5]):  # Show first 5
            time = forecast.get('time', 'N/A')
            wave = forecast.get('wave_height')
            wind = forecast.get('wind')
            temp = forecast.get('temperature')

            print(f"  {time}:")
            if wave:
                print(f"    Waves: {wave['avg_m']:.2f}m ({wave['min_cm']}-{wave['max_cm']} cm)")
            if wind:
                print(f"    Wind: {wind['speed_kmh']} km/h {wind.get('direction', '')}")
            if temp:
                print(f"    Temp: {temp}°C")

        if len(hourly) > 5:
            print(f"  ... and {len(hourly) - 5} more hours")

        # Show metadata
        metadata = body.get('metadata', {})
        if metadata:
            print(f"\n🌅 Metadata:")
            if 'water_temperature_c' in metadata:
                print(f"  Water temp: {metadata['water_temperature_c']}°C")
            if 'sunrise' in metadata:
                print(f"  Sunrise: {metadata['sunrise']}")
            if 'sunset' in metadata:
                print(f"  Sunset: {metadata['sunset']}")

        # Pretty print full JSON
        print(f"\n📄 Full JSON response:")
        print(json.dumps(body, indent=2, ensure_ascii=False))

    else:
        print(f"\n❌ Error:")
        print(result['body'])


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Usage: python test_url.py <gosurf-url>")
        print("\nExamples:")
        print("  python test_url.py https://gosurf.co.il/forecast/tel-baruch")
        print("  python test_url.py https://gosurf.co.il/forecast/betset")
        sys.exit(1)

    url = sys.argv[1]
    test_with_url(url)
