package net.impleri.playerskills.server.integrations.ftbquests.tasks

import dev.ftb.mods.ftblibrary.icon.Icon
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.task.TaskType
import dev.ftb.mods.ftbquests.quest.task.TaskTypes
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.server.PlayerSkillsServer
import net.impleri.playerskills.utils.SkillResourceLocation
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.integrations.ftbquests.helpers.StringValueHandling
import net.impleri.playerskills.skills.specialized.SpecializedSkillType
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

case class SpecializedSkillTask(
  q: Quest,
  override val playerOps: Player,
  override val skillOps: SkillOps,
  override val skillTypeOps: SkillTypeOps,
)
  extends SkillTask[String](q, playerOps, skillOps) with StringValueHandling {
  override val skillType: ResourceLocation = SpecializedSkillType.NAME

  override def getType: TaskType = SpecializedSkillTask.TASK_TYPE
}

object SpecializedSkillTask {
  val TASK_TYPE: TaskType = TaskTypes.register(
    SkillResourceLocation.of("specialized_skill_task").get,
    apply,
    () => Icon.getIcon("minecraft:item/diamond_shovel"),
  )

  TASK_TYPE.setDisplayName(Component.translatable("playerskills.quests.specialized_skill"))

  def apply(quest: Quest): SpecializedSkillTask = {
    new SpecializedSkillTask(
      quest,
      PlayerSkillsServer.STATE.PLAYER_OPS,
      PlayerSkills.STATE.getSkillOps,
      PlayerSkills.STATE.getSkillTypeOps,
    )
  }
}
