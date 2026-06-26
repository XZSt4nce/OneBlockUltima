package ru.defea.oneblockultima.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.List;
import java.util.Random;

public class BlockCustomBedrock extends Block {
    public BlockCustomBedrock() {
        super(Material.ROCK);

        this.setHardness(50.0F);
        this.setResistance(2000.0F);
        this.setUnlocalizedName("custom_bedrock");
        this.setRegistryName("custom_bedrock");
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.OBSIDIAN.getExplosionResistance(exploder);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        List<ItemStack> drops = new java.util.ArrayList<>();
        drops.add(new ItemStack(Blocks.BEDROCK));
        return drops;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.BEDROCK);
    }

    @Override
    public int quantityDropped(Random random) {
        return 1;
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        return 3;
    }

    @Override
    public String getHarvestTool(IBlockState state) {
        return "pickaxe";
    }

    @Override
    public boolean isToolEffective(String tool, IBlockState state) {
        return tool.equals("pickaxe");
    }
}
