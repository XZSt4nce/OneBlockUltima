package ru.defea.oneblockultima.capability;

import ru.defea.oneblockultima.config.BlockSetConfig;

import java.util.HashMap;
import java.util.Map;

public class OneBlockPlayerData implements IOneBlockPlayerData
{
    private int currency;
    private final Map<String, Integer> setLevels = new HashMap<String, Integer>();

    @Override
    public int getCurrency()
    {
        return currency;
    }

    @Override
    public void addCurrency(int amount)
    {
        if (amount > 0)
        {
            currency += amount;
        }
    }

    @Override
    public boolean spendCurrency(int amount)
    {
        if (amount <= 0 || currency < amount)
        {
            return false;
        }

        currency -= amount;
        return true;
    }

    @Override
    public int getSetLevel(String setId)
    {
        Integer level = setLevels.get(setId);
        if (level != null)
        {
            return level;
        }

        String defaultSetId = BlockSetConfig.get().getDefaultSetId();
        return setId != null && setId.equals(defaultSetId) ? 1 : 0;
    }

    @Override
    public boolean upgradeSet(String setId, int cost, int maxLevel)
    {
        int currentLevel = getSetLevel(setId);
        if (currentLevel >= maxLevel || !spendCurrency(cost))
        {
            return false;
        }

        setLevels.put(setId, currentLevel + 1);
        return true;
    }

    public Map<String, Integer> getSetLevels()
    {
        return setLevels;
    }

    public void setCurrency(int currency)
    {
        this.currency = Math.max(0, currency);
    }
}
