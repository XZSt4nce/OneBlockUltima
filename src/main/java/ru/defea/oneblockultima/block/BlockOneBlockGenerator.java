package ru.defea.oneblockultima.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.event.ModEvents;
import ru.defea.oneblockultima.gui.GuiHandler;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;
import ru.defea.oneblockultima.world.GeneratedBlockRegistry;

import java.util.Random;

public class BlockOneBlockGenerator extends BlockContainer
{
    public static final int GENERATOR_X = 0;
    public static final int GENERATOR_Y = 63;
    public static final int GENERATOR_Z = 0;
    public static final int GENERATED_BLOCK_Y = GENERATOR_Y + 1;
    public static final int FLUID_BARRIER_Y = GENERATED_BLOCK_Y + 1;

    public BlockOneBlockGenerator()
    {
        super(Material.ground);
        setHardness(-1.0F);
        setResistance(6000000.0F);
        setHarvestLevel("pickaxe", 0);
        setCreativeTab(OneBlockUltima.modTab);
        setBlockName("one_block_generator");
        this.setLightOpacity(0);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        if (player.isSneaking())
        {
            return false;
        }

        if (world.isRemote)
        {
            return true;
        }

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityOneBlockGenerator)
        {
            TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) tileEntity;
            if (generator.isFree() && generator.canBeClaimedBy(player.getUniqueID()))
            {
                GuiHandler.openClaimScreen(player, x, y, z);
                return true;
            }

            if (!ModEvents.ensureGeneratorAccess(world, x, y, z, player, generator))
            {
                return true;
            }

            GuiHandler.open(player, x, y, z);
            return true;
        }

        GuiHandler.open(player, x, y, z);
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityOneBlockGenerator();
    }

    @Override
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
        return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 2, z + 1);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
    {
        return AxisAlignedBB.getBoundingBox(x, y + 1, z, x + 1, y + 2, z + 1);
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
    public boolean canSustainPlant(IBlockAccess world, int x, int y, int z, ForgeDirection direction, IPlantable plantable)
    {
        return direction == ForgeDirection.UP;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        if (!world.isRemote)
        {
            ModEvents.applyPendingGeneratorOwner(world, x, y, z);

            if (world.isAirBlock(x, y, z - 1) && world.getBlock(x, y - 2, z) == ModBlocks.ONE_BLOCK_GENERATOR)
            {
                world.setBlock(x, y, z, ModBlocks.FLUID_BARRIER);
            }

            world.scheduleBlockUpdate(x, y, z, this, 1);
        }
        super.onBlockAdded(world, x, y, z);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand)
    {
        if (world.isRemote) return;

        if (world.getBlock(x, y - 2, z) == ModBlocks.ONE_BLOCK_GENERATOR && world.isAirBlock(x, y, z))
        {
            world.setBlock(x, y, z, ModBlocks.FLUID_BARRIER, 0, 2);
        }

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityOneBlockGenerator)
        {
            ModEvents.applyPendingGeneratorOwner(world, x, y, z);
            ((TileEntityOneBlockGenerator) tileEntity).tryGenerateBlock();
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
    {
        super.onNeighborBlockChange(world, x, y, z, block);
        if (world.isRemote) return;

        int fromY = y;
        if (!world.isAirBlock(x, fromY, z)) return;

        if (world.getBlock(x, fromY - 1, z) == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityOneBlockGenerator)
            {
                ((TileEntityOneBlockGenerator) tileEntity).tryGenerateBlock();
            }
        }
        else if (world.getBlock(x, fromY - 2, z) == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            if (world.getBlock(x, fromY, z) == ModBlocks.FLUID_BARRIER)
            {
                world.setBlockToAir(x, fromY, z);
            }
            world.setBlock(x, fromY, z, ModBlocks.FLUID_BARRIER, 0, 2);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
        if (world.getBlock(x, y - 1, z) == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            GeneratedBlockRegistry.get(world).remove(x, y, z);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta)
    {
        return Blocks.planks.getIcon(side, meta);
    }
}
