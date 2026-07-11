package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;

public class BlockCompressedQuintupleLapisBlock extends BlockCompressedQuadrupleLapisBlock {
    public BlockCompressedQuintupleLapisBlock() {
        super("compressed_lapis_block_5x");
    }

    protected BlockCompressedQuintupleLapisBlock(String name) {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.LAPIS_BLOCK.getExplosionResistance(exploder) * 9.0F;
    }
}
