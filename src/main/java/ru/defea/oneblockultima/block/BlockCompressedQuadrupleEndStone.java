package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuadrupleEndStone extends BlockCompressedTripleEndStone {
    public BlockCompressedQuadrupleEndStone()
    {
        super("compressed_end_stone_4x");
    }

    protected BlockCompressedQuadrupleEndStone(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
