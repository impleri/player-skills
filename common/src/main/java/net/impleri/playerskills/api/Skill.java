package net.impleri.playerskills.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base generic skill. These are meant to be nothing more than
 * containers for data. All logic should be handled by the SkillType.
 */
public class Skill<T> {
    public static final int UNLIMITED_CHANGES = -1;
    
    protected ResourceLocation name;
    protected ResourceLocation type;
    protected boolean notify;
    protected String notifyKey;
    protected TeamMode teamMode;
    protected T value;
    protected String description;
    protected List<T> options;
    protected int changesAllowed;

    public Skill(ResourceLocation name, ResourceLocation type, T value, String description, List<T> options, int changesAllowed, TeamMode teamMode, boolean notify, String notifyKey) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.description = description;
        this.options = options;
        this.changesAllowed = changesAllowed;
        this.teamMode = teamMode;
        this.notify = notify;
        this.notifyKey = notifyKey;
    }

    public Skill<T> copy() {
        return copy(value, changesAllowed);
    }

    public Skill<T> copy(T value, int changesAllowed) {
        return new Skill<T>(name, type, value, description, options, changesAllowed, teamMode, notify, notifyKey);
    }

    public ResourceLocation getName() {
        return name;
    }

    public void setName(ResourceLocation name) {
        this.name = name;
    }

    public ResourceLocation getType() {
        return type;
    }

    public void setType(ResourceLocation type) {
        this.type = type;
    }

    public TeamMode getTeamMode() {
        return teamMode;
    }

    public void setTeamMode(TeamMode teamMode) {
        this.teamMode = teamMode;
    }

    @Nullable
    public T getValue() {
        return value;
    }

    public void setValue(@Nullable T value) {
        this.value = value;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @NotNull
    public List<T> getOptions() {
        return options;
    }

    public void setOptions(@NotNull List<T> options) {
        this.options = options;
    }

    public int getChangesAllowed() {
        return changesAllowed;
    }

    public boolean areChangesAllowed() {
        return changesAllowed != 0;
    }

    public void consumeChange() {
        if (changesAllowed > 0) {
            changesAllowed--;
        }
    }

    @Nullable
    public Component getNotification(T oldValue) {
        if (!notify || value == null) {
            return null;
        }

        return notifyKey == null ? getDefaultNotification() : formatNotification(notifyKey, oldValue);
    }

    protected Component getDefaultNotification() {
        return formatNotification("playerskills.notify.skill_change");
    }

    protected Component formatNotification(String messageKey) {
        return formatNotification(messageKey, null);
    }

    protected Component formatNotification(String messageKey, @Nullable T oldValue) {
        var skillName = formatSkillName();
        var skillValue = formatSkillValue();
        var oldSkillValue = oldValue == null ? "" : formatSkillValue(oldValue);

        return Component.translatable(messageKey, skillName, skillValue, oldSkillValue);
    }

    protected Component formatSkillName() {
        return Component.literal(getName().getPath().replace("_", " "))
                .withStyle(ChatFormatting.DARK_AQUA)
                .withStyle(ChatFormatting.BOLD);
    }

    protected Component formatSkillValue() {
        return formatSkillValue(value);
    }

    protected Component formatSkillValue(T value) {
        return Component.literal(value.toString())
                .withStyle(ChatFormatting.GOLD);
    }


    public boolean isAllowedValue(T nextVal) {
        return options.size() == 0 || options.contains(nextVal);
    }

    public boolean isSameAs(Skill<?> that) {
        return that.name == this.name;
    }

    public boolean isSameType(Skill<?> that) {
        return that.type == this.type;
    }

    private <V> boolean canEquals(Skill<V> that) {
        return that.getClass().isInstance(this);
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof Skill<?> skill) {
            return skill.canEquals(this) && isSameAs(skill) && isSameType(skill);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * type.hashCode();
    }
}
