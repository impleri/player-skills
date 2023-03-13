package net.impleri.playerskills.server.registry.storage;

import net.impleri.playerskills.PlayerSkills;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.UUID;

public class SkillStorage {
    private static final PersistentStorage storage = new SkillNbtStorage();

    public static void setup(MinecraftServer server) {
        SkillResourceFile.createInstance(server);
    }

    public static void write(UUID playerUuid, List<String> skills) {
        var file = SkillResourceFile.forPlayer(playerUuid);
        PlayerSkills.LOGGER.debug("Writing to {}", file.getPath());
        storage.write(file, skills);
    }

    public static List<String> read(UUID playerUuid) {
        var file = SkillResourceFile.forPlayer(playerUuid);
        PlayerSkills.LOGGER.debug("Reading file {}", file.getPath());
        List<String> skills = storage.read(file);

        // write skill list back to file
        write(playerUuid, skills);

        return skills;
    }
}
