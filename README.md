# srcref - Dynamic Line-Specific GitHub Permalinks

It is problematic to embed line-specific GitHub permalinks in documentation because
changes to the target file can invalidate the permalink line references.

_srcref_ is a simple utility that allows you to embed GitHub permalinks into your
docs without worrying about the line numbers of the code changing.

A public instance of _srcref_ is available at [www.srcref.com](https://www.srcref.com),
but you can run your own instance as well.

## Usage

1) Enter the required information in the form.
   For the `Regex` value, use [regex101.com](https://regex101.com)
   to create a pattern that matches the line of code to which you want to link.
   Remember to protect regex characters like `()`, `[]` and `{}` by prefixing them with a `\`.
2) Click the "Generate URL" button to generate the _srcref_ URL.
3) Click the "View Permalink" button to see the line-specific GitHub permalink.
4) Click the "Copy URL" button to copy the _srcref_ URL to your clipboard.

An example of a _srcref_ reference that leads to the first occurrence
of _embeddedServer_ in `src/main/kotlin/Main.kt` in this repo can be see
[here](https://www.srcref.com?account=pambrose&repo=srcref&branch=master&path=src%2Fmain%2Fkotlin%2FMain.kt&regex=embeddedServer&occurrence=1&offset=0&topdown=true)

### Query Parameters

| Parameter      | Default | Description                                            |
|----------------|---------|--------------------------------------------------------|
| **account**    |         | GitHub account or organization name                    |
| **repo**       |         | Repo name                                              |
| **branch**     | master  | Branch name                                            |
| **path**       |         | Path of the file                                       |
| **regex**      |         | Regex used in the `contains()` evaluated for each line |
| **occurrence** | 1       | The number of matches                                  |
| **offset**     | 0       | The number of lines above or below the final match     |
| **topdown**    | true    | The direction to evaluate the file                     |

### Misc

* The regex parameter uses [this syntax](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).
* Add `&edit` to an existing _srcref_ URL to edit it.
* If you deploy your own version of _srcref_, use the `PREFIX` environment variable to specify URL prefix and
  the `PORT` environment variable to specify the HTTP port.

