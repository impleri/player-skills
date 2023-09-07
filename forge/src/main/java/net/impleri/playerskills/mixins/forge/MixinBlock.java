package net.impleri.playerskills.mixins.forge;

import net.impleri.playerskills.api.BlockRestrictions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public class MixinBlock {

  public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        var replacement = BlockRestrictions.Companion.getReplacement(level, pos);

        return ((FireBlock) Blocks.FIRE).getBurnOdds(replacement);
    }

    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        var replacement = BlockRestrictions.Companion.getReplacement(level, pos);

        return getFlammability(replacement, level, pos, direction) > 0;
    }
}
