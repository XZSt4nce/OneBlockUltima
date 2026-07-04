package ru.defea.oneblockultima;

import org.junit.Test;
import ru.defea.oneblockultima.capability.OneBlockPlayerData;
import ru.defea.oneblockultima.config.BlockSetConfig;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BlockSetConfigUnlockConditionTest
{
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
}
