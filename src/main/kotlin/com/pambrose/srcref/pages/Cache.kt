package com.pambrose.srcref.pages

import com.github.pambrose.common.response.PipelineCall
import com.github.pambrose.common.response.respondWith
import com.pambrose.srcref.ContentCache.Companion.contentCache
import com.pambrose.srcref.Urls.RAW_PREFIX
import com.pambrose.srcref.pages.Common.commonHead
import com.pambrose.srcref.pages.Common.githubIcon
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.document
import kotlinx.html.dom.serialize

object Cache {
  internal suspend fun PipelineCall.displayCache() {
    respondWith {
      document {
        append.html {
          head {
            commonHead()
            title { +"srcref Content Cache" }
          }
          body {
            githubIcon()
            div("page-indent") {
              h2 { +"srcref Content Cache" }
              h3 { +"Cache Size: ${contentCache.size}" }
              if (contentCache.size > 0) {
                table {
                  id = "cachetable"
                  style = "width: 100%; border-collapse: collapse;"
                  tr {
                    th { +"Hits" }
                    th { +"Last Access" }
                    th { +"Age" }
                    th { +"Size" }
                    th { +"Lines" }
                    th {
                      style = "padding-left: 5px; text-align: left;"
                      +"Url"
                    }
                    th {
                      style = "padding-left: 5px; text-align: left;"
                      +"ETag"
                    }
                  }
                  contentCache.sortedByLastReferenced().forEach { (url, v) ->
                    tr {
                      td {
                        style = "text-align: center;"
                        +v.hits.toString()
                      }
                      td {
                        style = "text-align: center;"
                        +v.lastReferenced.toString()
                      }
                      td {
                        style = "text-align: center;"
                        +v.age.toString()
                      }
                      td {
                        style = "text-align: center;"
                        +v.contentLength.toString()
                      }
                      td {
                        style = "text-align: center;"
                        +v.pageLines.size.toString()
                      }
                      td { +url.let { if (url.startsWith(RAW_PREFIX)) it.substring(RAW_PREFIX.length) else it } }
                      td { +v.etag.substring(1..20).let { if (v.etag.length > 20) "$it..." else it } }
                    }
                  }
                }
              }
            }
          }
        }
      }.serialize()
    }
  }
}
