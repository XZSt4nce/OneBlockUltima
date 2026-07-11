package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedTripleNetherrack extends BlockCompressedDoubleNetherrack {
    public BlockCompressedTripleNetherrack()
    {
        super("compressed_netherrack_3x");
    }

    protected BlockCompressedTripleNetherrack(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
