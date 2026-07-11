package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuintupleEndStone extends BlockCompressedQuadrupleEndStone {
    public BlockCompressedQuintupleEndStone()
    {
        super("compressed_end_stone_5x");
    }

    protected BlockCompressedQuintupleEndStone(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
