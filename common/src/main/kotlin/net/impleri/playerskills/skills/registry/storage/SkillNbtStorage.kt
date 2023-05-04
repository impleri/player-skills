package net.impleri.playerskills.skills.registry.storage

import net.impleri.playerskills.PlayerSkills
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import java.io.File
import java.io.IOException

/**
 * Save data in NBT format
 */
internal class SkillNbtStorage : PersistentStorage {
  override fun read(file: File): List<String> {
    val skills: List<String> = ArrayList()

    val tag: CompoundTag = try {
      NbtIo.readCompressed(file)
    } catch (e: IOException) {
      PlayerSkills.LOGGER.debug("Player data file ${file.path} does not exist")
      return skills
    }

    if (!tag.contains(SKILLS_TAG)) {
      PlayerSkills.LOGGER.error("Player data file ${file.path} does not match the expected format")
      return skills
    }

    val list = tag.getList(SKILLS_TAG, Tag.TAG_STRING.toInt())
    return list.stream().map { obj: Tag -> obj.asString }.toList()
  }

  override fun write(file: File, skills: List<String>) {
    val skillList = ListTag()

    skills.stream()
      .map { string -> StringTag.valueOf(string) }
      .forEach { tag -> skillList.add(tag) }

    val storage = CompoundTag()
    storage.put(SKILLS_TAG, skillList)

    try {
      NbtIo.writeCompressed(storage, file)
    } catch (e: IOException) {
      PlayerSkills.LOGGER.error("Failed to write to ${file.path}")
    }
  }

  companion object {
    private const val SKILLS_TAG = "acquiredSkills"
  }
}
