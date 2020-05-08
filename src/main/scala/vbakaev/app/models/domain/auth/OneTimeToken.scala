package vbakaev.app.models.domain.auth

import java.time.Instant

final case class OneTimeToken(
    token: String,
    email: String,
    expireAt: Instant,
    createdAt: Instant
)
