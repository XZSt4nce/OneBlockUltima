package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedDoubleEndStone extends BlockCompressedEndStone {
    public BlockCompressedDoubleEndStone()
    {
        super("compressed_end_stone_2x");
    }

    protected BlockCompressedDoubleEndStone(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
