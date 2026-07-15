package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;

public class BlockCompressedDoubleLapisBlock extends BlockCompressedLapisBlock {
    public BlockCompressedDoubleLapisBlock() {
        super("compressed_lapis_block_2x");
    }

    protected BlockCompressedDoubleLapisBlock(String name) {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.LAPIS_BLOCK.getExplosionResistance(exploder) * 9.0F;
    }
}
