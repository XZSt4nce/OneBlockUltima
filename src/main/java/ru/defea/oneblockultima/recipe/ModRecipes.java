package ru.defea.oneblockultima.recipe;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.block.ModBlocks;
import ru.defea.oneblockultima.item.ModItems;

public final class ModRecipes
{
    private ModRecipes()
    {
    }

    public static void init()
    {
        registerCompressedBlockRecipes();
        registerCompressedMineralRecipes();
        registerItemRecipes();
    }

    private static void registerCompressedBlockRecipes()
    {
        addShapeless("compressed_bedrock_1x", ModBlocks.COMPRESSED_BEDROCK_1X,
                Blocks.bedrock, Blocks.bedrock, Blocks.bedrock,
                Blocks.bedrock, Blocks.bedrock, Blocks.bedrock,
                Blocks.bedrock, Blocks.bedrock, Blocks.bedrock);

        addCompressedChain("compressed_bedrock", 4,
                ModBlocks.COMPRESSED_BEDROCK_1X, ModBlocks.COMPRESSED_BEDROCK_2X,
                ModBlocks.COMPRESSED_BEDROCK_3X, ModBlocks.COMPRESSED_BEDROCK_4X);

        addShapeless("compressed_coal_block_1x", ModBlocks.COMPRESSED_COAL_1X,
                Blocks.coal_block, Blocks.coal_block, Blocks.coal_block,
                Blocks.coal_block, Blocks.coal_block, Blocks.coal_block,
                Blocks.coal_block, Blocks.coal_block, Blocks.coal_block);

        addCompressedChain("compressed_coal_block", 5,
                ModBlocks.COMPRESSED_COAL_1X, ModBlocks.COMPRESSED_COAL_2X,
                ModBlocks.COMPRESSED_COAL_3X, ModBlocks.COMPRESSED_COAL_4X,
                ModBlocks.COMPRESSED_COAL_5X);

        addShapeless("compressed_diamond_block_1x", ModBlocks.COMPRESSED_DIAMOND_1X,
                Blocks.diamond_block, Blocks.diamond_block, Blocks.diamond_block,
                Blocks.diamond_block, Blocks.diamond_block, Blocks.diamond_block,
                Blocks.diamond_block, Blocks.diamond_block, Blocks.diamond_block);

        addCompressedChain("compressed_diamond_block", 5,
                ModBlocks.COMPRESSED_DIAMOND_1X, ModBlocks.COMPRESSED_DIAMOND_2X,
                ModBlocks.COMPRESSED_DIAMOND_3X, ModBlocks.COMPRESSED_DIAMOND_4X,
                ModBlocks.COMPRESSED_DIAMOND_5X);

        addShapeless("compressed_emerald_block_1x", ModBlocks.COMPRESSED_EMERALD_1X,
                Blocks.emerald_block, Blocks.emerald_block, Blocks.emerald_block,
                Blocks.emerald_block, Blocks.emerald_block, Blocks.emerald_block,
                Blocks.emerald_block, Blocks.emerald_block, Blocks.emerald_block);

        addCompressedChain("compressed_emerald_block", 5,
                ModBlocks.COMPRESSED_EMERALD_1X, ModBlocks.COMPRESSED_EMERALD_2X,
                ModBlocks.COMPRESSED_EMERALD_3X, ModBlocks.COMPRESSED_EMERALD_4X,
                ModBlocks.COMPRESSED_EMERALD_5X);

        addShapeless("compressed_end_stone_1x", ModBlocks.COMPRESSED_END_STONE_1X,
                Blocks.end_stone, Blocks.end_stone, Blocks.end_stone,
                Blocks.end_stone, Blocks.end_stone, Blocks.end_stone,
                Blocks.end_stone, Blocks.end_stone, Blocks.end_stone);

        addCompressedChain("compressed_end_stone", 5,
                ModBlocks.COMPRESSED_END_STONE_1X, ModBlocks.COMPRESSED_END_STONE_2X,
                ModBlocks.COMPRESSED_END_STONE_3X, ModBlocks.COMPRESSED_END_STONE_4X,
                ModBlocks.COMPRESSED_END_STONE_5X);

        addShapeless("compressed_gold_block_1x", ModBlocks.COMPRESSED_GOLD_1X,
                Blocks.gold_block, Blocks.gold_block, Blocks.gold_block,
                Blocks.gold_block, Blocks.gold_block, Blocks.gold_block,
                Blocks.gold_block, Blocks.gold_block, Blocks.gold_block);

        addCompressedChain("compressed_gold_block", 5,
                ModBlocks.COMPRESSED_GOLD_1X, ModBlocks.COMPRESSED_GOLD_2X,
                ModBlocks.COMPRESSED_GOLD_3X, ModBlocks.COMPRESSED_GOLD_4X,
                ModBlocks.COMPRESSED_GOLD_5X);

        addShapeless("compressed_iron_block_1x", ModBlocks.COMPRESSED_IRON_1X,
                Blocks.iron_block, Blocks.iron_block, Blocks.iron_block,
                Blocks.iron_block, Blocks.iron_block, Blocks.iron_block,
                Blocks.iron_block, Blocks.iron_block, Blocks.iron_block);

        addCompressedChain("compressed_iron_block", 5,
                ModBlocks.COMPRESSED_IRON_1X, ModBlocks.COMPRESSED_IRON_2X,
                ModBlocks.COMPRESSED_IRON_3X, ModBlocks.COMPRESSED_IRON_4X,
                ModBlocks.COMPRESSED_IRON_5X);

        addShapeless("compressed_lapis_block_1x", ModBlocks.COMPRESSED_LAPIS_1X,
                Blocks.lapis_block, Blocks.lapis_block, Blocks.lapis_block,
                Blocks.lapis_block, Blocks.lapis_block, Blocks.lapis_block,
                Blocks.lapis_block, Blocks.lapis_block, Blocks.lapis_block);

        addCompressedChain("compressed_lapis_block", 6,
                ModBlocks.COMPRESSED_LAPIS_1X, ModBlocks.COMPRESSED_LAPIS_2X,
                ModBlocks.COMPRESSED_LAPIS_3X, ModBlocks.COMPRESSED_LAPIS_4X,
                ModBlocks.COMPRESSED_LAPIS_5X, ModBlocks.COMPRESSED_LAPIS_6X);

        addShapeless("compressed_netherrack_1x", ModBlocks.COMPRESSED_NETHERRACK_1X,
                Blocks.netherrack, Blocks.netherrack, Blocks.netherrack,
                Blocks.netherrack, Blocks.netherrack, Blocks.netherrack,
                Blocks.netherrack, Blocks.netherrack, Blocks.netherrack);

        addCompressedChain("compressed_netherrack", 5,
                ModBlocks.COMPRESSED_NETHERRACK_1X, ModBlocks.COMPRESSED_NETHERRACK_2X,
                ModBlocks.COMPRESSED_NETHERRACK_3X, ModBlocks.COMPRESSED_NETHERRACK_4X,
                ModBlocks.COMPRESSED_NETHERRACK_5X);

        addShapeless("compressed_redstone_block_1x", ModBlocks.COMPRESSED_REDSTONE_1X,
                Blocks.redstone_block, Blocks.redstone_block, Blocks.redstone_block,
                Blocks.redstone_block, Blocks.redstone_block, Blocks.redstone_block,
                Blocks.redstone_block, Blocks.redstone_block, Blocks.redstone_block);

        addCompressedChain("compressed_redstone_block", 6,
                ModBlocks.COMPRESSED_REDSTONE_1X, ModBlocks.COMPRESSED_REDSTONE_2X,
                ModBlocks.COMPRESSED_REDSTONE_3X, ModBlocks.COMPRESSED_REDSTONE_4X,
                ModBlocks.COMPRESSED_REDSTONE_5X, ModBlocks.COMPRESSED_REDSTONE_6X);
    }

