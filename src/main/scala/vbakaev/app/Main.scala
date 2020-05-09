package vbakaev.app

import java.time.Clock

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RejectionHandler
import com.typesafe.scalalogging.LazyLogging
import pureconfig.ConfigSource
import vbakaev.app.config.AppConfig
import vbakaev.app.interfaces.{AuthInterface, ErrorHandler, Interface}
import vbakaev.app.repositories.{AccountRepository, RegistrationTokenRepository}
import vbakaev.app.services.AuthServiceImpl
import vbakaev.app.services.mail.{MailGenerationServiceImpl, MailServiceImpl}
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
        val mailGenerator               = new MailGenerationServiceImpl(config.mail.host, config.mail.sender)
        val mailService                 = new MailServiceImpl(config.mailjet)
        val repository                  = new AccountRepository(config.mongo)
        val registrationTokenRepository = new RegistrationTokenRepository(config.mongo)
        val authService                 = new AuthServiceImpl(repository, registrationTokenRepository, mailGenerator, mailService)

        val api: Set[Interface]                         = Set(new AuthInterface(authService))
        val serverRoutes                                = new ServerRoutes(config, api).routes
        implicit def rejectionHandler: RejectionHandler = ErrorHandler.default
        Http().bindAndHandle(serverRoutes, config.http.interface, config.http.port)

        logger.info(s"Server is running on http://${config.http.interface}:${config.http.port}/status")
        logger.info(s"See documentation http://${config.http.interface}:${config.http.port}/swagger")
      }
    )

}
