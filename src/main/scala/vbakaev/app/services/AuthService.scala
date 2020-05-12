package vbakaev.app.services

import java.time.Clock
import java.util.UUID

import vbakaev.app.models.domain.Account
import vbakaev.app.models.domain.auth.RegistrationToken
import vbakaev.app.models.exceptions.{TokenNotFoundException, UnexpectedAccountState}
import vbakaev.app.repositories._
import vbakaev.app.services.mail.{MailGenerationService, MailService}

import scala.concurrent.{ExecutionContext, Future}

trait AuthService {
  def register(email: String): Future[Unit]
  def confirmRegistration(email: String, token: UUID): Future[String]
  def access(email: String): Future[Option[Unit]]
}

class AuthServiceImpl(
    accountRepository: ReadRepository[Account] with CreateRepository[Account] with UpdateRepository[Account],
    registrationRepository: SafeRepository[RegistrationToken] with DeleteRepository[RegistrationToken],
    mailGenerationService: MailGenerationService,
    mailService: MailService,
    tokenGenerationService: TokenGenerationService
)(implicit ec: ExecutionContext, clock: Clock)
    extends AuthService {

  override def register(email: String): Future[Unit] = {
    accountRepository.read(email).flatMap {
      case Some(account) if account.activatedAt.isDefined =>
        sendAccessEmail(account)
      case Some(account) =>
        sendConfirmRegistrationEmail(account)
      case None =>
        accountRepository
          .create(Account(email, activatedAt = None, clock.instant()))
          .map(sendConfirmRegistrationEmail)
          .map(_ => ())
    }
  }

  override def confirmRegistration(email: String, userToken: UUID): Future[String] =
    for {
      maybeRegistrationToken <- registrationRepository.read(email)
      registrationToken = maybeRegistrationToken.getOrElse(throw TokenNotFoundException(userToken))
      _                 = if (registrationToken.token != userToken) throw TokenNotFoundException(userToken)
      maybeAccount <- accountRepository.read(email)
      account = maybeAccount.getOrElse(throw UnexpectedAccountState(email))
      _                   <- registrationRepository.delete(email)
      maybeUpdatedAccount <- accountRepository.update(account.copy(activatedAt = Some(clock.instant())))
      _               = if (maybeUpdatedAccount.isEmpty) throw UnexpectedAccountState(email)
      navigationToken = tokenGenerationService.generateNavigationToken(account)
    } yield navigationToken

  override def access(email: String): Future[Option[Unit]] = ???

  private def sendAccessEmail(account: Account): Future[Unit] = {
    print(account)
    Future.unit
  }

  private def sendConfirmRegistrationEmail(account: Account): Future[Unit] =
    for {
      token <- registrationRepository.readOrCreate(account.email).map(_.token)
      mail = mailGenerationService.accountConfirmation(account.email, token)
      _ <- mailService.sendEmail(mail)
    } yield ()

}
