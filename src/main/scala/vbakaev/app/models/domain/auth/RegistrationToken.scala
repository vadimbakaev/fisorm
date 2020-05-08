package vbakaev.app.models.domain.auth

import java.time.Instant

final case class RegistrationToken(
    token: String,
    email: String,
    confirmedAt: Option[Instant],
    createdAt: Instant
)
