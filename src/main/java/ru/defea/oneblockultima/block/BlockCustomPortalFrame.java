package ru.defea.oneblockultima.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.defea.oneblockultima.OneBlockUltima;

import java.util.List;
import java.util.Random;

public class BlockCustomPortalFrame extends Block {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool EYE = PropertyBool.create("eye");
    protected static final AxisAlignedBB AABB_BLOCK = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.8125D, 1.0D);

    public BlockCustomPortalFrame() {
        super(Material.ROCK);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(EYE, false));
        this.setHardness(50.0F);
        this.setResistance(2000.0F);
        this.setUnlocalizedName("custom_end_portal_frame");
        this.setRegistryName(OneBlockUltima.MODID, "custom_end_portal_frame");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB_BLOCK;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);

        // Проверяем, есть ли у игрока глаз Края
        if (!stack.isEmpty() && stack.getItem() == Items.ENDER_EYE) {
            // Если глаз уже есть - ничего не делаем
            if (state.getValue(EYE)) {
                return false;
            }

            // Вставляем глаз
            if (!world.isRemote) {
                world.setBlockState(pos, state.withProperty(EYE, true), 2);
                stack.shrink(1);

                // Активируем портал, если все рамки с глазами
                this.tryActivatePortal(world, pos, state.withProperty(EYE, true));
            }
            return true;
        }

        // Если игрок в творческом режиме с пустой рукой - можно убрать глаз
        if (player.isCreative() && stack.isEmpty() && state.getValue(EYE)) {
            if (!world.isRemote) {
                world.setBlockState(pos, state.withProperty(EYE, false), 2);
                // Даем игроку глаз в творческом режиме
                if (!player.isCreative()) {
                    player.addItemStackToInventory(new ItemStack(Items.ENDER_EYE));
                }
            }
            return true;
        }

        return false;
    }

    private void tryActivatePortal(World world, BlockPos pos, IBlockState state) {
        // Проверяем, все ли рамки вокруг имеют глаза
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
        }
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
        drops.add(new ItemStack(Blocks.END_PORTAL_FRAME));
        return drops;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.END_PORTAL_FRAME);
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

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(EYE, (meta & 1) == 1)
                .withProperty(FACING, EnumFacing.HORIZONTALS[meta >> 1]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | (state.getValue(EYE) ? 1 : 0);
        i = i | (state.getValue(FACING).getHorizontalIndex() << 1);
        return i;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, EYE);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }
}