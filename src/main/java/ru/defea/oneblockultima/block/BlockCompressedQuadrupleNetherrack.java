package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuadrupleNetherrack extends BlockCompressedTripleNetherrack {
    public BlockCompressedQuadrupleNetherrack()
    {
        super("compressed_netherrack_4x");
    }

    protected BlockCompressedQuadrupleNetherrack(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
