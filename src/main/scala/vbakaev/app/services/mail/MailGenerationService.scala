package vbakaev.app.services.mail

import java.util.UUID

import vbakaev.app.models.domain.Mail

trait MailGenerationService {
  def accountConfirmation(email: String, token: UUID): Mail
  def accountAccess(email: String, token: UUID, hoursDuration: Long): Mail
}

class MailGenerationServiceImpl(
    host: String,
    sender: String
) extends MailGenerationService {
  private val accountConfirmation = "accountConfirmation"
  private val accountAccess       = "accountAccess"

  def accountConfirmation(email: String, token: UUID): Mail = {
    val magicLink = s"$host/confirmRegistration?email=$email&token=${token.toString}"
    Mail(
      customId = accountConfirmation,
      from = sender,
      to = email,
      subject = "Please confirm your Fisorm account",
      html = s"<a href='$magicLink'>$magicLink</a>",
    )
  }

  override def accountAccess(email: String, token: UUID, hoursDuration: Long): Mail = {
    val magicLink = s"$host/access?email=$email&token=${token.toString}"
    Mail(
      customId = accountAccess,
      from = sender,
      to = email,
      subject = "Please click to access your Fisorm account",
      html = s"The link will be expires in $hoursDuration hours " +
      s"<a href='$magicLink'>$magicLink</a>",
    )
  }
}
