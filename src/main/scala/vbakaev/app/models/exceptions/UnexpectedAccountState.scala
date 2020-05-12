package vbakaev.app.models.exceptions

final case class UnexpectedAccountState(email: String) extends RuntimeException
