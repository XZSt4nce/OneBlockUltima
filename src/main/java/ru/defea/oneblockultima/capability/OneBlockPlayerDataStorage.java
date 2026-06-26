package ru.defea.oneblockultima.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import java.util.Map;

public class OneBlockPlayerDataStorage implements Capability.IStorage<IOneBlockPlayerData>
{
    @Override
    public NBTBase writeNBT(Capability<IOneBlockPlayerData> capability, IOneBlockPlayerData instance, EnumFacing side)
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("currency", instance.getCurrency());

        NBTTagCompound levels = new NBTTagCompound();
        if (instance instanceof OneBlockPlayerData)
        {
            for (Map.Entry<String, Integer> entry : ((OneBlockPlayerData) instance).getSetLevels().entrySet())
            {
                levels.setInteger(entry.getKey(), entry.getValue());
            }
        }
        tag.setTag("setLevels", levels);
        return tag;
    }

    @Override
    public void readNBT(Capability<IOneBlockPlayerData> capability, IOneBlockPlayerData instance, EnumFacing side, NBTBase nbt)
    {
        if (!(instance instanceof OneBlockPlayerData) || !(nbt instanceof NBTTagCompound))
        {
            return;
        }

        NBTTagCompound tag = (NBTTagCompound) nbt;
        OneBlockPlayerData data = (OneBlockPlayerData) instance;
        data.setCurrency(tag.getInteger("currency"));
        data.getSetLevels().clear();

        NBTTagCompound levels = tag.getCompoundTag("setLevels");
        for (String key : levels.getKeySet())
        {
            data.getSetLevels().put(key, levels.getInteger(key));
        }
    }
}