    private static void registerCompressedMineralRecipes()
    {
        addShapeless("compressed_mineral", ModItems.COMPRESSED_MINERAL,
                ModBlocks.COMPRESSED_REDSTONE_6X, ModBlocks.COMPRESSED_LAPIS_6X,
                ModBlocks.COMPRESSED_DIAMOND_5X, ModBlocks.COMPRESSED_GOLD_5X,
                ModBlocks.COMPRESSED_IRON_5X, ModBlocks.COMPRESSED_END_STONE_5X,
                ModBlocks.COMPRESSED_EMERALD_5X, ModBlocks.COMPRESSED_COAL_5X,
                ModBlocks.COMPRESSED_NETHERRACK_5X);

        addShapeless("compressed_mineral_block", ModBlocks.BLOCK_COMPRESSED_MINERAL_BLOCK,
                ModItems.COMPRESSED_MINERAL, ModItems.COMPRESSED_MINERAL, ModItems.COMPRESSED_MINERAL,
                ModItems.COMPRESSED_MINERAL, ModItems.COMPRESSED_MINERAL, ModItems.COMPRESSED_MINERAL,
                ModItems.COMPRESSED_MINERAL, ModItems.COMPRESSED_MINERAL, ModItems.COMPRESSED_MINERAL);
    }

