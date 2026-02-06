# Marine & Weather Data Provider Comparison

## Overview

This document provides a side-by-side comparison of all data providers integrated into the marine data aggregation system. Use this to understand which provider is best for specific use cases and how to leverage their strengths.

## Provider Summary

| Provider | Type | Coverage | Cost | Auth Required | Marine Data | Weather Data | Ground Truth |
|----------|------|----------|------|---------------|-------------|--------------|--------------|
| **Open-Meteo Marine** | Forecast | Global | Free | No | ✅ Yes | ✅ Yes | ❌ No |
| **IMS** | Observation | Israel only | Free | ✅ Yes (Token) | ❌ No | ✅ Yes | ✅ Yes |
| **GoSurf** | Aggregated | Israel coast | TBD | TBD | TBD | TBD | ❌ No |

## Detailed Comparison

### Open-Meteo Marine API

#### Overview
- **Provider:** Open-Meteo Foundation (open-source)
- **Data Type:** **Forecast** (predictions)
- **Coverage:** Global including Israeli coastline
- **Update Frequency:** Hourly forecasts (updated every ~15 min)
- **Cost:** Free, no rate limits
- **Authentication:** None required

#### Strengths
✅ **Comprehensive marine data:** Wave height, swell, wave period, sea surface temperature
✅ **Global coverage:** Works anywhere in the world
✅ **No authentication:** Easy to integrate, no API keys
✅ **7-day forecast:** Full week of hourly predictions
✅ **Reliable:** High uptime, maintained by open-source community
✅ **Combined weather:** Also provides temperature, wind, cloud cover, UV

#### Weaknesses
❌ **Forecast only:** No actual observations (ground truth)
❌ **Unknown accuracy for Israeli coast:** May be optimized for other regions
❌ **No provider metadata:** Doesn't specify which weather model used
❌ **Limited control:** Can't request specific forecast models

#### Data Coverage

| Metric | Available | Unit | Temporal Resolution | Forecast Horizon |
|--------|-----------|------|---------------------|-------------------|
| Wave Height | ✅ | meters | Hourly | 7 days |
| Wave Direction | ✅ | degrees | Hourly | 7 days |
| Wave Period | ✅ | seconds | Hourly | 7 days |
| Swell Height | ✅ | meters | Hourly | 7 days |
| Swell Direction | ✅ | degrees | Hourly | 7 days |
| Swell Period | ✅ | seconds | Hourly | 7 days |
| Sea Surface Temp | ✅ | °C | Hourly | 7 days |
| Air Temperature | ✅ | °C | Hourly | 7 days |
| Wind Speed | ✅ | km/h | Hourly | 7 days |
| Wind Direction | ✅ | degrees | Hourly | 7 days |
| Cloud Cover | ✅ | % | Hourly | 7 days |
| UV Index | ✅ | dimensionless | Hourly | 7 days |
| Humidity | ❌ | - | - | - |
| Pressure | ❌ | - | - | - |
| Rainfall | ❌ | - | - | - |

#### Best Used For
- 🌊 **Primary wave forecasting:** Main source for surf predictions
- 🌍 **Global locations:** Anywhere outside Israel
- 🆓 **Rapid prototyping:** No auth barriers
- 📊 **Baseline comparisons:** Reference forecast to compare others against

#### Example Response Size
- **Marine API:** ~9 KB per request (168 hourly values × 7 metrics)
- **Weather API:** ~8.5 KB per request (168 hourly values × 5 metrics)
- **Total:** ~18 KB per combined fetch

#### Integration Difficulty
🟢 **Easy** - Simple REST API, no authentication, JSON response

---

### IMS (Israel Meteorological Service)

#### Overview
- **Provider:** Israeli Government (השירות המטאורולוגי הישראלי)
- **Data Type:** **Observation** (actual measurements - GROUND TRUTH!)
- **Coverage:** Israel only (85+ weather stations)
- **Update Frequency:** 10-minute intervals
- **Cost:** Free after token approval
- **Authentication:** Required (API token via email)

#### Strengths
✅ **Ground truth data:** Actual observations, not predictions
✅ **High temporal resolution:** 10-minute intervals (144 readings/day)
✅ **Official source:** Israeli government meteorological service
✅ **Comprehensive weather:** Temperature, wind, humidity, pressure, radiation
✅ **Multiple coastal stations:** Tel Aviv, Haifa, Ashdod, etc.
✅ **Data quality flags:** Each measurement has status and validation
✅ **Free:** No usage costs

#### Weaknesses
❌ **No marine data:** No wave height, swell, or sea measurements
❌ **Israel only:** Can't use for global locations
❌ **Authentication required:** Must request API token via email
❌ **Time zone quirk:** LST (winter time) year-round, 1-hour offset in summer
❌ **No forecasts:** Only observations (what DID happen, not what WILL happen)

#### Data Coverage

