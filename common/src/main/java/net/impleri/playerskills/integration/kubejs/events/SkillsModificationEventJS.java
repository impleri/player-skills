package net.impleri.playerskills.integration.kubejs.events;

import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.integration.kubejs.skills.GenericSkillBuilderJS;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.server.registry.Skills;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;


public class SkillsModificationEventJS extends BaseSkillsRegistryEventJS {
    public SkillsModificationEventJS(Map<String, RegistryObjectBuilderTypes.BuilderType<Skill<?>>> types) {
        super(types);
    }

    public <T> boolean modify(String name, Consumer<GenericSkillBuilderJS<T>> consumer) {


        return modify(name, null, consumer);
    }

    @HideFromJS
    @Nullable
    private <T> Skill<T> getSkill(String skillName) {
        var name = SkillResourceLocation.of(skillName);
        Skill<T> skill = null;
        try {
            skill = Skills.find(name);
            return skill;
        } catch (RegistryItemNotFound e) {
            ConsoleJS.SERVER.error("Unable to find skill " + name);
        }

        return null;
    }

    public <T> boolean modify(String skillName, @Nullable String skillType, Consumer<GenericSkillBuilderJS<T>> consumer) {
        @Nullable Skill<T> skill = getSkill(skillName);
        if (skill == null) {
            return false;
        }

        String type = (skillType != null) ? skillType : skill.getType().toString();
        ResourceLocation name = SkillResourceLocation.of(skillName);

        GenericSkillBuilderJS<T> builder = getBuilder(type, name);
        if (builder == null) {
            return false;
        }

        builder.syncWith(skill);
        consumer.accept(builder);

        Skill<T> newSkill = builder.createObject();
        Skills.upsert(newSkill);
        ConsoleJS.SERVER.info("Updated " + type + " skill " + name);

        return false;
    }

    public <T> boolean remove(String name) {
        @Nullable Skill<T> skill = getSkill(name);
        if (skill == null) {
            return true;
        }

        try {
            Skills.remove(skill);
        } catch (RegistryItemNotFound e) {
            return true;
        }

        ConsoleJS.SERVER.info("Removed skill " + name);

        return true;
    }
}
