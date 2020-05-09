package vbakaev.app.interfaces

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import vbakaev.app.interfaces.validators.Validator
import vbakaev.app.models.response.{FieldErrorInfo, ModelValidationRejection}

object ValidationDirective {

  def validateModel[T](model: T)(implicit validator: Validator[T]): Directive1[T] = {
    validator(model) match {
      case Nil                         => provide(model)
      case errors: Seq[FieldErrorInfo] => reject(ModelValidationRejection(errors))
    }
  }

}
