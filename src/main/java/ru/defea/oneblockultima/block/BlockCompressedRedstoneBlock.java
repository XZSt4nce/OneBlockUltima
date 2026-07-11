package ru.defea.oneblockultima.block;

import net.minecraft.block.BlockCompressedPowered;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import ru.defea.oneblockultima.OneBlockUltima;

public class BlockCompressedRedstoneBlock extends BlockCompressedPowered {
    public BlockCompressedRedstoneBlock() {
        super(Material.IRON, MapColor.TNT);
        init("compressed_redstone_block_1x");
    }

    protected BlockCompressedRedstoneBlock(String name) {
        super(Material.IRON, MapColor.TNT);
        init(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.REDSTONE_BLOCK.getExplosionResistance(exploder) * 9.0F;
    }

    private void init(String name)
    {
        this.setSoundType(SoundType.METAL);
        this.setHardness(super.blockHardness * 9.0F);
        this.setResistance(super.blockResistance * 9.0F);
        setCreativeTab(OneBlockUltima.modTab);
        this.setUnlocalizedName(name);
        this.setRegistryName(OneBlockUltima.MODID, name);
    }
}
