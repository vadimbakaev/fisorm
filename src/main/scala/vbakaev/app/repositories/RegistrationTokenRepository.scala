package vbakaev.app.repositories

import java.time.Clock
import java.util.UUID

import com.mongodb.ConnectionString
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.connection.ClusterSettings
import org.mongodb.scala.model.{Filters, IndexOptions, Indexes}
import org.mongodb.scala.{MongoClient, MongoClientSettings, MongoCollection, MongoDatabase}
import vbakaev.app.config.MongoDBConfiguration
import vbakaev.app.models.domain.auth.RegistrationToken

import scala.concurrent.{ExecutionContext, Future}

class RegistrationTokenRepository(
    config: MongoDBConfiguration
)(implicit ec: ExecutionContext, clock: Clock)
    extends SafeRepository[RegistrationToken] {

  private lazy val clusterSettings = ClusterSettings
    .builder()
    .applyConnectionString(new ConnectionString(config.uri))
    .build()

  private lazy val clientSettings: MongoClientSettings = MongoClientSettings
    .builder()
    .applyToClusterSettings((b: ClusterSettings.Builder) => {
      b.applySettings(clusterSettings)
      ()
    })
    .build()

  private lazy val database: MongoDatabase = MongoClient(clientSettings)
    .getDatabase(config.database)
    .withCodecRegistry(
      fromRegistries(
        fromProviders(classOf[RegistrationToken]),
        DEFAULT_CODEC_REGISTRY
      )
    )

  private lazy val collectionF: Future[MongoCollection[RegistrationToken]] = initCollection(database)

  private def initCollection(database: MongoDatabase): Future[MongoCollection[RegistrationToken]] = {
    val collection = database.getCollection[RegistrationToken](classOf[RegistrationToken].getSimpleName.toLowerCase)
    collection
      .createIndex(Indexes.ascending("email"), IndexOptions().background(false).unique(true))
      .toFuture()
      .map(_ => collection)
  }

  override def read(email: String): Future[Option[RegistrationToken]] = collectionF.flatMap { collection =>
    collection.find[RegistrationToken](Filters.eq("email", email)).toFuture().map(_.headOption)
  }

  override def create(item: RegistrationToken): Future[RegistrationToken] = collectionF.flatMap { collection =>
    collection.insertOne(item).toFuture().map(_ => item)
  }

  override def newItem(email: String): RegistrationToken =
    RegistrationToken(
      token = UUID.randomUUID().toString,
      email = email,
      confirmedAt = None,
      createdAt = clock.instant()
    )
}
