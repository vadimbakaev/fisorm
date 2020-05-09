package vbakaev.app.interfaces.validators

import vbakaev.app.models.response.FieldErrorInfo

trait Validator[T] extends (T => Seq[FieldErrorInfo]) {

  protected def validationStage(rule: Boolean, fieldName: String, errorText: String): Option[FieldErrorInfo] =
    if (rule) Some(FieldErrorInfo(fieldName, errorText)) else None

  protected def emailRule(email: String): Boolean =
    if ("""\A([^@\s]+)@((?:[-a-z0-9]+\.)+[a-z]{2,})\z""".r.findFirstMatchIn(email).isEmpty) true else false

}
