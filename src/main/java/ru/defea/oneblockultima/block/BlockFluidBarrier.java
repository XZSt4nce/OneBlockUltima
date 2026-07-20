package ru.defea.oneblockultima.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.defea.oneblockultima.OneBlockUltima;

public class BlockFluidBarrier extends Block
{
    public BlockFluidBarrier()
    {
        super(Material.ground);
        setHardness(-1.0F);
        setResistance(6000000.0F);
        setBlockName("fluid_barrier");
        this.setLightOpacity(0);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
        return null;
    }

    @Override
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 start, Vec3 end)
    {
        return null;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
    {
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB bb, java.util.List list, Entity entity)
    {
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, int x, int y, int z)
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta)
    {
        return Blocks.planks.getIcon(side, meta);
    }
}
