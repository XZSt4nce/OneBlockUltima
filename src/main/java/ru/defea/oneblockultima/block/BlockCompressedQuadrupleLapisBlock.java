package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;

public class BlockCompressedQuadrupleLapisBlock extends BlockCompressedTripleLapisBlock {
    public BlockCompressedQuadrupleLapisBlock() {
        super("compressed_lapis_block_4x");
    }

    protected BlockCompressedQuadrupleLapisBlock(String name) {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.LAPIS_BLOCK.getExplosionResistance(exploder) * 9.0F;
    }
}
