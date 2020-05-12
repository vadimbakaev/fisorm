package vbakaev.app.models.domain.auth

import java.time.Instant
import java.util.UUID

final case class RegistrationToken(
    token: UUID,
    email: String,
    confirmedAt: Option[Instant],
    createdAt: Instant
)
