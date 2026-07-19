package ru.defea.oneblockultima.item;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public final class ModItems
{
    public static final ItemGraviton GRAVITON = new ItemGraviton();
    public static final ItemDarkMatter DARK_MATTER = new ItemDarkMatter();
    public static final ItemEnergyOrb ENERGY_ORB = new ItemEnergyOrb();
    public static final ItemHiggsBoson HIGGS_BOSON = new ItemHiggsBoson();
    public static final ItemFarStar FAR_STAR = new ItemFarStar();
    public static final ItemProtein PROTEIN = new ItemProtein();
    public static final ItemSuperProtein SUPER_PROTEIN = new ItemSuperProtein();
    public static final ItemUltimateProtein ULTIMATE_PROTEIN = new ItemUltimateProtein();
    public static final ItemMashedVegetables MASHED_VEGETABLES = new ItemMashedVegetables();
    public static final ItemSuperMashedVegetables SUPER_MASHED_VEGETABLES = new ItemSuperMashedVegetables();
    public static final ItemUltimateMashedVegetables ULTIMATE_MASHED_VEGETABLES = new ItemUltimateMashedVegetables();
    public static final ItemNaturalPoison NATURAL_POISON = new ItemNaturalPoison();
    public static final ItemSuperPoison SUPER_POISON = new ItemSuperPoison();
    public static final ItemLiquidDeath LIQUID_DEATH = new ItemLiquidDeath();
    public static final ItemSpaceSoup SPACE_SOUP = new ItemSpaceSoup();
    public static final ItemCompressedMineral COMPRESSED_MINERAL = new ItemCompressedMineral();

    private ModItems()
    {
    }

    public static void init()
    {
        registerItem(GRAVITON, "graviton");
        registerItem(DARK_MATTER, "dark_matter");
        registerItem(ENERGY_ORB, "energy_orb");
        registerItem(HIGGS_BOSON, "higgs_boson");
        registerItem(FAR_STAR, "far_star");
        registerItem(PROTEIN, "protein");
        registerItem(SUPER_PROTEIN, "super_protein");
        registerItem(ULTIMATE_PROTEIN, "ultimate_protein");
        registerItem(MASHED_VEGETABLES, "mashed_vegetables");
        registerItem(SUPER_MASHED_VEGETABLES, "super_mashed_vegetables");
        registerItem(ULTIMATE_MASHED_VEGETABLES, "ultimate_mashed_vegetables");
        registerItem(NATURAL_POISON, "natural_poison");
        registerItem(SUPER_POISON, "super_poison");
        registerItem(LIQUID_DEATH, "liquid_death");
        registerItem(SPACE_SOUP, "space_soup");
        registerItem(COMPRESSED_MINERAL, "compressed_mineral");
    }

    private static void registerItem(Item item, String name)
    {
        GameRegistry.registerItem(item, name);
    }
}
