package vbakaev.app

import java.time.Clock

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import com.typesafe.scalalogging.LazyLogging
import org.bson.UuidRepresentation
import org.bson.codecs.UuidCodec
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.{MongoClient, MongoDatabase}
import pureconfig.ConfigSource
import vbakaev.app.config.AppConfig
import vbakaev.app.interfaces.{AuthInterface, ErrorHandler, Interface}
import vbakaev.app.repositories.mongo.{AccessTokenRepository, AccountRepository}
import vbakaev.app.services.mail.{MailGenerationServiceImpl, MailServiceImpl}
import vbakaev.app.services.{AuthServiceImpl, JwtService}
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContextExecutor

object Main extends App with LazyLogging {
  implicit val clock: Clock                               = Clock.systemUTC()
  implicit val system: ActorSystem                        = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  ConfigSource.default
    .load[AppConfig]
    .fold(
      error => logger.error(s"Configuration loading error $error"),
      config => {
        val database: MongoDatabase = MongoClient(config.mongo.uri)
          .getDatabase(config.mongo.database)
          .withCodecRegistry(
            fromRegistries(
              CodecRegistries.fromCodecs(new UuidCodec(UuidRepresentation.STANDARD)),
              DEFAULT_CODEC_REGISTRY
            )
          )
        val mailGenerator               = new MailGenerationServiceImpl(config.mail.host, config.mail.sender)
        val mailService                 = new MailServiceImpl(config.mailjet)
        val repository                  = new AccountRepository(database)
        val registrationTokenRepository = new AccessTokenRepository(database)
        val tokenGenerationService      = new JwtService(config.jwt)
        val authService = new AuthServiceImpl(
          repository,
          registrationTokenRepository,
          mailGenerator,
          mailService,
          tokenGenerationService
        )

        val api: Set[Interface]                         = Set(new AuthInterface(authService))
        val serverRoutes                                = new ServerRoutes(config, api).routes
        implicit def rejectionHandler: RejectionHandler = ErrorHandler.rejectionHandler
        implicit def exceptionHandler: ExceptionHandler = ErrorHandler.exceptionHandler
        Http().bindAndHandle(serverRoutes, config.http.interface, config.http.port)

        logger.info(s"Server is running on http://${config.http.interface}:${config.http.port}/status")
        logger.info(s"See documentation http://${config.http.interface}:${config.http.port}/swagger")
      }
    )

}
