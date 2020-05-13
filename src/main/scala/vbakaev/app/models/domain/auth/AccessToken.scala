package vbakaev.app.models.domain.auth

import java.time.Instant
import java.util.UUID

final case class AccessToken(
    token: UUID,
    email: String,
    expiredAt: Option[Instant],
    createdAt: Instant
)