    private static void registerItemRecipes()
    {
        addShapeless("protein", ModItems.PROTEIN,
                Items.cooked_porkchop, Items.cooked_beef, Items.cooked_chicken,
                Items.milk_bucket,
                new ItemStack(Items.cooked_fished, 1, 0),
                new ItemStack(Items.cooked_fished, 1, 1),
                new ItemStack(Items.fish, 1, 0),
                new ItemStack(Items.fish, 1, 1),
                new ItemStack(Items.fish, 1, 2));

        addShapeless("super_protein", ModItems.SUPER_PROTEIN,
                ModItems.PROTEIN, ModItems.PROTEIN, ModItems.PROTEIN,
                ModItems.PROTEIN, ModItems.PROTEIN, ModItems.PROTEIN,
                ModItems.PROTEIN, ModItems.PROTEIN, ModItems.PROTEIN);

        addShapeless("ultimate_protein", ModItems.ULTIMATE_PROTEIN,
                ModItems.SUPER_PROTEIN, ModItems.SUPER_PROTEIN, ModItems.SUPER_PROTEIN,
                ModItems.SUPER_PROTEIN, ModItems.SUPER_PROTEIN, ModItems.SUPER_PROTEIN,
                ModItems.SUPER_PROTEIN, ModItems.SUPER_PROTEIN, ModItems.SUPER_PROTEIN);

        addShapeless("mashed_vegetables", ModItems.MASHED_VEGETABLES,
                Items.bread, Items.mushroom_stew, Items.golden_apple,
                Items.cookie, Items.melon, Items.carrot,
                Items.baked_potato, Items.pumpkin_pie, Items.golden_carrot);

        addShapeless("super_mashed_vegetables", ModItems.SUPER_MASHED_VEGETABLES,
                ModItems.MASHED_VEGETABLES, ModItems.MASHED_VEGETABLES, ModItems.MASHED_VEGETABLES,
                ModItems.MASHED_VEGETABLES, ModItems.MASHED_VEGETABLES, ModItems.MASHED_VEGETABLES,
                ModItems.MASHED_VEGETABLES, ModItems.MASHED_VEGETABLES, ModItems.MASHED_VEGETABLES);

        addShapeless("ultimate_mashed_vegetables", ModItems.ULTIMATE_MASHED_VEGETABLES,
                ModItems.SUPER_MASHED_VEGETABLES, ModItems.SUPER_MASHED_VEGETABLES, ModItems.SUPER_MASHED_VEGETABLES,
                ModItems.SUPER_MASHED_VEGETABLES, ModItems.SUPER_MASHED_VEGETABLES, ModItems.SUPER_MASHED_VEGETABLES,
                ModItems.SUPER_MASHED_VEGETABLES, ModItems.SUPER_MASHED_VEGETABLES, ModItems.SUPER_MASHED_VEGETABLES);

        GameRegistry.addShapedRecipe(
                new ItemStack(ModItems.NATURAL_POISON),
                "EFE", "PIP", "EFE",
                'E', Items.fermented_spider_eye,
                'F', Items.rotten_flesh,
                'P', Items.poisonous_potato,
                'I', new ItemStack(Items.fish, 1, 3));

        GameRegistry.addShapedRecipe(
                new ItemStack(ModItems.NATURAL_POISON),
                "EPE", "FIF", "EPE",
                'E', Items.fermented_spider_eye,
                'F', Items.rotten_flesh,
                'P', Items.poisonous_potato,
                'I', new ItemStack(Items.fish, 1, 3));

        GameRegistry.addShapedRecipe(
                new ItemStack(ModItems.SUPER_POISON),
                "NNN", "NPN", "NNN",
                'N', ModItems.NATURAL_POISON,
                'P', Items.nether_wart);

        GameRegistry.addShapedRecipe(
                new ItemStack(ModItems.LIQUID_DEATH),
                "SSS", "SRS", "SSS",
                'S', ModItems.SUPER_POISON,
                'R', Items.blaze_rod);

        GameRegistry.addShapedRecipe(
                new ItemStack(ModItems.ENERGY_ORB),
                "RRR", "RTR", "RRR",
                'R', ModBlocks.COMPRESSED_REDSTONE_6X,
                'T', Item.getItemFromBlock(Blocks.tnt));

        GameRegistry.addShapedRecipe(
                new ItemStack(ModItems.HIGGS_BOSON),
                "DGD", "GDG", "DGD",
                'D', ModBlocks.COMPRESSED_DIAMOND_5X,
                'G', ModBlocks.COMPRESSED_GOLD_5X);

        GameRegistry.addShapedRecipe(
                new ItemStack(ModItems.FAR_STAR),
                "EHE", "HSH", "EHE",
                'S', Items.nether_star,
                'E', ModItems.ENERGY_ORB,
                'H', ModItems.HIGGS_BOSON);

        GameRegistry.addShapedRecipe(
                new ItemStack(ModItems.FAR_STAR),
                "HEH", "ESE", "HEH",
                'S', Items.nether_star,
                'E', ModItems.ENERGY_ORB,
                'H', ModItems.HIGGS_BOSON);

        GameRegistry.addShapedRecipe(
                new ItemStack(ModItems.SPACE_SOUP),
                "LPL", "VCV", "LPL",
                'L', ModItems.LIQUID_DEATH,
                'V', ModItems.ULTIMATE_MASHED_VEGETABLES,
                'P', ModItems.ULTIMATE_PROTEIN,
                'C', Items.cake);

        GameRegistry.addShapedRecipe(
                new ItemStack(ModItems.SPACE_SOUP),
                "LVL", "PCP", "LVL",
                'L', ModItems.LIQUID_DEATH,
                'V', ModItems.ULTIMATE_MASHED_VEGETABLES,
                'P', ModItems.ULTIMATE_PROTEIN,
                'C', Items.cake);

        GameRegistry.addShapedRecipe(
                new ItemStack(ModItems.GRAVITON),
                "BBB", "BMB", "BBB",
                'B', ModBlocks.COMPRESSED_BEDROCK_4X,
                'M', ModBlocks.BLOCK_COMPRESSED_MINERAL_BLOCK);

        addShapeless("dark_matter", ModItems.DARK_MATTER,
                ModItems.GRAVITON, ModItems.GRAVITON, ModItems.GRAVITON,
                ModItems.GRAVITON, ModItems.GRAVITON, ModItems.GRAVITON,
                ModItems.GRAVITON, ModItems.GRAVITON, ModItems.GRAVITON);

        GameRegistry.addShapedRecipe(
                new ItemStack(ModBlocks.ONE_BLOCK_GENERATOR),
                "SDS", "DFD", "SDS",
                'S', ModItems.SPACE_SOUP,
                'D', ModItems.DARK_MATTER,
                'F', ModItems.FAR_STAR);

        GameRegistry.addShapedRecipe(
                new ItemStack(ModBlocks.ONE_BLOCK_GENERATOR),
                "DSD", "SFS", "DSD",
                'S', ModItems.SPACE_SOUP,
                'D', ModItems.DARK_MATTER,
                'F', ModItems.FAR_STAR);
    }

    private static void addCompressedChain(String baseName, int count, Block... blocks)
    {
        for (int i = 0; i < count - 1; i++)
        {
            Block input = blocks[i];
            Block output = blocks[i + 1];
            String name = baseName + "_" + (i + 2) + "x";
            addShapeless(name, output,
                    input, input, input,
                    input, input, input,
                    input, input, input);
        }
    }

    private static void addShapeless(String name, Object output, Object... inputs)
    {
        ItemStack result;
        if (output instanceof ItemStack)
        {
            result = (ItemStack) output;
        }
        else if (output instanceof Block)
        {
            result = new ItemStack((Block) output);
        }
        else if (output instanceof Item)
        {
            result = new ItemStack((Item) output);
        }
        else
        {
            throw new IllegalArgumentException("Invalid output type: " + output);
        }
        GameRegistry.addShapelessRecipe(result, inputs);
    }
}
