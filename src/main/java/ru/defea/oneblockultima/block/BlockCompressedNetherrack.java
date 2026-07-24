package ru.defea.oneblockultima.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import ru.defea.oneblockultima.OneBlockUltima;

public class BlockCompressedNetherrack extends Block {
    public BlockCompressedNetherrack() {
        super(Material.rock);
        init("compressed_netherrack_1x");
    }
    protected BlockCompressedNetherrack(String name) {
        super(Material.rock);
        init(name);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.netherrack.getExplosionResistance(exploder) * 9.0F;
    }
    private void init(String name) {
        this.setHardness(0.4F * 9F);
        this.setResistance(0.4F * 9F);
        setCreativeTab(OneBlockUltima.modTab);
        this.setBlockName(name);
        this.setBlockTextureName(OneBlockUltima.MODID + ":" + name);
    }
}
