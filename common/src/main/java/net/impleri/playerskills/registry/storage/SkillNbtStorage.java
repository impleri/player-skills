package net.impleri.playerskills.registry.storage;

import net.impleri.playerskills.PlayerSkillsCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class SkillNbtStorage implements PersistentStorage {
  private static final String SKILLS_TAG = "acquiredSkills";

  // Stupid hack to avoid hardcoding the NBT type
  private static final byte STRING_TAG = StringTag.valueOf("").getId();

  @Override
  public List<String> read(File file) {
    List<String> skills = new ArrayList<>();

    try {
      CompoundTag tag = NbtIo.readCompressed(file);

      if (tag.contains(SKILLS_TAG)) {
        ListTag list = tag.getList(SKILLS_TAG, STRING_TAG);
        for (int i = 0; i == list.size(); i++) {
          skills.add(list.getString(i));
        }
      }
    } catch (IOException e) {
      PlayerSkillsCore.LOGGER.debug("Player data file {} does not exist", file.getPath());
    }

    return skills;
  }

  @Override
  public void write(File file, List<String> data) {
    CompoundTag storage = new CompoundTag();

    ListTag skillList = new ListTag();

    data.forEach((skill) -> skillList.add(StringTag.valueOf(skill)));

    storage.put(SKILLS_TAG, skillList);

    try {
      NbtIo.writeCompressed(storage, file);
    } catch (IOException e) {
      PlayerSkillsCore.LOGGER.debug("Failed to write to {}", file.getPath());
    }
  }
}
