package net.impleri.playerskills.server.commands

import com.mojang.brigadier.CommandDispatcher
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.server.api.TeamOps
import net.minecraft.commands.CommandSourceStack

class PlayerSkillsCommandsSpec extends BaseSpec {
  private val skillOpsMock: SkillOps = mock[SkillOps]
  private val skillTypeOpsMock: SkillTypeOps = mock[SkillTypeOps]
  private val playerOpsMock: Player = mock[Player]
  private val teamOpsMock: TeamOps = mock[TeamOps]

  private val testUnit: PlayerSkillsCommands = new PlayerSkillsCommands(
    skillOpsMock,
    skillTypeOpsMock,
    playerOpsMock,
    teamOpsMock,
  )
  
  "PlayerSkillsCommands.register" should "register all of the commands" in {
    val dispatcher = mock[CommandDispatcher[CommandSourceStack]]
    testUnit.register(dispatcher)

    dispatcher.register(*) wasCalled once
  }
}
