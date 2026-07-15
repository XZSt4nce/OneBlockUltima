package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;

public class BlockCompressedSextupleLapisBlock extends BlockCompressedQuintupleLapisBlock {
    public BlockCompressedSextupleLapisBlock() {
        super("compressed_lapis_block_6x");
    }

    protected BlockCompressedSextupleLapisBlock(String name) {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.LAPIS_BLOCK.getExplosionResistance(exploder) * 9.0F;
    }
}
