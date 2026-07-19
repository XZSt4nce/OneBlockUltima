package ru.defea.oneblockultima.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

public class BlockCompressedQuadrupleGoldBlock extends BlockCompressedTripleGoldBlock {
    public BlockCompressedQuadrupleGoldBlock() {
        super("compressed_gold_block_4x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedQuadrupleGoldBlock(String name) {
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
        return Blocks.gold_block.getIcon(side, meta);
    }
}
