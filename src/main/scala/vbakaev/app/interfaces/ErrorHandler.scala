package vbakaev.app.interfaces

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.generic.auto._
import vbakaev.app.models.exceptions.{TokenNotFoundException, UnexpectedAccountState}
import vbakaev.app.models.response.{FieldErrorInfo, ModelValidationRejection}

object ErrorHandler extends ErrorAccumulatingCirceSupport {

  def rejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case ModelValidationRejection(invalidFields: Seq[FieldErrorInfo]) =>
          complete(BadRequest -> invalidFields)
      }
      .result()

  def exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case UnexpectedAccountState(email) =>
        extractUri { uri =>
          println(uri)
          complete(InternalServerError -> email)
        }
      case TokenNotFoundException(token) =>
        complete(NotFound -> token)
    }

}
