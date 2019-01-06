package model


import akka.actor.ActorSystem
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import io.redisearch.client.Client
import model.RedisConnection._
import redis.RedisClient
import redis.clients.jedis.Jedis

case class RedisConnection() {
  val config = ConfigFactory.load()
    .withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("OFF"))
    .withValue("akka.stdout-loglevel", ConfigValueFactory.fromAnyRef("OFF"))
  implicit val system = ActorSystem("AlwaysNameYourSystem", config)

  val redisHost: String = System.getenv("REDIS_HOST")
  val redisPort: String = System.getenv("REDIS_PORT")
  val redisPw: String = System.getenv("REDIS_PW")
  val redisUri: Option[String] = Option(System.getenv("REDIS_URL"))

  /**
    * Return an open database connection
    *
    * @return
    * an open connection to the database
    */
  def getDatabaseConnection: RedisClient = {

    redisUri match {
      case Some(uri) =>
        val splittedUri = uri.replaceFirst("redis://h:", "").split("@")
        val pw = splittedUri(0)
        val host = splittedUri(1).split(":")(0)
        val port = splittedUri(1).split(":")(1)

        RedisClient(host, port.toInt, Some(pw))
      case None =>
        if (redisHost == null || redisPort == null || redisPw == null) {
          RedisClient(REDIS_HOST, REDIS_PORT, REDIS_PW)
        } else {
          RedisClient(redisHost, redisPort.toInt, Some(redisPw))
        }
    }


  }

  def getBlockingConnection: Jedis = {
    if (redisHost == null || redisPort == null || redisPw == null) {
      val db: Jedis = new Jedis(REDIS_HOST, REDIS_PORT)
      if (REDIS_PW.isDefined) {
        db.auth(REDIS_PW.get)
      }

      db
    } else {
      val db: Jedis = new Jedis(redisHost, redisPort.toInt)
      db.auth(redisPw)

      db
    }
  }

}

object RedisConnection {
  private var redisHost: String = "127.0.0.1"
  private var redisPort: Int = 6379
  private var redisPw: Option[String] = None

  def setRedisHost(value: String): Unit = redisHost = value

  def REDIS_HOST: String = redisHost

  def setRedisPort(value: Int): Unit = redisPort = value

  def REDIS_PORT: Int = redisPort

  def setRedisPw(value: String): Unit = redisPw = Some(value)

  def REDIS_PW: Option[String] = redisPw

}
