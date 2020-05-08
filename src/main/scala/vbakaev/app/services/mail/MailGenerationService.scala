package vbakaev.app.services.mail

import vbakaev.app.models.domain.Mail

trait MailGenerationService {
  def accountConfirmation(email: String, token: String): Mail
}

class MailGenerationServiceImpl(
    host: String = "http://localhost:3000/",
    sender: String
) extends MailGenerationService {
  private val AccountConfirmation = "accountConfirmation"
  override def accountConfirmation(email: String, token: String): Mail = {
    val magicLink = s"$host/confirmRegistration?email=$email&token=$token"
    Mail(
      customId = AccountConfirmation,
      from = sender,
      to = email,
      subject = "Please confirm your Fisorm account",
      html = s"<a href='$magicLink'>$magicLink</a>",
    )
  }
}
