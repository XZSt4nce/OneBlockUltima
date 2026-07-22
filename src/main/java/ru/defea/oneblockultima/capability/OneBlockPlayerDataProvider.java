package ru.defea.oneblockultima.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.Map;

public final class OneBlockPlayerDataProvider
{
    private static final String PERSISTENT_TAG = "oneblockultima_player_data";
    private static final String CURRENCY_TAG = "currency";
    private static final String BROKEN_BLOCKS_TOTAL_TAG = "brokenBlocksTotal";
    private static final String SET_LEVELS_TAG = "setLevels";
    private static final String BROKEN_BLOCKS_BY_SET_TAG = "brokenBlocksBySet";

    private OneBlockPlayerDataProvider()
    {
    }

    public static void register()
    {
        // No capability registration needed in 1.5.2
    }

    public static IOneBlockPlayerData get(EntityPlayer player)
    {
        if (player == null)
        {
            return null;
        }
        OneBlockPlayerData data = new OneBlockPlayerData();
        loadFromEntity(player, data);
        return data;
    }

    public static void saveToEntity(EntityPlayer player, IOneBlockPlayerData data)
    {
        if (player == null || data == null)
        {
            return;
        }

        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(CURRENCY_TAG, data.getCurrency());
        tag.setInteger(BROKEN_BLOCKS_TOTAL_TAG, data.getBrokenBlocksCount());

        NBTTagCompound setLevels = new NBTTagCompound();
        if (data instanceof OneBlockPlayerData)
        {
            OneBlockPlayerData playerData = (OneBlockPlayerData) data;
            for (Map.Entry<String, Integer> entry : playerData.getSetLevels().entrySet())
            {
                setLevels.setInteger(entry.getKey(), entry.getValue());
            }
        }
        tag.setTag(SET_LEVELS_TAG, setLevels);

        NBTTagCompound brokenBlocksBySet = new NBTTagCompound();
        if (data instanceof OneBlockPlayerData)
        {
            OneBlockPlayerData playerData = (OneBlockPlayerData) data;
            for (Map.Entry<String, Integer> entry : playerData.getBrokenBlocksBySet().entrySet())
            {
                brokenBlocksBySet.setInteger(entry.getKey(), entry.getValue());
            }
        }
        tag.setTag(BROKEN_BLOCKS_BY_SET_TAG, brokenBlocksBySet);

        player.getEntityData().setTag(PERSISTENT_TAG, tag);
    }

    public static void loadFromEntity(EntityPlayer player, IOneBlockPlayerData data)
    {
        if (player == null || data == null)
        {
            return;
        }

        NBTTagCompound tag = player.getEntityData().getCompoundTag(PERSISTENT_TAG);
        if (tag == null || !tag.hasKey(CURRENCY_TAG))
        {
            return;
        }

        if (data instanceof OneBlockPlayerData)
        {
            OneBlockPlayerData playerData = (OneBlockPlayerData) data;
            playerData.setCurrency(tag.getInteger(CURRENCY_TAG));
            playerData.setBrokenBlocksTotal(tag.getInteger(BROKEN_BLOCKS_TOTAL_TAG));

            playerData.getSetLevels().clear();
            if (tag.hasKey(SET_LEVELS_TAG))
            {
                NBTTagCompound setLevels = tag.getCompoundTag(SET_LEVELS_TAG);
                NBTTagList keyList = setLevels.getTagList("keys", 9);
                NBTTagList valueList = setLevels.getTagList("values", 9);
                if (keyList != null && valueList != null)
                {
                    for (int i = 0; i < keyList.tagCount(); i++)
                    {
                        String key = keyList.getStringTagAt(i);
                        int value = (int) valueList.func_150309_d(i);
                        playerData.getSetLevels().put(key, value);
                    }
                }
            }

            playerData.getBrokenBlocksBySet().clear();
            if (tag.hasKey(BROKEN_BLOCKS_BY_SET_TAG))
            {
                NBTTagCompound brokenBlocks = tag.getCompoundTag(BROKEN_BLOCKS_BY_SET_TAG);
                NBTTagList keyList = brokenBlocks.getTagList("keys", 9);
                NBTTagList valueList = brokenBlocks.getTagList("values", 9);
                if (keyList != null && valueList != null)
                {
                    for (int i = 0; i < keyList.tagCount(); i++)
                    {
                        String key = keyList.getStringTagAt(i);
                        int value = (int) valueList.func_150309_d(i);
                        playerData.getBrokenBlocksBySet().put(key, value);
                    }
                }
            }
        }
    }

    private static NBTTagCompound serializeMap(Map<String, Integer> map)
    {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList keys = new NBTTagList();
        NBTTagList values = new NBTTagList();
        for (Map.Entry<String, Integer> entry : map.entrySet())
        {
            keys.appendTag(new net.minecraft.nbt.NBTTagString(entry.getKey()));
            values.appendTag(new net.minecraft.nbt.NBTTagInt(entry.getValue()));
        }
        compound.setTag("keys", keys);
        compound.setTag("values", values);
        return compound;
    }
}
