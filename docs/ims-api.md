# IMS (Israel Meteorological Service) API Documentation

## Overview
- **Provider:** Israel Meteorological Service (השירות המטאורולוגי)
- **Type:** **Observation data** (actual measurements - GROUND TRUTH!)
- **Authentication:** `Authorization: ApiToken {TOKEN}` header required
- **Token Request:** Email ims@ims.gov.il
- **Rate Limits:** Not specified (reasonable use assumed)
- **Cost:** Free (after token approval)
- **Update Frequency:** 10-minute intervals
- **Stations:** 85+ automated weather stations across Israel

## Critical Note: Time Zone
⚠️ **All times are in LST (Local Standard Time - winter time) year-round!**
During daylight saving time (summer), there's a 1-hour offset between displayed time and actual time.
- Example: API shows `2017-10-23T11:40:00+03:00` but actual time is `2017-10-23T12:40:00+03:00`

## API Endpoints

### 1. Get All Stations (Metadata)
**Endpoint:** `GET https://api.ims.gov.il/v1/envista/stations`

**Headers:**
```
Authorization: ApiToken YOUR_TOKEN_HERE
```

**Response:**
```json
[
  {
    "stationId": 1,
    "name": "AFULA ETA",
    "active": true,
    "location": {
      "latitude": 32.817,
      "longitude": 35.763
    },
    "regionId": 4,
    "monitors": [
      {
        "active": true,
        "channelId": 1,
        "name": "Rain",
        "typeId": 1,
        "units": "mm"
      },
      ...more channels...
    ]
  },
  ...more stations...
]
```

### 2. Get Specific Station Metadata
**Endpoint:** `GET https://api.ims.gov.il/v1/envista/stations/{STATION_ID}`

**Example:** `GET https://api.ims.gov.il/v1/envista/stations/178` (Tel Aviv)

**Response:** Single station object (same structure as above)

### 3. Get Regions (Groups of Stations)
**Endpoint:** `GET https://api.ims.gov.il/v1/envista/regions`

**Response:**
```json
[
  {
    "regionId": 4,
    "name": "GALILEE & GOLAN",
    "stations": [...]
  },
  ...more regions...
]
```

### 4. Get Measurement Data - Latest
**Endpoint:** `GET https://api.ims.gov.il/v1/envista/stations/{STATION_ID}/data/latest`

Get most recent measurement for all channels.

**Response:**
```json
{
  "stationId": 178,
  "datetime": "2025-12-05T14:40:00+02:00",
  "channels": [
    {
      "id": 1,
      "name": "Rain",
      "alias": "null",
      "description": "null",
      "status": 1,
      "valid": true,
      "value": 0
    },
    {
      "id": 2,
      "name": "WSmax",
      "alias": "null",
      "description": "null",
      "status": 1,
      "valid": true,
      "value": 8.2
    },
    ...more channels...
  ]
}
```

### 5. Get Data for Specific Channel
**Endpoint:** `GET https://api.ims.gov.il/v1/envista/stations/{STATION_ID}/data/{CHANNEL_ID}/latest`

**Example:** `GET https://api.ims.gov.il/v1/envista/stations/178/data/2/latest`
(Get latest wind speed for Tel Aviv)

### 6. Get Today's Data
**Endpoint:** `GET https://api.ims.gov.il/v1/envista/stations/{STATION_ID}/data/daily`

Returns all measurements from today (up to 144 records at 10-min intervals).

### 7. Get Specific Day's Data
**Endpoint:** `GET https://api.ims.gov.il/v1/envista/stations/{STATION_ID}/data/daily/YYYY/MM/DD`

**Example:** `GET https://api.ims.gov.il/v1/envista/stations/178/data/daily/2025/12/05`

### 8. Get Data for Date Range
**Endpoint:** `GET https://api.ims.gov.il/v1/envista/stations/{STATION_ID}/data?from=YYYY/MM/DD&to=YYYY/MM/DD`

**Example:**
```
GET https://api.ims.gov.il/v1/envista/stations/178/data?from=2025/12/01&to=2025/12/05
```

