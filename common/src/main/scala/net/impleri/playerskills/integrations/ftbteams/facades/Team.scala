package net.impleri.playerskills.integrations.ftbteams.facades

import dev.ftb.mods.ftbteams.data.{Team => FtbTeam}
import dev.ftb.mods.ftbteams.data.TeamRank

import java.util.UUID
import scala.jdk.CollectionConverters._

case class Team(private val underlying: FtbTeam) {
  def getAllMembers: List[UUID] = {
    underlying.getRanked(TeamRank.NONE).asScala.keys.toList
  }
}
