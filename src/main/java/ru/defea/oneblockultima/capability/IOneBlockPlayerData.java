package ru.defea.oneblockultima.capability;

public interface IOneBlockPlayerData
{
    int getCurrency();

    void addCurrency(int amount);

    boolean spendCurrency(int amount);

    int getSetLevel(String setId);

    boolean upgradeSet(String setId, int cost, int maxLevel);
}
