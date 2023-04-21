package net.impleri.playerskills.integration.ftbteams;

import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamRank;
import net.impleri.playerskills.server.TeamApi;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FTBTeamApi extends TeamApi {
    public static void registerInstance() {
        TeamApi.setInstance(new FTBTeamApi());
    }

    @Nullable
    private Team getTeam(UUID playerUuid) {
        return FTBTeamsAPI.getPlayerTeam(playerUuid);
    }

    protected List<UUID> getTeamMembersFor(UUID playerId) {
        var team = getTeam(playerId);

        if (team == null) {
            return new ArrayList<>();
        }

        return team.getRanked(TeamRank.NONE).keySet().stream().toList();
    }

}
