package net.impleri.playerskills.facades

import net.impleri.playerskills.api.restrictions.RestrictionsOps

trait GuaranteeResponse {
  protected def guarantee(response: Option[Boolean]): Boolean = response.getOrElse(RestrictionsOps.DEFAULT_RESPONSE)
}
