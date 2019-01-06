import controller.Dispatcher
import io.vertx.scala.core.Vertx

object Main extends App {
  println("SplitMe!")

  Vertx.vertx().deployVerticle(new Dispatcher())
}