# srcref - Dynamic Line-Specific GitHub Permalinks

[![](https://jitpack.io/v/pambrose/srcref.svg)](https://jitpack.io/#pambrose/srcref)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/pambrose/srcref)
[![Kotlin version](https://img.shields.io/badge/kotlin-2.2.0-red?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/github/license/pambrose/srcref)](https://github.com/pambrose/srcref/blob/master/LICENSE.md)

## Overview

It is problematic to embed line-specific GitHub permalinks in documentation because
changes to the target file can invalidate the permalink line references.

**srcref** solves this problem by using regex patterns to dynamically locate code sections,
generating GitHub permalinks that remain valid even when files are modified.

**Key Benefits:**

- 🔗 **Dynamic permalinks** that survive code changes
- 🎯 **Regex-based targeting** for precise code section selection
- ⚡ **Fast performance** with intelligent caching
- 🌐 **Public service** available at [www.srcref.com](https://www.srcref.com)
- 🔧 **Self-hostable** for private repositories and custom deployments
- 📚 **Programmatic API** for automation and tooling integration

## Quick Start

### Using the Web Interface

1. Visit [www.srcref.com](https://www.srcref.com)
2. Fill in your GitHub repository details and regex patterns
3. Generate and copy your dynamic permalink
4. Use the permalink in your documentation

### Using the API

```kotlin
val permalink = Api.srcrefUrl(
    account = "pambrose",
    repo = "srcref",
    path = "src/main/kotlin/Main.kt",
    beginRegex = "fun main",
    endRegex = "embeddedServer"
)
```

## Usage

1) Enter the _srcref_ information in the form [here](https://www.srcref.com).
   The `regex` values use [this syntax](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).
   Remember to protect regex characters like `()`, `[]` and `{}` by prefixing them with a `\`.
   Use [regex101.com](https://regex101.com) to assist in creating regex values.
2) If the _End_ _Regex_ field is empty, the _srcref_ URL will highlight a single line on GitHub.
3) Click the "Generate URL" button to generate the _srcref_ URL.
4) Click the "View GitHub Permalink" button to verify the line-specific GitHub permalink.
5) Click the "Copy URL" button to copy the _srcref_ URL to your clipboard.
6) Paste the _srcref_ URL into your documentation.

## Example

To highlight all the lines from the first occurrence
of `install(CallLogging)` to 3 lines beyond the first occurrence of `install(Compression)` in
[src/main/kotlin/Main.kt](https://github.com/pambrose/srcref/blob/master/src/main/kotlin/com/pambrose/srcref/Main.kt),
create a _srcref_ URL using
[these values](https://www.srcref.com/edit?account=pambrose&repo=srcref&branch=master&path=src%2Fmain%2Fkotlin%2Fcom%2Fpambrose%2Fsrcref%2FMain.kt&bregex=install%5C%28CallLogging%5C%29&boccur=1&boffset=0&btopd=true&eregex=install%5C%28Compression%5C%29&eoccur=1&eoffset=3&etopd=false)
.

The corresponding _srcref_ URL generates this
[GitHub permalink](https://www.srcref.com/github?account=pambrose&repo=srcref&branch=master&path=src%2Fmain%2Fkotlin%2Fcom%2Fpambrose%2Fsrcref%2FMain.kt&bregex=install%5C%28CallLogging%5C%29&boccur=1&boffset=0&btopd=true&eregex=install%5C%28Compression%5C%29&eoccur=1&eoffset=3&etopd=false)
.

## Query Parameters

| Parameter | Default  | Required | Description                                                |
|-----------|----------|----------|------------------------------------------------------------|
| _account_ |          | Yes      | GitHub account or organization name                        |
| _repo_    |          | Yes      | Repo name                                                  |
| _branch_  | "master" | Yes      | Branch name                                                |
| _path_    |          | Yes      | File path in repo                                          |
| _bregex_  |          | Yes      | The regex used to determine the beginning match            |
| _boccur_  | 1        | Yes      | The number of matches for the beginning match              |
| _boffset_ | 0        | Yes      | The number of lines above or below the beginning match     |
| _btopd_   | true     | Yes      | The direction to evaluate the file for the beginning match |
| _eregex_  |          | No       | The regex used to determine the ending match               |
| _eoccur_  | 1        | No       | The number of matches for the ending match                 |
| _eoffset_ | 0        | No       | The number of lines above or below the ending match        |
| _etopd_   | true     | No       | The direction to evaluate the file for the ending match    |

## Editing a _srcref_ URL

Add `&edit` to a _srcref_ URL to edit it.

## Programmatic Usage

_srcref_ URLs can be generated programmatically with the `srcrefUrl()` call. An example can be seen
[here](https://www.srcref.com/github?account=kslides&repo=kslides&branch=master&path=kslides-core%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fkslides%2FPresentation.kt&bregex=srcrefUrl%5C%28&boccur=1&boffset=0&btopd=true&eregex=escapeHtml4+%3D+true&eoccur=1&eoffset=1&etopd=true)
.

<details open>
<summary>Gradle:</summary>

```groovy
allprojects {
   repositories {
      maven { url 'https://jitpack.io' }
   }
}
```

```groovy
dependencies {
  implementation 'com.github.pambrose:srcref:2.0.0'
}
```

</details>


<details>
<summary>Maven:</summary>

```xml

<repositories>
   <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
   </repository>
</repositories>
```

```xml

<dependency>
   <groupId>com.github.pambrose</groupId>
   <artifactId>srcref</artifactId>
  <version>2.0.0</version>
</dependency>
```

</details>

## How It Works

srcref uses regex patterns to dynamically locate code sections in GitHub repositories:

1. **Pattern Matching**: Your regex patterns are applied to the target file
2. **Line Calculation**: The service finds the matching lines and applies any offsets
3. **Permalink Generation**: A standard GitHub permalink is generated using the calculated line numbers
4. **Intelligent Caching**: Files are cached with ETags to minimize GitHub API calls

## Common Use Cases

### Documentation

Reference specific code sections in:

- README files
- Technical documentation
- API documentation
- Tutorial guides

### Code Reviews

- Link to specific problematic code sections
- Reference implementations during discussions
- Share code examples that stay current

### Issue Tracking

- Link to relevant code when reporting bugs
- Reference specific lines in feature requests
- Maintain valid links in long-lived issues

## Regex Examples

### Basic Patterns

```
// Match function declarations
fun\s+\w+\(

// Match class definitions
class\s+\w+

// Match import statements
import\s+[\w.]+

// Match specific method calls
\.methodName\(
```

### Advanced Patterns

```
// Match multi-line constructs
try\s*\{     // Begin regex
}\s*catch   // End regex

// Match configuration blocks
install\([\w]+\)     // Begin regex
}                    // End regex (with offset)

// Match between comments
//\s*START.*          // Begin regex
//\s*END.*            // End regex
```

## Deployment Options

### Option 1: Local Development

```bash
# Clone the repository
git clone https://github.com/pambrose/srcref.git
cd srcref

# Run locally
./gradlew build
java -jar build/libs/srcref.jar
```

### Option 2: Docker Deployment

```bash
# Build Docker image
docker build -t srcref .

# Run with environment variables
docker run -p 8080:8080 \
  -e PORT=8080 \
  -e PREFIX=https://your-domain.com \
  srcref
```

### Option 3: Heroku Deployment

```bash
# Deploy to Heroku
heroku create your-srcref-app
git push heroku master
```

### Environment Variables

| Variable         | Default                  | Description                                               |
|------------------|--------------------------|-----------------------------------------------------------|
| `PORT`           | 8080                     | HTTP port to listen on                                    |
| `PREFIX`         | "https://www.srcref.com" | Prefix for generated URLs                                 |
| `MAX_LENGTH`     | 5MB                      | Maximum allowed file size                                 |
| `MAX_CACHE_SIZE` | 2048                     | Maximum cache size before evictions                       |
| `DEFAULT_BRANCH` | "master"                 | Default branch name to use if branch parameter is missing |

## API Reference

### `srcrefUrl()` Function

```kotlin
fun srcrefUrl(
  account: String,              // GitHub username or organization
  repo: String,                 // Repository name
  path: String,                 // File path in repository
  beginRegex: String,           // Regex to find start position
  beginOccurrence: Int = 1,     // Which occurrence to use (1st, 2nd, etc.)
  beginOffset: Int = 0,         // Lines to offset from match
  beginTopDown: Boolean = true, // Search direction
  endRegex: String = "",        // Optional: regex to find end position
  endOccurrence: Int = 1,       // Which end occurrence to use
  endOffset: Int = 0,           // Lines to offset from end match
  endTopDown: Boolean = true,   // End search direction
  prefix: String = "https://www.srcref.com", // Base URL
  branch: String = "master",    // Git branch
  escapeHtml4: Boolean = false  // HTML escape the output
): String
```

### URL Endpoints

| Endpoint       | Purpose                       | Parameters                    |
|----------------|-------------------------------|-------------------------------|
| `/edit`        | Main form interface           | All query parameters          |
| `/github`      | Generate GitHub permalink     | All query parameters          |
| `/github?edit` | Edit existing srcref URL      | All query parameters + `edit` |
| `/problem`     | Display error information     | `msg` + original parameters   |
| `/what`        | Information about srcref      | None                          |
| `/cache`       | Cache status and statistics   | None                          |
| `/version`     | Version and build information | None                          |
| `/ping`        | Health check endpoint         | None                          |

## Error Handling

srcref provides detailed error messages for common issues:

- **Invalid regex patterns**: Syntax errors with suggestions
- **Pattern not found**: Clear indication when regex doesn't match
- **File too large**: Size limit exceeded warnings
- **Network errors**: GitHub API connectivity issues
- **Invalid parameters**: Missing or malformed inputs

## Performance & Caching

- **HTTP Caching**: Uses ETags to avoid unnecessary GitHub API calls
- **Memory Caching**: Intelligent LRU cache with configurable size limits
- **Request Optimization**: Batched operations and connection reuse
- **Background Cleanup**: Automatic cache maintenance

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Setup

```bash
# Clone repository
git clone https://github.com/pambrose/srcref.git
cd srcref

# Build project
./gradlew build

# Run tests
./gradlew test

# Start development server
./gradlew run
```

### Building

```bash
# Create executable JAR
make uberjar

# Build Docker image
make build-docker

# Run all checks
make tests
```

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## Acknowledgments

- Built with [Ktor](https://ktor.io/) for HTTP server functionality
- Uses [kotlinx.html](https://github.com/Kotlin/kotlinx.html) for type-safe HTML generation

