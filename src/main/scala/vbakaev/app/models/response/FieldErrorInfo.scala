package vbakaev.app.models.response

import akka.http.scaladsl.server.Rejection

final case class FieldErrorInfo(name: String, error: String)

final case class ModelValidationRejection(invalidFields: Seq[FieldErrorInfo]) extends Rejection
