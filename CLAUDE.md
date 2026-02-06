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

## Remember

🧪 **No code change is complete until tests pass!**
