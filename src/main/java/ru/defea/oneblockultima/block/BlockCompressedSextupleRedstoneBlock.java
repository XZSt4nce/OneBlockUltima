package ru.defea.oneblockultima.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

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
    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        return Blocks.redstone_block.getIcon(side, meta);
    }
}