**Response Structure:**
```json
{
  "data": [
    {
      "datetime": "2025-12-01T00:00:00+02:00",
      "channels": [
        {
          "id": 1,
          "name": "Rain",
          "status": 1,
          "valid": true,
          "value": 0.0
        },
        ...more channels...
      ]
    },
    ...more timestamps...
  ]
}
```

## Available Channels (Measurements)

| Channel | Name | Units | Description |
|---------|------|-------|-------------|
| BP | Pressure | mb | Atmospheric pressure at station height |
| TD | Temperature | °C | Dry bulb temperature |
| TDmax | Max Temp | °C | Maximum temperature (period) |
| TDmin | Min Temp | °C | Minimum temperature (period) |
| TG | Ground Temp | °C | Temperature near ground |
| RH | Humidity | % | Relative humidity |
| Rain | Rainfall | mm | Precipitation amount |
| WS | Wind Speed | m/sec | Average wind speed |
| WD | Wind Dir | deg | Wind direction (0-360) |
| WSmax | Max Gust | m/sec | Maximum gust speed |
| WDmax | Gust Dir | deg | Direction of max gust |
| Ws10mm | 10-min Wind | m/sec | Max 10-minute average wind |
| WS1mm | 1-min Wind | m/sec | Max 1-minute average wind |
| STDwd | Wind StdDev | deg | Standard deviation of wind direction |
| Grad | Global Rad | w/m² | Global solar radiation |
| DiffR | Diffuse Rad | w/m² | Diffuse solar radiation |
| NIP | Direct Rad | w/m² | Direct/normal solar radiation |
| Time | Time Code | hhmm | Time of max 10-min period |

## Channel Status Values
- `status = 1`: Data is valid
- `status = 2`: Data is invalid/suspect
- `valid = true/false`: Additional validation flag

**⚠️ Important:** Always check `status === 1` AND `valid === true` before using data!

## Coastal Stations (To Be Identified)
Need to query stations API and filter by coastal locations:
- Tel Aviv area (approximate lat/lon: 32.08, 34.78)
- Haifa area (32.82, 34.99)
- Ashdod area (31.80, 34.65)
- Netanya area (32.33, 34.85)
- Ashkelon area (31.67, 34.57)

**Action:** Query all stations and identify closest to coastline.

## Data Characteristics
- **Temporal Resolution:** 10 minutes
- **Records per Day:** 144 (24 hours × 6 per hour)
- **Records per Month:** ~4,320
- **Missing Values:** Channel may be absent if sensor inactive
- **Data Quality:** Each reading has status and valid flags
- **Timestamp Format:** ISO 8601 with timezone (e.g., "2025-12-05T14:40:00+02:00")

## Storage Requirements (IMS)
**Per Station, Per Day:**
- 144 measurements × ~15 channels × ~50 bytes per value = ~108 KB/day
- **Monthly:** 108 KB × 30 = 3.24 MB/station/month
- **3 months:** ~10 MB/station

**5 Coastal Stations:**
- Per day: 108 KB × 5 = 540 KB
- Per month: 3.24 MB × 5 = 16.2 MB
- **3 months: ~50 MB**

## Error Handling
**HTTP Status Codes:**
- `200 OK` - Success
- `401 Unauthorized` - Invalid/missing API token
- `404 Not Found` - Station/channel not found
- `400 Bad Request` - Invalid parameters
- `500 Internal Server Error` - Server error

## Authentication Example (curl)
```bash
curl -H "Authorization: ApiToken YOUR_TOKEN_HERE" \
  https://api.ims.gov.il/v1/envista/stations
```

## Key Differences vs Open-Meteo
| Aspect | Open-Meteo | IMS |
|--------|------------|-----|
| **Data Type** | Forecast (predictions) | Observations (actual) |
| **Purpose** | What WILL happen | What DID happen |
| **Frequency** | Hourly forecasts | 10-minute observations |
| **Coverage** | Global | Israel only |
| **Marine Data** | Yes (waves, swell, SST) | No (land/coastal stations) |
| **Ground Truth** | No | **YES!** |
| **Auth** | None | API Token required |

## Integration Strategy
1. **Use Open-Meteo for:** Wave forecasts (no alternative source)
2. **Use IMS for:**
   - Ground truth weather (temp, wind, humidity)
   - Validate Open-Meteo weather accuracy
   - Potentially correlate coastal wind with wave conditions
