package vbakaev.app

import java.time.Clock

import akka.http.scaladsl.server.{Route, RouteConcatenation}
import vbakaev.app.config.AppConfig
import vbakaev.app.interfaces._
import vbakaev.app.interfaces.swagger.{SwaggerInterface, SwaggerUIInterface}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

class ServerRoutes(appConfig: AppConfig, interfaces: Set[Interface])(implicit clock: Clock) extends RouteConcatenation {

  private val documentedServices: Set[Interface] = Set(
    new StatusInterface()
  ) ++ interfaces

  private val services: Set[Interface] = Set(
    SwaggerUIInterface,
    new SwaggerInterface(appConfig.http, documentedServices)
  )

  val routes: Route = cors() { (services ++ documentedServices).map(_.routes).reduce(_ ~ _) }

}
