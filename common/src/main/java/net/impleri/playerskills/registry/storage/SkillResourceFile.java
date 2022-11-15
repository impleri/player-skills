package net.impleri.playerskills.registry.storage;

import net.impleri.playerskills.PlayerSkillsCore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

class SkillResourceFile {
    private static SkillResourceFile instance;

    public static void createInstance(MinecraftServer server) {
        LevelResource levelResource = new LevelResource(PlayerSkillsCore.MOD_ID);
        Path gameFolder = server.getWorldPath(levelResource);

        instance = new SkillResourceFile(gameFolder);
    }

    public static void destroyInstance() {
        instance = null;
    }

    public static File forPlayer(UUID playerUuid) {
        if (instance == null) {
            throw new RuntimeException("Accessing file when the server is not running");
        }

        return instance.getPlayerFile(playerUuid);
    }

    private static void ensureDirectory(File file) {
        file.mkdirs();
    }

    private final Path storage;

    private SkillResourceFile(Path storagePath) {
        storage = storagePath;
    }

    private File getPlayerFile(UUID playerUuid) {
        return new File(getPlayerDirectory(), playerUuid.toString() + ".skills");
    }

    private File getPlayerDirectory() {
        File playerDir = new File(getStorageDirectory(), "players");
        ensureDirectory(playerDir);

        return playerDir;
    }

    private File getStorageDirectory() {
        File dataDir = storage.toFile();
        ensureDirectory(dataDir);

        return dataDir;
    }
}
