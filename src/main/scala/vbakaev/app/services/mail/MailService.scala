package vbakaev.app.services.mail

import com.mailjet.client.resource.Emailv31
import com.mailjet.client.resource.Emailv31.Message._
import com.mailjet.client.{ClientOptions, MailjetClient, MailjetRequest}
import com.typesafe.scalalogging.LazyLogging
import org.json.{JSONArray, JSONObject}
import vbakaev.app.config.MailjetConfig
import vbakaev.app.models.domain.Mail

import scala.concurrent.{ExecutionContext, Future}

trait MailService {
  def sendEmail(mail: Mail): Future[Unit]
}

class MailServiceImpl(config: MailjetConfig)(implicit ec: ExecutionContext) extends MailService with LazyLogging {

  private val client = new MailjetClient(config.apiKey, config.apiSecret, new ClientOptions("v3.1"))

  override def sendEmail(mail: Mail): Future[Unit] = Future {
    val Mail(_, from, to, subject, html) = mail
    val request =
      new MailjetRequest(Emailv31.resource)
        .property(
          Emailv31.MESSAGES,
          new JSONArray()
            .put(
              new JSONObject()
                .put(FROM, new JSONObject().put("Email", from))
                .put(TO, new JSONArray().put(new JSONObject().put("Email", to)))
                .put(SUBJECT, subject)
                .put(TEXTPART, "My first Mailjet email")
                .put(
                  HTMLPART,
                  html
                )
                .put(CUSTOMID, "AppGettingStartedTest")
            )
        )
    val response = client.post(request)

    logger.info("Main sent status {}", response.getStatus)
    logger.info("Main sent data {}", response.getData)

    require(response.getStatus == 200, "Email sending failed")
  }

}
