package ru.defea.oneblockultima.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import ru.defea.oneblockultima.OneBlockUltima;

public class BlockCompressedBedrock extends Block {
    public BlockCompressedBedrock()
    {
        super(Material.ROCK);
        init("compressed_bedrock_1x");
    }

    protected BlockCompressedBedrock(String name)
    {
        super(Material.ROCK);
        init(name);
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return false;
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.BEDROCK.getExplosionResistance(exploder) * 9.0F;
    }

    @Override
    public boolean isToolEffective(String tool, IBlockState state) {
        return false;
    }

    private void init(String name)
    {
        this.setBlockUnbreakable();
        this.setSoundType(SoundType.STONE);
        this.setResistance(6000000.0F * 9F);
        setCreativeTab(OneBlockUltima.modTab);
        this.setUnlocalizedName(name);
        this.setRegistryName(OneBlockUltima.MODID, name);
    }
}
