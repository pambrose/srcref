---
icon: lucide/rocket
---

# Getting Started

There are two ways to create srcref URLs: through the **web interface** or
using the **programmatic API**.

## Using the Web Interface

### Step 1: Open the Editor

Visit [www.srcref.com](https://www.srcref.com) to open the srcref editor form.

### Step 2: Fill in Repository Details

Enter your GitHub repository information:

| Field       | Example             | Description                   |
|-------------|---------------------|-------------------------------|
| **Account** | `pambrose`          | GitHub username or org        |
| **Repo**    | `srcref`            | Repository name               |
| **Branch**  | `master`            | Branch name (default: master) |
| **Path**    | `src/main/.../Main.kt` | File path within the repo |

### Step 3: Define Begin Pattern

The **Begin Regex** determines which line to highlight. For example, to find
a function declaration:

```
fun main
```

For patterns containing regex metacharacters like parentheses, escape them:

```
install\(CallLogging\)
```

### Step 4: Optionally Define End Pattern

Leave the **End Regex** empty to highlight a single line. Fill it in to
highlight a range of lines from the begin match to the end match.

### Step 5: Generate and Use

1. Click **Generate URL** to create the srcref URL
2. Click **View GitHub Permalink** to verify it points to the right lines
3. Click **Copy URL** to copy the srcref URL to your clipboard
4. Paste it into your documentation

### Editing an Existing URL

Add `&edit` to any srcref URL to open it in the editor with all fields pre-filled:

```
--8<-- "src/test/kotlin/website/UrlExamples.txt:web-edit"
```

## Using the Programmatic API

### Add the Dependency

=== "Gradle (Kotlin DSL)"

    ```kotlin
    dependencies {
        implementation("com.pambrose:srcref:2.0.9")
    }
    ```

=== "Gradle (Groovy DSL)"

    ```groovy
    dependencies {
        implementation 'com.pambrose:srcref:2.0.9'
    }
    ```

=== "Maven"

    ```xml
    <dependency>
        <groupId>com.pambrose</groupId>
        <artifactId>srcref</artifactId>
        <version>2.0.9</version>
    </dependency>
    ```

### Generate a Single-Line Link

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:single-line"
```

### Generate a Line-Range Link

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:line-range"
```

## Example Walkthrough

Let's create a srcref URL that highlights the middleware setup in srcref's
own `Main.kt` — from `install(CallLogging)` to 3 lines past `install(Compression)`.

### Using the Web Form

Enter these values:

| Field           | Value                                                   |
|-----------------|---------------------------------------------------------|
| Account         | `pambrose`                                              |
| Repo            | `srcref`                                                |
| Branch          | `master`                                                |
| Path            | `src/main/kotlin/com/pambrose/srcref/Main.kt`          |
| Begin Regex     | `install\(CallLogging\)`                                |
| Begin Occur     | `1`                                                     |
| Begin Offset    | `0`                                                     |
| Begin Top-Down  | `true`                                                  |
| End Regex       | `install\(Compression\)`                                |
| End Occur       | `1`                                                     |
| End Offset      | `3`                                                     |
| End Top-Down    | `false`                                                 |

### Using the API

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:line-range"
```

Both approaches produce a URL that, when clicked, redirects to the exact
GitHub lines showing the middleware configuration — no matter how the
surrounding code changes.

## Common Patterns

Here are some patterns you'll use frequently:

### Highlight a Function

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:function-highlight"
```

### Highlight a Class

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:class-definition"
```

### Highlight All Imports

```kotlin
--8<-- "src/test/kotlin/website/ApiExamples.kt:import-statement"
```

## What's Next?

- Learn about all [query parameters](query-parameters.md) for fine-tuning your links
- Master [regex patterns](regex-guide.md) for precise code targeting
- Explore the full [programmatic API](api.md) for automation
- See [advanced usage](advanced-usage.md) for offset tricks, bottom-up search, and more
