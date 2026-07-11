package ru.defea.oneblockultima.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import ru.defea.oneblockultima.OneBlockUltima;

public class BlockCompressedNetherrack extends Block {
    public BlockCompressedNetherrack()
    {
        super(Material.ROCK, MapColor.NETHERRACK);
        init("compressed_netherrack_1x");
    }

    protected BlockCompressedNetherrack(String name)
    {
        super(Material.ROCK, MapColor.NETHERRACK);
        init(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.NETHERRACK.getExplosionResistance(exploder) * 9.0F;
    }

    private void init(String name) {
        this.setSoundType(SoundType.STONE);
        this.setHardness(2F * 9F);
        this.setResistance(10F * 9F);
        setCreativeTab(OneBlockUltima.modTab);
        this.setUnlocalizedName(name);
        this.setRegistryName(OneBlockUltima.MODID, name);
    }
}
