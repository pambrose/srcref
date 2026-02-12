import com.pambrose.srcref.Endpoints
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EndpointsTest :
  StringSpec(
    {
      "Endpoints has 8 entries" {
        Endpoints.entries.size shouldBe 8
      }

      "path returns lowercase name for all entries" {
        Endpoints.entries.forEach { endpoint ->
          endpoint.path shouldBe endpoint.name.lowercase()
        }
      }

      "toString returns path for all entries" {
        Endpoints.entries.forEach { endpoint ->
          endpoint.toString() shouldBe endpoint.path
        }
      }
    },
  )
