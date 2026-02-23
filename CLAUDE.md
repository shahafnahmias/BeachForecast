# Claude Code Instructions

## Critical Rules

### Always Run Tests After Code Changes

**IMPORTANT**: After making ANY code changes (features, refactoring, bug fixes, etc.), you MUST run the test suite to verify nothing broke.

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew test --console=plain
```

**When to run tests:**
- ✅ After adding new features
- ✅ After refactoring code
- ✅ After fixing bugs
- ✅ After updating dependencies
- ✅ After deleting files
- ✅ After renaming classes/methods
- ✅ Before marking a task as complete

**What to check:**
1. All tests pass (BUILD SUCCESSFUL)
2. No new test failures
3. Test coverage hasn't decreased significantly
4. No unexpected deprecation warnings in critical code

**If tests fail:**
1. Read the failure message carefully
2. Fix the broken code
3. Run tests again
4. Repeat until all tests pass

### Test-First Development

When adding new features:
1. Write the test first (if applicable)
2. Implement the feature
3. Run tests to verify
4. Refactor if needed
5. Run tests again

### Never Leave Multiple Versions of the Same Class

**CRITICAL**: When creating improved/enhanced versions of classes, ALWAYS migrate all usages and delete old versions immediately.

❌ **WRONG - Technical Debt:**
```
LocationProviderImpl.kt (old)
LocationProviderV2.kt (new, improved)
WeatherRepository.kt (still using old LocationProviderImpl)
```

✅ **CORRECT - Clean Migration:**
1. Create improved version (e.g., `LocationProviderV2`)
2. **Immediately search for ALL usages** of old version:
   ```bash
   grep -r "LocationProviderImpl" app/src/main/
   ```
3. **Update ALL files** to use new version
4. **Delete the old file** completely
5. **Run tests** to verify migration
6. **Rename new version** to standard name (remove "V2" suffix) if appropriate

**Why this matters:**
- Multiple versions cause confusion
- Old bugs persist in unused code
- New features don't reach all consumers
- Code becomes unmaintainable
- Increases technical debt

**Process for class improvements:**
1. ✅ Create new version with improvements
2. ✅ Search codebase for ALL usages of old version
3. ✅ Update every single usage to new version
4. ✅ Delete old version file
5. ✅ Run tests to verify nothing broke
6. ✅ Consider renaming if version suffix is temporary

**Examples of what to search:**
- Direct instantiation: `LocationProviderImpl(context)`
- Imports: `import ...LocationProviderImpl`
- Type declarations: `val provider: LocationProviderImpl`
- Dependency injection: `@Inject locationProvider: LocationProviderImpl`

**Grep commands to find all usages:**
```bash
# Find all references to a class
grep -r "ClassName" app/src/

# Find imports
grep -r "import.*ClassName" app/src/

# Find instantiations
grep -r "ClassName(" app/src/
```

## Project-Specific Guidelines

### Architecture
- This is a Clean Architecture project with clear layer separation
- Domain layer should have NO Android dependencies
- Always maintain separation of concerns

### Code Style
- Use Kotlin idiomatic patterns
- Prefer immutability
- Use nullable types safely
- Add meaningful comments for complex logic

### Widget Development
- Test widget updates thoroughly
- Consider background restrictions
- Handle location/network failures gracefully
- Cache data when appropriate

### Location Handling
- Always fall back to cached location
- Location cache is valid for 24 hours
- Handle permission denials gracefully

## Common Tasks

### Running Tests
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew test --console=plain
```

### Building the App
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug
```

### Checking Test Coverage
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew testDebugUnitTestCoverage
```

### Track Issues and TODOs in This File

**IMPORTANT**: Whenever you discover issues, bugs, inconsistencies, or things that still need to be done during work, document them in the "Known Issues & TODOs" section below.

**Rules:**
- Add new items as soon as they're discovered, even if you can't fix them right now
- Mark items as **DONE** (with date) when completed — don't delete them immediately, so we have a record
- Periodically clean up old **DONE** items to keep the list manageable
- Include enough context so the issue is actionable in a future session
- Prefix with priority: `[P0]` critical, `[P1]` important, `[P2]` nice-to-have

## Known Issues & TODOs

### Play Store Blockers (Creative/External — not code)
- `[P0]` Export 512x512 PNG app icon for Play Console upload (adaptive icon exists in-app, just needs export)
- `[P0]` Create 1024x500 feature graphic for Play Store listing
- `[P0]` Capture 5-8 actual device screenshots from running app
- `[P0]` Write short description (80 chars max) and full description (4000 chars max) for Play Store
- `[P0]` Enable GitHub Pages on repo (Settings > Pages > main branch > /docs folder) for privacy policy URL

### Play Store Warnings (Completed Feb 10, 2026)
- **DONE** `[P0]` Privacy policy email mismatch — fixed to use shahafnahmias@gmail.com consistently
- **DONE** `[P0]` Privacy policy missing background location disclosure — added section explaining widget location access
- **DONE** `[P0]` Privacy policy missing data deletion instructions — added "How to Delete Your Data" section
- **DONE** `[P0]` `SeaLevelApplication.kt:39`: ANR risk from `runBlocking` — switched to UserPreferences.getAppLanguageSync() with try-catch
- **DONE** `[P0]` `WeatherRepository.kt:66`: force unwrap `cachedData!!` — replaced with safe `?.let`
- **DONE** `[P1]` Wave height PNGs and UV PNGs — confirmed unused, deleted all 16 files
- **DONE** `[P1]` `LocationProvider.kt:160`: unsafe cast — replaced with safe cast `as? LocationManager`
- **DONE** `[P1]` `URLConnectionHttpClient.kt:17`: unsafe cast — replaced with safe cast `as? HttpURLConnection`
- **DONE** `[P1]` Privacy policy date too vague — updated to "February 10, 2026"
- **DONE** `[P1]` `MainActivity.kt:108`: permission re-request gating — removed `onboardingCompleted` check
- **DONE** `[P1]` Location rationale strings — updated to mention background widget access
- **DONE** `[P1]` `rememberSaveable` — replaced `remember` with `rememberSaveable` in HomeScreen (2), ForecastScreen (2), TrendsScreen (1), SettingsScreen (5 dialog toggles)

### Play Store Warnings (Remaining)
- **DONE (Feb 10, 2026)** `[P1]` No Firebase Analytics opt-out toggle in Settings — added toggle in About section, preference storage, app startup hook, and privacy policy mention

### Backlog
- `[P2]` No branded splash screen — consider `core-splashscreen` library
- `[P2]` TODO comments in production: `GroupTodayByPeriodsUseCase:41` (sunset calc), `WeatherRepository:124` (coastal proximity)
- `[P2]` `GenerateTodaySummaryUseCase` (in widget/) and `GroupTodayByPeriodsUseCase` still take Context — should be refactored
- **DONE (Feb 10, 2026)** `[P2]` Deprecated Material icons — replaced with AutoMirrored versions (ShowChart, TrendingUp, KeyboardArrowRight)
- **DONE (Feb 10, 2026)** `[P2]` String resource format warnings — fixed positional format in wave summary strings (%1$s, %2$s)

## Remember

🧪 **No code change is complete until tests pass!**
