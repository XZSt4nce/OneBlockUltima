package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedDoubleBedrock extends BlockCompressedBedrock {
    public BlockCompressedDoubleBedrock()
    {
        super("compressed_bedrock_2x");
        this.setResistance(super.blockResistance * 9.0F);
    }

    protected BlockCompressedDoubleBedrock(String name)
    {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
