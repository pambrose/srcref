---
icon: lucide/regex
---

# Regex Guide

srcref uses [Java regex syntax](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)
to match lines in the target file. Each line is tested individually — you don't
need to worry about multi-line matching.

!!! tip
    Use [regex101.com](https://regex101.com) (select **Java 8** flavor) to
    develop and test your regex patterns interactively.

## Literal Strings

The simplest pattern is a literal string. If the target line contains the
string, it matches:

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:literal-string"
```

No special syntax needed — just type the text you want to find.

## Escaping Metacharacters

Certain characters have special meaning in regex. When you want to match
them literally, prefix them with a backslash (`\`).

!!! warning "Double Escaping in Kotlin"
    In Kotlin string literals, `\` is also an escape character. To get a
    literal `\(` in the regex, you write `\\(` in your Kotlin code.

### Characters That Need Escaping

| Character | Meaning in Regex         | Escaped Form |
|-----------|--------------------------|-------------|
| `(`  `)`  | Grouping                 | `\(`  `\)` |
| `[`  `]`  | Character class          | `\[`  `\]` |
| `{`  `}`  | Quantifier               | `\{`  `\}` |
| `.`       | Any character            | `\.`        |
| `*`       | Zero or more             | `\*`        |
| `+`       | One or more              | `\+`        |
| `?`       | Zero or one              | `\?`        |
| `^`       | Start of line            | `\^`        |
| `$`       | End of line              | `\$`        |
| `\|`      | Alternation (OR)         | `\\|`       |
| `\`       | Escape character         | `\\`        |

### Parentheses

The most common escaping need — function calls, constructors, and method
invocations all contain parentheses:

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:escape-parens"
```

### Square Brackets

Used in array access, destructuring, and Kotlin lambda syntax:

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:escape-brackets"
```

### Curly Braces

Used in string templates, blocks, and quantifiers:

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:escape-braces"
```

## Wildcards and Quantifiers

### The Dot (`.`) — Any Character

A dot matches any single character. Combined with quantifiers, it matches
flexible stretches of text:

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:dot-wildcard"
```

This matches any line containing `install(` followed by one or more characters
and a closing `)`.

### Common Quantifiers

| Pattern  | Meaning                | Example           |
|----------|------------------------|-------------------|
| `X+`     | One or more of X       | `\w+` = one or more word chars |
| `X*`     | Zero or more of X      | `\s*` = optional whitespace |
| `X?`     | Zero or one of X       | `\(?\w+\)?` = optional parens |
| `X{n}`   | Exactly n of X         | `\d{4}` = four digits |
| `X{n,m}` | Between n and m of X   | `\w{2,10}` = 2-10 word chars |

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:quantifiers"
```

!!! danger "Avoid Catastrophic Backtracking"
    Patterns like `(.+)+` or `(a*)*` can cause exponential backtracking.
    srcref enforces a **5-second timeout** per regex match to protect against
    this, but it's better to write efficient patterns.

    **Safe alternatives:**

    - Use `[^)]+` instead of `.+` when matching inside delimiters
    - Use possessive quantifiers like `\w++` when backtracking isn't needed
    - Prefer `\w+` over `.*` when matching identifiers

## Anchors

### `^` — Start of Line

Use `^` to match only at the beginning of a line. This is useful for
distinguishing top-level declarations from nested ones:

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:anchor-start"
```

### `$` — End of Line

Use `$` to match at the end of a line.

## Character Classes

Match any single character from a set:

| Class    | Meaning                         |
|----------|---------------------------------|
| `[abc]`  | Matches `a`, `b`, or `c`       |
| `[a-z]`  | Matches any lowercase letter    |
| `[A-Z]`  | Matches any uppercase letter    |
| `[0-9]`  | Matches any digit               |
| `[^abc]` | Matches anything except `a`, `b`, `c` |

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:character-class"
```

### Shorthand Character Classes

| Shorthand | Equivalent    | Meaning              |
|-----------|---------------|----------------------|
| `\d`      | `[0-9]`       | Digit                |
| `\D`      | `[^0-9]`      | Non-digit            |
| `\w`      | `[a-zA-Z0-9_]`| Word character       |
| `\W`      | `[^a-zA-Z0-9_]`| Non-word character  |
| `\s`      | `[ \t\n\r\f]` | Whitespace           |
| `\S`      | `[^ \t\n\r\f]`| Non-whitespace       |

## Word Boundaries

Use `\b` to match at word boundaries, preventing partial matches:

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:word-boundary"
```

Without `\b`, a pattern like `fun` would also match inside words like
`function` or `refund`.

## Alternation

Use `|` to match one of several alternatives:

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:alternation"
```

## Recipes for Common Code Patterns

### Match a Function Declaration

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:function-pattern"
```

### Match a Class or Object

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:class-pattern"
```

### Match an Enum Entry

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:enum-entry-pattern"
```

### Match an Annotation

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:annotation-pattern"
```

### Match a Comment

```kotlin
--8<-- "src/test/kotlin/website/RegexExamples.kt:comment-pattern"
```

## Language-Specific Patterns

### Kotlin

| Target                  | Pattern                        |
|-------------------------|--------------------------------|
| Function declaration    | `\bfun\s+\w+\(`               |
| Class declaration       | `\bclass\s+\w+`               |
| Object declaration      | `\bobject\s+\w+`              |
| Data class              | `\bdata\s+class\s+\w+`        |
| Extension function      | `\bfun\s+\w+\.\w+\(`          |
| Property declaration    | `\bval\s+\w+`  or `\bvar\s+\w+` |
| Companion object        | `companion object`             |
| When expression         | `\bwhen\s*[\({]`               |

### Java

| Target                  | Pattern                         |
|-------------------------|---------------------------------|
| Method declaration      | `(public\|private\|protected).*\w+\s*\(` |
| Class declaration       | `\bclass\s+\w+`                |
| Interface               | `\binterface\s+\w+`            |
| Annotation              | `@\w+`                          |
| Import                  | `^import\s+`                    |

### Python

| Target                  | Pattern                        |
|-------------------------|--------------------------------|
| Function definition     | `\bdef\s+\w+\(`                |
| Class definition        | `\bclass\s+\w+`               |
| Decorator               | `^@\w+`                        |
| Import                  | `^(import\|from)\s+`           |

### Go

| Target                  | Pattern                        |
|-------------------------|--------------------------------|
| Function declaration    | `\bfunc\s+\w+\(`               |
| Method declaration      | `\bfunc\s+\(\w+`               |
| Struct definition       | `\btype\s+\w+\s+struct`        |
| Interface               | `\btype\s+\w+\s+interface`     |

### TypeScript/JavaScript

| Target                  | Pattern                        |
|-------------------------|--------------------------------|
| Function declaration    | `\bfunction\s+\w+\(`           |
| Arrow function          | `\bconst\s+\w+\s*=.*=>`       |
| Class declaration       | `\bclass\s+\w+`               |
| Export                  | `^export\s+`                   |
| Interface               | `\binterface\s+\w+`           |
