package vbakaev.app.services

import java.time.Clock

import vbakaev.app.models.domain.Account
import vbakaev.app.models.domain.auth.RegistrationToken
import vbakaev.app.repositories.{CreateRepository, ReadRepository, SafeRepository}
import vbakaev.app.services.mail.{MailGenerationService, MailService}

import scala.concurrent.{ExecutionContext, Future}

trait AuthService {
  def register(email: String): Future[Option[Unit]]
  def access(email: String): Future[Option[Unit]]
}

class AuthServiceImpl(
    accountRepository: ReadRepository[Account] with CreateRepository[Account],
    registrationRepository: SafeRepository[RegistrationToken],
    mailGenerationService: MailGenerationService,
    mailService: MailService
)(implicit ec: ExecutionContext, clock: Clock)
    extends AuthService {

  override def register(email: String): Future[Option[Unit]] = {
    accountRepository.read(email).flatMap {
      case Some(account) if account.activatedAt.isDefined =>
        Future.successful(None)
      case Some(account) =>
        sendConfirmRegistrationEmail(account).map(Some(_))
      case None =>
        for {
          account <- accountRepository.create(Account(email, activatedAt = None, clock.instant()))
          _       <- sendConfirmRegistrationEmail(account)
        } yield Some(())
    }
  }

  override def access(email: String): Future[Option[Unit]] = ???

  private def sendConfirmRegistrationEmail(account: Account): Future[Unit] =
    for {
      token <- registrationRepository.readOrCreate(account.email).map(_.token)
      mail = mailGenerationService.accountConfirmation(account.email, token)
      _ <- mailService.sendEmail(mail)
    } yield ()
}
