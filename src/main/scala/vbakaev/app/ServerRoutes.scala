package vbakaev.app

import java.time.Clock

import akka.http.scaladsl.model.headers.HttpOrigin
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher
import ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher.Default
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import vbakaev.app.config.AppConfig
import vbakaev.app.interfaces._
import vbakaev.app.interfaces.swagger.{SwaggerInterface, SwaggerUIInterface}

class ServerRoutes(appConfig: AppConfig, interfaces: Set[Interface])(implicit clock: Clock) extends RouteConcatenation {

  private val documentedServices: Set[Interface] = Set(
    new StatusInterface()
  ) ++ interfaces

  private val services: Set[Interface] = Set(
    SwaggerUIInterface,
    new SwaggerInterface(appConfig.http.appRoot, documentedServices)
  )

  private val settings = CorsSettings.defaultSettings.withAllowedOrigins(
    Default(
      List(
        HttpOrigin("http://localhost:3000"),
        HttpOrigin(s"http://${appConfig.http.appRoot}"),
        HttpOrigin(s"https://${appConfig.http.appRoot}"),
      )
    )
  )

  val routes: Route = cors(settings) { (services ++ documentedServices).map(_.routes).reduce(_ ~ _) }

}
