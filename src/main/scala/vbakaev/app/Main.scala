package vbakaev.app

import java.time.Clock

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ClosedShape
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import vbakaev.app.config.AppConfig
import akka.stream.scaladsl.GraphDSL.Implicits._
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import vbakaev.app.interfaces.AuthInterface
import vbakaev.app.repositories.{AccountRepository, RegistrationTokenRepository}
import vbakaev.app.services.AuthServiceImpl
import vbakaev.app.services.mail.{MailGenerationServiceImpl, MailServiceImpl}

import scala.concurrent.ExecutionContextExecutor

object Main extends App with LazyLogging {
  implicit val clock: Clock                               = Clock.systemUTC()
  implicit val system: ActorSystem                        = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  ConfigSource.default
    .load[AppConfig]
    .fold(
      error =>
        Source
          .single(s"Configuration loading error $error")
          .to(Sink.foreach(msg => logger.error(msg))),
      config =>
        RunnableGraph
          .fromGraph(GraphDSL.create() { implicit b =>
            val mailGenerator               = new MailGenerationServiceImpl(sender = config.mail.sender)
            val mailService                 = new MailServiceImpl(config.mailjet)
            val repository                  = new AccountRepository(config.mongo)
            val registrationTokenRepository = new RegistrationTokenRepository(config.mongo)
            val authService                 = new AuthServiceImpl(repository, registrationTokenRepository, mailGenerator, mailService)
            val authInterface               = new AuthInterface(authService)

            logger.info(s"Server is running on http://${config.http.interface}:${config.http.port}/status")
            logger.info(s"See documentation http://${config.http.interface}:${config.http.port}/swagger")

            val serverRoutes   = new ServerRoutes(config, Set(authInterface)).routes
            val httpConnection = Http()(system).bind(config.http.interface, config.http.port)
            val httpConnectionHandler = Sink.foreach[Http.IncomingConnection] { connection =>
              connection.handleWith(serverRoutes)
              ()
            }

            httpConnection ~> httpConnectionHandler

            ClosedShape
          })
    )
    .run()

}
