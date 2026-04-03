package com.pambrose.srcref

/**
 * Enumeration of all HTTP endpoints served by srcref.
 *
 * Each entry's [path] is the lowercase version of its name, used as the URL path segment.
 */
enum class Endpoints {
  EDIT,
  GITHUB,
  PROBLEM,
  WHAT,
  CACHE,
  VERSION,
  PING,
  THREADDUMP,
  ;

  val path = name.lowercase()

  override fun toString() = path
}
