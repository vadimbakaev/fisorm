package vbakaev.app.models.response

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse

@ApiResponse
final case class AccessTokenResponse(
    @Schema(required = true, `type` = "string", example = "OK")
    navigationToken: String
)
