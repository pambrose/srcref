---
icon: lucide/code
---

# Programmatic API

srcref is published to **Maven Central** as `com.pambrose:srcref`, providing
the `Api.srcrefUrl()` function for generating srcref URLs programmatically.

## Installation

=== "Gradle (Kotlin DSL)"

    ```kotlin
    dependencies {
        implementation("com.pambrose:srcref:2.0.8")
    }
    ```

=== "Gradle (Groovy DSL)"

    ```groovy
    dependencies {
        implementation 'com.pambrose:srcref:2.0.8'
    }
    ```

=== "Maven"

    ```xml
    <dependency>
        <groupId>com.pambrose</groupId>
        <artifactId>srcref</artifactId>
        <version>2.0.8</version>
    </dependency>
    ```

## `srcrefUrl()` Function

The `Api.srcrefUrl()` function generates a srcref URL string that, when
visited, dynamically resolves to a GitHub permalink.

### Signature

```kotlin
fun srcrefUrl(
    account: String,
    repo: String,
    path: String,
    beginRegex: String,
    beginOccurrence: Int = 1,
    beginOffset: Int = 0,
    beginTopDown: Boolean = true,
    endRegex: String = "",
    endOccurrence: Int = 1,
    endOffset: Int = 0,
    endTopDown: Boolean = true,
    prefix: String = "https://www.srcref.com",
    branch: String = "master",
    escapeHtml4: Boolean = false,
): String
```

### Parameters

| Parameter         | Type      | Default                   | Description |
|-------------------|-----------|---------------------------|-------------|
| `account`         | `String`  | —                         | GitHub username or organization |
| `repo`            | `String`  | —                         | Repository name |
| `path`            | `String`  | —                         | File path in the repository |
| `beginRegex`      | `String`  | —                         | Regex to find the start line |
| `beginOccurrence` | `Int`     | `1`                       | Which occurrence of the begin match (1-based) |
| `beginOffset`     | `Int`     | `0`                       | Lines to offset from begin match |
| `beginTopDown`    | `Boolean` | `true`                    | `true` = top-down, `false` = bottom-up |
| `endRegex`        | `String`  | `""` (empty)              | Regex to find the end line (empty = single line) |
| `endOccurrence`   | `Int`     | `1`                       | Which occurrence of the end match |
| `endOffset`       | `Int`     | `0`                       | Lines to offset from end match |
| `endTopDown`      | `Boolean` | `true`                    | Search direction for end match |
| `prefix`          | `String`  | `https://www.srcref.com`  | Base URL of the srcref service |
| `branch`          | `String`  | `master`                  | Git branch name |
| `escapeHtml4`     | `Boolean` | `false`                   | HTML-escape the output URL |

### Return Value

Returns a `String` containing the complete srcref URL. When visited, this URL
will dynamically resolve the regex patterns and redirect to the corresponding
GitHub permalink.

## Examples

### Basic Usage — Single Line

Highlight a single line by providing only the begin regex:

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:basic-usage"
```

### Line Range

Highlight a range of lines from begin to end pattern:

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:line-range"
```

### Selecting the Nth Occurrence

When a pattern matches multiple lines, select which one:

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:occurrence"
```

### Bottom-Up Search

Search from the end of the file backward — useful for finding the **last**
occurrence of a pattern:

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:bottom-up"
```

### Line Offsets

Shift the highlight above or below the actual match:

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:offset"
```

### HTML-Escaped Output

When embedding a srcref URL in HTML, enable escaping to safely handle `&`
characters:

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:html-escaped"
```

### Custom Prefix and Branch

For self-hosted instances or non-default branches:

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:custom-prefix"
```

### Complex Range Selection

Combine multiple parameters for precise targeting:

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:complex-range"
```

## Use Cases

### Embedding in Markdown Documentation

Generate links for use in README files or documentation:

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:documentation-link"
```

### Embedding in HTML Pages

Use `escapeHtml4 = true` for safe HTML embedding:

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:html-link"
```

### Linking to Multiple Files

Create links to different files in the same repository:

```kotlin
--8<-- "src/test/kotlin/website/AdvancedExamples.kt:multiple-files"
```

### Linking to Presentation Slides

srcref was originally created for [kslides](https://github.com/kslides/kslides),
a Kotlin DSL for creating presentations. It enables presentation slides to
link to live code that stays current:

```kotlin
val slideLink =
    srcrefUrl(
        account = "kslides",
        repo = "kslides",
        path = "kslides-core/src/main/kotlin/com/kslides/Presentation.kt",
        beginRegex = "srcrefUrl\\(",
        endRegex = "escapeHtml4 = true",
        endOffset = 1,
    )
```

## URL Structure

The generated URL follows this format:

```
{prefix}/github?account={account}&repo={repo}&branch={branch}&path={path}&bregex={bregex}&boccur={boccur}&boffset={boffset}&btopd={btopd}[&eregex={eregex}&eoccur={eoccur}&eoffset={eoffset}&etopd={etopd}]
```

- All parameter values are **URL-encoded** automatically
- End parameters are **omitted** when `endRegex` is empty
- The `escapeHtml4` flag only affects the output string — it is not a URL parameter
