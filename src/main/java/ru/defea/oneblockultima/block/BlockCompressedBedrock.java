package ru.defea.oneblockultima.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import ru.defea.oneblockultima.OneBlockUltima;

public class BlockCompressedBedrock extends Block
{
    public BlockCompressedBedrock()
    {
        super(Material.rock);
        init("compressed_bedrock_1x");
    }

    protected BlockCompressedBedrock(String name)
    {
        super(Material.rock);
        init(name);
    }

    @Override
    public boolean canHarvestBlock(EntityPlayer player, int meta)
    {
        return false;
    }

    @Override
    public float getExplosionResistance(Entity exploder)
    {
        return Blocks.bedrock.getExplosionResistance(exploder) * 9.0F;
    }

    private void init(String name)
    {
        this.setBlockUnbreakable();
        this.setStepSound(Block.soundTypeStone);
        this.setResistance(6000000.0F * 9F);
        setCreativeTab(OneBlockUltima.modTab);
        this.setBlockName(name);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta)
    {
        return Blocks.bedrock.getIcon(side, meta);
    }
}
