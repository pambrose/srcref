# srcref - Dynamic GitHub Permalinks

It is sometimes problematic to embed GitHub permalinks (references to specific lines of code)
in docs because the permalinks are not updated when the referenced code is changed.

_srcref_ is a simple utility that allows you to embed GitHub permalinks into your
docs without worrying about the line numbers of the code changing.

A public instance of _srcref_ is available at [www.srcref.com](https://www.srcref.com),
but you can run your own instance as well.

## Usage

1) Enter the required information in the form.
   For the `Regex` value, use [regex101.com](https://regex101.com)
   to create a pattern that matches the line of code to which you want to link.
2) Click the "Generate URL" button.
3) Click the  "View Permalink" button to see the GitHub permalink.
4) Click the "Copy URL" button to copy the _srcref_ URL to your clipboard.

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

