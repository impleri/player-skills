package net.impleri.playerskills.registry.storage;

import net.impleri.playerskills.PlayerSkillsCore;
import net.minecraft.nbt.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class SkillNbtStorage implements PersistentStorage {
    private static final String SKILLS_TAG = "acquiredSkills";

    @Override
    public List<String> read(File file) {
        List<String> skills = new ArrayList<>();
        CompoundTag tag;

        try {
            tag = NbtIo.readCompressed(file);
        } catch (IOException e) {
            PlayerSkillsCore.LOGGER.debug("Player data file {} does not exist", file.getPath());
            return skills;
        }

        if (!tag.contains(SKILLS_TAG)) {
            PlayerSkillsCore.LOGGER.warn("Player data file {} does not match the expected format", file.getPath());
            return skills;
        }

        ListTag list = tag.getList(SKILLS_TAG, Tag.TAG_STRING);
        return list.stream().map(Tag::getAsString).toList();
    }

    @Override
    public void write(File file, List<String> data) {
        ListTag skillList = new ListTag();
        data.stream()
                .map(StringTag::valueOf)
                .forEach(skillList::add);

        CompoundTag storage = new CompoundTag();
        storage.put(SKILLS_TAG, skillList);

        try {
            NbtIo.writeCompressed(storage, file);
        } catch (IOException e) {
            PlayerSkillsCore.LOGGER.debug("Failed to write to {}", file.getPath());
        }
    }
}
