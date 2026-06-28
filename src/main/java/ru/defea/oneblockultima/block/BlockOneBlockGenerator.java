package ru.defea.oneblockultima.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.gui.GuiHandler;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;
import ru.defea.oneblockultima.world.GeneratedBlockRegistry;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockOneBlockGenerator extends Block implements ITileEntityProvider
{
    public static final BlockPos GENERATOR_POS = new BlockPos(0, 63, 0);
    public static final BlockPos GENERATED_BLOCK_POS = GENERATOR_POS.up();
    public static final BlockPos FLUID_BARRIER_POS = GENERATED_BLOCK_POS.up();

    private static final AxisAlignedBB COLLISION_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 2.0D, 1.0D);

    public BlockOneBlockGenerator()
    {
        super(Material.GROUND);
        setHardness(-1.0F);
        setResistance(6000000.0F);
        setHarvestLevel("pickaxe", 0);
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC);
        setRegistryName(OneBlockUltima.MODID, "one_block_generator");
        setUnlocalizedName("one_block_generator");
        this.setLightOpacity(0);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            player.openGui(OneBlockUltima.instance, GuiHandler.ONE_BLOCK_GUI, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityOneBlockGenerator();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.001D, 1.0D);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        return COLLISION_AABB;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, java.util.List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_)
    {
        super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end)
    {
        // Make the block untargetable to avoid raytrace crashes when player looks at it
        return null;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable)
    {
        if (plantable == null || direction != EnumFacing.UP)
        {
            return false;
        }

        EnumPlantType plantType = plantable.getPlantType(world, pos.offset(direction));
        return plantType == EnumPlantType.Crop
                || plantType == EnumPlantType.Plains
                || plantType == EnumPlantType.Beach;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state)
    {
        if (!world.isRemote)
        {
            // Ставим барьер над генератором
            OneBlockUltima.getLogger().info("[Generator] onBlockAdded: barrierPos=" + pos + ", block=" + world.getBlockState(pos).getBlock());
            if (world.getBlockState(pos).getBlock() == Blocks.AIR && world.getBlockState(pos.down(2)).getBlock() == ModBlocks.ONE_BLOCK_GENERATOR)
            {
                world.setBlockState(pos, ModBlocks.FLUID_BARRIER.getDefaultState(), 3);
                OneBlockUltima.getLogger().info("[Generator] BARRIER placed at " + pos);
            }

            world.scheduleUpdate(pos, this, 1);
        }
        super.onBlockAdded(world, pos, state);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
        if (world.isRemote)
        {
            return;
        }

        // Проверяем, нужно ли восстановить барьер
        if (world.getBlockState(pos.down(2)).getBlock() == ModBlocks.ONE_BLOCK_GENERATOR && world.getBlockState(pos).getBlock() == Blocks.AIR)
        {
            world.setBlockState(pos, ModBlocks.FLUID_BARRIER.getDefaultState(), 2);
            OneBlockUltima.getLogger().info("[Generator] BARRIER restored at " + pos + " from updateTick");
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityOneBlockGenerator)
        {
            ((TileEntityOneBlockGenerator) tileEntity).tryGenerateBlock();
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        super.neighborChanged(state, world, pos, blockIn, fromPos);
        if (world.isRemote)
        {
            return;
        }

        IBlockState currentState = world.getBlockState(fromPos);
        if (currentState != Blocks.AIR.getDefaultState()) {
            return;
        }

        if (world.getBlockState(fromPos.down()).getBlock() == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityOneBlockGenerator)
            {
                ((TileEntityOneBlockGenerator) tileEntity).tryGenerateBlock();
            }
        }
        else if (world.getBlockState(fromPos.down(2)).getBlock() == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            if (world.getBlockState(fromPos).getBlock() == ModBlocks.FLUID_BARRIER)
            {
                world.setBlockState(fromPos, Blocks.AIR.getDefaultState(), 3);
            }
            world.setBlockState(fromPos, ModBlocks.FLUID_BARRIER.getDefaultState(), 2);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        if (world.getBlockState(pos.down()).getBlock() == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            GeneratedBlockRegistry.get(world).remove(pos);
        }
        super.breakBlock(world, pos, state);
    }
}
