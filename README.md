# srcref - Dynamic Line-Specific GitHub Permalinks

[![](https://jitpack.io/v/pambrose/srcref.svg)](https://jitpack.io/#pambrose/srcref)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/pambrose/srcref)
[![Kotlin version](https://img.shields.io/badge/kotlin-1.8.20-red?logo=kotlin)](http://kotlinlang.org)

It is problematic to embed line-specific GitHub permalinks in documentation because
changes to the target file can invalidate the permalink line references.

_srcref_ is a simple utility that allows you to embed GitHub permalinks into your
docs without worrying about the line numbers of the code changing.

A public instance of _srcref_ is available at [www.srcref.com](https://www.srcref.com),
but you can run your own server as well.

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
   implementation 'com.github.pambrose:srcref:1.0.24'
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
   <version>1.0.24</version>
</dependency>
```

</details>

## Deploying a _srcref_ server

Environment variables available with a _srcref_ server:

| Env Var          | Default                  | Description                                                 |
|------------------|--------------------------|-------------------------------------------------------------|
| _PORT_           | 8080                     | HTTP port to listen on                                      |
| _PREFIX_         | "https://www.srcref.com" | Prefix for URLs                                             |
| _MAX_LENGTH_     | 5MB                      | Maximum allowed file size                                   |
| _MAX_CACHE_SIZE_ | 2048                     | Maximum cache size before evictions                         |
| _DEFAULT_BRANCH_ | "master"                 | Default branch name to use if _branch_ parameter is missing |

