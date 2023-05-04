package net.impleri.playerskills.skills.registry.storage

import java.io.File

/**
 * Manages _how_ to save data
 */
internal interface PersistentStorage {
  fun read(file: File): List<String>
  fun write(file: File, skills: List<String>)
}
