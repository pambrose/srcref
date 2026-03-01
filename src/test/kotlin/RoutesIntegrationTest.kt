import com.pambrose.srcref.module
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication

class RoutesIntegrationTest :
  StringSpec(
    {
      "GET / redirects to /edit" {
        testApplication {
          application { module() }
          val noRedirectClient =
            createClient {
              followRedirects = false
            }
          noRedirectClient.get("/").apply {
            status shouldBe HttpStatusCode.Found
          }
        }
      }

      "GET /ping returns pong" {
        testApplication {
          application { module() }
          client.get("/ping").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText() shouldBe "pong"
          }
        }
      }

      "GET /edit returns form page" {
        testApplication {
          application { module() }
          client.get("/edit").apply {
            status shouldBe HttpStatusCode.OK
            val body = bodyAsText()
            body shouldContain "Username/Org Name:"
            body shouldContain "Example Values"
          }
        }
      }

      "GET /edit with partial params shows exception" {
        testApplication {
          application { module() }
          client.get("/edit?account=testuser").apply {
            status shouldBe HttpStatusCode.OK
            val body = bodyAsText()
            body shouldContain "Exception:"
            body shouldContain "Missing: repo value"
          }
        }
      }

      "GET /version returns version page" {
        testApplication {
          application { module() }
          client.get("/version").apply {
            status shouldBe HttpStatusCode.OK
            val body = bodyAsText()
            body shouldContain "Version"
            body shouldContain "Release Date"
          }
        }
      }

      "GET /what returns documentation page" {
        testApplication {
          application { module() }
          client.get("/what").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText() shouldContain "What is srcref?"
          }
        }
      }

      "GET /cache returns cache page" {
        testApplication {
          application { module() }
          client.get("/cache").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText() shouldContain "Cache Size"
          }
        }
      }

      "GET /problem with msg displays error message" {
        testApplication {
          application { module() }
          client.get("/problem?msg=Test+Error+Message").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText() shouldContain "Test Error Message"
          }
        }
      }

      "GET /problem without msg shows missing message" {
        testApplication {
          application { module() }
          client.get("/problem").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText() shouldContain "Missing message value"
          }
        }
      }

      "GET /github?edit shows edit form" {
        testApplication {
          application { module() }
          client.get("/github?edit").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText() shouldContain "Username/Org Name:"
          }
        }
      }

      "GET /github without params redirects" {
        testApplication {
          application { module() }
          val noRedirectClient =
            createClient {
              followRedirects = false
            }
          noRedirectClient.get("/github").apply {
            status shouldBe HttpStatusCode.Found
          }
        }
      }

      "GET /robots.txt returns robot rules with correct paths" {
        testApplication {
          application { module() }
          client.get("/robots.txt").apply {
            status shouldBe HttpStatusCode.OK
            val body = bodyAsText()
            body shouldContain "User-agent"
            body shouldContain "Disallow: /problem"
            body shouldContain "Disallow: /cache"
            body shouldContain "Disallow: /threaddump"
            body shouldNotContain "/error"
          }
        }
      }

      "GET /threaddump returns content" {
        testApplication {
          application { module() }
          client.get("/threaddump").apply {
            status shouldBe HttpStatusCode.OK
          }
        }
      }

      "GET /nonexistent returns 404" {
        testApplication {
          application { module() }
          client.get("/nonexistent").apply {
            status shouldBe HttpStatusCode.NotFound
            bodyAsText() shouldContain "Page not found"
          }
        }
      }
    },
  )
