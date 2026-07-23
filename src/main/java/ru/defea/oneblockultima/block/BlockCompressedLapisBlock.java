package ru.defea.oneblockultima.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import ru.defea.oneblockultima.OneBlockUltima;

public class BlockCompressedLapisBlock extends Block {
    public BlockCompressedLapisBlock() {
        super(Material.rock);
        init("compressed_lapis_block_1x");
    }
    protected BlockCompressedLapisBlock(String name) {
        super(Material.rock);
        init(name);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.lapis_block.getExplosionResistance(exploder) * 9.0F;
    }
    private void init(String name) {
        this.setHardness(5F * 9F);
        this.setResistance(10F * 9F);
        setCreativeTab(OneBlockUltima.modTab);
        this.setBlockName(name);
        this.setBlockTextureName(OneBlockUltima.MODID + ":" + name);
    }
}
