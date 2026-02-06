# Provider Integration Guide

## Overview

This guide provides step-by-step instructions for integrating new weather/marine data providers into the aggregation system.

**Note:** The complete, comprehensive version of this guide (including detailed code templates, 11-step process, troubleshooting, etc.) is available in the plan file at:
`/Users/shahafnahmias/.claude/plans/eager-sniffing-castle.md` (see earlier conversation messages for full content)

## Quick Start Checklist

Before integrating a new provider, gather:

### Provider Information
- [ ] Provider name and official documentation URL
- [ ] Data type (forecast, observation, or both)
- [ ] Geographic coverage
- [ ] Authentication requirements (API key, token, OAuth, etc.)
- [ ] Rate limits
- [ ] Cost structure
- [ ] Update frequency
- [ ] Available metrics
- [ ] Typical response size

### Technical Requirements
- [ ] API base URL
- [ ] Authentication method and credentials
- [ ] Request format (GET/POST, query params, headers)
- [ ] Response structure (JSON, XML, CSV)
- [ ] Error response format
- [ ] Expected latency

## Integration Steps (Summary)

1. **Create Provider Documentation** - Document API in `docs/{provider}-api.md`
2. **Create Provider Module** - Build client, parser, normalizer
3. **Implement HTTP Client** - Handle API requests
4. **Implement Parser** - Parse API responses
5. **Implement Normalizer** - Convert to unified schema
6. **Add Configuration** - Update provider config
7. **Create Fetcher** - Build data fetcher Lambda/script
8. **Update Database Schema** - Add new columns if needed
9. **Add Tests** - Unit and integration tests
10. **Update Comparison Docs** - Add to api-comparison.md
11. **Deploy and Monitor** - Deploy and verify data collection

## Example: Adding a New Provider

### Step 1: Create Documentation

Create `docs/new-provider-api.md`:

```markdown
# New Provider API Documentation

## Overview
- **Provider:** Provider Name
- **Type:** Forecast | Observation
- **Authentication:** API Key | Token | None
- **Rate Limits:** X requests/minute
- **Cost:** Free | Paid
- **Update Frequency:** Hourly | 10-min | etc.

## API Endpoints
...
```

### Step 2: Create Module Structure

```
src/providers/new-provider/
├── client.ts          # HTTP client
├── parser.ts          # Parse responses
├── normalizer.ts      # Convert to unified schema
└── types.ts           # TypeScript types
```

### Step 3-7: Implementation

See the complete guide in the plan file for:
- Complete code templates for client, parser, normalizer
- Configuration examples
- Fetcher Lambda implementation
- Error handling patterns
- Testing strategies

## Common Issues & Solutions

### Rate Limiting
- Add delays between requests
- Implement exponential backoff
- Use provider's batch endpoints

### Authentication Failures
- Verify API key/token format
- Check token expiration
- Ensure correct header format

### Timezone Confusion
- Always store as UTC
- Document provider's timezone behavior
- Handle timezone conversions explicitly

## Complete Documentation

For the full integration guide including:
- Detailed 11-step integration process
- Complete code templates (client.ts, parser.ts, normalizer.ts, fetcher.ts)
- Provider information checklist
- Technical requirements checklist
- Common issues and solutions
- Best practices
- Testing patterns
- Example: GoSurf integration walkthrough

**Refer to:** The plan file at `/Users/shahafnahmias/.claude/plans/eager-sniffing-castle.md`
**Or:** Review the comprehensive content documented during the planning phase (see FILE 5 content in earlier messages)

## Resources

- Provider configurations: `config/providers.ts`
- Database schema: `docs/data-schema.md`
- API comparisons: `docs/api-comparison.md`
- Test examples: `tests/providers/`
- Open-Meteo implementation: `src/providers/open-meteo/`
- IMS implementation: `src/providers/ims/` (when completed)
