package vbakaev.app.models.domain

final case class Mail(
    customId: String,
    from: String,
    to: String,
    subject: String,
    html: String,
)
