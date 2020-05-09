package vbakaev.app.interfaces.validators

import vbakaev.app.models.request.RegistrationRequest
import vbakaev.app.models.response.FieldErrorInfo

object RegistrationRequestValidator extends Validator[RegistrationRequest] {

  override def apply(model: RegistrationRequest): Seq[FieldErrorInfo] = {
    val emailErrorOpt: Option[FieldErrorInfo] = validationStage(emailRule(model.email), "email", "email must be valid")

    List(emailErrorOpt).flatten
  }

}
