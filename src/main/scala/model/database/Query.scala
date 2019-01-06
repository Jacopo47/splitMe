package model.database

import model.RedisConnection
import redis.RedisClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Create a space for executing Redis commands.
  * Give to the body a connection to the db and after all operations close the connection.
  *
  * @param body
  * Function
  * @param callback
  * Function that takes in input the body's return value.
  * @param db
  * Database connection.
  */
class Query[T](body: RedisClient => T, callback: T => Unit)(implicit db: RedisClient = RedisConnection().getDatabaseConnection) {
  var result: T = _
  Future {
    result = body(db)
  }.andThen { case _ => callback(result) }
    .andThen { case _ => db.closeConnection() }
}

object Query {
  def apply[T](body: RedisClient => T): Query[T] = {
    new Query[T](body, _ => {})
  }

  def withCallback[T](body: RedisClient => T)(callback: T => Unit): Query[T] = {
    new Query[T](body, callback)
  }
}
