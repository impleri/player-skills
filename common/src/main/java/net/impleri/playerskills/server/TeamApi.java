package net.impleri.playerskills.server;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class TeamApi {
    private static TeamApi INSTANCE = new StubTeamApi();

    /**
     * Sets the TeamApi instance to be used by the static methods
     */
    public static void setInstance(TeamApi instance) {
        INSTANCE = instance;
    }

    /**
     * Does the team have room for the skill
     */
    public static <T> boolean allows(Player player, Skill<T> skill) {
        var players = INSTANCE.getTeamMembersFor(player);
        var teamMode = skill.getTeamMode();

        if (players.size() < 2 || teamMode.isOff() || teamMode.isShared()) {
            PlayerSkills.LOGGER.info("No need to bother team");
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

        var allowed = count < limit;

        PlayerSkills.LOGGER.info("Does the team allow updating skill? {} ({} < {})", allowed, count, limit);

        return allowed;
    }

    /**
     * Set the skill for the entire team
     */
    public static <T> boolean updateTeam(Player player, Skill<T> newSkill) {
        // Team Shared: Update for all team members
        var teamMembers = INSTANCE.getTeamMembersFor(player);

        PlayerSkills.LOGGER.debug("Syncing skills with team: {}", teamMembers.stream().map(UUID::toString).collect(Collectors.joining(", ")));

        // Emit SkillChanged event to all team members currently logged in
        syncTeam(teamMembers, newSkill, player.getServer());

        return true;
    }

    /**
     * Syncs the player's shared skills with the rest of the team, overriding any progress one may have
     */
    public static boolean syncFromPlayer(ServerPlayer player) {
        PlayerSkills.LOGGER.debug("Syncing skills from {}", player.getName().getString());

        getSharedSkills(player).forEach(skill -> updateTeam(player, skill));

        return true;
    }

    /**
     * Syncs the "best" skill value for each shared skill currently help by the team with the rest of the team
     */
    public static boolean syncEntireTeam(ServerPlayer player) {
        PlayerSkills.LOGGER.debug("Syncing entire team connected to {}", player.getName().getString());

        var teamMembers = INSTANCE.getTeamMembersFor(player);
        var offlineMembers = net.impleri.playerskills.server.registry.PlayerSkills.openPlayers(teamMembers);
        var onlineMembers = teamMembers.stream().filter(playerId -> !offlineMembers.contains(playerId)).toList();

        var maxSkills = getSharedSkills(player)
                .map(skill -> getMaxSkill(teamMembers, skill))
                .filter(Objects::nonNull)
                .toList();

        maxSkills.stream().parallel().forEach(skill -> syncTeam(onlineMembers, skill, player.getServer()));
        maxSkills.stream().parallel().forEach(skill -> syncTeam(offlineMembers, skill, null));

        net.impleri.playerskills.server.registry.PlayerSkills.closePlayers(offlineMembers);

        return true;
    }

    private static <T> Skill<T> getMaxSkill(List<UUID> teamMembers, Skill<T> skill) {
        SkillType<T> skillType = SkillType.maybeForSkill(skill);

        if (skillType == null) {
            return null;
        }

        AtomicReference<Skill<T>> maxSkill = new AtomicReference<>(skill);

        teamMembers.stream()
                .map(playerId -> ServerApi.getSkill(playerId, skill.getName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(s -> (Skill<T>) s)
                .forEach(newSkill -> {
                    // New skill is better, so update reference
                    if (skillType.can(newSkill, maxSkill.get().getValue()) && !skillType.can(maxSkill.get(), newSkill.getValue())) {
                        maxSkill.set(newSkill);
                    }
                });

        return maxSkill.get();
    }

    private static <T> void syncTeam(List<UUID> teamMembers, Skill<T> skill, MinecraftServer server) {
        var oldSkillsMap = net.impleri.playerskills.server.registry.PlayerSkills.upsertMany(teamMembers, skill);

        // Emit SkillChanged event to all team members currently logged in
        if (server != null) {
            var playerList = server.getPlayerList();
            oldSkillsMap.forEach((playerId, oldForPlayer) -> {
                var teamMember = playerList.getPlayer(playerId);
                PlayerSkills.emitSkillChanged(teamMember, skill, oldForPlayer);
            });
        }
    }

    private static Stream<Skill<?>> getSharedSkills(Player player) {
        return ServerApi.getAllSkills(player).stream()
                .filter(skill -> skill.getTeamMode().isShared());
    }

    private static class StubTeamApi extends TeamApi {
        public List<UUID> getTeamMembersFor(UUID playerId) {
            return new ArrayList<>() {
                {
                    add(playerId);
                }
            };
        }
    }

    /**
     * Mod-specific implementation to get all other players "on the same team" as the given player ID
     */
    abstract protected List<UUID> getTeamMembersFor(UUID playerId);

    protected List<UUID> getTeamMembersFor(Player player) {
        return getTeamMembersFor(player.getUUID());
    }
}
