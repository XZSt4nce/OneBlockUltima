package ru.defea.oneblockultima.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.ArrayList;

public class BlockCustomBedrock extends Block
{
    public BlockCustomBedrock()
    {
        super(Material.rock);
        this.setHardness(50.0F);
        this.setResistance(2000.0F);
        this.setBlockName("custom_bedrock");
    }

    @Override
    public boolean canHarvestBlock(EntityPlayer player, int meta)
    {
        return true;
    }

    @Override
    public float getExplosionResistance(Entity exploder)
    {
        return Blocks.obsidian.getExplosionResistance(exploder);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(new ItemStack(Item.getItemFromBlock(Blocks.bedrock)));
        return drops;
    }

    @Override
    public int getHarvestLevel(int metadata)
    {
        return 3;
    }

    @Override
    public String getHarvestTool(int metadata)
    {
        return "pickaxe";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta)
    {
        return Blocks.bedrock.getIcon(side, meta);
    }
}
