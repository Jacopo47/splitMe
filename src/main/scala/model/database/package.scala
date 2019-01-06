package model

import redis.RedisClient
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}

package object database {
  implicit class RedisClientCloser(db: RedisClient) {
    def closeConnection(): Unit = db.quit().onComplete {
      case Success(res) => if (res) {
        println("Redis connection closed successfully!")
        db.stop()
      } else {
        println("Error on Redis connection closing!")
      }
      case Failure(cause) => println(s"Unexpected error on Redis connection closing. \nDetails: ${cause.getMessage}!")
    }
  }
}