| Metric | Available | Unit | Temporal Resolution | Notes |
|--------|-----------|------|---------------------|-------|
| Wave Height | ❌ | - | - | No marine sensors |
| Sea Surface Temp | ❌ | - | - | No marine sensors |
| Air Temperature | ✅ | °C | 10 minutes | TD channel |
| Max Temperature | ✅ | °C | 10 minutes | TDmax channel |
| Min Temperature | ✅ | °C | 10 minutes | TDmin channel |
| Ground Temperature | ✅ | °C | 10 minutes | TG channel |
| Humidity | ✅ | % | 10 minutes | RH channel |
| Pressure | ✅ | mb | 10 minutes | BP channel |
| Wind Speed | ✅ | m/sec | 10 minutes | WS channel |
| Wind Direction | ✅ | degrees | 10 minutes | WD channel |
| Max Gust Speed | ✅ | m/sec | 10 minutes | WSmax channel |
| Rainfall | ✅ | mm | 10 minutes | Rain channel |
| Global Radiation | ✅ | w/m² | 10 minutes | Grad channel |
| Diffuse Radiation | ✅ | w/m² | 10 minutes | DiffR channel |
| Direct Radiation | ✅ | w/m² | 10 minutes | NIP channel |
| Cloud Cover | ❌ | - | - | Not available |
| UV Index | ❌ | - | - | Not available |

#### Best Used For
- ✅ **Accuracy validation:** Compare forecasts to actual observations
- 📍 **Coastal weather:** Wind, temperature for Israeli beaches
- 🎯 **Ground truth:** Training ML models, evaluating provider accuracy
- 🇮🇱 **Israel-specific:** When you need official Israeli data

#### Example Response Size
- **Latest data:** ~5 KB per station (all channels)
- **Daily data:** ~108 KB per station (144 measurements × 15 channels)
- **Per month:** ~3.24 MB per station

#### Integration Difficulty
🟡 **Medium** - Requires API token, time zone handling, channel system

#### Critical Implementation Note
⚠️ **Time Zone Handling:** IMS uses LST (Local Standard Time = winter time) year-round. During daylight saving time (late March to late October), there's a **1-hour offset** between API timestamps and actual time.

**Example:**
- Actual time: `2017-10-23T12:40:00+03:00` (summer, UTC+3)
- IMS API shows: `2017-10-23T11:40:00+03:00` (always UTC+2 in timestamp, but claims +03 timezone)

**Solution:** Add 1 hour to timestamps during Israeli daylight saving time.

---

### GoSurf (To Be Integrated)

#### Overview
- **Provider:** GoSurf (gosurf.co.il)
- **Data Type:** Aggregated forecast (likely combines multiple sources)
- **Coverage:** Israeli coastline (surf spots)
- **Update Frequency:** Unknown
- **Cost:** Unknown (likely free)
- **Authentication:** Unknown

#### Research Needed
- [ ] Does GoSurf have a public API?
- [ ] If no API, can we scrape their site legally?
- [ ] What are their underlying data sources? (IMS? Open-Meteo? Others?)
- [ ] Do they add value beyond aggregation? (ML predictions, local adjustments?)
- [ ] Update frequency?
- [ ] Rate limits?

#### Hypothetical Strengths (To Be Verified)
🔮 **Local expertise:** May tune predictions for Israeli conditions
🔮 **Combined view:** Aggregates multiple sources
🔮 **Surf-specific:** Might include surf quality ratings
🔮 **Spot-specific:** Forecasts for specific surf breaks

#### Hypothetical Weaknesses (To Be Verified)
⚠️ **Aggregator:** May not add information beyond sources
⚠️ **No API:** Might require web scraping (fragile)
⚠️ **Unknown quality:** No reputation for accuracy
⚠️ **Commercial:** Could change/paywall at any time

#### Next Steps
1. Inspect GoSurf website structure
2. Check for public API documentation
3. Determine data sources (inspect network traffic)
4. Evaluate if integration adds value over Open-Meteo + IMS

---

## Use Case Recommendations

### Use Case 1: Wave Forecasting for Surfers
**Primary:** Open-Meteo Marine API
**Secondary:** GoSurf (if it adds local adjustments)
**Ground Truth:** Not available (no buoy data from IMS)

**Why:** Only Open-Meteo provides wave height, swell, and period. IMS doesn't have marine sensors.

### Use Case 2: Weather Forecast Accuracy Validation
**Forecast:** Open-Meteo Weather API
**Ground Truth:** IMS observations
**Comparison:** Compare Open-Meteo temperature/wind predictions vs IMS observations

**Why:** IMS provides actual measurements to validate forecasts. Use this to compute Mean Absolute Error (MAE) for temperature, wind, etc.

### Use Case 3: Real-Time Coastal Wind Conditions
**Primary:** IMS observations (10-minute intervals)
**Secondary:** Open-Meteo forecast

**Why:** IMS provides real-time actual wind measurements. Use Open-Meteo for future predictions.

