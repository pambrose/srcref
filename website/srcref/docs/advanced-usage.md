---
icon: lucide/wrench
---

# Advanced Usage

This page covers advanced techniques for creating precise srcref links using
offset tricks, directional search, and pattern strategies.

## Search Direction Strategies

### Top-Down (Default)

Top-down search scans the file from line 1 to the last line. This is the
default and works well for most cases:

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:basic-usage"
```

### Bottom-Up Search

Bottom-up search scans from the last line upward. This is powerful for
finding **closing** constructs or the **last** occurrence of a pattern:

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:bottom-up-last-brace"
```

!!! tip "When to Use Bottom-Up"
    - Finding the closing brace of a class, function, or block
    - Finding the last import statement
    - Finding the last occurrence of a repeated pattern
    - Finding the end of a file-level construct

### Combining Directions

You can use different directions for begin and end patterns. A common
pattern is top-down for the start and bottom-up for the end:

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:entire-function"
```

This finds `calcLineNumber` from the top and its closing `}` from the bottom,
covering the entire function regardless of how many internal braces it has.

## Offset Techniques

### Skipping Past Signatures

Use a positive offset to skip past a function signature and highlight only
the body:

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:skip-to-body"
```

### Context Above the Match

Use a negative offset to include lines **before** the match — useful for
capturing documentation comments or annotations:

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:negative-offset"
```

This highlights 5 lines above `calcLineNumber`, capturing its KDoc comment.

### Trimming Range Edges

Combine offsets on both begin and end to narrow a range:

- `beginOffset = 1` skips the opening line
- `endOffset = -1` skips the closing line

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:skip-to-body"
```

## Occurrence Selection

### Finding the Nth Match

When a pattern appears multiple times, use occurrence to pick the right one:

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:nth-occurrence"
```

### Occurrence with Bottom-Up Search

When combined with bottom-up search, occurrences count from the **end** of
the file:

| `btopd` | `boccur` | Selects                       |
|---------|----------|-------------------------------|
| `true`  | `1`      | First match from the top      |
| `true`  | `2`      | Second match from the top     |
| `false` | `1`      | Last match (first from bottom)|
| `false` | `2`      | Second-to-last match          |

## Targeting Code Constructs

### Highlighting an Entire Function

Match the declaration at the top and the closing brace from the bottom:

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:entire-function"
```

### Highlighting a Companion Object

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:companion-object"
```

### Highlighting a When Expression

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:when-expression"
```

### Highlighting Enum Entries

Match from the first entry to the last:

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:enum-values"
```

### Highlighting a Route Handler

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:route-handler"
```

## Indentation-Aware Patterns

When matching closing braces (`}`), indentation helps disambiguate between
nested and top-level braces.

| Pattern       | Matches                        |
|---------------|--------------------------------|
| `^\}`         | Top-level closing brace (no indent) |
| `^  \}`       | 2-space indented closing brace |
| `^    \}`     | 4-space indented closing brace |
| `^\s+\}`      | Any indented closing brace     |

!!! note
    In Kotlin strings, spaces in regex are literal. `"^\\ \\ \\}"` matches
    a line starting with two spaces followed by `}`.

## Regex Timeout Protection

srcref enforces a **5-second timeout** on each regex search to prevent
catastrophic backtracking from consuming server resources.

Patterns that can cause timeouts:

| Dangerous Pattern | Why                                | Safe Alternative          |
|-------------------|------------------------------------|---------------------------|
| `(.+)+`           | Exponential backtracking           | `.+`                      |
| `(a*)*`           | Nested quantifiers                 | `a*`                      |
| `(a\|aa)+`        | Overlapping alternatives           | `a+`                      |
| `.*.*.*`          | Multiple unbounded quantifiers     | `.*`                      |

If a timeout occurs, srcref returns:
`Regex matching timed out for begin regex: "pattern"`

## Working with URLs

### Editing an Existing srcref URL

Append `&edit` to any srcref URL to open it in the web editor with all
fields pre-populated:

```
--8<-- "src/test/kotlin/website/UrlExamples.kt:web-edit"
```

### Manually Constructing URLs

When you can't use the API, construct URLs directly. Remember to URL-encode
parameter values:

```
--8<-- "src/test/kotlin/website/UrlExamples.kt:web-single-line"
```

For line ranges, add the end parameters:

```
--8<-- "src/test/kotlin/website/UrlExamples.kt:web-line-range"
```

## Self-Hosting Tips

### Custom URL Prefix

When self-hosting, set the `prefix` parameter to your instance URL:

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:custom-prefix"
```

### Private Repositories

srcref fetches files from GitHub's raw content URL
(`raw.githubusercontent.com`). For private repositories, you'll need to
self-host and configure authentication at the network level.

## Performance Considerations

### Caching

srcref caches file content with **ETag-based validation**. Subsequent requests
for the same file use HTTP conditional requests (304 Not Modified) to avoid
re-downloading unchanged content.

- Default cache size: **2048 entries** (configurable via `MAX_CACHE_SIZE`)
- LRU eviction runs every **5 minutes** when the cache exceeds its limit
- Maximum file size: **5MB** (configurable via `MAX_LENGTH`)

### Regex Efficiency

For best performance:

1. **Be specific** — `install\(CallLogging\)` is faster than `install\(.+\)`
2. **Anchor when possible** — `^import` is faster than `import`
3. **Avoid backtracking** — use possessive quantifiers or atomic groups
4. **Keep it simple** — the simplest pattern that uniquely matches is best
