package com.pambrose.srcref

/**
 * Enumeration of all URL query parameters accepted by the srcref service.
 *
 * Each entry defines its query string [arg] name, a [defaultValue], and whether it is [optional].
 * Begin parameters (`bregex`, `boccur`, `boffset`, `btopd`) are required; end parameters
 * (`eregex`, `eoccur`, `eoffset`, `etopd`) are optional and only used when specifying a line range.
 */
internal enum class QueryParams(
  internal val arg: String,
  private val defaultValue: String,
  private val optional: Boolean,
) {
  ACCOUNT("account", "", false),
  REPO("repo", "", false),
  BRANCH("branch", System.getenv("DEFAULT_BRANCH") ?: "master", false),
  PATH("path", "", false),
  BEGIN_REGEX("bregex", "", false),
  BEGIN_OCCURRENCE("boccur", "1", false),
  BEGIN_OFFSET("boffset", "0", false),
  BEGIN_TOPDOWN("btopd", "true", false),
  END_REGEX("eregex", "", true),
  END_OCCURRENCE("eoccur", "1", true),
  END_OFFSET("eoffset", "0", true),
  END_TOPDOWN("etopd", "true", true),
  ;

  /** Returns the parameter value from [params], falling back to [defaultValue] if absent. */
  fun defaultIfNull(params: Map<String, String?>) = params[arg] ?: defaultValue

  /** Returns the parameter value from [params], falling back to [defaultValue] if absent or blank. */
  fun defaultIfBlank(params: Map<String, String?>) = (params[arg] ?: "").let { it.ifBlank { defaultValue } }

  /**
   * Returns the parameter value from [params], throwing [IllegalArgumentException] if absent or blank.
   */
  fun required(params: Map<String, String?>) =
    (params[arg] ?: "").let { it.ifBlank { throw IllegalArgumentException("Missing: $arg value") } }

  companion object {
    val optionalParams = entries.filter { it.optional }.map { it.arg }
  }
}
