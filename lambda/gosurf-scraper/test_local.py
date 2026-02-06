#!/usr/bin/env python3
"""
Local testing script for GoSurf scraper Lambda function.

Usage:
    python test_local.py betset
    python test_local.py tel-aviv
"""

import sys
import json
from lambda_function import lambda_handler


def test_beach(beach_slug: str):
    """Test scraping a specific beach."""
    print(f"🌊 Testing GoSurf scraper for beach: {beach_slug}")
    print("=" * 60)

    event = {
        'beach_slug': beach_slug
    }

    try:
        result = lambda_handler(event, None)

        print(f"\n📊 Status Code: {result['statusCode']}")

        if result['statusCode'] == 200:
            body = json.loads(result['body'])
            print(f"\n✅ Successfully scraped forecast!")
            print(f"Beach: {body['beach_name']} (Spot ID: {body['spot_id']})")
            print(f"Fetched at: {body['fetched_at']}")

            # Show weekly wave data
            if body.get('weekly_wave_data'):
                weekly_data = body['weekly_wave_data']
                print(f"\n📈 Weekly Wave Data ({len(weekly_data)} points):")
                print(f"  Min: {min(weekly_data):.2f} cm")
                print(f"  Max: {max(weekly_data):.2f} cm")
                print(f"  Avg: {sum(weekly_data)/len(weekly_data):.2f} cm")

            # Show hourly forecasts summary
            hourly = body.get('hourly_forecasts', [])
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

            # Save full output to file
            output_file = f"test_output_{beach_slug}.json"
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(body, f, ensure_ascii=False, indent=2)
            print(f"\n💾 Full output saved to: {output_file}")

        else:
            print(f"\n❌ Error:")
            print(result['body'])

    except Exception as e:
        print(f"\n❌ Exception occurred:")
        print(f"  {type(e).__name__}: {e}")
        import traceback
        traceback.print_exc()


def test_all_beaches():
    """Test multiple popular beaches."""
    beaches = [
        'betset',
        'tel-aviv',
        'haifa',
        'ashdod',
        'herzliya',
    ]

    print("🌊 Testing multiple beaches...")
    print("=" * 60)

    results = []
    for beach in beaches:
        print(f"\nTesting {beach}...")
        event = {'beach_slug': beach}
        result = lambda_handler(event, None)

        status = "✅ OK" if result['statusCode'] == 200 else f"❌ {result['statusCode']}"
        results.append((beach, status))

    print("\n" + "=" * 60)
    print("Summary:")
    for beach, status in results:
        print(f"  {beach:20s} {status}")


if __name__ == '__main__':
    if len(sys.argv) > 1:
        if sys.argv[1] == '--all':
            test_all_beaches()
        else:
            beach_slug = sys.argv[1]
            test_beach(beach_slug)
    else:
        # Default: test Betzet
        test_beach('betset')
