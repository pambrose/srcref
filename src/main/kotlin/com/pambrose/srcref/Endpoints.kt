package com.pambrose.srcref

enum class Endpoints {
  EDIT, GITHUB, ERROR, CACHE, VERSION, PING;

  val path = name.lowercase()

  override fun toString() = path
}