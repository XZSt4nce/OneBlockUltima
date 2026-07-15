package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedDoubleGoldBlock extends BlockCompressedGoldBlock {
    public BlockCompressedDoubleGoldBlock()
    {
        super("compressed_gold_block_2x");
        this.setResistance(super.blockResistance * 9F);
    }

    protected BlockCompressedDoubleGoldBlock(String name)
    {
        super(name);
        this.setResistance(super.blockResistance * 9F);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
