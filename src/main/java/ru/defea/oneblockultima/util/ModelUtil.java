package ru.defea.oneblockultima.util;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

public final class ModelUtil {
    private ModelUtil() {}

    public static void renderBlockModelToGUI(Block block, int metadata, int x, int y, int size)
    {
        if (block == null) return;
        try
        {
            GL11.glPushMatrix();
            GL11.glTranslatef(x, y, 100.0F);
            GL11.glScalef(size / 16.0F, size / 16.0F, size / 16.0F);
            GL11.glRotatef(180F, 1F, 0F, 0F);
            RenderHelper.enableStandardItemLighting();
            RenderBlocks renderBlocks = new RenderBlocks();
            renderBlocks.renderBlockAsItem(block, metadata, 1.0F);
            RenderHelper.disableStandardItemLighting();
            GL11.glPopMatrix();
        }
        catch (Exception ignored) { }
    }

    public static void renderFluidSprite(Fluid fluid, int x, int y, int w, int h)
    {
        if (fluid == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        IIcon icon = null;
        try
        {
            icon = fluid.getStillIcon();
            if (icon == null) icon = fluid.getFlowingIcon();
        }
        catch (Exception ignored) { }

        if (icon == null) return;

        try
        {
            GL11.glPushMatrix();
            mc.renderEngine.bindTexture(new ResourceLocation("textures/blocks/terrain.png"));

            float minU = icon.getMinU();
            float maxU = icon.getMaxU();
            float minV = icon.getMinV();
            float maxV = icon.getMaxV();

            RenderHelper.enableStandardItemLighting();
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            Tessellator tess = Tessellator.instance;
            tess.startDrawingQuads();
            tess.addVertexWithUV(x, y + h, 0.0D, minU, maxV);
            tess.addVertexWithUV(x + w, y + h, 0.0D, maxU, maxV);
            tess.addVertexWithUV(x + w, y, 0.0D, maxU, minV);
            tess.addVertexWithUV(x, y, 0.0D, minU, minV);
            tess.draw();

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            RenderHelper.disableStandardItemLighting();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
        }
        catch (Exception ignored) { }
    }

    public static void drawEntityOnScreen(int posX, int posY, EntityLivingBase ent, int baseScale)
    {
        if (ent == null) return;

        float origRenderYawOffset = ent.renderYawOffset;
        float origPrevRotationYaw = ent.prevRotationYaw;
        float origRotationYaw = ent.rotationYaw;
        float origRotationYawHead = ent.rotationYawHead;
        float origPrevRotationYawHead = ent.prevRotationYawHead;
        float origRotationPitch = ent.rotationPitch;
        float origPrevRotationPitch = ent.prevRotationPitch;
        float origLimbSwing = ent.limbSwing;
        float origLimbSwingAmount = ent.limbSwingAmount;
        float origPrevLimbSwingAmount = ent.prevLimbSwingAmount;

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        try
        {
            GL11.glTranslatef(posX, posY, 50.0F);

            float finalScale = getScale(baseScale / 2, ent);

            GL11.glScalef(-finalScale, finalScale, finalScale);
            GL11.glRotatef(170.0F, 0.3F, 0.0F, 1.0F);

            ent.renderYawOffset = 0.0F;
            ent.prevRotationYaw = 0.0F;
            ent.rotationYaw = 0.0F;
            ent.rotationYawHead = 0.0F;
            ent.prevRotationYawHead = 0.0F;
            ent.rotationPitch = 0.0F;
            ent.prevRotationPitch = 0.0F;
            ent.limbSwing = 0.0F;
            ent.limbSwingAmount = 0.0F;
            ent.prevLimbSwingAmount = 0.0F;

            RenderHelper.enableStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            RenderManager renderManager = RenderManager.instance;
            float prevPlayerViewY = renderManager.playerViewY;
            renderManager.playerViewY = 180.0F;
            renderManager.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
            renderManager.playerViewY = prevPlayerViewY;

            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }
        catch (Exception ignored) { }
        finally
        {
            GL11.glPopMatrix();
            RenderHelper.disableStandardItemLighting();
            GL13.glActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL13.glActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glDisable(GL11.GL_COLOR_MATERIAL);
            GL11.glDisable(GL11.GL_LIGHTING);

            ent.renderYawOffset = origRenderYawOffset;
            ent.prevRotationYaw = origPrevRotationYaw;
            ent.rotationYaw = origRotationYaw;
            ent.rotationYawHead = origRotationYawHead;
            ent.prevRotationYawHead = origPrevRotationYawHead;
            ent.rotationPitch = origRotationPitch;
            ent.prevRotationPitch = origPrevRotationPitch;
            ent.limbSwing = origLimbSwing;
            ent.limbSwingAmount = origLimbSwingAmount;
            ent.prevLimbSwingAmount = origPrevLimbSwingAmount;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static float getScale(int scale, EntityLivingBase ent) {
        float heightScale = scale / ent.height;
        float widthScale = scale / ent.width;
        return Math.min(heightScale, widthScale);
    }
}
