# Database Schema Design

## Overview

This document defines the unified database schema for storing multi-provider marine and weather data. The schema is designed to:
- Support multiple data providers with different fields
- Distinguish between forecasts (predictions) and observations (actual measurements)
- Enable efficient time-series queries
- Support TimescaleDB optimizations

**Note:** The complete, comprehensive version of this documentation (including full SQL schemas, TimescaleDB optimizations, query patterns, migrations, etc.) is available in the plan file at:
`/Users/shahafnahmias/.claude/plans/eager-sniffing-castle.md` (Appendix C: Data Storage Schema Design)

## Quick Reference

### Technology Stack
- **Database:** PostgreSQL 14+ with TimescaleDB extension
- **ORM:** Optional (Prisma, TypeORM, or raw SQL)
- **Indexing:** B-tree indexes + TimescaleDB partitioning

### Main Table: forecasts

```sql
CREATE TABLE forecasts (
  -- Primary key
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

  -- Provider identification
  provider VARCHAR(50) NOT NULL,
  data_type VARCHAR(20) NOT NULL,

  -- Location
  location_name VARCHAR(100) NOT NULL,
  latitude DECIMAL(10, 8) NOT NULL,
  longitude DECIMAL(11, 8) NOT NULL,
  station_id VARCHAR(50),

  -- Timestamps
  fetched_at TIMESTAMPTZ NOT NULL,
  forecast_time TIMESTAMPTZ NOT NULL,
  issued_at TIMESTAMPTZ,

  -- Marine metrics
  wave_height DECIMAL(5, 2),
  wave_direction INT,
  wave_period DECIMAL(5, 2),
  swell_height DECIMAL(5, 2),
  swell_direction INT,
  swell_period DECIMAL(5, 2),
  sea_surface_temp DECIMAL(4, 2),

  -- Weather metrics
  temperature DECIMAL(4, 2),
  temperature_max DECIMAL(4, 2),
  temperature_min DECIMAL(4, 2),
  ground_temperature DECIMAL(4, 2),
  humidity DECIMAL(5, 2),
  pressure DECIMAL(6, 2),
  wind_speed DECIMAL(5, 2),
  wind_direction INT,
  wind_gust_max DECIMAL(5, 2),
  cloud_cover INT,
  uv_index DECIMAL(3, 1),
  rainfall DECIMAL(5, 2),

  -- Solar radiation
  global_radiation DECIMAL(7, 2),
  diffuse_radiation DECIMAL(7, 2),
  direct_radiation DECIMAL(7, 2),

  -- Data quality
  completeness DECIMAL(3, 2),
  is_valid BOOLEAN DEFAULT true,
  channel_status INT,

  -- Raw data
  raw_data JSONB,

  -- Metadata
  created_at TIMESTAMPTZ DEFAULT NOW(),

  CONSTRAINT forecasts_provider_check CHECK (provider IN ('open-meteo-marine', 'ims', 'gosurf'))
);

-- Convert to hypertable (TimescaleDB)
SELECT create_hypertable('forecasts', 'forecast_time');
```

### Field Mappings

#### Open-Meteo → Database
- `wave_height` → `wave_height`
- `wave_direction` → `wave_direction`
- `temperature_2m` → `temperature`
- `wind_speed_10m` → `wind_speed`
- See plan file for complete mappings

#### IMS → Database
- `TD` → `temperature`
- `RH` → `humidity`
- `WS` → `wind_speed` (convert m/sec to km/h: × 3.6)
- `BP` → `pressure`
- See plan file for complete mappings

### Unit Conversions

**Wind Speed:**
- Open-Meteo: km/h (store as-is)
- IMS: m/sec → convert to km/h: `wind_speed_raw * 3.6`

## Complete Documentation

For the full documentation including:
- Detailed SQL schema with all constraints
- Complete field mapping tables
- TimescaleDB continuous aggregations
- Retention policies and compression
- Common query patterns with examples
- Data quality calculation functions
- Migration strategy (SQLite → PostgreSQL → TimescaleDB)
- Backup & recovery procedures
- Performance considerations

**Refer to:** The plan file at `/Users/shahafnahmias/.claude/plans/eager-sniffing-castle.md`
**Section:** APPENDIX C: Data Storage Schema Design

Or refer to the high-level design document created during the planning phase.
