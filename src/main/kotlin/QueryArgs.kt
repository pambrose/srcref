enum class QueryArgs(val defaultValue: String = "") {
  ACCOUNT,
  REPO,
  BRANCH("master"),
  PATH("/src/main/kotlin/"),
  REGEX,
  OFFSET("0"),
  OCCURRENCE("1"),
  TOPDOWN("true");

  val arg get() = name.lowercase()

  fun defaultIfNull(params: Map<String, String?>) = params[arg] ?: defaultValue

  fun defaultIfBlank(params: Map<String, String?>) = (params[arg] ?: "").let { if (it.isBlank()) defaultValue else it }

  fun required(params: Map<String, String?>) =
    (params[arg] ?: "").let { if (it.isBlank()) throw IllegalArgumentException("Missing: $arg") else it }
}