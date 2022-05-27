package com.pambrose.srcref

internal enum class QueryArgs(private val paramName: String, private val defaultValue: String = "") {
  ACCOUNT("account"),
  REPO("repo"),
  BRANCH("branch", "master"),
  PATH("path"),
  BEGIN_REGEX("bregex"),
  BEGIN_OCCURRENCE("boccur", "1"),
  BEGIN_OFFSET("boffset", "0"),
  BEGIN_TOPDOWN("btopd", "true"),
  END_REGEX("eregex"),
  END_OCCURRENCE("eoccur", "1"),
  END_OFFSET("eoffset", "0"),
  END_TOPDOWN("etopd", "true");

  val arg get() = paramName.lowercase()

  fun defaultIfNull(params: Map<String, String?>) = params[arg] ?: defaultValue

  fun defaultIfBlank(params: Map<String, String?>) = (params[arg] ?: "").let { it.ifBlank { defaultValue } }

  fun required(params: Map<String, String?>) =
    (params[arg] ?: "").let { it.ifBlank { throw IllegalArgumentException("Missing: $arg") } }
}