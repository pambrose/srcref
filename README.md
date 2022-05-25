# srcref - Dynamic Line-Specific GitHub Permalinks

[![](https://jitpack.io/v/pambrose/srcref.svg)](https://jitpack.io/#pambrose/srcref)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/pambrose/srcref)
[![Kotlin version](https://img.shields.io/badge/kotlin-1.6.21-red?logo=kotlin)](http://kotlinlang.org)

It is problematic to embed line-specific GitHub permalinks in documentation because
changes to the target file can invalidate the permalink line references.

_srcref_ is a simple utility that allows you to embed GitHub permalinks into your
docs without worrying about the line numbers of the code changing.

A public instance of _srcref_ is available at [www.srcref.com](https://www.srcref.com),
but you can run your own instance as well.

## Usage

1) Enter the _srcref_ information in the form [here](https://www.srcref.com).
   The `Regex` values use [this syntax](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).
   Remember to protect regex characters like `()`, `[]` and `{}` by prefixing them with a `\`.
   Use [regex101.com](https://regex101.com) to assist creating regex values.
2) If the _End_ _Regex_ field is empty, the _srcref_ URL will highlight a single line.
3) Click the "Generate URL" button to generate the _srcref_ URL.
4) Click the "View GitHub Permalink" button to verify the line-specific GitHub permalink.
5) Click the "Copy URL" button to copy the _srcref_ URL to your clipboard.
6) Paste the _srcref_ URL into your documentation.

### Example

In order to highlight all the lines from the first occurrence
of `install\(CallLogging\)` to 3 lines beyond the first occurrence of `install\(Compression\)` in
`src/main/kotlin/Main.kt`, create a _srcref_ URL using
[these values](https://www.srcref.com/edit?account=pambrose&repo=srcref&branch=master&path=%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fpambrose%2Fsrcref%2FMain.kt&bregex=install%5C%28CallLogging%5C%29&boccur=1&boffset=0&btopd=true&eregex=install%5C%28Compression%5C%29&eoccur=1&eoffset=3&etopd=false)
.

The resulting _srcref_ link generates this
[GitHub permalink](https://www.srcref.com/github?account=pambrose&repo=srcref&branch=master&path=%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fpambrose%2Fsrcref%2FMain.kt&bregex=install%5C%28CallLogging%5C%29&boccur=1&boffset=0&btopd=true&eregex=install%5C%28Compression%5C%29&eoccur=1&eoffset=3&etopd=false)
.

### Query Parameters

| Parameter   | Default | Description                                                                        |
|-------------|---------|------------------------------------------------------------------------------------|
| **account** |         | GitHub account or organization name                                                |
| **repo**    |         | Repo name                                                                          |
| **branch**  | master  | Branch name                                                                        |
| **path**    |         | Path of the file                                                                   |
| **bregex**  |         | The regex used in the `contains()` evaluated for each line for the beginning match |
| **boccur**  | 1       | The number of matches for the beginning match                                      |
| **boffset** | 0       | The number of lines above or below the beginning match                             |
| **btopd**   | true    | The direction to evaluate the file for the beginning match                         |
| **eregex**  |         | The regex used in the `contains()` evaluated for each line for the ending match    |
| **eoccur**  | 1       | The number of matches for the ending match                                         |
| **eoffset** | 0       | The number of lines above or below the ending match                                |
| **etopd**   | true    | The direction to evaluate the file for the ending match                            |

## Programmatic Usage

You can generate _srcref_ URLs programmatically with the `srcrefUrl()` call
([example](https://www.srcref.com/github?account=kslides&repo=kslides&branch=master&path=kslides-core%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fkslides%2FPresentation.kt&bregex=srcrefUrl%5C%28&boccur=1&boffset=0&btopd=true&eregex=escapeHtml4+%3D+true&eoccur=1&eoffset=1&etopd=true))
.

Add this to your gradle dependencies:

```
implementation "com.github.pambrose:srcref:1.0.2"
```

## Misc

* Add `&edit` to a _srcref_ URL to edit it.
* If you deploy your own version of _srcref_, use the `PREFIX` environment variable to specify URL prefix and
  the `PORT` environment variable to specify the HTTP port.

