# Marine Data Aggregation System Documentation

## Overview

This directory contains comprehensive documentation for the multi-provider marine and weather data aggregation system. The system is designed to fetch data from multiple providers (Open-Meteo, IMS, GoSurf), store historical forecasts in a time-series database, and enable comparative analysis to improve prediction accuracy for Israeli coastal conditions.

## Documentation Files

### 1. [open-meteo-api.md](./open-meteo-api.md)
Complete API reference for Open-Meteo Marine & Weather API.

**Contents:**
- API endpoints (Marine API, Weather Forecast API)
- Request parameters and response structures
- Data characteristics and error handling
- Storage requirements
- Example requests and responses

**Key Info:**
- Type: Forecast data (predictions)
- Coverage: Global
- Cost: Free, no authentication required
- Update Frequency: Hourly
- Data Size: ~18 KB per combined fetch

### 2. [ims-api.md](./ims-api.md)
Complete API reference for IMS (Israel Meteorological Service) API.

**Contents:**
- 8 API endpoints (stations, regions, data retrieval)
- 17 available measurement channels
- Authentication requirements
- Critical timezone handling notes
- Data characteristics and storage requirements

**Key Info:**
- Type: Observation data (GROUND TRUTH!)
- Coverage: Israel only (85+ stations)
- Cost: Free (requires API token via email)
- Update Frequency: 10-minute intervals
- Data Size: ~108 KB per station per day

**⚠️ Important:** IMS uses LST (winter time) year-round, creating a 1-hour offset during daylight saving time.

### 3. [api-comparison.md](./api-comparison.md)
Side-by-side comparison of all data providers.

**Contents:**
- Provider summary table
- Detailed comparison (strengths, weaknesses, data coverage)
- Use case recommendations
- Data quality comparison
- Provider selection matrix
- Integration priority and fallback strategies
- Cost analysis

**Use This To:**
- Choose the right provider for your use case
- Understand provider strengths and limitations
- Plan integration priorities
- Design fallback strategies

### 4. [data-schema.md](./data-schema.md)
Database schema design and field mappings.

**Contents:**
- Complete SQL schema for `forecasts` table
- Field mappings (Open-Meteo → DB, IMS → DB)
- Unit conversion formulas
- TimescaleDB optimizations
- Query patterns and examples

**Use This To:**
- Set up the database
- Understand how provider data maps to unified schema
- Implement data normalization
- Query historical data efficiently

**Note:** Full comprehensive version with ~400 lines of SQL, query examples, migrations, etc. is available in the plan file.

### 5. [integration-guide.md](./integration-guide.md)
Step-by-step guide for integrating new providers.

**Contents:**
- Provider information checklist
- 11-step integration process
- Code templates (client, parser, normalizer)
- Common issues and solutions
- Best practices

**Use This To:**
- Add new data providers to the system
- Troubleshoot integration issues
- Follow consistent integration patterns
- Implement provider-specific logic

**Note:** Full comprehensive version with complete code templates (~500 lines) is available in the plan file.

## System Design Documents

The complete high-level design is available in:
- **Plan File:** `/Users/shahafnahmias/.claude/plans/eager-sniffing-castle.md`

**High-Level Design Contains:**
- Executive summary with phased approach (Phase 1-4)
- Known provider data structures
- Phased implementation details (minimal → production)
- Architecture diagrams
- Data collection architecture (Lambdas, SQS, S3)
- Data processing layer
- Storage layer (TimescaleDB)
- Analysis & visualization (Grafana)
- Infrastructure as Code (Terraform)
- Cost estimation (~$0-90/month depending on phase)
- Complete appendices with all API documentation

## Quick Navigation

### For Implementation:
1. **Start Here:** Review `api-comparison.md` to understand providers
2. **Database Setup:** Follow `data-schema.md` for schema creation
3. **Add Provider:** Use `integration-guide.md` for step-by-step process
4. **API Reference:** Use `open-meteo-api.md` or `ims-api.md` as needed

### For Planning:
1. **Understand Options:** Read `api-comparison.md`
2. **Review Design:** Check the plan file for complete high-level design
3. **Estimate Costs:** See cost analysis in plan file and `api-comparison.md`
4. **Choose Phase:** Review phased approach in plan file

### For Analysis:
1. **Query Patterns:** See `data-schema.md` for example queries
2. **Provider Comparison:** Use `api-comparison.md` for metrics
3. **Data Quality:** Check completeness and accuracy sections in `api-comparison.md`

## Phased Implementation Summary

### Phase 1: Minimal Viable System (Week 1-2)
- **Storage:** ~50 MB (3 months)
- **Providers:** Open-Meteo only
- **Database:** SQLite
- **Infrastructure:** Local/single server
- **Cost:** $0-5/month

### Phase 2: Multi-Provider Comparison (Week 3-4)
- **Storage:** ~200 MB (3 months)
- **Providers:** Open-Meteo + IMS
- **Database:** PostgreSQL
- **Infrastructure:** PostgreSQL + Grafana
- **Cost:** $15-25/month

### Phase 3: Multiple Locations & AWS Lambda (Month 2)
- **Storage:** ~1-2 GB (3 months)
- **Providers:** Open-Meteo + IMS + GoSurf
- **Database:** PostgreSQL
- **Infrastructure:** AWS Lambda + SQS + RDS
- **Cost:** $40-60/month

### Phase 4: Advanced Analytics & TimescaleDB (Month 3+)
- **Storage:** ~2-3 GB (6 months)
- **Providers:** All + ML models
- **Database:** TimescaleDB
- **Infrastructure:** Full production (Lambda, SQS, RDS, S3, Grafana)
- **Cost:** $70-90/month

## Key Insights

### Provider Roles
- **Open-Meteo:** Primary wave forecasting (only source for marine data)
- **IMS:** Ground truth for accuracy validation (actual observations)
- **GoSurf:** TBD - pending research (possible local expertise)

### Critical Discoveries
1. **IMS provides ground truth!** It's observation data (actual measurements), not forecasts
2. **No marine ground truth available** - IMS has no buoy/wave sensors
3. **IMS timezone quirk** - Uses LST year-round, 1-hour offset in summer
4. **Zero API costs** - All providers are free (infrastructure costs only)

### Data Sizes
- **Open-Meteo:** 18 KB per fetch (combined marine + weather)
- **IMS:** 108 KB per station per day (10-minute intervals)
- **Total Phase 1:** ~40 MB for 3 months (single location)

## Next Steps

1. **Review Documentation:** Read through all 5 files
2. **Choose Starting Phase:** Decide on Phase 1-4 based on needs
3. **Request IMS Token:** Email ims@ims.gov.il for API access
4. **Research GoSurf:** Determine if they have an API
5. **Set Up Infrastructure:** Follow Phase 1 minimal approach
6. **Start Data Collection:** Begin with Open-Meteo only

## Questions?

For detailed implementation questions, refer to:
- **High-level design:** Plan file
- **API specifics:** Individual API documentation files
- **Database questions:** `data-schema.md`
- **Integration questions:** `integration-guide.md`
- **Provider selection:** `api-comparison.md`

---

**Last Updated:** 2025-12-05
**Version:** 1.0
**Status:** Complete high-level design and documentation
