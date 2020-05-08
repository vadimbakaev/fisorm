package vbakaev.app.interfaces

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import vbakaev.app.models.request.RegistrationRequest
import vbakaev.app.services.AuthService

import scala.concurrent.ExecutionContext

class AuthInterface(
    authService: AuthService
)(implicit ec: ExecutionContext)
    extends Interface
    with JsonSupport {
  override def routes: Route = {
    path("account") {
      post {
        entity(as[RegistrationRequest]) { request =>
          complete(authService.register(request.email).map {
            case Some(_) => StatusCodes.Created
            case None    => StatusCodes.BadRequest
          })
        }
      }
    }
  }
}
