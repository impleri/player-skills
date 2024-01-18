package net.impleri.playerskills.integrations.ftbquests.rewards

import dev.ftb.mods.ftblibrary.icon.Icon
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.reward.RewardType
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.server.PlayerSkillsServer
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.integrations.ftbquests.helpers.BooleanValueHandling
import net.minecraft.network.chat.Component

case class BasicSkillReward(
  q: Quest,
  override val playerOps: PlayerOps,
  override val skillOps: SkillOps,
  override val skillTypeOps: SkillTypeOps,
) extends SkillReward[Boolean](q, playerOps, skillOps, skillTypeOps) with BooleanValueHandling {
  override val skillType: ResourceLocation = BasicSkillType.NAME

  override def getType: RewardType = BasicSkillReward.REWARD_TYPE
}

object BasicSkillReward {
  val REWARD_TYPE: RewardType = RewardTypes.register(
    ResourceLocation("basic_skill_reward").get.name,
    apply,
    () => Icon.getIcon("minecraft:item/wooden_hoe"),
  )

  REWARD_TYPE.setDisplayName(Component.translatable("playerskills.quests.basic_skill"))

  def apply(quest: Quest): BasicSkillReward = {
    new BasicSkillReward(
      quest,
      PlayerSkillsServer.STATE.PLAYER_OPS,
      PlayerSkills.STATE.SKILL_OPS,
      PlayerSkills.STATE.SKILL_TYPE_OPS,
    )
  }
}
