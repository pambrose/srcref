import Page.displayForm
import SrcRef.githubRefUrl
import SrcRef.githubref
import SrcRef.logger
import SrcRef.queryParams
import com.github.pambrose.common.response.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

fun main() {
  embeddedServer(CIO, port = System.getenv("PORT")?.toInt() ?: 8080) {
    install(CallLogging)
    install(DefaultHeaders) { header("X-Engine", "Ktor") }
    install(Compression) {
      gzip { priority = 1.0 }
      deflate { priority = 10.0; minimumSize(1024) /* condition*/ }
    }

    routing {
      get("/") {
        displayForm()
      }

      get(githubref) {
        val params = queryParams.onEach { (k, v) -> logger.info { "$k=$v" } }
        redirectTo { githubRefUrl(params) }
      }
    }
  }.start(wait = true)
}

typealias PipelineCall = PipelineContext<Unit, ApplicationCall>
