package net.impleri.playerskills.registry.storage;

import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.UUID;

public class SkillStorage {
  private static final SkillNbtStorage storage = new SkillNbtStorage();

  public static void setup(MinecraftServer server) {
    SkillResourceFile.createInstance(server);
  }

  public static void write(UUID playerUuid, List<String> skills) {
    storage.write(SkillResourceFile.forPlayer(playerUuid), skills);
  }

  public static List<String> read(UUID playerUuid) {
    List<String> skills = storage.read(SkillResourceFile.forPlayer(playerUuid));

    // write skill list back to file
    write(playerUuid, skills);

    return skills;
  }
}
