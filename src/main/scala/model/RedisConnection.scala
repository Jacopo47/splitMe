package model


import akka.actor.ActorSystem
import io.redisearch.client.Client

import model.RedisConnection._
import redis.RedisClient

case class RedisConnection() {
  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()


  val redisHost: String = System.getenv("REDIS_HOST")
  val redisPort: String = System.getenv("REDIS_PORT")
  val redisPw: String = System.getenv("REDIS_PW")

  /**
    * Return an open database connection
    *
    * @return
    * an open connection to the database
    */
  def getDatabaseConnection: RedisClient = {

    if (redisHost == null || redisPort == null || redisPw == null) {
      RedisClient(REDIS_HOST, REDIS_PORT, REDIS_PW)
    } else {
      RedisClient(redisHost, redisPort.toInt, Some(redisPw))
    }
  }

  def getSearchConnection: Client = {
    new Client("splitMe", "localhost", 6379)
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
