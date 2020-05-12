package vbakaev.app.models.request

import java.util.UUID

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import vbakaev.app.interfaces.validators.Validator

@RequestBody
final case class ConfirmRegistrationRequest(
    @Schema(
      required = true,
      `type` = "string",
      format = "uuid",
      pattern = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
      example = "0d65e09f-3a16-4a86-abc0-8fa81c0681f2"
    )
    token: UUID
)

object ConfirmRegistrationRequest {
  implicit val confirmRegistrationRequestValidator: Validator[ConfirmRegistrationRequest] = _ => Nil
}
