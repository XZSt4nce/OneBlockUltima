package ru.defea.oneblockultima.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import ru.defea.oneblockultima.OneBlockUltima;

public class BlockCompressedLapisBlock extends Block {
    public BlockCompressedLapisBlock() {
        super(Material.IRON, MapColor.LAPIS);
        init("compressed_lapis_block_1x");
    }

    protected BlockCompressedLapisBlock(String name) {
        super(Material.IRON, MapColor.LAPIS);
        init(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.LAPIS_BLOCK.getExplosionResistance(exploder) * 9.0F;
    }

    private void init(String name)
    {
        this.setSoundType(SoundType.STONE);
        this.setHardness(3.0F * 9.0F);
        this.setResistance(5.0F * 9.0F);
        setCreativeTab(OneBlockUltima.modTab);
        this.setUnlocalizedName(name);
        this.setRegistryName(OneBlockUltima.MODID, name);
    }
}
