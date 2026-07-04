package ru.defea.oneblockultima;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Blocks;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.defea.oneblockultima.block.ModBlocks;
import ru.defea.oneblockultima.capability.OneBlockPlayerData;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.util.BlockUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BlockSetConfigUnlockConditionTest
{
    @BeforeClass
    public static void initMinecraftBootstrap()
    {
        Bootstrap.register();
    }

    @Test
    public void unlockConditionsCanRequireBrokenBlocksOrSetLevels()
    {
        BlockSetConfig.BlockSetDefinition set = new BlockSetConfig.BlockSetDefinition();
        set.unlockConditions = new BlockSetConfig.UnlockConditionGroup();
        set.unlockConditions.mode = "any";
        set.unlockConditions.conditions.add(new BlockSetConfig.UnlockConditionDefinition());
        set.unlockConditions.conditions.get(0).type = "broken_blocks_total";
        set.unlockConditions.conditions.get(0).count = 100;

        OneBlockPlayerData data = new OneBlockPlayerData();
        assertFalse(set.hasUnlockRequirementsMet(data));

        data.addBrokenBlocks("classic", 100);
        assertTrue(set.hasUnlockRequirementsMet(data));
    }

    @Test
    public void clonedPlayerDataKeepsCurrencyAndProgress()
    {
        OneBlockPlayerData source = new OneBlockPlayerData();
        source.setCurrency(120);
        source.addBrokenBlocks("classic", 7);
        source.getSetLevels().put("classic", 2);

        OneBlockPlayerData target = new OneBlockPlayerData();
        target.copyFrom(source);

        assertEquals(120, target.getCurrency());
        assertEquals(7, target.getBrokenBlocksCount());
        assertEquals(7, target.getBrokenBlocksCount("classic"));
        assertEquals(2, target.getSetLevel("classic"));
    }

    @Test
    public void replacesBedrockAndEndPortalFrameWhenPlacedAboveGenerator()
    {
        assertEquals(ModBlocks.CUSTOM_BEDROCK.getDefaultState(),
                BlockUtil.getReplacementStateForGeneratorPlacement(Blocks.BEDROCK.getDefaultState(), ModBlocks.ONE_BLOCK_GENERATOR.getDefaultState()));

        assertEquals(ModBlocks.CUSTOM_PORTAL_FRAME.getDefaultState(),
                BlockUtil.getReplacementStateForGeneratorPlacement(Blocks.END_PORTAL_FRAME.getDefaultState(), ModBlocks.ONE_BLOCK_GENERATOR.getDefaultState()));
    }
}
