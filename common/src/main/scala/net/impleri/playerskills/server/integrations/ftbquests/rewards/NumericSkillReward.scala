package net.impleri.playerskills.server.integrations.ftbquests.rewards

import dev.ftb.mods.ftblibrary.icon.Icon
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.reward.RewardType
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.server.PlayerSkillsServer
import net.impleri.playerskills.utils.SkillResourceLocation
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.integrations.ftbquests.helpers.DoubleValueHandling
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

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
    SkillResourceLocation.of("numeric_skill_reward").get,
    apply,
    () => Icon.getIcon("minecraft:item/iron_hoe"),
  )

  REWARD_TYPE.setDisplayName(Component.translatable("playerskills.quests.numeric_skill"))


  def apply(quest: Quest): NumericSkillReward = {
    new NumericSkillReward(
      quest,
      PlayerSkillsServer.STATE.PLAYER_OPS,
      PlayerSkills.STATE.getSkillOps,
      PlayerSkills.STATE.getSkillTypeOps,
    )
  }
}
