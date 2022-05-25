package com.pambrose.srcref

enum class QueryArgs(val pname: String, val defaultValue: String = "") {
  ACCOUNT("account"),
  REPO("repo"),
  BRANCH("branch", "master"),
  PATH("path", "/src/main/kotlin/"),
  BEGIN_REGEX("bregex", ""),
  BEGIN_OCCURRENCE("boccur", "1"),
  BEGIN_OFFSET("boffset", "0"),
  BEGIN_TOPDOWN("btopd", "true"),
  END_REGEX("eregex", ""),
  END_OCCURRENCE("eoccur", "1"),
  END_OFFSET("eoffset", "0"),
  END_TOPDOWN("etopd", "false");

  val arg get() = pname.lowercase()

  fun defaultIfNull(params: Map<String, String?>) = params[arg] ?: defaultValue

  fun defaultIfBlank(params: Map<String, String?>) = (params[arg] ?: "").let { if (it.isBlank()) defaultValue else it }

  fun required(params: Map<String, String?>) =
    (params[arg] ?: "").let { if (it.isBlank()) throw IllegalArgumentException("Missing: $arg") else it }
}