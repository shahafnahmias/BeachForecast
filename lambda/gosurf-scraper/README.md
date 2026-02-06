# GoSurf Scraper Lambda Function

## Overview

AWS Lambda function that scrapes surf forecast data from [gosurf.co.il](https://gosurf.co.il) for Israeli beaches. Extracts wave height, wind, swell, temperature, and other marine/weather data.

## Features

- ✅ Scrapes hourly forecast data (wave height, wind, swell, temperature)
- ✅ Extracts weekly wave data arrays
- ✅ Handles Hebrew text encoding properly
- ✅ Robust error handling and logging
- ✅ Returns structured JSON data
- ✅ Easy to deploy to AWS Lambda

## Data Extracted

### Per Beach
- **Beach name** and spot ID
- **Water temperature**
- **Sunrise/sunset times**
- **Weekly wave data** (array of wave heights)

### Per Hour (3-hour intervals)
- **Wave height**: Min/max/average in cm and meters
- **Wind**: Speed (km/h, m/s) and direction (Hebrew)
- **Temperature**: Air temperature in Celsius
- **Swell**: Period (seconds) and direction

## Input Event

```json
{
  "beach_slug": "betset"
}
```

**Available beach slugs:**
- `betset` - Betzet
- `tel-aviv` - Tel Aviv
- `haifa` - Haifa
- `ashdod` - Ashdod
- (check gosurf.co.il for more)

## Output Response

```json
{
  "statusCode": 200,
  "body": {
    "beach_slug": "betset",
    "beach_name": "בצת",
    "spot_id": 7,
    "fetched_at": "2025-12-06T10:30:00Z",
    "provider": "gosurf",
    "provider_url": "https://gosurf.co.il/forecast/betset",
    "weekly_wave_data": [41.53, 50.33, 51.22, ...],
    "hourly_forecasts": [
      {
        "time": "00:00",
        "wave_height": {
          "min_cm": 30,
          "max_cm": 50,
          "avg_cm": 40,
          "avg_m": 0.4
        },
        "wind": {
          "speed_kmh": 16,
          "speed_ms": 4.44,
          "direction": "דרום מזרחית"
        },
        "temperature": 20.0,
        "swell": {
          "period_seconds": 5.2,
          "direction": "מערבי"
        }
      },
      ...
    ],
    "metadata": {
      "water_temperature_c": 22.6,
      "sunrise": "06:28",
      "sunset": "16:32"
    }
  }
}
```

## Local Testing

### Prerequisites

```bash
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

### Run Locally

```bash
python lambda_function.py
```

This will scrape Betzet beach and print the JSON output.

### Test Different Beaches

Edit the `test_event` in `lambda_function.py`:

```python
test_event = {
    'beach_slug': 'tel-aviv'  # Change to any beach
}
```

## Deployment

### Option 1: AWS Console

1. **Create Lambda Function**:
   - Runtime: Python 3.11
   - Architecture: x86_64 or arm64
   - Timeout: 30 seconds
   - Memory: 512 MB

2. **Package Dependencies**:
   ```bash
   mkdir package
   pip install -r requirements.txt -t package/
   cp lambda_function.py package/
   cd package && zip -r ../gosurf-scraper.zip . && cd ..
   ```

3. **Upload**:
   - Upload `gosurf-scraper.zip` to Lambda console

### Option 2: AWS CLI

```bash
# Package
./package.sh

# Deploy
aws lambda create-function \
  --function-name gosurf-scraper \
  --runtime python3.11 \
  --role arn:aws:iam::YOUR_ACCOUNT:role/lambda-execution-role \
  --handler lambda_function.lambda_handler \
  --zip-file fileb://gosurf-scraper.zip \
  --timeout 30 \
  --memory-size 512
```

### Option 3: Terraform

See `terraform/` directory for IaC deployment.

## Environment Variables

None required currently. Could add:

- `GOSURF_BASE_URL` - Override base URL (default: https://gosurf.co.il)
- `TIMEOUT_SECONDS` - Request timeout (default: 10)
- `LOG_LEVEL` - Logging level (default: INFO)

## Error Handling

The function returns appropriate HTTP status codes:

- **200**: Success
- **400**: Bad request (missing beach_slug)
- **500**: Internal parsing error
- **502**: Failed to fetch from GoSurf (network error)

## Logging

All operations are logged using Python's logging module:
- Info: Successful fetches, parsing progress
- Warning: Non-critical parsing failures
- Error: HTTP errors, parsing exceptions

View logs in CloudWatch Logs.

## Limitations

1. **Web Scraping**: No official API - scrapes HTML (may break if GoSurf changes their layout)
2. **Rate Limiting**: No built-in rate limiting (add if needed)
3. **Hebrew Text**: Directions and names are in Hebrew (could add translation)
4. **3-Hour Intervals**: GoSurf shows forecasts every 3 hours, not hourly
5. **No Historical Data**: Only current forecast available

## Future Enhancements

- [ ] Add caching layer (Redis/ElastiCache)
- [ ] Translate Hebrew directions to English/degrees
- [ ] Add retry logic with exponential backoff
- [ ] Implement rate limiting
- [ ] Add metrics/monitoring (CloudWatch Metrics)
- [ ] Support batch processing (multiple beaches in one call)
- [ ] Add data validation against schema
- [ ] Store scraped data in database (DynamoDB/RDS)

## Related Documentation

- **API Comparison**: `../docs/api-comparison.md` - How GoSurf compares to other providers
- **Integration Guide**: `../docs/integration-guide.md` - How to integrate providers
- **Data Schema**: `../docs/data-schema.md` - Unified database schema

## Troubleshooting

### "Could not find forecast table"
- GoSurf may have changed their HTML structure
- Check the page manually: https://gosurf.co.il/forecast/betset
- Update CSS selectors in `_extract_hourly_forecasts()`

### Hebrew text shows as ???
- Ensure `response.encoding = 'utf-8'` is set
- Check `ensure_ascii=False` in `json.dumps()`

### Timeout errors
- Increase Lambda timeout (30s recommended)
- Check TIMEOUT_SECONDS constant
- Verify network connectivity from Lambda

## License

Part of SeaLevelWidget project - see parent LICENSE file.

## Contact

For issues or questions, see project documentation.
