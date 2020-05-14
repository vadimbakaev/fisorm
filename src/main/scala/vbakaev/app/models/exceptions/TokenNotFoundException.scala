package vbakaev.app.models.exceptions

import java.util.UUID

final case class TokenNotFoundException(token: UUID) extends RuntimeException

final case class TokenExpiredException(token: UUID) extends RuntimeException
