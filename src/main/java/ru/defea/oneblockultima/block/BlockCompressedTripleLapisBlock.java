package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;

public class BlockCompressedTripleLapisBlock extends BlockCompressedDoubleLapisBlock {
    public BlockCompressedTripleLapisBlock() {
        super("compressed_lapis_block_3x");
    }

    protected BlockCompressedTripleLapisBlock(String name) {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.LAPIS_BLOCK.getExplosionResistance(exploder) * 9.0F;
    }
}
