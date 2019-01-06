package controller

import java.util.stream.Collectors

import controller.Dispatcher.{PORT, TIMEOUT}
import io.redisearch.Suggestion
import io.redisearch.client.SuggestionOptions
import io.vertx.core.http.HttpHeaders
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.http.HttpServerOptions
import io.vertx.scala.ext.web.handler.StaticHandler
import io.vertx.scala.ext.web.{Router, RoutingContext}
import model.{Error, GET, Message, POST, RedisConnection, RouterResponse, SearchResult}
import org.json4s.jackson.Serialization.write

import scala.collection.JavaConverters._

object Dispatcher {
  var HOST: String = "localhost"
  val PORT: Int = 4700
  val TIMEOUT = 1000

}

class Dispatcher extends ScalaVerticle {


  override def start(): Unit = {
    val router = Router.router(vertx)


    val options = HttpServerOptions()
    options.setCompressionSupported(true)
      .setIdleTimeout(TIMEOUT)


    router.route("/view*").handler(StaticHandler.create().setWebRoot("view"))

    GET(router, "/", (ctx, _) => ctx.reroute("/view/search.html"))

    GET(router, "/hello", hello)

    GET(router, "/api/search", search)

    POST(router, "/api/insert", insert)

    router.route().handler(ctx => {
      val err = Error(Some(s"Error 404 not found, retry!"))

      ctx.response().setStatusCode(404)
      ctx.response().putHeader(HttpHeaders.CONTENT_TYPE.toString, "application/json; charset=utf-8")
      ctx.response().end(write(err))
    })


    vertx.createHttpServer(options)
      .requestHandler(router.accept _).listen(PORT)

  }


  /**
    * Welcome response.
    */
  private val hello: (RoutingContext, RouterResponse) => Unit = (_, res) => {
    res.sendResponse(Message("Hello to everyone"))
  }

  private val insert: (RoutingContext, RouterResponse) => Unit = (ctx, res) => {
    try {


      val params = ctx.request().formAttributes()

      val description = params.get("description").getOrElse({
        res.sendResponse(Message("Il campo descrizione è obbligatorio"))
        throw BreakHandler()
      })
      val element = params.get("type").getOrElse({
        res.sendResponse(Message("Il campo tipo è obbligatorio"))
        throw BreakHandler()
      })

      val client = RedisConnection().getSearchConnection

      val suggestion: Suggestion = Suggestion.builder()
        .str(description + " -> " + element.toUpperCase)
        .build()

      if (client.addSuggestion(suggestion, false) > 0) {
        res.sendResponse(Message("Inserimento avvenuto con successo!"))
      } else {
        res.sendResponse(Message("Non è stato possibile completare l'inserimento!"))
      }
    } catch {
      case _: BreakHandler =>
      case ex: Exception => res.sendResponse(Message("Non è stato possibile completare l'inserimento! Causa: " + ex.getMessage))

    }
  }

  /**
    * Welcome response.
    */
  private val search: (RoutingContext, RouterResponse) => Unit = (ctx, res) => {
    val term = ctx.queryParams().get("term")

    term match {
      case Some(t) =>
        val client = RedisConnection().getSearchConnection

        val suggestions = client.getSuggestion(t, SuggestionOptions.builder().build())


        val list = suggestions.stream().iterator().asScala.map(_.getString)

        if(list.isEmpty) {
          res.sendResponse(SearchResult(Seq("Nessun elemento corrisponde alla ricerca..")))
        } else {
          res.sendResponse(SearchResult(list.toSeq))
        }

      case None => res.sendResponse(Message("No input!"))
    }
  }


  case class BreakHandler() extends Exception

}



