package vbakaev.app.services.mail

import java.util.UUID

import vbakaev.app.models.domain.Mail

trait MailGenerationService {
  def accountConfirmation(email: String, token: UUID): Mail
}

class MailGenerationServiceImpl(
    host: String,
    sender: String
) extends MailGenerationService {
  private val accountConfirmation = "accountConfirmation"
  override def accountConfirmation(email: String, token: UUID): Mail = {
    val magicLink = s"$host/confirmRegistration?email=$email&token=${token.toString}"
    Mail(
      customId = accountConfirmation,
      from = sender,
      to = email,
      subject = "Please confirm your Fisorm account",
      html = s"<a href='$magicLink'>$magicLink</a>",
    )
  }
}
