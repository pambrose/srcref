package com.pambrose.srcref

enum class Endpoints {
  EDIT,
  GITHUB,
  ERROR,
  WHAT,
  CACHE,
  VERSION,
  PING,
  ;

  val path = name.lowercase()

  override fun toString() = path
}
