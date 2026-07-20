package ru.defea.oneblockultima.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

public class OneBlockRenderHandler implements ISimpleBlockRenderingHandler
{
    private final int renderId;

    public OneBlockRenderHandler(int renderId)
    {
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
    {
        Tessellator tessellator = Tessellator.instance;
        renderer.setRenderBounds(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        renderer.renderFaceYNeg(block, -0.5, -0.5, -0.5, block.getIcon(0, metadata));
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderer.renderFaceYPos(block, -0.5, -0.5, -0.5, block.getIcon(1, metadata));
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderer.renderFaceZNeg(block, -0.5, -0.5, -0.5, block.getIcon(2, metadata));
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderer.renderFaceZPos(block, -0.5, -0.5, -0.5, block.getIcon(3, metadata));
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderer.renderFaceXNeg(block, -0.5, -0.5, -0.5, block.getIcon(4, metadata));
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderer.renderFaceXPos(block, -0.5, -0.5, -0.5, block.getIcon(5, metadata));
        tessellator.draw();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
    {
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId)
    {
        return true;
    }

    @Override
    public int getRenderId()
    {
        return this.renderId;
    }
}
