package ru.defea.oneblockultima.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import ru.defea.oneblockultima.OneBlockUltima;

public class BlockCompressedMineralBlock extends Block {
    public BlockCompressedMineralBlock()
    {
        super(Material.IRON);
        init("compressed_mineral_block");
    }

    protected BlockCompressedMineralBlock(String name)
    {
        super(Material.IRON);
        init(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.IRON_BLOCK.getExplosionResistance(exploder) * 9.0F;
    }

    private void init(String name) {
        this.setSoundType(SoundType.METAL);
        this.setHardness(10F * 9F);
        this.setResistance(20F * 9F);
        setCreativeTab(OneBlockUltima.modTab);
        this.setUnlocalizedName(name);
        this.setRegistryName(OneBlockUltima.MODID, name);
    }
}
