package vbakaev.app.models.domain

import java.time.Instant

final case class Account(
    email: String,
    activatedAt: Option[Instant],
    createdAt: Instant
)
