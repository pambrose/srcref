import Page.displayForm
import Page.githubref
import Utils.githubRefUrl
import Utils.logger
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
        val params = queryParams.onEach { (k, v) -> logger.info { "$k=$v" } }
        displayForm(params)
      }

      get(githubref) {
        val params = queryParams.onEach { (k, v) -> logger.info { "$k=$v" } }
        if (call.request.queryParameters.contains("edit"))
          displayForm(params)
        else
          redirectTo { githubRefUrl(params) }
      }
    }
  }.start(wait = true)
}

val PipelineCall.queryParams
  get() =
    mutableMapOf<String, String?>()
      .also {
        QueryArgs
          .values()
          .map { it.arg }
          .forEach { arg -> it[arg] = call.request.queryParameters[arg] }
      }

typealias PipelineCall = PipelineContext<Unit, ApplicationCall>
