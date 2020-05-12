package vbakaev.app.services

import java.time.Clock

import vbakaev.app.config.JwtConfig
import vbakaev.app.models.domain.Account
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}

trait TokenGenerationService {
  def generateNavigationToken(account: Account): String
}

class JwtService(config: JwtConfig)(implicit clock: Clock) extends TokenGenerationService {
  private val algo: JwtAlgorithm = JwtAlgorithm.fromString(config.algo)
  override def generateNavigationToken(account: Account): String = {
    val claim = JwtClaim(
      expiration = Some(clock.instant().plusSeconds(157784760).getEpochSecond),
      issuedAt = Some(clock.instant().getEpochSecond)
    )

    JwtCirce.encode(claim, config.secretKey, algo)
  }
}
