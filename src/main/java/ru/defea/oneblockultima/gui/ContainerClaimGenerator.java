package ru.defea.oneblockultima.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import ru.defea.oneblockultima.network.PacketSyncPlayerData;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;

public class ContainerClaimGenerator extends Container
{
    private final World world;
    private final int generatorX;
    private final int generatorY;
    private final int generatorZ;
    private final EntityPlayer player;

    public ContainerClaimGenerator(EntityPlayer player, World world, int generatorX, int generatorY, int generatorZ)
    {
        this.world = world;
        this.generatorX = generatorX;
        this.generatorY = generatorY;
        this.generatorZ = generatorZ;
        this.player = player;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return playerIn.getDistanceSq(generatorX + 0.5D, generatorY + 0.5D, generatorZ + 0.5D) <= 64.0D;
    }

    public boolean claimOwnership()
    {
        if (world.isRemote)
        {
            return false;
        }

        TileEntityOneBlockGenerator generator = getGenerator();
        if (generator == null || !generator.isFree())
        {
            return false;
        }

        if (!generator.tryAssignOwnerIfEligible(player.getUniqueID()))
        {
            return false;
        }

        if (player instanceof EntityPlayerMP)
        {
            PacketSyncPlayerData.sendToPlayer(player);
        }
        return true;
    }

    public TileEntityOneBlockGenerator getGenerator()
    {
        if (world.getTileEntity(generatorX, generatorY, generatorZ) instanceof TileEntityOneBlockGenerator)
        {
            return (TileEntityOneBlockGenerator) world.getTileEntity(generatorX, generatorY, generatorZ);
        }

        return null;
    }

    public int getGeneratorX()
    {
        return generatorX;
    }

    public int getGeneratorY()
    {
        return generatorY;
    }

    public int getGeneratorZ()
    {
        return generatorZ;
    }
}
