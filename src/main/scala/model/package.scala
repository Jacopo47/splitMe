import org.json4s.DefaultFormats
import redis.clients.jedis.Jedis

package object model {

  implicit val formats: DefaultFormats.type = DefaultFormats

  implicit class JedisCloser(db: Jedis) {
    def closeConnection(): Unit = try{
      println(s"Redis blocking client closing connection result: ${db.quit()}")
      db.close()
    } catch {
      case cause: Exception => println(s"Error on closing a Redis blocking connection. \n Details: ${cause.getMessage}")
    }
  }
}
