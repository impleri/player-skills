package net.impleri.playerskills.server;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class TeamApi {
    private static TeamApi INSTANCE = new StubTeamApi();

    public static void setInstance(TeamApi instance) {
        INSTANCE = instance;
    }

    public static TeamApi getInstance() {
        return INSTANCE;
    }

    private static class StubTeamApi extends TeamApi {
        @Nullable
        public UUID getTeamFor(UUID playerUuid) {
            return null;
        }

        public List<UUID> getTeamMembersFor(UUID playerId) {
            return new ArrayList<>();
        }
    }

    @Nullable
    abstract protected UUID getTeamFor(UUID playerUuid);

    abstract protected List<UUID> getTeamMembersFor(UUID playerId);

    @Nullable
    public UUID getTeamFor(Player player) {
        return getTeamFor(player.getUUID());
    }

    public List<UUID> getTeamMembersFor(Player player) {
        return getTeamMembersFor(player.getUUID());
    }

    /**
     * Does the team have room for the skill
     */
    public <T> boolean allows(Player player, Skill<T> skill) {
        var players = getTeamMembersFor(player);
        var teamMode = skill.getTeamMode();

        if (players.size() < 2 || teamMode.isOff() || teamMode.isShared()) {
            return true;
        }

        double count = net.impleri.playerskills.server.registry.PlayerSkills.countValue(players, skill);
        double limit = count + 1;

        if (teamMode.isLimited()) {
            limit = teamMode.rate;
        } else if (teamMode.isProportional()) {
            var percentage = teamMode.rate / 100;
            var teamSize = players.size();

            limit = Math.ceil(teamSize * percentage);
        } else if (teamMode.isSplitEvenly()) {
            var options = skill.getOptions().size();
            double teamSize = players.size();

            limit = Math.ceil(teamSize / options);
        } else if (teamMode.isPyramid()) {
            var newIndex = skill.getOptions().indexOf(skill.getValue());

            // Always allow the lowest tier
            if (newIndex != 1) {
                // Transform options into a reversed list of indices
                var optionIndices = new ArrayList<>(skill.getOptions().stream().map(option -> skill.getOptions().indexOf(option)).toList());
                Collections.reverse(optionIndices);

                // Transform indices into a list of limits (and reverse again)
                var tiers = new ArrayList<>(optionIndices.stream().map(index -> Math.pow(2, index)).toList());
                Collections.reverse(tiers);

                limit = tiers.get(newIndex);
            }
        }

        return count < limit;
    }

    /**
     * Set the skill for the entire team
     */
    public <T> boolean updateTeam(Player player, Skill<T> newSkill) {
        // Team Shared: Update for all team members
        var players = getTeamMembersFor(player);
        var oldSkillsMap = net.impleri.playerskills.server.registry.PlayerSkills.upsertMany(players, newSkill);

        // Emit SkillChanged event to all team members currently logged in
        var server = player.getServer();
        if (server != null) {
            var playerList = server.getPlayerList();
            oldSkillsMap.forEach((playerId, oldForPlayer) -> {
                var teamMember = playerList.getPlayer(playerId);
                PlayerSkills.emitSkillChanged(teamMember, newSkill, oldForPlayer);
            });
        } else {
            // if we can't get the server for whatever reason, at least update the current player
            var oldSkill = oldSkillsMap.get(player.getUUID());
            PlayerSkills.emitSkillChanged(player, newSkill, oldSkill);
        }

        return true;
    }
}
