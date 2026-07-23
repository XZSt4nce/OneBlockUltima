package ru.defea.oneblockultima.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import ru.defea.oneblockultima.OneBlockUltima;

public class BlockCompressedDiamondBlock extends Block {
    public BlockCompressedDiamondBlock() {
        super(Material.rock);
        init("compressed_diamond_block_1x");
    }
    protected BlockCompressedDiamondBlock(String name) {
        super(Material.rock);
        init(name);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.diamond_block.getExplosionResistance(exploder) * 9.0F;
    }
    private void init(String name) {
        this.setHardness(5F * 9F);
        this.setResistance(10F * 9F);
        setCreativeTab(OneBlockUltima.modTab);
        this.setBlockName(name);
        this.setBlockTextureName(OneBlockUltima.MODID + ":" + name);
    }
}
