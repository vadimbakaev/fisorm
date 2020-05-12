package vbakaev.app.interfaces

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs.{POST, PUT, Path}
import vbakaev.app.interfaces.ValidationDirective._
import vbakaev.app.models.request.{ConfirmRegistrationRequest, RegistrationRequest}
import vbakaev.app.services.AuthService

import scala.concurrent.ExecutionContext

class AuthInterface(
    authService: AuthService
)(implicit ec: ExecutionContext)
    extends Interface
    with JsonSupport {

  @Path("/accounts")
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
      new ApiResponse(responseCode = "400", description = "The email is invalid", content = Array(new Content()))
    )
  )
  def createAccounts: Route = path("accounts") {
    post {
      entity(as[RegistrationRequest]) { request =>
        validateModel(request).apply { validRequest =>
          complete(authService.register(validRequest.email).map(_ => StatusCodes.Created))
        }
      }
    }
  }

  @Path("/accounts/{email}")
  @PUT
  @Operation(
    summary = "Confirm account registration",
    tags = Array("auth"),
    parameters = Array(
      new Parameter(name = "email",
                    in = ParameterIn.PATH,
                    description = "user email",
                    example = "flatmap.io@gmail.com",
                    required = true,
                    allowEmptyValue = false)
    ),
    requestBody = new RequestBody(
      content = Array(
        new Content(
          schema = new Schema(implementation = classOf[ConfirmRegistrationRequest]),
          mediaType = "application/json"
        )
      )
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "An account is confirmed", content = Array(new Content())),
      new ApiResponse(responseCode = "400",
                      description = "The email isn't valid, invalid format or already exist",
                      content = Array(new Content()))
    )
  )
  def confirmAccount: Route = path("accounts" / Segment) { email =>
    put {
      entity(as[ConfirmRegistrationRequest]) { request =>
        validateModel(request).apply { validRequest =>
          complete(StatusCodes.OK -> authService.confirmRegistration(email, validRequest.token))
        }
      }
    }
  }

  override def routes: Route = createAccounts ~ confirmAccount

}
