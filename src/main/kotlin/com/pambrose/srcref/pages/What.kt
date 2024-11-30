package com.pambrose.srcref.pages

import com.github.pambrose.common.response.respondWith
import com.pambrose.srcref.Endpoints.EDIT
import com.pambrose.srcref.pages.Common.commonHead
import com.pambrose.srcref.pages.Common.githubIcon
import io.ktor.server.routing.RoutingContext
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.dom.document
import kotlinx.html.dom.serialize
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.p
import kotlinx.html.pre
import kotlinx.html.span
import kotlinx.html.title

object What {
  internal suspend fun RoutingContext.displayWhat() {
    respondWith {
      document {
        append.html {
          head {
            commonHead()
            title { +"What is srcref?" }
          }
          body {
            githubIcon()
            val url =
              "https://github.com/pambrose/srcref/blob/master/src/main/kotlin/com/pambrose/srcref/Main.kt#L23-L39"
            div("page-indent") {
              id = "whatisthis"
              h2 { +"What is srcref?" }
              p {
                span {
                  +"Line-specific GitHub permalinks look like this: "
                }
                pre { +url }
              }
              p {
                span {
                  +"They are typically embedded in documentation, and the resulting page looks like "
                  a {
                    href = url
                    target = "_blank"
                    +"this"
                  }
                  +"."
                }
              }
              p {
                +"""
                   The problem is: what happens when the underlying content changes?
                   All relevant URLs referencing specific lines will need to be checked, and sometimes
                   adjusted. This is obviously problematic and discourages people from embedding line-specific
                   GitHub permalinks in their docs.
                """.trimIndent()
              }
              p {
                +"""
                   srcref attempts to help this problem by using regular expressions to define the
                   beginning and end of the range of lines to be highlighted. In addition,
                   you can specify the occurrence of each match, an offset, above or below a final match,
                   and whether to search top-down or bottom-up, to define the desired range.
                """.trimIndent()
              }
              p {
                +"""
                   When a srcref URL is clicked, the srcref.com server dynamically computes a GitHub permalink
                   with the appropriate line numbers based on the current content of the file.
                """.trimIndent()
              }
              p {
                val edit =
                  "https://www.srcref.com/github?account=pambrose&repo=srcref&branch=master&" +
                    "path=src%2Fmain%2Fkotlin%2Fcom%2Fpambrose%2Fsrcref%2FMain.kt&" +
                    "bregex=install%5C%28CallLogging%5C%29&boccur=1&boffset=0&" +
                    "btopd=true&eregex=install%5C%28Compression%5C%29&eoccur=1&eoffset=6&etopd=true&edit=true"
                val pattern = "https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html"
                +"For example, these "
                a {
                  href = edit
                  target = "_blank"
                  +"values"
                }
                +" produce a srcref URL that, when clicked, will highlight the lines between the first occurrence of"
                b { +""" "install\(CallLogging\)" """ }
                +"and 6 lines beyond the first occurrence of"
                b { +""" "install\(Compression\)" """ }
                +"""in the specified file. Notice that the "()" characters are escaped because we want their literal
                  value, not their regex interpretation.
                """.trimIndent()
                +"The regex values use "
                a {
                  href = pattern
                  target = "_blank"
                  +"this syntax"
                }
                +""". As mentioned, characters like "()", "[]" and "{}" may require escaping. """
                +"Use "
                a {
                  href = "https://regex101.com"
                  target = "_blank"
                  +"regex101.com"
                }
                +" to assist in creating regex values."
              }
              p("backlink") {
                a {
                  href = "/$EDIT"
                  +"⬅ Back"
                }
              }
            }
          }
        }
      }.serialize()
    }
  }
}
