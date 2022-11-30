package net.impleri.playerskills;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class PlayerSkillsLogger {

    public static PlayerSkillsLogger create(String modId) {
        return create(modId, null);
    }

    public static PlayerSkillsLogger create(String modId, @Nullable String prefix) {

        return new PlayerSkillsLogger(modId, prefix);
    }

    private final Logger instance;

    private String prefix = "TAG";

    private PlayerSkillsLogger(String modId, String prefix) {
        this.prefix = prefix;
        this.instance = LogManager.getLogger(modId);
    }

    private String addPrefix(String message) {
        return "[" + prefix + "] " + message;
    }

    public void error(String message) {
        if (instance != null) {
            instance.error(addPrefix(message));
        }
    }

    public void error(String message, Object... params) {
        if (instance != null) {
            instance.error(addPrefix(message), Arrays.stream(params).toArray());
        }
    }

    public void warn(String message) {
        instance.warn(addPrefix(message));
    }

    public void warn(String message, Object... params) {
        instance.warn(addPrefix(message), Arrays.stream(params).toArray());
    }

    public void info(String message) {
        instance.info(addPrefix(message));
    }

    public void info(String message, Object... params) {
        instance.info(addPrefix(message), Arrays.stream(params).toArray());
    }

    public void debug(String message) {
        instance.debug(addPrefix(message));
    }

    public void debug(String message, Object... params) {
        instance.debug(addPrefix(message), Arrays.stream(params).toArray());
    }
}
