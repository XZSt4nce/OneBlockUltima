package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedSextupleRedstoneBlock extends BlockCompressedQuintupleRedstoneBlock {
    public BlockCompressedSextupleRedstoneBlock() {
        super("compressed_redstone_block_6x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedSextupleRedstoneBlock(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
