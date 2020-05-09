package vbakaev.app.interfaces

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RejectionHandler
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.generic.auto._
import vbakaev.app.models.response.{FieldErrorInfo, ModelValidationRejection}

object ErrorHandler extends ErrorAccumulatingCirceSupport {
  def default: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case ModelValidationRejection(invalidFields: Seq[FieldErrorInfo]) =>
          complete(BadRequest -> invalidFields)
      }
      .result()
}
