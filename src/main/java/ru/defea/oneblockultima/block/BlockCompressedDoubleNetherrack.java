package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedDoubleNetherrack extends BlockCompressedNetherrack {
    public BlockCompressedDoubleNetherrack()
    {
        super("compressed_netherrack_2x");
    }

    protected BlockCompressedDoubleNetherrack(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
