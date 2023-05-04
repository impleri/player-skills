package net.impleri.playerskills.integrations.ftbteams

import dev.ftb.mods.ftbteams.FTBTeamsAPI
import dev.ftb.mods.ftbteams.data.Team
import dev.ftb.mods.ftbteams.data.TeamRank
import java.util.UUID
import net.impleri.playerskills.api.Team as TeamApi

class FTBTeamsPlugin : TeamApi() {
  private fun getTeam(playerUuid: UUID): Team? {
    return FTBTeamsAPI.getPlayerTeam(playerUuid)
  }

  override fun getTeamMembersFor(playerId: UUID): List<UUID> {
    val team = getTeam(playerId) ?: return ArrayList()
    return team.getRanked(TeamRank.NONE).keys.stream().toList()
  }

  companion object {
    fun registerInstance() {
      setInstance(FTBTeamsPlugin())
    }
  }
}
