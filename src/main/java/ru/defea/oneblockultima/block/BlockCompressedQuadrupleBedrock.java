package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuadrupleBedrock extends BlockCompressedTripleBedrock {
    public BlockCompressedQuadrupleBedrock()
    {
        super("compressed_bedrock_4x");
        this.setResistance(super.blockResistance * 9.0F);
    }

    protected BlockCompressedQuadrupleBedrock(String name)
    {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