### Use Case 4: Multi-Day Surf Trip Planning
**Primary:** Open-Meteo Marine API (7-day forecast)
**Analysis:** Historical accuracy of Open-Meteo for this location

**Why:** 7-day horizon, wave-specific data. Check historical accuracy to adjust confidence.

### Use Case 5: Machine Learning Training Data
**Features:** Open-Meteo forecasts (input)
**Labels:** IMS observations (target/output)
**Goal:** Learn correction factors for Open-Meteo predictions

**Why:** Train a model to adjust Open-Meteo forecasts based on IMS ground truth.

---

## Data Quality Comparison

### Completeness

| Provider | Expected Fields Filled | Typical Completeness |
|----------|------------------------|----------------------|
| Open-Meteo Marine | 13/13 (100%) | Very high - rarely missing |
| Open-Meteo Weather | 5/5 (100%) | Very high |
| IMS | Varies by station | Medium - some sensors inactive |

### Reliability (Uptime)

| Provider | Expected Uptime | Observed Outages |
|----------|-----------------|------------------|
| Open-Meteo | 99.5%+ | Rare, community-maintained |
| IMS | 99%+ | Occasional sensor failures |
| GoSurf | Unknown | TBD |

### Accuracy (To Be Measured)

Once data collection begins, measure forecast accuracy:

```sql
-- Example: Measure Open-Meteo temperature accuracy against IMS
SELECT
  AVG(ABS(om.temperature - ims.temperature)) AS mae_temperature,
  AVG(ABS(om.wind_speed - ims.wind_speed * 3.6)) AS mae_wind_speed_kmh
FROM forecasts om
JOIN forecasts ims
  ON om.location_name = ims.location_name
  AND om.forecast_time = ims.forecast_time
WHERE om.provider = 'open-meteo-marine'
  AND om.data_type = 'forecast'
  AND ims.provider = 'ims'
  AND ims.data_type = 'observation'
  AND om.forecast_time >= NOW() - INTERVAL '30 days';
```

**Target Metrics:**
- **Temperature MAE:** < 2°C (good)
- **Wind Speed MAE:** < 5 km/h (good)
- **Wave Height MAE:** Unknown (no ground truth available yet)

---

## Provider Selection Matrix

### Choose Open-Meteo When:
- ✅ You need marine data (waves, swell, sea temp)
- ✅ Location is outside Israel
- ✅ You want 7-day forecasts
- ✅ No authentication is a priority
- ✅ You need a free, reliable baseline

### Choose IMS When:
- ✅ You need actual observations (ground truth)
- ✅ Location is in Israel
- ✅ You need high-frequency data (10-min intervals)
- ✅ You're validating other forecasts
- ✅ You need official government data

### Choose GoSurf When:
- 🔮 It provides unique local insights (TBD)
- 🔮 It has better accuracy than Open-Meteo (TBD)
- 🔮 It aggregates sources we don't have (TBD)
- ⚠️ Don't choose if it's just repackaging Open-Meteo/IMS

### Choose Multiple Providers When:
- ✅ You want to compare forecasts (ensemble predictions)
- ✅ You're building an accuracy analysis system
- ✅ You want to detect when providers disagree (uncertainty indicator)
- ✅ You're training an ML model to combine sources

---

## Integration Priority

### Phase 1: Essential (Week 1-2)
1. ✅ **Open-Meteo Marine** - Primary wave forecasting
2. ⚠️ **IMS** - Ground truth for validation

### Phase 2: Expansion (Week 3-4)
3. 🔮 **GoSurf** - Only if adds value beyond Phase 1 providers

### Phase 3: Advanced (Month 2+)
4. 🌊 **Buoy data** - If available from IMS or other sources
5. 👥 **User reports** - Crowd-sourced ground truth

---

## Redundancy & Fallback Strategy

### If Open-Meteo is down:
- Fallback to GoSurf (if integrated)
- Use cached latest forecast
- Alert user of stale data

### If IMS is down:
- Can't validate accuracy (skip validation step)
- Continue collecting Open-Meteo forecasts
- No real-time weather observations available

### If both are down:
- Use cached data
- Display warning to user
- Alert operations team

---

## Cost Analysis

| Provider | Cost per 1000 Requests | Cost per Month (Hourly) | Cost per Year |
|----------|------------------------|-------------------------|---------------|
| Open-Meteo | $0 | $0 | $0 |
| IMS | $0 | $0 | $0 |
| GoSurf | TBD | TBD | TBD |
| **Total** | **$0** | **$0** | **$0** |

**Note:** Infrastructure costs (Lambda, RDS, S3) are separate and estimated in main design document (~$70-90/month for Phase 4).

---

## Summary

**Best All-Around:** Open-Meteo Marine API  
**Most Accurate (Weather):** TBD (measure Open-Meteo vs IMS)  
**Ground Truth Source:** IMS  
**Unknown Value:** GoSurf (pending research)

**Recommendation:** Start with Open-Meteo + IMS, add GoSurf only if research shows it provides unique value.
