package vbakaev.app.interfaces

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs.{GET, POST, Path}
import vbakaev.app.models.request.RegistrationRequest
import vbakaev.app.models.response.StatusResponse
import vbakaev.app.services.AuthService

import scala.concurrent.ExecutionContext

@Path("/accounts")
class AuthInterface(
    authService: AuthService
)(implicit ec: ExecutionContext)
    extends Interface
    with JsonSupport {

  @POST
  @Operation(
    summary = "Register a new account",
    tags = Array("auth"),
    requestBody = new RequestBody(
      content = Array(
        new Content(
          schema = new Schema(implementation = classOf[RegistrationRequest]),
          mediaType = "application/json"
        )
      )
    ),
    responses = Array(
      new ApiResponse(responseCode = "201",
                      description = "A new account created and a confirmation email sent",
                      content = Array(new Content())),
      new ApiResponse(responseCode = "400",
                      description = "The email isn't valid, invalid format or already exist",
                      content = Array(new Content()))
    )
  )
  override def routes: Route = {
    path("accounts") {
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
