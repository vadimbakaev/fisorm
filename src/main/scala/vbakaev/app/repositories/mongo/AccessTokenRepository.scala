package vbakaev.app.repositories.mongo

import java.time.Clock
import java.util.UUID

import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.{Filters, IndexOptions, Indexes}
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import vbakaev.app.models.domain.auth.AccessToken
import vbakaev.app.repositories.{DeleteRepository, SafeRepository}

import scala.concurrent.{ExecutionContext, Future}

class AccessTokenRepository(
    db: MongoDatabase
)(implicit ec: ExecutionContext, clock: Clock)
    extends SafeRepository[AccessToken]
    with DeleteRepository[AccessToken] {

  private val database: MongoDatabase = db
    .withCodecRegistry(
      fromRegistries(
        fromProviders(classOf[AccessToken]),
        db.codecRegistry
      )
    )

  private val collectionF: Future[MongoCollection[AccessToken]] = initCollection(database)

  private def initCollection(database: MongoDatabase): Future[MongoCollection[AccessToken]] = {
    val collection = database.getCollection[AccessToken](classOf[AccessToken].getSimpleName.toLowerCase)
    collection
      .createIndex(Indexes.ascending("email"), IndexOptions().background(false).unique(true))
      .toFuture()
      .map(_ => collection)
  }

  override def read(email: String): Future[Option[AccessToken]] = collectionF.flatMap { collection =>
    collection.find[AccessToken](Filters.eq("email", email)).toFuture().map(_.headOption)
  }

  override def create(item: AccessToken): Future[AccessToken] = collectionF.flatMap { collection =>
    collection.insertOne(item).toFuture().map(_ => item)
  }

  override def newItem(email: String): AccessToken =
    AccessToken(
      token = UUID.randomUUID(),
      email = email,
      expiredAt = None,
      createdAt = clock.instant()
    )

  override def delete(email: String): Future[Option[AccessToken]] = collectionF.flatMap { collection =>
    collection.findOneAndDelete(Filters.eq("email", email)).toFutureOption()
  }
}
