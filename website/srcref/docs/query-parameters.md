---
icon: lucide/settings
---

# Query Parameters

srcref URLs accept 12 query parameters that control how lines are located
in the target file. Parameters are divided into **required** (repository and
begin-line matching) and **optional** (end-line matching for ranges).

## Parameter Reference

### Repository Parameters

| Parameter   | Default    | Required | Description                        |
|-------------|------------|----------|------------------------------------|
| `account`   |            | Yes      | GitHub username or organization    |
| `repo`      |            | Yes      | Repository name                    |
| `branch`    | `master`   | Yes      | Branch name                        |
| `path`      |            | Yes      | File path within the repository    |

### Begin Parameters

These parameters determine the **start line** of the highlight.

| Parameter | Default | Required | Description                                      |
|-----------|---------|----------|--------------------------------------------------|
| `bregex`  |         | Yes      | Regex pattern to match the beginning line        |
| `boccur`  | `1`     | Yes      | Which occurrence of the match to use (1-based)   |
| `boffset` | `0`     | Yes      | Lines to offset from the match (positive=below, negative=above) |
| `btopd`   | `true`  | Yes      | Search direction: `true`=top-down, `false`=bottom-up |

### End Parameters

These parameters determine the **end line** of the highlight. They are
**optional** — when omitted or when `eregex` is empty, srcref highlights a
single line.

| Parameter | Default | Required | Description                                      |
|-----------|---------|----------|--------------------------------------------------|
| `eregex`  |         | No       | Regex pattern to match the ending line           |
| `eoccur`  | `1`     | No       | Which occurrence of the end match to use         |
| `eoffset` | `0`     | No       | Lines to offset from the end match               |
| `etopd`   | `true`  | No       | Search direction for end match                   |

## Detailed Parameter Guide

### `bregex` / `eregex` — Regex Patterns

The regex patterns use
[Java regex syntax](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).
Each line of the file is tested individually against the pattern.

!!! tip
    Use [regex101.com](https://regex101.com) (select Java flavor) to develop
    and test your patterns before using them in srcref URLs.

**Simple literal match:**

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:literal-string"
```

**Escaped metacharacters:**

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:escape-parens"
```

!!! warning "URL Encoding"
    When constructing URLs manually, remember to URL-encode special
    characters. The web form and API handle this automatically.

    | Character | URL Encoded |
    |-----------|-------------|
    | `\`       | `%5C`       |
    | `(`       | `%28`       |
    | `)`       | `%29`       |
    | `[`       | `%5B`       |
    | `]`       | `%5D`       |
    | `{`       | `%7B`       |
    | `}`       | `%7D`       |
    | `+`       | `%2B`       |
    | space     | `%20` or `+`|

### `boccur` / `eoccur` — Occurrence Selection

When a regex matches multiple lines, the occurrence parameter selects
**which match** to use. The value is **1-based** (first match = 1).

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:occurrence"
```

!!! note
    If the file contains fewer matches than the specified occurrence,
    srcref returns an error: `Required matches (N) not found for begin regex: "pattern"`.

### `boffset` / `eoffset` — Line Offset

Offsets shift the highlighted line **relative to the match**:

- **Positive offset**: moves the highlight below the match
- **Negative offset**: moves the highlight above the match
- **Zero (default)**: highlights the matched line itself

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:offset"
```

In this example:

- The begin line is moved **1 line above** the `install(CallLogging)` match
- The end line is moved **5 lines below** the `install(Compression)` match

!!! warning
    The final line number (match + offset) must be >= 1. If the offset
    would produce a line number less than 1, srcref returns an error.

### `btopd` / `etopd` — Search Direction

Controls whether the file is searched **top-down** or **bottom-up**:

- `true` (default): searches from the first line to the last
- `false`: searches from the last line to the first

**Top-down (default)** — finds the first occurrence from the top:

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:basic-usage"
```

**Bottom-up** — finds the first occurrence from the bottom:

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:bottom-up"
```

Bottom-up search is particularly useful for finding closing braces or the
**last** occurrence of a pattern:

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:bottom-up-last-brace"
```

### `branch` — Branch Selection

Defaults to `master` (or the value of the `DEFAULT_BRANCH` environment variable
on self-hosted instances). Override it to target other branches:

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:custom-branch"
```

## How Parameters Interact

### Single-Line Highlight

When `eregex` is empty (or omitted), srcref highlights a single line:

```
Final line = (bregex match) + boffset
```

### Line-Range Highlight

When `eregex` is provided, srcref highlights from the begin line to the end line:

```
Begin line = (bregex match) + boffset
End line   = (eregex match) + eoffset
GitHub URL = ...#L{begin}-L{end}
```

If the begin and end lines are the same, only a single line is highlighted.

### Search Direction + Occurrence

The occurrence counter resets based on direction:

- **Top-down**: occurrence 1 = the first match reading from line 1
- **Bottom-up**: occurrence 1 = the first match reading from the last line

This means `boccur=1` with `btopd=false` gives you the **last** match in the file,
while `boccur=2` with `btopd=false` gives you the **second-to-last** match.

## Error Handling

srcref provides clear error messages when parameters are invalid:

| Error | Cause |
|-------|-------|
| `Missing: bregex value` | Required parameter is missing or blank |
| `Invalid regex:"..." - ...` | The regex pattern has a syntax error |
| `Required matches (N) not found for begin regex: "..."` | Fewer matches than the specified occurrence |
| `Begin line number is less than 1` | Negative offset pushed the line number below 1 |
| `Regex matching timed out for begin regex: "..."` | Pattern took longer than 5 seconds (catastrophic backtracking) |
| `Invalid content type: ...` | Target file is binary, not text |
| `Content-Length exceeds maximum length: ...` | File exceeds the 5MB size limit |

On error, srcref redirects to a `/problem` page displaying the error message
along with all the original parameters, so you can fix the issue and retry.
