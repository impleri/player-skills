package net.impleri.playerskills.integrations.ftbquests.rewards

import dev.ftb.mods.ftblibrary.icon.Icon
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.reward.RewardType
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.server.PlayerSkillsServer
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.integrations.ftbquests.helpers.DoubleValueHandling
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.minecraft.network.chat.Component

case class NumericSkillReward(
  q: Quest,
  override val playerOps: PlayerOps,
  override val skillOps: SkillOps,
  override val skillTypeOps: SkillTypeOps,
)
  extends MinMaxSkillReward[Double](q, playerOps, skillOps, skillTypeOps) with DoubleValueHandling {
  override val skillType: ResourceLocation = NumericSkillType.NAME

  override def getType: RewardType = NumericSkillReward.REWARD_TYPE
}

object NumericSkillReward {
  val REWARD_TYPE: RewardType = RewardTypes.register(
    ResourceLocation("numeric_skill_reward").get.name,
    apply,
    () => Icon.getIcon("minecraft:item/iron_hoe"),
  )

  REWARD_TYPE.setDisplayName(Component.translatable("playerskills.quests.numeric_skill"))


  def apply(quest: Quest): NumericSkillReward = {
    new NumericSkillReward(
      quest,
      PlayerSkillsServer.STATE.PLAYER_OPS,
      PlayerSkills.STATE.SKILL_OPS,
      PlayerSkills.STATE.SKILL_TYPE_OPS,
    )
  }
}
