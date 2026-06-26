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
        String defaultVariantIn = "inventory";
        registerBlockModel(ModBlocks.ONE_BLOCK_GENERATOR, defaultVariantIn);
        registerBlockModel(ModBlocks.CUSTOM_PORTAL_FRAME, defaultVariantIn);
        registerBlockModel(ModBlocks.CUSTOM_BEDROCK, "normal");
        registerCustomPortalFrameStateMapper();
        registerCustomBedrockStateMapper();
    }

    @SideOnly(Side.CLIENT)
    private static void registerBlockModel(net.minecraft.block.Block block, String variantIn)
    {
        Item item = Item.getItemFromBlock(block);
        ModelLoader.setCustomModelResourceLocation(
                item,
                0,
                new ModelResourceLocation(block.getRegistryName(), variantIn)
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
