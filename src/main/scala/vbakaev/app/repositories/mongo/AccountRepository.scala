package vbakaev.app.repositories.mongo

import com.mongodb.ConnectionString
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.connection.ClusterSettings
import org.mongodb.scala.model.{Filters, IndexOptions, Indexes}
import org.mongodb.scala.{MongoClient, MongoClientSettings, MongoCollection, MongoDatabase}
import vbakaev.app.config.MongoDBConfig
import vbakaev.app.models.domain.Account
import vbakaev.app.repositories.{CreateRepository, ReadRepository, UpdateRepository}

import scala.concurrent.{ExecutionContext, Future}

class AccountRepository(
    config: MongoDBConfig
)(implicit ec: ExecutionContext)
    extends CreateRepository[Account]
    with ReadRepository[Account]
    with UpdateRepository[Account] {

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
        fromProviders(classOf[Account]),
        DEFAULT_CODEC_REGISTRY
      )
    )

  private lazy val collectionF: Future[MongoCollection[Account]] = initCollection(database)

  private def initCollection(database: MongoDatabase): Future[MongoCollection[Account]] = {
    val collection = database.getCollection[Account](classOf[Account].getSimpleName.toLowerCase)
    collection
      .createIndex(Indexes.ascending("email"), IndexOptions().background(false).unique(true))
      .toFuture()
      .map(_ => collection)
  }

  override def create(item: Account): Future[Account] = collectionF.flatMap { collection =>
    collection.insertOne(item).toFuture().map(_ => item)
  }

  override def read(email: String): Future[Option[Account]] = collectionF.flatMap { collection =>
    collection.find[Account](Filters.eq("email", email)).toFuture().map(_.headOption)
  }

  override def update(item: Account): Future[Option[Account]] = collectionF.flatMap { collection =>
    collection.findOneAndReplace(Filters.eq("email", item.email), item).toFutureOption()
  }
}
