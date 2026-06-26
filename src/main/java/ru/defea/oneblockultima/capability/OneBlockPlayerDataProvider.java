package ru.defea.oneblockultima.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.*;

import javax.annotation.Nullable;
import java.util.Map;

public class OneBlockPlayerDataProvider implements ICapabilitySerializable<NBTTagCompound>
{
    @CapabilityInject(IOneBlockPlayerData.class)
    public static Capability<IOneBlockPlayerData> ONE_BLOCK_PLAYER_DATA = null;

    private final OneBlockPlayerData instance = new OneBlockPlayerData();

    public static void register()
    {
        CapabilityManager.INSTANCE.register(
                IOneBlockPlayerData.class,
                new OneBlockPlayerDataStorage(),
                OneBlockPlayerData.class
        );
    }

    public static IOneBlockPlayerData get(ICapabilityProvider provider)
    {
        if (provider != null && ONE_BLOCK_PLAYER_DATA != null && provider.hasCapability(ONE_BLOCK_PLAYER_DATA, null))
        {
            return provider.getCapability(ONE_BLOCK_PLAYER_DATA, null);
        }

        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == ONE_BLOCK_PLAYER_DATA;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == ONE_BLOCK_PLAYER_DATA)
        {
            return ONE_BLOCK_PLAYER_DATA.cast(instance);
        }

        return null;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("currency", instance.getCurrency());

        NBTTagCompound levels = new NBTTagCompound();
        for (Map.Entry<String, Integer> entry : instance.getSetLevels().entrySet())
        {
            levels.setInteger(entry.getKey(), entry.getValue());
        }
        tag.setTag("setLevels", levels);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        instance.setCurrency(nbt.getInteger("currency"));
        instance.getSetLevels().clear();

        NBTTagCompound levels = nbt.getCompoundTag("setLevels");
        for (String key : levels.getKeySet())
        {
            instance.getSetLevels().put(key, levels.getInteger(key));
        }
    }
}
