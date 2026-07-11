package ru.defea.oneblockultima.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.block.BlockCustomPortalFrame;
import ru.defea.oneblockultima.block.ModBlocks;
import ru.defea.oneblockultima.item.ModItems;

import java.util.Objects;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = OneBlockUltima.MODID)
public final class ModModels
{
    private ModModels()
    {
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event)
    {
        for (ModBlocks.RegisterBlock modBlock : ModBlocks.modBlocks)
        {
            registerBlockModel(modBlock.getBlock(), modBlock.getVariantIn(), modBlock.getMeta());
        }
        for (ModItems.RegisterItem modItems : ModItems.modItems)
        {
            registerItemModel(modItems.getItem(), modItems.getVariantIn(), modItems.getMeta());
        }
        registerCustomPortalFrameStateMapper();
        registerCustomBedrockStateMapper();
    }

    @SideOnly(Side.CLIENT)
    private static void registerBlockModel(net.minecraft.block.Block block, String variantIn, int meta)
    {
        Item item = Item.getItemFromBlock(block);
        registerItemModel(item, variantIn, meta);
    }

    @SideOnly(Side.CLIENT)
    private static void registerItemModel(net.minecraft.item.Item item, String variantIn, int meta)
    {
        ModelLoader.setCustomModelResourceLocation(
                item,
                meta,
                new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), variantIn)
        );
    }

    @SideOnly(Side.CLIENT)
    private static void registerCustomPortalFrameStateMapper()
    {
        ModelLoader.setCustomStateMapper(
                ModBlocks.CUSTOM_PORTAL_FRAME,
                new StateMapperBase()
                {
                    @Override
                    protected ModelResourceLocation getModelResourceLocation(IBlockState state)
                    {
                        boolean eye = state.getValue(BlockCustomPortalFrame.EYE);
                        EnumFacing facing = state.getValue(BlockCustomPortalFrame.FACING);
                        String modelName = "minecraft:end_portal_frame";
                        String variant = "eye=" + eye + ",facing=" + facing.getName();
                        return new ModelResourceLocation(modelName, variant);
                    }
                }
        );
    }

    @SideOnly(Side.CLIENT)
    private static void registerCustomBedrockStateMapper()
    {
        ModelLoader.setCustomStateMapper(
                ModBlocks.CUSTOM_BEDROCK,
                new StateMapperBase()
                {
                    @Override
                    protected ModelResourceLocation getModelResourceLocation(IBlockState state)
                    {
                        String modelName = "minecraft:bedrock";
                        String variant = "normal";
                        return new ModelResourceLocation(modelName, variant);
                    }
                }
        );
    }
}
