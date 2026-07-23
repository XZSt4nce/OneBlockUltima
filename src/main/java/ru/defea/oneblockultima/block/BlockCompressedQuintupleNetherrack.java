package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuintupleNetherrack extends BlockCompressedQuadrupleNetherrack {
    public BlockCompressedQuintupleNetherrack() {
        super("compressed_netherrack_5x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedQuintupleNetherrack(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
