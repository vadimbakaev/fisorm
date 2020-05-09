package vbakaev.app.models.request

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody

@RequestBody
final case class RegistrationRequest(
    @Schema(required = true, `type` = "string", example = "flatmap.io@gmail.com")
    email: String
)
