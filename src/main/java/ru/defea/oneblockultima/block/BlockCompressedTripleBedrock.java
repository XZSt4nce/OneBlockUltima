package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedTripleBedrock extends BlockCompressedDoubleBedrock {
    public BlockCompressedTripleBedrock()
    {
        super("compressed_bedrock_3x");
        this.setResistance(super.blockResistance * 9.0F);
    }

    protected BlockCompressedTripleBedrock(String name)
    {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
