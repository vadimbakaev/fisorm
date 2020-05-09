package vbakaev.app.models.request

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import vbakaev.app.interfaces.validators.Validator
import vbakaev.app.models.response.FieldErrorInfo

@RequestBody
final case class RegistrationRequest(
    @Schema(required = true, `type` = "string", example = "flatmap.io@gmail.com")
    email: String
)

object RegistrationRequest {
  implicit val registrationRequestValidator: Validator[RegistrationRequest] = new Validator[RegistrationRequest] {
    override def apply(v1: RegistrationRequest): Seq[FieldErrorInfo] = {
      val emailErrorOpt: Option[FieldErrorInfo] = validationStage(emailRule(v1.email), "email", "email must be valid")

      List(emailErrorOpt).flatten
    }
  }
}
