package com.pambrose.srcref

enum class Endpoints {
  EDIT,
  GITHUB,
  ERROR,
  WHAT,
  CACHE,
  VERSION,
  PING,
  THREADDUMP,
  ;

  val path = name.lowercase()

  override fun toString() = path
}
