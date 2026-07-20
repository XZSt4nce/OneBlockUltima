package ru.defea.oneblockultima.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.util.ModelUtil;

import java.io.*;
import java.util.*;

public class GuiSetsConfig extends GuiScreen
{
    private static final int BUTTON_ADD_SET = 0;
    private static final int BUTTON_SAVE = 1;
    private static final int BUTTON_BACK = 3;
    private static final int BUTTON_ADD_BLOCK = 4;
    private static final int BUTTON_ADD_MOB = 5;
    private static final int BUTTON_REMOVE_ENTRY = 6;
    private static final int BUTTON_CONFIRM_DELETE = 7;
    private static final int BUTTON_CANCEL = 2;
    private static final int BUTTON_RESET = 8;
    private static final int BUTTON_SAVE_CURRENCY = 10;
    private static final int BUTTON_CANCEL_CURRENCY = 11;
    private static final int BUTTON_EDIT_REQUIRED_MODS = 12;
    private static final int BUTTON_REQUIRED_MODS_TOGGLE = 13;
    private static final int BUTTON_REQUIRED_MODS_BACK = 14;
    private static final int BUTTON_REQUIRED_MODS_SAVE = 15;
    private static final int BUTTON_REQUIRED_MODS_DELETE = 16;
    private static final int BUTTON_REQUIRED_MODS_ADD = 17;
    private static final int BUTTON_UNLOCK_CONDITIONS_TOGGLE = 18;
    private static final int BUTTON_UNLOCK_CONDITIONS_BACK = 19;
    private static final int BUTTON_UNLOCK_CONDITIONS_SAVE = 20;
    private static final int BUTTON_UNLOCK_CONDITIONS_ADD = 21;
    private static final int BUTTON_UNLOCK_CONDITIONS_DELETE = 22;
    private static final int BUTTON_EDIT_UNLOCK_CONDITIONS = 23;
    private static final int BUTTON_UNLOCK_CONDITIONS_CYCLE_TYPE = 24;
    private static final int BUTTON_UNLOCK_CONDITIONS_CYCLE_SET = 25;

    private static final ResourceLocation COIN_TEXTURE = new ResourceLocation(OneBlockUltima.MODID, "textures/gui/coin.png");

    private static final int VIEW_SETS = 0;
    private static final int VIEW_SET_DETAILS = 1;
    private static final int VIEW_ADD_ENTRY = 2;
    private static final int VIEW_CONFIRM_DELETE = 3;
    private static final int VIEW_EDIT_CURRENCY = 4;
    private static final int VIEW_REQUIRED_MODS_EDITOR = 5;
    private static final int VIEW_REQUIRED_MODS_ADD = 6;
    private static final int VIEW_UNLOCK_CONDITIONS = 7;

    private static class BlockDisplayEntry
    {
        private final int blockIndex;
        private final int meta;

        private BlockDisplayEntry(int blockIndex, int meta)
        {
            this.blockIndex = blockIndex;
            this.meta = meta;
        }
    }

    private final GuiScreen parent;
    private int currentView = VIEW_SETS;
    private int selectedSetIndex = -1;
    private int selectedBlockIndex = -1;
    private int selectedBlockMeta = -1;
    private int selectedMobIndex = -1;
    private int scrollOffset = 0;
    private final int scrollWidth = 4;
    private int entryScrollOffset = 0;
    private int searchScrollOffset = 0;
    private int deleteTargetIndex = -1;
    private int editingCurrencyIndex = -1;
    private int editingSetSourceIndex = -1;
    private EntryType editingEntryType = EntryType.BLOCK;
    private int requiredModsScrollOffset = 0;
    private final int innerPadding = 2;
    private static Map<String, Map<String, String>> staticSetLocalizedNames = new HashMap<String, Map<String, String>>();

    private BlockSetConfig config;
    private List<BlockSetConfig.BlockSetDefinition> sets = new ArrayList<BlockSetConfig.BlockSetDefinition>();
    private List<BlockSetConfig.BlockSetDefinition> filteredSets = new ArrayList<BlockSetConfig.BlockSetDefinition>();
    private List<SearchResult> searchResults = new ArrayList<SearchResult>();

    private GuiTextField searchField;
    private GuiTextField setNameField;
    private GuiTextField setIdField;
    private GuiTextField unlockCostField;
    private GuiTextField entrySearchField;
    private GuiTextField currencyField;
    private GuiTextField addLevelField;
    private GuiTextField addChanceField;
    private GuiTextField requiredModsField;
    private GuiTextField editCurrencyField;
    private GuiTextField editLevelField;
    private GuiTextField editChanceField;

    private GuiButton addSetButton;
    private GuiButton saveButton;
    private GuiButton backButton;
    private GuiButton addBlockButton;
    private GuiButton addMobButton;
    private GuiButton removeEntryButton;
    private GuiButton confirmDeleteButton;
    private GuiButton cancelDeleteButton;
    private GuiButton resetButton;
    private GuiButton saveCurrencyButton;
    private GuiButton cancelCurrencyButton;
    private GuiButton editRequiredModsButton;
    private GuiButton requiredModsToggleButton;
    private GuiButton requiredModsBackButton;
    private GuiButton requiredModsSaveButton;
    private GuiButton requiredModsDeleteButton;
    private GuiButton requiredModsAddButton;
    private GuiButton editUnlockConditionsButton;
    private GuiButton unlockConditionsToggleButton;
    private GuiButton unlockConditionsBackButton;
    private GuiButton unlockConditionsSaveButton;
    private GuiButton unlockConditionsAddButton;
    private GuiButton unlockConditionsDeleteButton;

    private boolean unlockConditionsLevelFieldVisible = false;
    private boolean unlockConditionsCountFieldVisible = false;
    private boolean editCurrencyFieldVisible = false;

    private String statusMessage = "";
    private int statusTimer = 0;
    private BlockSetConfig.BlockSetDefinition editingSet = null;
    private boolean isNewSet = false;
    private boolean suppressMouseUntilRelease = false;
    private boolean suppressNextMouseClick = false;

    private String savedNewSetName = "";
    private String savedNewSetId = "";
    private String savedNewSetCost = "0";
    private String savedNewSetMods = "";

    private String searchQuery = "";
    private List<String> requiredModsEditorMods = new ArrayList<String>();
    private BlockSetConfig.SetRequiredModsDefinition.TYPE requiredModsEditorType = BlockSetConfig.SetRequiredModsDefinition.TYPE.ALL;
    private boolean requiredModsEditorInitialized = false;
    private final Set<String> selectedRequiredModsForRemoval = new LinkedHashSet<String>();
    private final Set<String> selectedRequiredModsToAdd = new LinkedHashSet<String>();

    private String unlockConditionsEditorMode = "any";
    private final List<BlockSetConfig.UnlockConditionDefinition> unlockConditionsEditorConditions = new ArrayList<BlockSetConfig.UnlockConditionDefinition>();
    private int selectedUnlockConditionIndex = -1;
    private int unlockConditionsScrollOffset = 0;
    private GuiTextField unlockConditionsLevelField;
    private GuiTextField unlockConditionsCountField;
    private List<BlockSetConfig.BlockSetDefinition> availableSetsForConditions = new ArrayList<BlockSetConfig.BlockSetDefinition>();
    private String newConditionTypeToAdd = "broken_blocks_total";
    private String newConditionSetId = "";
    private GuiButton unlockConditionsCycleTypeButton;
    private GuiButton unlockConditionsCycleSetButton;

    private Map<String, Map<String, String>> setLocalizedNames = new HashMap<String, Map<String, String>>();

    private final int pad = 8;
    private int textHeight;
    private final int btnHeight = 20;
    private int gap = 6;
    private int formMargin;
    private int formWidth;
    private int formLabelWidth;
    private int formFieldX;
    private int formFieldWidth;
    private int iconSize;
    private int entryHeight;
    private final RenderItem itemRender = new RenderItem();

    private enum SearchType { BLOCKS, MOBS }
    private enum EntryType { BLOCK, MOB }

    private SearchType currentSearchType = SearchType.BLOCKS;
    private EntryType currentEntryType = EntryType.BLOCK;

    private static class RequiredModEntry
    {
        private final String modId;
        private final String displayName;

        private RequiredModEntry(String modId, String displayName)
        {
            this.modId = modId;
            this.displayName = displayName;
        }
    }

    private static class SearchResult
    {
        String registry;
        String name;
        String modId;
        ItemStack stack;
        Class<?> entityClass;
        boolean isMob;
        boolean isFluid;
        Fluid fluid;

        public SearchResult(String registry, String name, String modId, ItemStack stack)
        {
            this.registry = registry;
            this.name = name;
            this.modId = modId;
            this.stack = stack;
            this.isMob = false;
            this.isFluid = false;
            this.entityClass = null;
            this.fluid = null;
        }

        public SearchResult(String registry, String name, String modId, Class<?> entityClass)
        {
            this.registry = registry;
            this.name = name;
            this.modId = modId;
            this.entityClass = entityClass;
            this.isMob = true;
            this.isFluid = false;
            this.stack = null;
            this.fluid = null;
        }

        public SearchResult(String registry, String name, String modId, Fluid fluid)
        {
            this.registry = registry;
            this.name = name;
            this.modId = modId;
            this.isFluid = true;
            this.isMob = false;
            this.stack = null;
            this.entityClass = null;
            this.fluid = fluid;
        }
    }

    private void changeView(int view)
    {
        if (currentView != view)
        {
            if ((currentView == VIEW_REQUIRED_MODS_EDITOR || currentView == VIEW_REQUIRED_MODS_ADD)
                    && view != VIEW_REQUIRED_MODS_EDITOR && view != VIEW_REQUIRED_MODS_ADD)
            {
                requiredModsEditorInitialized = false;
            }
            currentView = view;
            clearAllTextFieldFocus();
            suppressMouseUntilRelease = true;
            suppressNextMouseClick = true;
            initGui();
        }
    }

    private void clearAllTextFieldFocus()
    {
        if (searchField != null) searchField.setFocused(false);
        if (setNameField != null) setNameField.setFocused(false);
        if (setIdField != null) setIdField.setFocused(false);
        if (unlockCostField != null) unlockCostField.setFocused(false);
        if (requiredModsField != null) requiredModsField.setFocused(false);
        if (entrySearchField != null) entrySearchField.setFocused(false);
        if (currencyField != null) currencyField.setFocused(false);
        if (addLevelField != null) addLevelField.setFocused(false);
        if (addChanceField != null) addChanceField.setFocused(false);
        if (editCurrencyField != null) editCurrencyField.setFocused(false);
        if (editLevelField != null) editLevelField.setFocused(false);
        if (editChanceField != null) editChanceField.setFocused(false);
        if (unlockConditionsLevelField != null) unlockConditionsLevelField.setFocused(false);
        if (unlockConditionsCountField != null) unlockConditionsCountField.setFocused(false);
    }

    private void reloadSetsFromConfig()
    {
        BlockSetConfig.reload();
        config = BlockSetConfig.get();
        sets.clear();
        sets.addAll((Collection<BlockSetConfig.BlockSetDefinition>)(config != null ? config.getSets() : Collections.emptyList()));
        updateFilteredSets();
    }

    private void discardEditingSetChanges()
    {
        editingSet = null;
        isNewSet = false;
        editingSetSourceIndex = -1;
        selectedBlockIndex = -1;
        selectedBlockMeta = -1;
        selectedMobIndex = -1;
        editingCurrencyIndex = -1;
        requiredModsEditorInitialized = false;
        reloadSetsFromConfig();
    }

    private List<BlockDisplayEntry> buildBlockDisplayEntries()
    {
        List<BlockDisplayEntry> entries = new ArrayList<BlockDisplayEntry>();
        if (editingSet == null || editingSet.blocks == null)
        {
            return entries;
        }

        for (int i = 0; i < editingSet.blocks.size(); i++)
        {
            BlockSetConfig.BlockElementDefinition block = editingSet.blocks.get(i);
            if (block == null)
            {
                continue;
            }
            for (int meta : block.getMetaValues())
            {
                entries.add(new BlockDisplayEntry(i, meta));
            }
        }
        return entries;
    }

    private Set<String> getExistingBlockRegistries()
    {
        Set<String> registries = new HashSet<String>();
        if (editingSet != null && editingSet.blocks != null)
        {
            for (BlockSetConfig.BlockElementDefinition block : editingSet.blocks)
            {
                if (block != null && block.registry != null && !block.registry.isEmpty())
                {
                    List<Integer> metaValues = block.getMetaValues();
                    if (metaValues.isEmpty())
                    {
                        registries.add(block.registry);
                    }
                    else
                    {
                        for (int m : metaValues)
                        {
                            registries.add(block.registry + ":" + m);
                        }
                    }
                }
            }
        }
        return registries;
    }

    private Set<String> getExistingMobRegistries()
    {
        Set<String> registries = new HashSet<String>();
        if (editingSet != null && editingSet.mobs != null)
        {
            for (BlockSetConfig.MobElementDefinition mob : editingSet.mobs)
            {
                if (mob != null && mob.registry != null && !mob.registry.isEmpty())
                {
                    registries.add(mob.registry);
                }
            }
        }
        return registries;
    }

    private List<Integer> collectMetasForRegistry(String registry)
    {
        List<Integer> metas = new ArrayList<Integer>();
        try
        {
            net.minecraft.block.Block block = (net.minecraft.block.Block) Block.blockRegistry.getObject(registry);
            if (block != null && block != net.minecraft.init.Blocks.air)
            {
                if (getFluidForRegistry(registry) != null)
                {
                    return Collections.singletonList(0);
                }

                Item item = Item.getItemFromBlock(block);
                if (item != null)
                {
                    List subItems = new ArrayList();
                    item.getSubItems(item, null, subItems);
                    for (Object obj : subItems)
                    {
                        ItemStack stack = (ItemStack) obj;
                        if (stack != null && stack.getItem() == item)
                        {
                            int damage = stack.getItemDamage();
                            if (!metas.contains(damage))
                            {
                                metas.add(damage);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ignored)
        {
        }

        if (metas.isEmpty())
        {
            metas.add(0);
        }
        Collections.sort(metas, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) { return a.compareTo(b); }
        });
        return metas;
    }

    private int parseNonNegativeInt(String value, int fallback)
    {
        if (value == null)
        {
            return fallback;
        }
        try
        {
            return Math.max(0, Integer.parseInt(value.trim()));
        }
        catch (NumberFormatException ignored)
        {
            return fallback;
        }
    }

    private int parsePositiveInt(String value, int fallback)
    {
        if (value == null)
        {
            return fallback;
        }
        try
        {
            return Math.max(1, Integer.parseInt(value.trim()));
        }
        catch (NumberFormatException ignored)
        {
            return fallback;
        }
    }

    private void applyMetaListToBlock(BlockSetConfig.BlockElementDefinition block, List<Integer> metas)
    {
        if (block == null)
        {
            return;
        }

        if (metas == null || metas.isEmpty())
        {
            block.metas = new ArrayList<Integer>();
            block.meta = 0;
            return;
        }

        if (metas.size() == 1)
        {
            block.metas = new ArrayList<Integer>();
            block.meta = metas.get(0);
            return;
        }

        block.metas = new ArrayList<Integer>(metas);
        block.meta = metas.get(0);
    }

    private String getRequiredModsEditorTypeLabel()
    {
        return requiredModsEditorType == BlockSetConfig.SetRequiredModsDefinition.TYPE.ANY
                ? StatCollector.translateToLocal("gui.oneblockultima.config.any")
                : StatCollector.translateToLocal("gui.oneblockultima.config.all");
    }

    private String getRequiredModsButtonLabel(BlockSetConfig.SetRequiredModsDefinition requiredMods)
    {
        if (requiredMods == null || requiredMods.getMods().isEmpty())
        {
            return StatCollector.translateToLocal("gui.oneblockultima.config.required_mods_edit");
        }

        String typeLabel = requiredMods.getType() == BlockSetConfig.SetRequiredModsDefinition.TYPE.ANY
                ? StatCollector.translateToLocal("gui.oneblockultima.config.any")
                : StatCollector.translateToLocal("gui.oneblockultima.config.all");
        return typeLabel + " (" + requiredMods.getMods().size() + ")";
    }

    private String getUnlockConditionsButtonLabel(BlockSetConfig.UnlockConditionGroup conditions)
    {
        if (conditions == null || conditions.conditions == null || conditions.conditions.isEmpty())
        {
            return StatCollector.translateToLocal("gui.oneblockultima.config.unlock_conditions_edit");
        }

        String typeLabel = "any".equalsIgnoreCase(conditions.mode)
                ? StatCollector.translateToLocal("gui.oneblockultima.config.any")
                : StatCollector.translateToLocal("gui.oneblockultima.config.all");
        return typeLabel + " (" + conditions.conditions.size() + ")";
    }

    private void applyRequiredModsFromFields()
    {
        if (editingSet == null)
        {
            return;
        }
        if (editingSet.requiredMods == null)
        {
            editingSet.requiredMods = new BlockSetConfig.SetRequiredModsDefinition();
        }
    }

    private List<String> getAvailableModIds()
    {
        List<String> ids = new ArrayList<String>();
        if (Loader.instance() != null && Loader.instance().getIndexedModList() != null)
        {
            ids.addAll(Loader.instance().getIndexedModList().keySet());
        }
        Collections.sort(ids, String.CASE_INSENSITIVE_ORDER);
        return ids;
    }

    private String getModDisplayName(String modId)
    {
        if (modId == null || modId.isEmpty())
        {
            return "";
        }

        if (Loader.instance() != null && Loader.instance().getIndexedModList() != null)
        {
            ModContainer container = Loader.instance().getIndexedModList().get(modId);
            if (container != null)
            {
                String name = container.getName();
                if (name != null && !name.trim().isEmpty())
                {
                    return name;
                }
            }
        }
        return modId;
    }

    private List<RequiredModEntry> getCurrentRequiredModEntries()
    {
        List<RequiredModEntry> entries = new ArrayList<RequiredModEntry>();
        for (String modId : requiredModsEditorMods)
        {
            entries.add(new RequiredModEntry(modId, getModDisplayName(modId)));
        }
        return entries;
    }

    private List<RequiredModEntry> getAvailableRequiredModEntries()
    {
        Set<String> ignoredModIds = new HashSet<String>(Arrays.asList(
                "minecraft",
                "forge",
                "fml",
                "minecraftforge",
                "mcp",
                "minecraftcoderpack",
                "oneblockultima"
        ));

        List<RequiredModEntry> entries = new ArrayList<RequiredModEntry>();
        for (String modId : getAvailableModIds())
        {
            if (requiredModsEditorMods.contains(modId) || ignoredModIds.contains(modId.toLowerCase(Locale.ROOT)))
            {
                continue;
            }
            entries.add(new RequiredModEntry(modId, getModDisplayName(modId)));
        }
        return entries;
    }

    private void openRequiredModsEditor()
    {
        if (editingSet == null)
        {
            return;
        }

        requiredModsEditorMods.clear();
        if (editingSet.requiredMods != null)
        {
            requiredModsEditorType = editingSet.requiredMods.getType();
            requiredModsEditorMods.addAll(editingSet.requiredMods.getMods());
        }
        else
        {
            requiredModsEditorType = BlockSetConfig.SetRequiredModsDefinition.TYPE.ALL;
        }
        requiredModsEditorInitialized = true;
        selectedRequiredModsForRemoval.clear();
        selectedRequiredModsToAdd.clear();
        requiredModsScrollOffset = 0;
        changeView(VIEW_REQUIRED_MODS_EDITOR);
    }

    private void openRequiredModsAddView()
    {
        selectedRequiredModsToAdd.clear();
        requiredModsScrollOffset = 0;
        changeView(VIEW_REQUIRED_MODS_ADD);
    }

    private void openUnlockConditionsEditor()
    {
        if (editingSet == null)
        {
            return;
        }

        unlockConditionsEditorConditions.clear();
        if (editingSet.unlockConditions != null && editingSet.unlockConditions.conditions != null)
        {
            unlockConditionsEditorMode = editingSet.unlockConditions.mode != null ? editingSet.unlockConditions.mode : "any";
            for (BlockSetConfig.UnlockConditionDefinition cond : editingSet.unlockConditions.conditions)
            {
                if (cond == null) continue;
                BlockSetConfig.UnlockConditionDefinition copy = new BlockSetConfig.UnlockConditionDefinition();
                copy.type = cond.type;
                copy.setId = cond.setId;
                copy.level = cond.level;
                copy.count = cond.count;
                unlockConditionsEditorConditions.add(copy);
            }
        }
        else
        {
            unlockConditionsEditorMode = "any";
        }
        selectedUnlockConditionIndex = -1;
        unlockConditionsScrollOffset = 0;

        availableSetsForConditions.clear();
        if (sets != null)
        {
            availableSetsForConditions.addAll(sets);
        }

        changeView(VIEW_UNLOCK_CONDITIONS);
    }

    private void removeSelectedRequiredMods()
    {
        if (selectedRequiredModsForRemoval.isEmpty())
        {
            return;
        }

        requiredModsEditorMods.removeAll(new ArrayList<String>(selectedRequiredModsForRemoval));
        selectedRequiredModsForRemoval.clear();
        statusMessage = StatCollector.translateToLocal("gui.oneblockultima.config.required_mods_removed");
        statusTimer = 60;
    }

    private void addSelectedRequiredMods()
    {
        if (selectedRequiredModsToAdd.isEmpty())
        {
            return;
        }

        for (String modId : new ArrayList<String>(selectedRequiredModsToAdd))
        {
            if (!requiredModsEditorMods.contains(modId))
            {
                requiredModsEditorMods.add(modId);
            }
        }
        selectedRequiredModsToAdd.clear();
        statusMessage = StatCollector.translateToLocal("gui.oneblockultima.config.required_mods_added");
        statusTimer = 60;
        changeView(VIEW_REQUIRED_MODS_EDITOR);
    }

    private void commitRequiredModsEditor()
    {
        if (editingSet == null)
        {
            return;
        }

        if (editingSet.requiredMods == null)
        {
            editingSet.requiredMods = new BlockSetConfig.SetRequiredModsDefinition();
        }
        editingSet.requiredMods.setType(requiredModsEditorType);
        editingSet.requiredMods.clear();
        for (String modId : requiredModsEditorMods)
        {
            editingSet.requiredMods.addMod(modId);
        }
        editingSet.computedLevels = null;
        statusMessage = StatCollector.translateToLocal("gui.oneblockultima.config.required_mods_saved");
        statusTimer = 60;
        changeView(VIEW_SET_DETAILS);
    }

    private void initUnlockConditionsView()
    {
        if (unlockConditionsLevelField == null)
        {
            unlockConditionsLevelField = new GuiTextField(fontRendererObj, 0, 0, 0, btnHeight);
            unlockConditionsCountField = new GuiTextField(fontRendererObj, 0, 0, 0, btnHeight);
        }

        unlockConditionsLevelFieldVisible = false;
        unlockConditionsCountFieldVisible = false;
        unlockConditionsLevelField.setFocused(false);
        unlockConditionsCountField.setFocused(false);

        boolean needsSet = "set_level".equals(newConditionTypeToAdd) || "broken_blocks".equals(newConditionTypeToAdd);
        boolean canAdd = canAddNewCondition(newConditionTypeToAdd);

        String toggleLabel = "any".equalsIgnoreCase(unlockConditionsEditorMode)
                ? StatCollector.translateToLocal("gui.oneblockultima.config.any")
                : StatCollector.translateToLocal("gui.oneblockultima.config.all");
        String cycleTypeLabel = StatCollector.translateToLocal("gui.oneblockultima.config.unlock_conditions_type_" + newConditionTypeToAdd);
        String addLabel = StatCollector.translateToLocal("gui.oneblockultima.config.add");
        String deleteLabel = StatCollector.translateToLocal("gui.oneblockultima.config.remove");
        String backLabel = StatCollector.translateToLocal("gui.oneblockultima.settings.back");
        String saveLabel = StatCollector.translateToLocal("gui.oneblockultima.save");

        int toggleBtnW = fontRendererObj.getStringWidth(toggleLabel) + pad * 2;
        int typeBtnW = fontRendererObj.getStringWidth(cycleTypeLabel) + pad * 2;
        int addBtnW = fontRendererObj.getStringWidth(addLabel) + pad * 2;
        int smallBtnW = Math.max(fontRendererObj.getStringWidth(deleteLabel), fontRendererObj.getStringWidth(backLabel));
        smallBtnW = Math.max(smallBtnW, fontRendererObj.getStringWidth(saveLabel)) + pad * 2;

        int row1Y = pad + textHeight + gap;
        int row1TotalW = toggleBtnW + gap + typeBtnW;
        int row1StartX = (width - row1TotalW) / 2;

        unlockConditionsToggleButton = new GuiButton(BUTTON_UNLOCK_CONDITIONS_TOGGLE, row1StartX, row1Y, toggleBtnW, btnHeight, toggleLabel);
        unlockConditionsCycleTypeButton = new GuiButton(BUTTON_UNLOCK_CONDITIONS_CYCLE_TYPE, row1StartX + toggleBtnW + gap, row1Y, typeBtnW, btnHeight, cycleTypeLabel);

        buttonList.add(unlockConditionsToggleButton);
        buttonList.add(unlockConditionsCycleTypeButton);

        int fieldWidth = Math.max(60, Math.min(100, width / 8));
        int row2Y = row1Y + btnHeight + gap;

        if (needsSet)
        {
            List<BlockSetConfig.BlockSetDefinition> availableSets = getAvailableSetsForType(newConditionTypeToAdd);
            if (newConditionSetId.isEmpty() && !availableSets.isEmpty())
            {
                newConditionSetId = availableSets.get(0).id;
            }

            String cycleSetLabel = getSetNameForConditions(newConditionSetId);
            int setBtnW = Math.max(fontRendererObj.getStringWidth(cycleSetLabel) + pad * 2, 80);
            String valueLabelText = "set_level".equals(newConditionTypeToAdd)
                    ? StatCollector.translateToLocal("gui.oneblockultima.config.unlock_conditions_level") + ":"
                    : StatCollector.translateToLocal("gui.oneblockultima.config.unlock_conditions_count") + ":";
            int valueLabelW = fontRendererObj.getStringWidth(valueLabelText) + gap;
            int row2TotalW = setBtnW + gap + valueLabelW + fieldWidth + gap + addBtnW;
            int row2StartX = (width - row2TotalW) / 2;

            unlockConditionsCycleSetButton = new GuiButton(BUTTON_UNLOCK_CONDITIONS_CYCLE_SET, row2StartX, row2Y, setBtnW, btnHeight, cycleSetLabel);
            unlockConditionsCycleSetButton.enabled = !availableSets.isEmpty();
            buttonList.add(unlockConditionsCycleSetButton);

            int valueFieldX = row2StartX + setBtnW + gap + valueLabelW;
            if ("set_level".equals(newConditionTypeToAdd))
            {
                unlockConditionsLevelField.xPosition = valueFieldX;
                unlockConditionsLevelField.yPosition = row2Y;
                unlockConditionsLevelField.width = fieldWidth;
                unlockConditionsLevelField.height = btnHeight;
                unlockConditionsLevelFieldVisible = true;
                unlockConditionsLevelField.setText("1");
                unlockConditionsLevelField.setFocused(true);
                unlockConditionsCountFieldVisible = false;
            }
            else
            {
                unlockConditionsCountField.xPosition = valueFieldX;
                unlockConditionsCountField.yPosition = row2Y;
                unlockConditionsCountField.width = fieldWidth;
                unlockConditionsCountField.height = btnHeight;
                unlockConditionsCountFieldVisible = true;
                unlockConditionsCountField.setText("1");
                unlockConditionsCountField.setFocused(true);
                unlockConditionsLevelFieldVisible = false;
            }

            unlockConditionsAddButton = new GuiButton(BUTTON_UNLOCK_CONDITIONS_ADD, valueFieldX + fieldWidth + gap, row2Y, addBtnW, btnHeight, addLabel);
            unlockConditionsAddButton.enabled = canAdd;
            buttonList.add(unlockConditionsAddButton);
        }
        else
        {
            unlockConditionsCycleSetButton = null;

            String valueLabelText = StatCollector.translateToLocal("gui.oneblockultima.config.unlock_conditions_count") + ":";
            int valueLabelW = fontRendererObj.getStringWidth(valueLabelText) + gap;
            int row2TotalW = valueLabelW + fieldWidth + gap + addBtnW;
            int row2StartX = (width - row2TotalW) / 2;

            unlockConditionsCountField.xPosition = row2StartX + valueLabelW;
            unlockConditionsCountField.yPosition = row2Y;
            unlockConditionsCountField.width = fieldWidth;
            unlockConditionsCountField.height = btnHeight;
            unlockConditionsCountFieldVisible = true;
            unlockConditionsCountField.setText("1");
            unlockConditionsCountField.setFocused(true);
            unlockConditionsLevelFieldVisible = false;

            unlockConditionsAddButton = new GuiButton(BUTTON_UNLOCK_CONDITIONS_ADD, unlockConditionsCountField.xPosition + fieldWidth + gap, row2Y, addBtnW, btnHeight, addLabel);
            unlockConditionsAddButton.enabled = canAdd;
            buttonList.add(unlockConditionsAddButton);
        }

        int bottomY = height - pad - gap - btnHeight;
        int bottomTotalW = smallBtnW * 3 + gap * 2;
        int bottomStartX = (width - bottomTotalW) / 2;

        unlockConditionsDeleteButton = new GuiButton(BUTTON_UNLOCK_CONDITIONS_DELETE, bottomStartX, bottomY, smallBtnW, btnHeight, deleteLabel);
        unlockConditionsDeleteButton.enabled = selectedUnlockConditionIndex >= 0 && selectedUnlockConditionIndex < unlockConditionsEditorConditions.size();
        unlockConditionsSaveButton = new GuiButton(BUTTON_UNLOCK_CONDITIONS_SAVE, bottomStartX + smallBtnW + gap, bottomY, smallBtnW, btnHeight, saveLabel);
        unlockConditionsBackButton = new GuiButton(BUTTON_UNLOCK_CONDITIONS_BACK, bottomStartX + (smallBtnW + gap) * 2, bottomY, smallBtnW, btnHeight, backLabel);

        buttonList.add(unlockConditionsDeleteButton);
        buttonList.add(unlockConditionsSaveButton);
        buttonList.add(unlockConditionsBackButton);
    }

    private boolean canAddNewCondition(String type)
    {
        if (type == null) return false;
        if ("broken_blocks_total".equals(type))
        {
            for (BlockSetConfig.UnlockConditionDefinition c : unlockConditionsEditorConditions)
            {
                if ("broken_blocks_total".equals(c.type)) return false;
            }
            return true;
        }
        else if ("broken_blocks".equals(type) || "set_level".equals(type))
        {
            return !getAvailableSetsForType(type).isEmpty();
        }
        else
        {
            return false;
        }
    }

    private List<BlockSetConfig.BlockSetDefinition> getAvailableSetsForType(String type)
    {
        Set<String> usedSetIds = new HashSet<String>();
        for (BlockSetConfig.UnlockConditionDefinition c : unlockConditionsEditorConditions)
        {
            if (type.equals(c.type) && c.setId != null && !c.setId.isEmpty())
            {
                usedSetIds.add(c.setId);
            }
        }
        List<BlockSetConfig.BlockSetDefinition> result = new ArrayList<BlockSetConfig.BlockSetDefinition>();
        for (BlockSetConfig.BlockSetDefinition set : availableSetsForConditions)
        {
            if (set != null && !usedSetIds.contains(set.id))
            {
                result.add(set);
            }
        }
        return result;
    }

    private int getUnlockConditionsToolbarHeight()
    {
        int row1Y = pad + textHeight + gap;
        int row2Y = row1Y + btnHeight + gap;
        return row2Y + btnHeight + gap;
    }

    private void commitUnlockConditionsEditor()
    {
        if (editingSet == null) return;

        if (editingSet.unlockConditions == null)
        {
            editingSet.unlockConditions = new BlockSetConfig.UnlockConditionGroup();
        }
        editingSet.unlockConditions.mode = unlockConditionsEditorMode;
        editingSet.unlockConditions.conditions = new ArrayList<BlockSetConfig.UnlockConditionDefinition>();
        for (BlockSetConfig.UnlockConditionDefinition cond : unlockConditionsEditorConditions)
        {
            if (cond == null) continue;
            BlockSetConfig.UnlockConditionDefinition copy = new BlockSetConfig.UnlockConditionDefinition();
            copy.type = cond.type;
            copy.setId = cond.setId;
            copy.level = cond.level;
            copy.count = cond.count;
            editingSet.unlockConditions.conditions.add(copy);
        }
        if (editingSet.unlockConditions.conditions.isEmpty())
        {
            editingSet.unlockConditions = null;
        }
        editingSet.computedLevels = null;
        statusMessage = StatCollector.translateToLocal("gui.oneblockultima.config.unlock_conditions_saved");
        statusTimer = 60;
        changeView(VIEW_SET_DETAILS);
    }

    private void drawUnlockConditionsView(int mouseX, int mouseY)
    {
        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.config.unlock_conditions_title"), width / 2, pad + textHeight / 2, 0xFFFFFF);

        int listX = pad + gap;
        int listY = getUnlockConditionsToolbarHeight();
        int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
        int listHeight = height - listY - pad - btnHeight - gap * 2;

        drawRect(listX, listY, listX + listWidth, listY + listHeight, 0xFF1A1F24);

        if (unlockConditionsEditorConditions.isEmpty())
        {
            drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.config.unlock_conditions_empty"), width / 2, listY + listHeight / 2, 0x808080);
        }
        else
        {
            int visibleEntries = listHeight / entryHeight;
            if (visibleEntries < 1) visibleEntries = 1;
            int maxScroll = Math.max(0, unlockConditionsEditorConditions.size() - visibleEntries);
            if (unlockConditionsScrollOffset > maxScroll) unlockConditionsScrollOffset = maxScroll;

            for (int i = unlockConditionsScrollOffset; i < Math.min(unlockConditionsEditorConditions.size(), unlockConditionsScrollOffset + visibleEntries); i++)
            {
                BlockSetConfig.UnlockConditionDefinition cond = unlockConditionsEditorConditions.get(i);
                int entryY = listY + innerPadding + (i - unlockConditionsScrollOffset) * entryHeight;
                boolean isSelected = (i == selectedUnlockConditionIndex);
                int bgColor = isSelected ? 0xFF3F5060 : (i % 2 == 0 ? 0xFF2A2F34 : 0xFF22272E);
                drawRect(listX + innerPadding, entryY, listX + listWidth - innerPadding - scrollWidth, entryY + entryHeight, bgColor);

                String typeLabel = getConditionTypeLabel(cond.type);
                String setText = "";
                String valueText = "";

                String type = cond.type != null ? cond.type.toLowerCase(Locale.ROOT) : "";
                if ("set_level".equals(type))
                {
                    setText = getSetNameForConditions(cond.setId);
                    valueText = String.valueOf(cond.level);
                }
                else if ("broken_blocks".equals(type))
                {
                    setText = getSetNameForConditions(cond.setId);
                    valueText = String.valueOf(cond.count);
                }
                else if ("broken_blocks_total".equals(type))
                {
                    valueText = String.valueOf(cond.count);
                }

                int textX = listX + gap;
                fontRendererObj.drawString(typeLabel, textX, entryY + innerPadding, 0xA0A0A0);
                if (!setText.isEmpty())
                {
                    int typeWidth = fontRendererObj.getStringWidth(typeLabel + "  ");
                    fontRendererObj.drawString(setText, textX + typeWidth, entryY + innerPadding, 0x808080);
                }
                int valueWidth = fontRendererObj.getStringWidth(valueText);
                fontRendererObj.drawString(valueText, listX + listWidth - gap - valueWidth - scrollWidth, entryY + innerPadding, 0xA0A0A0);
            }

            if (unlockConditionsEditorConditions.size() > visibleEntries)
            {
                int scrollbarX = listX + listWidth - gap;
                int scrollbarY = listY + innerPadding;
                int scrollbarHeight = listHeight - innerPadding * 2;
                int thumbHeight = Math.max(10, scrollbarHeight * visibleEntries / unlockConditionsEditorConditions.size());
                int thumbY = scrollbarY + (unlockConditionsScrollOffset * (scrollbarHeight - thumbHeight) / Math.max(1, maxScroll));
                drawRect(scrollbarX, scrollbarY, scrollbarX + scrollWidth, scrollbarY + scrollbarHeight, 0xFF2A2F34);
                drawRect(scrollbarX, thumbY, scrollbarX + scrollWidth, thumbY + thumbHeight, 0xFF7A7F84);
            }
        }

        if (unlockConditionsLevelField != null && unlockConditionsLevelFieldVisible)
        {
            boolean needsSet = "set_level".equals(newConditionTypeToAdd);
            String valueLabelText = needsSet
                    ? StatCollector.translateToLocal("gui.oneblockultima.config.unlock_conditions_level") + ":"
                    : StatCollector.translateToLocal("gui.oneblockultima.config.unlock_conditions_count") + ":";
            drawString(fontRendererObj, valueLabelText, unlockConditionsLevelField.xPosition - fontRendererObj.getStringWidth(valueLabelText) - gap, unlockConditionsLevelField.yPosition + btnHeight / 2 - textHeight / 2, 0xA0A0A0);
            unlockConditionsLevelField.drawTextBox();
        }
        if (unlockConditionsCountField != null && unlockConditionsCountFieldVisible)
        {
            if (!"set_level".equals(newConditionTypeToAdd) || unlockConditionsCycleSetButton == null)
            {
                String valueLabelText = StatCollector.translateToLocal("gui.oneblockultima.config.unlock_conditions_count") + ":";
                drawString(fontRendererObj, valueLabelText, unlockConditionsCountField.xPosition - fontRendererObj.getStringWidth(valueLabelText) - gap, unlockConditionsCountField.yPosition + btnHeight / 2 - textHeight / 2, 0xA0A0A0);
            }
            unlockConditionsCountField.drawTextBox();
        }
    }

    private boolean handleUnlockConditionsViewClick(int mouseX, int mouseY)
    {
        int listX = pad + gap;
        int listY = getUnlockConditionsToolbarHeight();
        int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
        int listHeight = height - listY - pad - btnHeight - gap;

        if (mouseX < listX || mouseX > listX + listWidth || mouseY < listY || mouseY > listY + listHeight)
        {
            return false;
        }

        int visibleEntries = listHeight / entryHeight;
        if (visibleEntries < 1) visibleEntries = 1;
        int row = (mouseY - listY - innerPadding) / entryHeight;
        int index = unlockConditionsScrollOffset + row;

        if (index < 0 || index >= unlockConditionsEditorConditions.size())
        {
            selectedUnlockConditionIndex = -1;
            if (unlockConditionsDeleteButton != null) unlockConditionsDeleteButton.enabled = false;
            return true;
        }

        if (index == selectedUnlockConditionIndex)
        {
            selectedUnlockConditionIndex = -1;
        }
        else
        {
            selectedUnlockConditionIndex = index;
        }
        if (unlockConditionsDeleteButton != null)
        {
            unlockConditionsDeleteButton.enabled = selectedUnlockConditionIndex >= 0;
        }
        return true;
    }

    private String getConditionTypeLabel(String type)
    {
        if (type == null) return "";
        String lowerType = type.toLowerCase(Locale.ROOT);
        if ("set_level".equals(lowerType)) return StatCollector.translateToLocal("gui.oneblockultima.config.unlock_conditions_type_set_level");
        if ("broken_blocks".equals(lowerType)) return StatCollector.translateToLocal("gui.oneblockultima.config.unlock_conditions_type_broken_blocks");
        if ("broken_blocks_total".equals(lowerType)) return StatCollector.translateToLocal("gui.oneblockultima.config.unlock_conditions_type_broken_blocks_total");
        return type;
    }

    private String getSetNameForConditions(String setId)
    {
        if (setId == null || setId.isEmpty()) return "-";
        for (BlockSetConfig.BlockSetDefinition set : availableSetsForConditions)
        {
            if (set != null && setId.equals(set.id))
            {
                return getLocalizedSetName(set);
            }
        }
        return setId;
    }

    private void addNewUnlockCondition()
    {
        if (!canAddNewCondition(newConditionTypeToAdd)) return;

        BlockSetConfig.UnlockConditionDefinition cond = new BlockSetConfig.UnlockConditionDefinition();
        cond.type = newConditionTypeToAdd;

        if ("set_level".equals(newConditionTypeToAdd) || "broken_blocks".equals(newConditionTypeToAdd))
        {
            cond.setId = newConditionSetId;
            if (cond.setId == null || cond.setId.isEmpty())
            {
                List<BlockSetConfig.BlockSetDefinition> available = getAvailableSetsForType(newConditionTypeToAdd);
                if (!available.isEmpty()) cond.setId = available.get(0).id;
                else return;
            }
        }

        if ("set_level".equals(newConditionTypeToAdd))
        {
            try { cond.level = Math.max(1, Integer.parseInt(unlockConditionsLevelField.getText().trim())); }
            catch (NumberFormatException e) { cond.level = 1; }
        }
        else
        {
            try { cond.count = Math.max(1, Integer.parseInt(unlockConditionsCountField.getText().trim())); }
            catch (NumberFormatException e) { cond.count = 1; }
        }

        unlockConditionsEditorConditions.add(cond);
        newConditionSetId = "";
        initGui();
    }

    private void cycleNewConditionType()
    {
        if ("broken_blocks_total".equals(newConditionTypeToAdd))
        {
            newConditionTypeToAdd = "broken_blocks";
        }
        else if ("broken_blocks".equals(newConditionTypeToAdd))
        {
            newConditionTypeToAdd = "set_level";
        }
        else
        {
            newConditionTypeToAdd = "broken_blocks_total";
        }
        newConditionSetId = "";
        initGui();
    }

    private void cycleNewConditionSet()
    {
        List<BlockSetConfig.BlockSetDefinition> available = getAvailableSetsForType(newConditionTypeToAdd);
        if (available.isEmpty()) return;

        int currentIndex = -1;
        for (int i = 0; i < available.size(); i++)
        {
            if (newConditionSetId.equals(available.get(i).id))
            {
                currentIndex = i;
                break;
            }
        }
        int nextIndex = (currentIndex + 1) % available.size();
        newConditionSetId = available.get(nextIndex).id;

        if (unlockConditionsCycleSetButton != null)
        {
            unlockConditionsCycleSetButton.displayString = getSetNameForConditions(newConditionSetId);
        }
    }

    private void removeSelectedUnlockCondition()
    {
        if (selectedUnlockConditionIndex < 0 || selectedUnlockConditionIndex >= unlockConditionsEditorConditions.size()) return;
        unlockConditionsEditorConditions.remove(selectedUnlockConditionIndex);
        if (selectedUnlockConditionIndex >= unlockConditionsEditorConditions.size())
        {
            selectedUnlockConditionIndex = unlockConditionsEditorConditions.size() - 1;
        }
        newConditionSetId = "";
        initGui();
    }

    private void selectRequiredModForRemoval(String modId)
    {
        if (modId == null || modId.isEmpty())
        {
            return;
        }

        if (selectedRequiredModsForRemoval.contains(modId))
        {
            selectedRequiredModsForRemoval.remove(modId);
        }
        else
        {
            selectedRequiredModsForRemoval.add(modId);
        }
    }

    private void selectRequiredModToAdd(String modId)
    {
        if (modId == null || modId.isEmpty())
        {
            return;
        }

        if (selectedRequiredModsToAdd.contains(modId))
        {
            selectedRequiredModsToAdd.remove(modId);
        }
        else
        {
            selectedRequiredModsToAdd.add(modId);
        }
    }

    private void syncEditingSetFromFields()
    {
        if (editingSet == null)
        {
            return;
        }

        if (setNameField != null)
        {
            String name = setNameField.getText().trim();
            if (!name.isEmpty())
            {
                saveLocalizedName(editingSet.id, name);
            }
        }

        applyRequiredModsFromFields();

        if (unlockCostField != null)
        {
            try
            {
                editingSet.unlockCost = Integer.parseInt(unlockCostField.getText().trim());
            }
            catch (NumberFormatException ignored)
            {
                editingSet.unlockCost = 0;
            }
        }
    }

    private Fluid getFluidForRegistry(String registry)
    {
        try
        {
            net.minecraft.block.Block block = (net.minecraft.block.Block) Block.blockRegistry.getObject(registry);
            if (block == null || block == net.minecraft.init.Blocks.air)
            {
                return null;
            }
            if (block instanceof IFluidBlock)
            {
                return ((IFluidBlock) block).getFluid();
            }
            return FluidRegistry.lookupFluidForBlock(block);
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

    private void renderFluidIcon(Fluid fluid, int x, int y, int size)
    {
        if (fluid == null)
        {
            return;
        }
        ModelUtil.renderFluidSprite(fluid, x, y, size, size);
    }

    public GuiSetsConfig(GuiScreen parent)
    {
        this.parent = parent;
        this.currentView = VIEW_SETS;
        loadCustomNames();
    }

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();

        textHeight = fontRendererObj.FONT_HEIGHT;
        iconSize = btnHeight - innerPadding * 2;
        entryHeight = btnHeight + innerPadding * 2;

        formMargin = Math.max(10, Math.min(24, width / 24));
        formWidth = Math.max(220, width - formMargin * 2);
        formLabelWidth = Math.max(70, Math.min(120, formWidth / 4));
        formFieldX = formMargin + formLabelWidth + gap;
        formFieldWidth = Math.max(120, formWidth - formLabelWidth - gap);

        if (config == null)
        {
            BlockSetConfig.reload();
            config = BlockSetConfig.get();
        }

        sets.clear();
        sets.addAll(config.getSets());
        updateFilteredSets();

        loadStaticCustomNames();

        switch (currentView)
        {
            case VIEW_SETS:
                initSetsView();
                break;
            case VIEW_SET_DETAILS:
                initSetDetailsView();
                break;
            case VIEW_ADD_ENTRY:
                initAddEntryView();
                performSearch();
                break;
            case VIEW_CONFIRM_DELETE:
                initConfirmDeleteView();
                break;
            case VIEW_EDIT_CURRENCY:
                initEditCurrencyView();
                break;
            case VIEW_REQUIRED_MODS_EDITOR:
                initRequiredModsView();
                break;
            case VIEW_REQUIRED_MODS_ADD:
                initRequiredModsAddView();
                break;
            case VIEW_UNLOCK_CONDITIONS:
                initUnlockConditionsView();
                break;
        }

        if (statusTimer > 0)
        {
            statusTimer--;
            if (statusTimer == 0) statusMessage = "";
        }
    }

    private void initSetsView()
    {
        reloadSetsFromConfig();
        selectedSetIndex = -1;

        int searchWidth = Math.min(width / 3, 250);
        int topY = pad + textHeight + pad;

        if (searchField == null)
        {
            searchField = new GuiTextField(fontRendererObj, pad, topY, searchWidth, btnHeight);
            searchField.setMaxStringLength(100);
        }
        searchField.xPosition = pad + gap;
        searchField.yPosition = topY;
        searchField.width = searchWidth;
        searchField.height = btnHeight;

        int addWidth = fontRendererObj.getStringWidth(StatCollector.translateToLocal("gui.oneblockultima.config.add_set")) + pad * 2;
        addSetButton = new GuiButton(BUTTON_ADD_SET, pad + searchField.xPosition + searchWidth, topY, addWidth, btnHeight, StatCollector.translateToLocal("gui.oneblockultima.config.add_set"));

        int resetWidth = fontRendererObj.getStringWidth(StatCollector.translateToLocal("gui.oneblockultima.reset_default")) + pad * 2;
        int backWidth = fontRendererObj.getStringWidth(StatCollector.translateToLocal("gui.oneblockultima.settings.back")) + pad * 2;

        resetButton = new GuiButton(BUTTON_RESET, width - pad - backWidth - resetWidth - gap * 2, topY, resetWidth, btnHeight, StatCollector.translateToLocal("gui.oneblockultima.reset_default"));
        backButton = new GuiButton(BUTTON_BACK, width - pad - backWidth, topY, backWidth - gap, btnHeight, StatCollector.translateToLocal("gui.oneblockultima.settings.back"));

        buttonList.add(addSetButton);
        buttonList.add(resetButton);
        buttonList.add(backButton);
    }

    private void initSetDetailsView()
    {
        int currentY = pad + textHeight + pad;
        int fieldX = formFieldX;
        int fieldWidth = formFieldWidth;

        if (setNameField == null)
        {
            setNameField = new GuiTextField(fontRendererObj, 0, 0, 0, btnHeight);
            setIdField = new GuiTextField(fontRendererObj, 0, 0, 0, btnHeight);
            unlockCostField = new GuiTextField(fontRendererObj, 0, 0, 0, btnHeight);
            requiredModsField = new GuiTextField(fontRendererObj, 0, 0, 0, btnHeight);
        }

        if (isNewSet)
        {
            setNameField.setText(savedNewSetName);
            setIdField.setText(savedNewSetId);
            unlockCostField.setText(savedNewSetCost);
            if (requiredModsField != null) { // ← Проверка на null
                requiredModsField.setText(savedNewSetMods);
            }
        }

        setNameField.xPosition = fieldX;
        setNameField.yPosition = currentY;
        setNameField.width = fieldWidth;
        setNameField.height = btnHeight;

        currentY += btnHeight + gap;
        setIdField.xPosition = fieldX;
        setIdField.yPosition = currentY;
        setIdField.width = fieldWidth;
        setIdField.height = btnHeight;
        setIdField.setEnabled(isNewSet);

        currentY += btnHeight + gap;
        unlockCostField.xPosition = fieldX;
        unlockCostField.yPosition = currentY;
        unlockCostField.width = fieldWidth / 2;
        unlockCostField.height = btnHeight;

        currentY += btnHeight + gap;
        int buttonWidth = Math.max(160, Math.min(260, formFieldWidth));
        int halfButtonWidth = (buttonWidth - gap) / 2;
        editRequiredModsButton = new GuiButton(
                BUTTON_EDIT_REQUIRED_MODS,
                fieldX,
                currentY,
                halfButtonWidth,
                btnHeight,
                getRequiredModsButtonLabel(editingSet != null ? editingSet.requiredMods : null));

        editUnlockConditionsButton = new GuiButton(
                BUTTON_EDIT_UNLOCK_CONDITIONS,
                fieldX + halfButtonWidth + gap,
                currentY,
                halfButtonWidth,
                btnHeight,
                getUnlockConditionsButtonLabel(editingSet != null ? editingSet.unlockConditions : null));

        if (!isNewSet && editingSet != null)
        {
            setNameField.setText(getLocalizedSetName(editingSet));
            setIdField.setText(editingSet.id);
            unlockCostField.setText(String.valueOf(editingSet.unlockCost));
        }

        setNameField.setFocused(true);
        if (setIdField != null)
        {
            setIdField.setFocused(false);
        }

        buttonList.add(editRequiredModsButton);
        buttonList.add(editUnlockConditionsButton);

        int footerY = Math.max(pad + btnHeight + gap, height - pad - btnHeight - pad);
        int btnWidth = 0;
        String[] labels = {
                StatCollector.translateToLocal("gui.oneblockultima.settings.back"),
                StatCollector.translateToLocal("gui.oneblockultima.save"),
                StatCollector.translateToLocal("gui.oneblockultima.config.add_block"),
                StatCollector.translateToLocal("gui.oneblockultima.config.add_mob"),
                StatCollector.translateToLocal("gui.oneblockultima.config.remove")
        };
        for (String s : labels) {
            btnWidth = Math.max(btnWidth, fontRendererObj.getStringWidth(s) + pad * 2);
        }

        int startX = (width - (btnWidth * 5 + gap * 4)) / 2;

        backButton = new GuiButton(BUTTON_BACK, startX, footerY, btnWidth, btnHeight, labels[0]);
        saveButton = new GuiButton(BUTTON_SAVE, startX + btnWidth + gap, footerY, btnWidth, btnHeight, labels[1]);
        addBlockButton = new GuiButton(BUTTON_ADD_BLOCK, startX + (btnWidth + gap) * 2, footerY, btnWidth, btnHeight, labels[2]);
        addMobButton = new GuiButton(BUTTON_ADD_MOB, startX + (btnWidth + gap) * 3, footerY, btnWidth, btnHeight, labels[3]);
        removeEntryButton = new GuiButton(BUTTON_REMOVE_ENTRY, startX + (btnWidth + gap) * 4, footerY, btnWidth, btnHeight, labels[4]);

        buttonList.add(backButton);
        buttonList.add(saveButton);
        buttonList.add(addBlockButton);
        buttonList.add(addMobButton);
        buttonList.add(removeEntryButton);
    }

    private void initRequiredModsView()
    {
        if (editingSet == null)
        {
            changeView(VIEW_SET_DETAILS);
            return;
        }

        if (!requiredModsEditorInitialized)
        {
            requiredModsEditorMods.clear();
            if (editingSet.requiredMods != null)
            {
                requiredModsEditorType = editingSet.requiredMods.getType();
                requiredModsEditorMods.addAll(editingSet.requiredMods.getMods());
            }
            else
            {
                requiredModsEditorType = BlockSetConfig.SetRequiredModsDefinition.TYPE.ALL;
            }
            requiredModsEditorInitialized = true;
        }
        requiredModsScrollOffset = 0;

        int footerY = height - pad - btnHeight - pad;
        int footerBtnWidth = Math.max(90, Math.min(120, width / 5));
        int addX = width / 2 - footerBtnWidth - gap / 2;
        int removeX = width / 2 + gap / 2;
        int saveX = width - pad - footerBtnWidth - gap;

        requiredModsToggleButton = new GuiButton(BUTTON_REQUIRED_MODS_TOGGLE, pad + gap, pad * 2 + textHeight + gap, footerBtnWidth, btnHeight, getRequiredModsEditorTypeLabel());
        requiredModsBackButton = new GuiButton(BUTTON_REQUIRED_MODS_BACK, pad + gap, footerY, footerBtnWidth, btnHeight, StatCollector.translateToLocal("gui.oneblockultima.settings.back"));
        requiredModsAddButton = new GuiButton(BUTTON_REQUIRED_MODS_ADD, addX, footerY, footerBtnWidth, btnHeight, StatCollector.translateToLocal("gui.oneblockultima.config.add"));
        requiredModsDeleteButton = new GuiButton(BUTTON_REQUIRED_MODS_DELETE, removeX, footerY, footerBtnWidth, btnHeight, StatCollector.translateToLocal("gui.oneblockultima.config.remove"));
        requiredModsSaveButton = new GuiButton(BUTTON_REQUIRED_MODS_SAVE, saveX, footerY, footerBtnWidth, btnHeight, StatCollector.translateToLocal("gui.oneblockultima.save"));

        buttonList.add(requiredModsToggleButton);
        buttonList.add(requiredModsBackButton);
        buttonList.add(requiredModsAddButton);
        buttonList.add(requiredModsDeleteButton);
        buttonList.add(requiredModsSaveButton);
    }

    private void initRequiredModsAddView()
    {
        requiredModsScrollOffset = 0;
        selectedRequiredModsToAdd.clear();

        int footerY = height - pad - btnHeight - pad;
        int footerBtnWidth = Math.max(90, Math.min(120, width / 4));
        int addX = width - pad - footerBtnWidth - gap;

        requiredModsBackButton = new GuiButton(BUTTON_REQUIRED_MODS_BACK, pad + gap, footerY, footerBtnWidth, btnHeight, StatCollector.translateToLocal("gui.oneblockultima.settings.back"));
        requiredModsAddButton = new GuiButton(BUTTON_REQUIRED_MODS_ADD, addX, footerY, footerBtnWidth, btnHeight, StatCollector.translateToLocal("gui.oneblockultima.config.add"));

        buttonList.add(requiredModsBackButton);
        buttonList.add(requiredModsAddButton);
    }

    private void initAddEntryView()
    {
        int topY = pad + textHeight + pad;

        if (entrySearchField == null)
        {
            entrySearchField = new GuiTextField(fontRendererObj, pad, topY, width - pad * 2, btnHeight);
        }
        entrySearchField.xPosition = pad + gap;
        entrySearchField.yPosition = topY;
        entrySearchField.width = width - (pad + gap) * 2;
        entrySearchField.height = btnHeight;
        entrySearchField.setFocused(true);

        int fieldsY = topY + btnHeight + gap + 6;
        int fieldHeight = btnHeight;
        int fieldWidthSmall = 60;
        int labelGap = 4;
        int levelLabelWidth = fontRendererObj.getStringWidth(StatCollector.translateToLocal("gui.oneblockultima.config.base_level") + ":");
        int chanceLabelWidth = fontRendererObj.getStringWidth(StatCollector.translateToLocal("gui.oneblockultima.chance") + ":");

        if (addLevelField == null)
        {
            addLevelField = new GuiTextField(fontRendererObj, 0, 0, 0, fieldHeight);
            addChanceField = new GuiTextField(fontRendererObj, 0, 0, 0, fieldHeight);
        }

        addLevelField.xPosition = pad + gap + levelLabelWidth + labelGap;
        addLevelField.yPosition = fieldsY;
        addLevelField.width = fieldWidthSmall;
        addLevelField.height = fieldHeight;
        if (addLevelField.getText().isEmpty())
        {
            addLevelField.setText("1");
        }

        addChanceField.xPosition = addLevelField.xPosition + fieldWidthSmall + gap * 2 + chanceLabelWidth + labelGap;
        addChanceField.yPosition = fieldsY;
        addChanceField.width = fieldWidthSmall;
        addChanceField.height = fieldHeight;
        if (addChanceField.getText().isEmpty())
        {
            addChanceField.setText("1");
        }

        if (currentEntryType == EntryType.BLOCK)
        {
            currentSearchType = SearchType.BLOCKS;
            if (currencyField == null)
            {
                currencyField = new GuiTextField(fontRendererObj, 0, 0, 0, fieldHeight);
            }
            int iconSizeLocal = 12;
            int currencyX = addChanceField.xPosition + fieldWidthSmall + gap * 2;
            currencyField.xPosition = currencyX + iconSizeLocal + gap;
            currencyField.yPosition = fieldsY;
            currencyField.width = fieldWidthSmall;
            currencyField.height = fieldHeight;
            if (currencyField.getText().isEmpty())
            {
                currencyField.setText("0");
            }
        }
        else
        {
            currentSearchType = SearchType.MOBS;
        }

        int footerY = height - pad - btnHeight - pad;
        int backWidth = fontRendererObj.getStringWidth(StatCollector.translateToLocal("gui.oneblockultima.settings.back")) + pad * 2;
        backButton = new GuiButton(BUTTON_BACK, pad + gap, footerY, backWidth, btnHeight, StatCollector.translateToLocal("gui.oneblockultima.settings.back"));

        buttonList.add(backButton);
    }

    private void initConfirmDeleteView()
    {
        int centerX = width / 2;
        int centerY = height / 2;

        String deleteName = deleteTargetIndex >= 0 && deleteTargetIndex < sets.size()
                ? getLocalizedSetName(sets.get(deleteTargetIndex))
                : "";
        String confirmLabel = StatCollector.translateToLocalFormatted("gui.oneblockultima.config.confirm", deleteName);
        String cancelLabel = StatCollector.translateToLocal("gui.oneblockultima.cancel");

        int btnWidth = Math.max(fontRendererObj.getStringWidth(confirmLabel) + pad * 2,
                fontRendererObj.getStringWidth(cancelLabel) + pad * 2);

        confirmDeleteButton = new GuiButton(BUTTON_CONFIRM_DELETE, centerX - btnWidth - gap / 2, centerY + btnHeight + gap, btnWidth, btnHeight, confirmLabel);
        cancelDeleteButton = new GuiButton(BUTTON_CANCEL, centerX + gap / 2, centerY + btnHeight + gap, btnWidth, btnHeight, cancelLabel);

        buttonList.add(confirmDeleteButton);
        buttonList.add(cancelDeleteButton);
    }

    private void initEditCurrencyView()
    {
        if (editCurrencyField == null)
        {
            editCurrencyField = new GuiTextField(fontRendererObj, 0, 0, 0, btnHeight);
            editLevelField = new GuiTextField(fontRendererObj, 0, 0, 0, btnHeight);
            editChanceField = new GuiTextField(fontRendererObj, 0, 0, 0, btnHeight);
        }

        int centerX = width / 2;
        int centerY = height / 2;
        int fieldWidth = 100;
        int fieldGap = btnHeight + gap;

        editLevelField.xPosition = centerX - fieldWidth / 2;
        editLevelField.yPosition = centerY - fieldGap;
        editLevelField.width = fieldWidth;
        editLevelField.height = btnHeight;

        editChanceField.xPosition = centerX - fieldWidth / 2;
        editChanceField.yPosition = centerY;
        editChanceField.width = fieldWidth;
        editChanceField.height = btnHeight;

        editCurrencyField.xPosition = centerX - fieldWidth / 2;
        editCurrencyField.yPosition = centerY + fieldGap;
        editCurrencyField.width = fieldWidth;
        editCurrencyField.height = btnHeight;
        editCurrencyFieldVisible = (editingEntryType == EntryType.BLOCK);

        if (editingEntryType == EntryType.BLOCK
                && editingSet != null
                && editingSet.blocks != null
                && editingCurrencyIndex >= 0
                && editingCurrencyIndex < editingSet.blocks.size())
        {
            BlockSetConfig.BlockElementDefinition entry = editingSet.blocks.get(editingCurrencyIndex);
            editLevelField.setText(String.valueOf(entry.baseLevel));
            editChanceField.setText(String.valueOf(entry.baseChance));
            editCurrencyField.setText(String.valueOf(entry.currency));
        }
        else if (editingEntryType == EntryType.MOB
                && editingSet != null
                && editingSet.mobs != null
                && editingCurrencyIndex >= 0
                && editingCurrencyIndex < editingSet.mobs.size())
        {
            BlockSetConfig.MobElementDefinition entry = editingSet.mobs.get(editingCurrencyIndex);
            editLevelField.setText(String.valueOf(entry.baseLevel));
            editChanceField.setText(String.valueOf(entry.baseChance));
            editCurrencyField.setText("0");
        }

        editLevelField.setFocused(true);
        editLevelField.setCursorPositionEnd();
        editChanceField.setFocused(false);
        editCurrencyField.setFocused(false);

        String saveLabel = StatCollector.translateToLocal("gui.oneblockultima.save");
        String cancelLabel = StatCollector.translateToLocal("gui.oneblockultima.cancel");
        int btnWidth = Math.max(fontRendererObj.getStringWidth(saveLabel) + pad * 2,
                fontRendererObj.getStringWidth(cancelLabel) + pad * 2);

        int lastFieldY = editingEntryType == EntryType.BLOCK ? editCurrencyField.yPosition : editChanceField.yPosition;

        saveCurrencyButton = new GuiButton(BUTTON_SAVE_CURRENCY, centerX - btnWidth - gap, lastFieldY + btnHeight + gap, btnWidth, btnHeight, saveLabel);
        cancelCurrencyButton = new GuiButton(BUTTON_CANCEL_CURRENCY, centerX + gap, lastFieldY + btnHeight + gap, btnWidth, btnHeight, cancelLabel);

        buttonList.add(saveCurrencyButton);
        buttonList.add(cancelCurrencyButton);
    }

    private void updateFilteredSets()
    {
        if (searchQuery.isEmpty())
        {
            filteredSets = new ArrayList<BlockSetConfig.BlockSetDefinition>(sets);
            return;
        }

        String query = searchQuery.toLowerCase(Locale.ROOT);
        filteredSets = new ArrayList<BlockSetConfig.BlockSetDefinition>();
        for (BlockSetConfig.BlockSetDefinition set : sets)
        {
            String name = getLocalizedSetName(set).toLowerCase(Locale.ROOT);
            if (name.contains(query) || set.id.toLowerCase(Locale.ROOT).contains(query))
            {
                filteredSets.add(set);
            }
        }
    }

    private String getLocalizedSetName(BlockSetConfig.BlockSetDefinition set) {
        return getLocalizedSetNameStatic(set);
    }

    private static Map<String, String> getOrCreateLangMap(Map<String, Map<String, String>> map, String key)
    {
        Map<String, String> result = map.get(key);
        if (result == null)
        {
            result = new HashMap<String, String>();
            map.put(key, result);
        }
        return result;
    }

    private static List<String> getOrCreateStringList(Map<String, List<String>> map, String key)
    {
        List<String> result = map.get(key);
        if (result == null)
        {
            result = new ArrayList<String>();
            map.put(key, result);
        }
        return result;
    }

    private static boolean matchesSearchTerms(String name, List<String> searchTerms)
    {
        String lowerName = name == null ? "" : name.toLowerCase(Locale.ROOT);

        if (searchTerms != null && !searchTerms.isEmpty())
        {
            for (String term : searchTerms)
            {
                if (term.isEmpty()) continue;
                boolean termMatched = lowerName.contains(term);
                if (!termMatched) return false;
            }
        }

        return true;
    }

    private void performSearch()
    {
        searchResults.clear();
        String query = entrySearchField != null ? entrySearchField.getText() : "";

        OneBlockUltima.getLogger().info("performSearch: query='" + query + "'");

        boolean emptyQuery = query.isEmpty();

        String[] parts = emptyQuery ? new String[0] : query.split(" ");
        List<String> searchTerms = new ArrayList<String>();
        String modFilter = null;
        String idFilter = null;

        for (String part : parts)
        {
            if (part.isEmpty()) continue;
            if (part.startsWith("@")) modFilter = part.substring(1).toLowerCase(Locale.ROOT);
            else if (part.startsWith("&")) idFilter = part.substring(1).toLowerCase(Locale.ROOT);
            else searchTerms.add(part.toLowerCase(Locale.ROOT));
        }

        if (currentSearchType == SearchType.BLOCKS)
        {
            Set<String> existingBlocks = getExistingBlockRegistries();
            for (Object obj : Block.blockRegistry)
            {
                net.minecraft.block.Block block = (net.minecraft.block.Block) obj;
                String regName = (String) Block.blockRegistry.getNameForObject(block);
                ResourceLocation reg = regName != null ? new ResourceLocation(regName) : null;
                if (reg == null) continue;

                String registry = reg.toString();
                String registryId = reg.getResourcePath();
                String modId = reg.getResourceDomain();

                if (modFilter != null && !modId.toLowerCase(Locale.ROOT).contains(modFilter)) continue;
                if (idFilter != null && !registryId.toLowerCase(Locale.ROOT).contains(idFilter)) continue;

                if (modId.equals(OneBlockUltima.MODID) && ("fluid_barrier".equals(registryId) || "custom_bedrock".equals(registryId) || "custom_end_portal_frame".equals(registryId))) continue;

                Fluid fluid = block instanceof IFluidBlock ? ((IFluidBlock) block).getFluid() : FluidRegistry.lookupFluidForBlock(block);
                if (fluid != null)
                {
                    if (existingBlocks.contains(registry))
                    {
                        continue;
                    }
                    String name = fluid.getLocalizedName(new FluidStack(fluid, 1000));

                    if (!emptyQuery && !searchTerms.isEmpty() && !matchesSearchTerms(name, searchTerms))
                    {
                        continue;
                    }

                    searchResults.add(new SearchResult(registry, name, modId, fluid));
                    continue;
                }

                Item item = Item.getItemFromBlock(block);
                if (item == null) continue;

                List subItems = new ArrayList();
                item.getSubItems(item, null, subItems);
                if (subItems.isEmpty())
                {
                    subItems.add(new ItemStack(item, 1, 0));
                }

                for (Object subObj : subItems)
                {
                    ItemStack subStack = (ItemStack) subObj;
                    if (subStack == null || subStack.getItem() != item) continue;

                    if (existingBlocks.contains(registry + ":" + subStack.getItemDamage()))
                    {
                        continue;
                    }

                    String name = "";
                    try { name = subStack.getDisplayName(); } catch (Exception ignored) {}
                    if (name == null || name.isEmpty() || name.contains(".name")) name = registry;

                    if (!emptyQuery && !searchTerms.isEmpty() && !matchesSearchTerms(name, searchTerms))
                    {
                        continue;
                    }

                    searchResults.add(new SearchResult(registry, name, modId, subStack.copy()));
                }
            }

            for (Object obj : Item.itemRegistry)
            {
                Item item = (Item) obj;
                String regNameItem = (String) Item.itemRegistry.getNameForObject(item);
                ResourceLocation reg = regNameItem != null ? new ResourceLocation(regNameItem) : null;
                if (reg == null) continue;
                if (item instanceof net.minecraft.item.ItemBlock) continue;
                if (item == null) continue;

                String registry = reg.toString();
                String registryId = reg.getResourcePath();
                String modId = reg.getResourceDomain();

                if (modFilter != null && !modId.toLowerCase(Locale.ROOT).contains(modFilter)) continue;
                if (idFilter != null && !registryId.toLowerCase(Locale.ROOT).contains(idFilter)) continue;

                String name = "";
                try { name = new ItemStack(item, 1).getDisplayName(); } catch (Exception ignored) {}
                if (name == null || name.isEmpty() || name.contains(".name")) name = registry;

                if (!emptyQuery && !searchTerms.isEmpty() && !matchesSearchTerms(name, searchTerms))
                {
                    continue;
                }

                searchResults.add(new SearchResult(registry, name, modId, new ItemStack(item, 1)));
            }
        }

        if (currentSearchType == SearchType.MOBS)
        {
            Set<String> existingMobs = getExistingMobRegistries();
            for (Object obj : EntityList.stringToClassMapping.keySet())
            {
                String entityName = (String) obj;
                String registry = "minecraft:" + entityName.toLowerCase(Locale.ROOT);
                if (existingMobs.contains(registry))
                {
                    continue;
                }
                String registryId = entityName.toLowerCase(Locale.ROOT);
                String modId = "minecraft";

                if (modFilter != null && !modId.toLowerCase(Locale.ROOT).contains(modFilter)) continue;
                if (idFilter != null && !registryId.contains(idFilter)) continue;

                String name = entityName;
                try
                {
                    String translationKey = "entity." + entityName + ".name";
                    String localized = StatCollector.translateToLocal(translationKey);
                    if (localized != null && !localized.equals(translationKey)) {
                        name = localized;
                    } else {
                        Entity entity = EntityList.createEntityByName(entityName, mc.theWorld);
                        if (entity != null) {
                            String displayName = entity.getCommandSenderName();
                            if (displayName != null && !displayName.isEmpty()) {
                                name = displayName;
                            }
                        }
                    }
                }
                catch (Exception ignored) {}

                if (!emptyQuery && !searchTerms.isEmpty() && !matchesSearchTerms(name, searchTerms))
                {
                    continue;
                }

                Class<?> entityClass = (Class<?>) EntityList.stringToClassMapping.get(entityName);
                if (entityClass != null && EntityLivingBase.class.isAssignableFrom(entityClass))
                {
                    searchResults.add(new SearchResult(registry, name, modId, entityClass));
                }
                }
            }

        Collections.sort(searchResults, new Comparator<SearchResult>() {
            @Override
            public int compare(SearchResult a, SearchResult b) {
                return a.name.compareToIgnoreCase(b.name);
            }
        });
        if (searchResults.size() > 200) searchResults = searchResults.subList(0, 200);

        OneBlockUltima.getLogger().info("performSearch: found " + searchResults.size() + " results");
    }

    private ItemStack getItemStackFromEntry(BlockSetConfig.BlockElementDefinition entry, int meta)
    {
        ItemStack stack = null;
        try {
            net.minecraft.block.Block block = (net.minecraft.block.Block) Block.blockRegistry.getObject(entry.registry);
            if (block != null && block != net.minecraft.init.Blocks.air) {
                Fluid fluid = getFluidForRegistry(entry.registry);
                if (fluid != null) {
                    return null;
                }
                net.minecraft.item.Item item = net.minecraft.item.Item.getItemFromBlock(block);
                if (item != null) {
                    stack = new ItemStack(item, 1, meta);
                }
            }
            if (stack == null) {
                net.minecraft.item.Item item = (net.minecraft.item.Item) Item.itemRegistry.getObject(entry.registry);
                if (item != null) {
                    stack = new ItemStack(item, 1, meta);
                }
            }
        } catch (Exception ignored) {}
        return stack;
    }

    private ItemStack getItemStackFromEntry(BlockSetConfig.BlockElementDefinition entry)
    {
        return getItemStackFromEntry(entry, entry.meta);
    }

    private String getLocalizedNameForBlock(BlockSetConfig.BlockElementDefinition entry, int meta)
    {
        try {
            net.minecraft.block.Block block = (net.minecraft.block.Block) Block.blockRegistry.getObject(entry.registry);
            if (block != null && block != net.minecraft.init.Blocks.air) {
                if (block instanceof IFluidBlock || FluidRegistry.lookupFluidForBlock(block) != null) {
                    Fluid fluid = block instanceof IFluidBlock ? ((IFluidBlock) block).getFluid() : FluidRegistry.lookupFluidForBlock(block);
                    if (fluid != null) {
                        FluidStack fluidStack = new FluidStack(fluid, 1000);
                        return fluid.getLocalizedName(fluidStack);
                    }
                }
                ItemStack stack = getItemStackFromEntry(entry, meta);
                if (stack != null) {
                    String name = stack.getDisplayName();
                    if (name != null && !name.isEmpty() && !name.contains(".name")) return name;
                }
            }
        } catch (Exception ignored) {}
        return entry.registry + ":" + meta;
    }

    private String getLocalizedNameForBlock(BlockSetConfig.BlockElementDefinition entry)
    {
        return getLocalizedNameForBlock(entry, entry.meta);
    }

    private String getLocalizedNameForMob(BlockSetConfig.MobElementDefinition entry)
    {
        try {
            String registry = entry.registry;
            String entityName = registry;
            if (entityName.contains(":")) {
                entityName = entityName.substring(entityName.indexOf(":") + 1);
            }

            String translationKey = "entity." + entityName + ".name";
            String name = StatCollector.translateToLocal(translationKey);
            if (name != null && !name.equals(translationKey)) {
                return name;
            }

            String capitalizedName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
            Entity entity = EntityList.createEntityByName(capitalizedName, mc.theWorld);
            if (entity != null) {
                String displayName = entity.getCommandSenderName();
                if (displayName != null && !displayName.isEmpty()) {
                    return displayName;
                }
            }
        } catch (Exception ignored) {}
        return entry.registry;
    }

    public static String getLocalizedSetNameStatic(BlockSetConfig.BlockSetDefinition set) {
        if (set == null || set.id == null) return "-";

        // Получаем текущий язык
        Minecraft mc = Minecraft.getMinecraft();
        String langCode = mc.getLanguageManager().getCurrentLanguage().getLanguageCode().toLowerCase();

        // Проверяем кастомное имя для текущего языка
        if (staticSetLocalizedNames.containsKey(set.id)) {
            Map<String, String> langMap = staticSetLocalizedNames.get(set.id);
            if (langMap.containsKey(langCode)) {
                return langMap.get(langCode);
            }
        }

        String key = "gui.oneblockultima.set." + set.id;
        String localized = StatCollector.translateToLocal(key);
        return localized.equals(key) ? set.id : localized;
    }

    // Статический метод для загрузки имен
    public static void loadStaticCustomNames() {
        staticSetLocalizedNames.clear();
        try {
            File langDir = new File(Loader.instance().getConfigDir(), "oneblockultima/lang");
            if (!langDir.exists()) {
                return;
            }

            for (File langFile : langDir.listFiles()) {
                if (!langFile.getName().endsWith(".lang")) continue;

                String langCode = langFile.getName().replace(".lang", "").toLowerCase();
                List<String> lines = new ArrayList<String>();
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(langFile), "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) { lines.add(line); }
                br.close();

                for (String subLine : lines) {
                    if (subLine.startsWith("gui.oneblockultima.set.")) {
                        String[] parts = subLine.split("=", 2);
                        if (parts.length == 2) {
                            String key = parts[0];
                            String value = parts[1];
                            if (key.startsWith("gui.oneblockultima.set.")) {
                                String setId = key.substring("gui.oneblockultima.set.".length());
                                getOrCreateLangMap(staticSetLocalizedNames, setId).put(langCode, value);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadSetDetails(int index)
    {
        if (index < 0 || index >= sets.size()) return;
        selectedSetIndex = index;
        editingSetSourceIndex = index;
        editingSet = BlockSetConfig.copyBlockSetDefinition(sets.get(index));
        isNewSet = false;
        selectedBlockIndex = -1;
        selectedBlockMeta = -1;
        selectedMobIndex = -1;
        entryScrollOffset = 0;
        savedNewSetName = "";
        savedNewSetId = "";
        savedNewSetCost = "0";
        savedNewSetMods = "";
        changeView(VIEW_SET_DETAILS);
    }

    private void saveSetDetails()
    {
        if (editingSet == null) {
            OneBlockUltima.getLogger().error("saveSetDetails: editingSet is null!");
            return;
        }

        String newName = setNameField != null ? setNameField.getText().trim() : "";

        if (isNewSet)
        {
            String newId = setIdField != null ? setIdField.getText().trim() : "";
            if (newId.isEmpty())
            {
                statusMessage = StatCollector.translateToLocal("gui.oneblockultima.config.error.empty_id");
                statusTimer = 100;
                return;
            }
            for (BlockSetConfig.BlockSetDefinition set : sets)
            {
                if (set != null && newId.equals(set.id))
                {
                    statusMessage = StatCollector.translateToLocal("gui.oneblockultima.config.error.duplicate_id");
                    statusTimer = 100;
                    return;
                }
            }

            syncEditingSetFromFields();
            editingSet.id = newId;

            if (!newName.isEmpty())
            {
                saveLocalizedName(newId, newName);
            }

            try
            {
                editingSet.unlockCost = Integer.parseInt(unlockCostField != null ? unlockCostField.getText().trim() : "0");
            }
            catch (NumberFormatException e)
            {
                statusMessage = StatCollector.translateToLocal("gui.oneblockultima.config.error.invalid_cost");
                statusTimer = 100;
                return;
            }

            if (editingSet.blocks == null) editingSet.blocks = new ArrayList<BlockSetConfig.BlockElementDefinition>();
            if (editingSet.mobs == null) editingSet.mobs = new ArrayList<BlockSetConfig.MobElementDefinition>();
            if (editingSet.requiredMods == null) editingSet.requiredMods = new BlockSetConfig.SetRequiredModsDefinition();

            sets.add(editingSet);
            updateFilteredSets();
            saveConfigToFile();

            statusMessage = StatCollector.translateToLocalFormatted("gui.oneblockultima.config.set_created", newId);
            statusTimer = 60;

            isNewSet = false;
            editingSet = null;
            savedNewSetName = "";
            savedNewSetId = "";
            savedNewSetCost = "0";
            savedNewSetMods = "";
            changeView(VIEW_SETS);
        }
        else
        {
            syncEditingSetFromFields();
            if (!newName.isEmpty())
            {
                saveLocalizedName(editingSet.id, newName);
            }

            try
            {
                editingSet.unlockCost = Integer.parseInt(unlockCostField != null ? unlockCostField.getText().trim() : "0");
            }
            catch (NumberFormatException e)
            {
                statusMessage = StatCollector.translateToLocal("gui.oneblockultima.config.error.invalid_cost");
                statusTimer = 100;
                return;
            }

            if (editingSetSourceIndex >= 0 && editingSetSourceIndex < sets.size())
            {
                sets.set(editingSetSourceIndex, editingSet);
            }

            saveConfigToFile();
            statusMessage = StatCollector.translateToLocal("gui.oneblockultima.config.saved");
            statusTimer = 60;
            editingSet.computedLevels = null;
            editingSetSourceIndex = -1;
            updateFilteredSets();
            changeView(VIEW_SETS);
        }
    }

    private void saveConfigToFile()
    {
        try
        {
            OneBlockUltima.getLogger().info("saveConfigToFile: saving config...");
            List<BlockSetConfig.BlockSetDefinition> snapshot = new ArrayList<BlockSetConfig.BlockSetDefinition>(sets);
            BlockSetConfig.applySets(snapshot);
            boolean saved = BlockSetConfig.saveCurrentConfig();
            OneBlockUltima.getLogger().info("saveConfigToFile: saveCurrentConfig returned " + saved);

            BlockSetConfig.reload();
            config = BlockSetConfig.get();

            sets.clear();
            sets.addAll((Collection<BlockSetConfig.BlockSetDefinition>)(config != null ? config.getSets() : Collections.emptyList()));
            OneBlockUltima.getLogger().info("saveConfigToFile: reloaded, sets size=" + sets.size());
            updateFilteredSets();
        }
        catch (Exception e)
        {
            statusMessage = StatCollector.translateToLocal("gui.oneblockultima.status.save_failed") + ": " + e.getMessage();
            statusTimer = 100;
            OneBlockUltima.getLogger().error("saveConfigToFile error", e);
        }
    }

    private void loadCustomNames() {
        setLocalizedNames.clear();
        try {
            File langDir = new File(Loader.instance().getConfigDir(), "oneblockultima/lang");
            if (!langDir.exists()) {
                return;
            }

            for (File langFile : langDir.listFiles()) {
                if (!langFile.getName().endsWith(".lang")) continue;

                String langCode = langFile.getName().replace(".lang", "").toLowerCase();
                List<String> lines = new ArrayList<String>();
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(langFile), "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) { lines.add(line); }
                br.close();

                for (String subLine : lines) {
                    if (subLine.startsWith("gui.oneblockultima.set.")) {
                        String[] parts = subLine.split("=", 2);
                        if (parts.length == 2) {
                            String key = parts[0];
                            String value = parts[1];
                            if (key.startsWith("gui.oneblockultima.set.")) {
                                String setId = key.substring("gui.oneblockultima.set.".length());
                                getOrCreateLangMap(setLocalizedNames, setId).put(langCode, value);
                                getOrCreateLangMap(staticSetLocalizedNames, setId).put(langCode, value);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    // Метод для сохранения кастомных имен
    private void saveCustomNames() {
        try {
            File langDir = new File(Loader.instance().getConfigDir(), "oneblockultima/lang");
            if (!langDir.exists()) {
                langDir.mkdirs();
            }

            // Для каждого языка создаем свой файл
            Map<String, List<String>> langLines = new HashMap<String, List<String>>();

            for (Map.Entry<String, Map<String, String>> setEntry : setLocalizedNames.entrySet()) {
                String setId = setEntry.getKey();
                for (Map.Entry<String, String> langEntry : setEntry.getValue().entrySet()) {
                    String langCode = langEntry.getKey();
                    String name = langEntry.getValue();
                    String key = "gui.oneblockultima.set." + setId;
                    getOrCreateStringList(langLines, langCode).add(key + "=" + name);
                }
            }

            // Сохраняем каждый язык в свой файл
            for (Map.Entry<String, List<String>> entry : langLines.entrySet()) {
                String langCode = entry.getKey();
                List<String> lines = entry.getValue();
                File langFile = new File(langDir, langCode + ".lang");
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(langFile), "UTF-8"));
                for (String line : lines) { bw.write(line); bw.newLine(); }
                bw.close();
            }
        } catch (Exception ignored) {}
    }

    private void saveLocalizedName(String setId, String name) {
        String langCode = mc.getLanguageManager().getCurrentLanguage().getLanguageCode().toLowerCase();

        getOrCreateLangMap(staticSetLocalizedNames, setId).put(langCode, name);
        saveCustomNames();
        if (langCode == null || langCode.isEmpty()) {
            langCode = "en_us";
        }
        langCode = langCode.toLowerCase();

        getOrCreateLangMap(staticSetLocalizedNames, setId).put(langCode, name);
        getOrCreateLangMap(setLocalizedNames, setId).put(langCode, name);
        saveCustomNames();
    }

    private void resetToDefault()
    {
        try
        {
            File file = BlockSetConfig.getConfigFile();
            if (file == null)
            {
                statusMessage = StatCollector.translateToLocal("gui.oneblockultima.status.reset_failed");
                statusTimer = 100;
                return;
            }
            if (file.getParentFile() != null && !file.getParentFile().exists())
            {
                file.getParentFile().mkdirs();
            }
            java.io.InputStream input = BlockSetConfig.class.getResourceAsStream("/assets/oneblockultima/blocksets.json");
            try
            {
                if (input == null)
                {
                    statusMessage = StatCollector.translateToLocal("gui.oneblockultima.status.reset_failed");
                    statusTimer = 100;
                    return;
                }
                java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read;
                while ((read = input.read(buffer)) != -1)
                {
                    output.write(buffer, 0, read);
                }
                java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                fos.write(output.toByteArray());
                fos.close();
            }
            finally
            {
                if (input != null)
                {
                    try { input.close(); } catch (Exception ignored) {}
                }
            }
            BlockSetConfig.reload();
            config = BlockSetConfig.get();
            sets.clear();
            sets.addAll(config.getSets());
            updateFilteredSets();
            currentView = VIEW_SETS;
            initGui();
            statusMessage = StatCollector.translateToLocal("gui.oneblockultima.status.reset_success");
            statusTimer = 60;
        }
        catch (Exception e)
        {
            statusMessage = StatCollector.translateToLocal("gui.oneblockultima.status.reset_failed");
            statusTimer = 100;
        }
    }

    private void addNewSet()
    {
        BlockSetConfig.BlockSetDefinition newSet = new BlockSetConfig.BlockSetDefinition();
        newSet.id = "";
        newSet.unlockCost = 0;
        newSet.blocks = new ArrayList<BlockSetConfig.BlockElementDefinition>();
        newSet.mobs = new ArrayList<BlockSetConfig.MobElementDefinition>();
        newSet.requiredMods = new BlockSetConfig.SetRequiredModsDefinition();
        newSet.unlockConditions = new BlockSetConfig.UnlockConditionGroup();
        newSet.unlockConditions.conditions = new ArrayList<BlockSetConfig.UnlockConditionDefinition>();

        editingSet = newSet;
        isNewSet = true;
        editingSetSourceIndex = -1;
        selectedBlockIndex = -1;
        selectedBlockMeta = -1;
        selectedMobIndex = -1;
        entryScrollOffset = 0;
        savedNewSetName = "";
        savedNewSetId = "";
        savedNewSetCost = "0";
        savedNewSetMods = "";

        changeView(VIEW_SET_DETAILS);
    }

    private void confirmDeleteSet(int index)
    {
        if (index < 0 || index >= sets.size()) return;
        deleteTargetIndex = index;
        changeView(VIEW_CONFIRM_DELETE);
    }

    private void executeDeleteSet()
    {
        if (deleteTargetIndex < 0 || deleteTargetIndex >= sets.size()) {
            OneBlockUltima.getLogger().error("executeDeleteSet: invalid index " + deleteTargetIndex);
            return;
        }

        String id = sets.get(deleteTargetIndex).id;
        OneBlockUltima.getLogger().info("executeDeleteSet: deleting " + id);

        sets.remove(deleteTargetIndex);
        OneBlockUltima.getLogger().info("executeDeleteSet: sets size after removal=" + sets.size());
        updateFilteredSets();
        if (selectedSetIndex >= sets.size()) selectedSetIndex = sets.size() - 1;

        saveConfigToFile();

        statusMessage = StatCollector.translateToLocalFormatted("gui.oneblockultima.config.set_deleted", id);
        statusTimer = 60;
        deleteTargetIndex = -1;
        changeView(VIEW_SETS);
    }

    private void addEntryToCurrentSet(EntryType type, SearchResult result)
    {
        if (editingSet == null) return;

        int baseLevel = 1;
        int baseChance = 1;
        int currency = 0;

        if (addLevelField != null)
        {
            try { baseLevel = Integer.parseInt(addLevelField.getText().trim()); } catch (NumberFormatException ignored) {}
        }
        if (addChanceField != null)
        {
            try { baseChance = Math.min(100, Math.max(1, Integer.parseInt(addChanceField.getText().trim()))); } catch (NumberFormatException ignored) {}
        }
        if (type == EntryType.BLOCK && currencyField != null)
        {
            try { currency = Integer.parseInt(currencyField.getText().trim()); } catch (NumberFormatException ignored) {}
        }

        if (type == EntryType.BLOCK)
        {
            BlockSetConfig.BlockElementDefinition entry = new BlockSetConfig.BlockElementDefinition();
            entry.registry = result.registry;
            entry.meta = result.stack != null ? result.stack.getItemDamage() : 0;
            entry.baseLevel = baseLevel;
            entry.baseChance = baseChance;
            entry.currency = currency;
            if (editingSet.blocks == null) editingSet.blocks = new ArrayList<BlockSetConfig.BlockElementDefinition>();
            editingSet.blocks.add(entry);
            statusMessage = StatCollector.translateToLocalFormatted("gui.oneblockultima.config.block_added", result.name);
        }
        else
        {
            BlockSetConfig.MobElementDefinition entry = new BlockSetConfig.MobElementDefinition();
            entry.registry = result.registry;
            entry.baseLevel = baseLevel;
            entry.baseChance = baseChance;
            entry.count = 1;
            if (editingSet.mobs == null) editingSet.mobs = new ArrayList<BlockSetConfig.MobElementDefinition>();
            editingSet.mobs.add(entry);
            statusMessage = StatCollector.translateToLocalFormatted("gui.oneblockultima.config.mob_added", result.name);
        }

        statusTimer = 60;
        editingSet.computedLevels = null;
        changeView(VIEW_SET_DETAILS);
    }

    private void removeSelectedEntry()
    {
        if (editingSet == null) return;

        if (selectedBlockIndex >= 0 && editingSet.blocks != null && selectedBlockIndex < editingSet.blocks.size())
        {
            BlockSetConfig.BlockElementDefinition block = editingSet.blocks.get(selectedBlockIndex);
            boolean hasSpecificMeta = selectedBlockMeta >= 0 && block.metas != null && block.metas.size() > 1 && block.metas.contains(selectedBlockMeta);

            if (hasSpecificMeta)
            {
                block.metas.remove(Integer.valueOf(selectedBlockMeta));
                statusMessage = StatCollector.translateToLocal("gui.oneblockultima.config.removed");
                statusTimer = 60;
            }
            else
            {
                editingSet.blocks.remove(selectedBlockIndex);
                statusMessage = StatCollector.translateToLocal("gui.oneblockultima.config.removed");
                statusTimer = 60;
            }

            selectedBlockIndex = -1;
            selectedBlockMeta = -1;
            editingSet.computedLevels = null;
            changeView(VIEW_SET_DETAILS);
        }
        else if (selectedMobIndex >= 0 && editingSet.mobs != null && selectedMobIndex < editingSet.mobs.size())
        {
            editingSet.mobs.remove(selectedMobIndex);
            statusMessage = StatCollector.translateToLocal("gui.oneblockultima.config.removed");
            statusTimer = 60;
            selectedMobIndex = -1;
            editingSet.computedLevels = null;
            changeView(VIEW_SET_DETAILS);
        }
    }

    private void editBlock(int index, EntryType type)
    {
        if (editingSet == null) return;

        editingEntryType = type;
        editingCurrencyIndex = index;
        changeView(VIEW_EDIT_CURRENCY);
    }

    private void saveCurrency()
    {
        if (editingSet == null || editingCurrencyIndex < 0) return;

        try {
            if (editingEntryType == EntryType.BLOCK && editingSet.blocks != null && editingCurrencyIndex < editingSet.blocks.size())
            {
                BlockSetConfig.BlockElementDefinition entry = editingSet.blocks.get(editingCurrencyIndex);
                entry.baseLevel = Integer.parseInt(editLevelField.getText().trim());
                entry.baseChance = Math.min(100, Math.max(1, Integer.parseInt(editChanceField.getText().trim())));
                entry.currency = Integer.parseInt(editCurrencyField.getText().trim());
            }
            else if (editingEntryType == EntryType.MOB && editingSet.mobs != null && editingCurrencyIndex < editingSet.mobs.size())
            {
                BlockSetConfig.MobElementDefinition entry = editingSet.mobs.get(editingCurrencyIndex);
                entry.baseLevel = Integer.parseInt(editLevelField.getText().trim());
                entry.baseChance = Math.min(100, Math.max(1, Integer.parseInt(editChanceField.getText().trim())));
            }
            else
            {
                return;
            }
            editingSet.computedLevels = null;
            statusMessage = StatCollector.translateToLocal("gui.oneblockultima.config.currency_updated");
            statusTimer = 60;
        } catch (NumberFormatException e) {
            statusMessage = StatCollector.translateToLocal("gui.oneblockultima.config.error.invalid_currency");
            statusTimer = 100;
            return;
        }

        changeView(VIEW_SET_DETAILS);
    }

    private void cancelCurrencyEdit()
    {
        changeView(VIEW_SET_DETAILS);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        OneBlockUltima.getLogger().info("actionPerformed: button id=" + button.id);

        switch (button.id)
        {
            case BUTTON_BACK:
                if (currentView == VIEW_ADD_ENTRY || currentView == VIEW_EDIT_CURRENCY)
                {
                    if (currentView == VIEW_EDIT_CURRENCY) cancelCurrencyEdit();
                    else changeView(VIEW_SET_DETAILS);
                }
                else if (currentView == VIEW_REQUIRED_MODS_EDITOR)
                {
                    changeView(VIEW_SET_DETAILS);
                }
                else if (currentView == VIEW_UNLOCK_CONDITIONS)
                {
                    changeView(VIEW_SET_DETAILS);
                }
                else if (currentView == VIEW_SET_DETAILS)
                {
                    savedNewSetName = "";
                    savedNewSetId = "";
                    savedNewSetCost = "0";
                    savedNewSetMods = "";
                    discardEditingSetChanges();
                    changeView(VIEW_SETS);
                }
                else
                {
                    mc.displayGuiScreen(parent);
                }
                break;

            case BUTTON_SAVE:
                if (currentView == VIEW_SET_DETAILS)
                {
                    saveSetDetails();
                }
                break;

            case BUTTON_REQUIRED_MODS_BACK:
                if (currentView == VIEW_REQUIRED_MODS_ADD)
                {
                    changeView(VIEW_REQUIRED_MODS_EDITOR);
                }
                else if (currentView == VIEW_REQUIRED_MODS_EDITOR)
                {
                    changeView(VIEW_SET_DETAILS);
                }
                break;

            case BUTTON_SAVE_CURRENCY:
                saveCurrency();
                break;

            case BUTTON_CANCEL:
            case BUTTON_CANCEL_CURRENCY:
                if (currentView == VIEW_CONFIRM_DELETE)
                {
                    deleteTargetIndex = -1;
                    changeView(VIEW_SETS);
                }
                else if (currentView == VIEW_EDIT_CURRENCY)
                {
                    cancelCurrencyEdit();
                }
                break;

            case BUTTON_CONFIRM_DELETE:
                executeDeleteSet();
                break;

            case BUTTON_RESET:
                resetToDefault();
                break;

            case BUTTON_ADD_SET:
                addNewSet();
                break;

            case BUTTON_ADD_BLOCK:
                if (editingSet != null)
                {
                    if (isNewSet)
                    {
                        savedNewSetName = setNameField.getText().trim();
                        savedNewSetId = setIdField.getText().trim();
                        savedNewSetCost = unlockCostField.getText().trim();
                        savedNewSetMods = requiredModsField.getText().trim();
                    }
                    currentEntryType = EntryType.BLOCK;
                    searchScrollOffset = 0;
                    changeView(VIEW_ADD_ENTRY);
                }
                break;

            case BUTTON_ADD_MOB:
                if (editingSet != null)
                {
                    if (isNewSet)
                    {
                        savedNewSetName = setNameField.getText().trim();
                        savedNewSetId = setIdField.getText().trim();
                        savedNewSetCost = unlockCostField.getText().trim();
                        savedNewSetMods = requiredModsField.getText().trim();
                    }
                    currentEntryType = EntryType.MOB;
                    searchScrollOffset = 0;
                    changeView(VIEW_ADD_ENTRY);
                }
                break;

            case BUTTON_EDIT_REQUIRED_MODS:
                if (editingSet != null)
                {
                    openRequiredModsEditor();
                }
                break;

            case BUTTON_REQUIRED_MODS_TOGGLE:
                requiredModsEditorType = requiredModsEditorType == BlockSetConfig.SetRequiredModsDefinition.TYPE.ALL
                        ? BlockSetConfig.SetRequiredModsDefinition.TYPE.ANY
                        : BlockSetConfig.SetRequiredModsDefinition.TYPE.ALL;
                if (requiredModsToggleButton != null)
                {
                    requiredModsToggleButton.displayString = getRequiredModsEditorTypeLabel();
                }
                break;

            case BUTTON_REQUIRED_MODS_SAVE:
                commitRequiredModsEditor();
                break;

            case BUTTON_REQUIRED_MODS_ADD:
                if (currentView == VIEW_REQUIRED_MODS_EDITOR)
                {
                    openRequiredModsAddView();
                }
                else if (currentView == VIEW_REQUIRED_MODS_ADD)
                {
                    addSelectedRequiredMods();
                }
                break;

            case BUTTON_REQUIRED_MODS_DELETE:
                removeSelectedRequiredMods();
                break;

            case BUTTON_EDIT_UNLOCK_CONDITIONS:
                if (editingSet != null)
                {
                    openUnlockConditionsEditor();
                }
                break;

            case BUTTON_UNLOCK_CONDITIONS_TOGGLE:
                unlockConditionsEditorMode = "any".equalsIgnoreCase(unlockConditionsEditorMode) ? "all" : "any";
                if (unlockConditionsToggleButton != null)
                {
                    unlockConditionsToggleButton.displayString = "any".equalsIgnoreCase(unlockConditionsEditorMode)
                            ? StatCollector.translateToLocal("gui.oneblockultima.config.any")
                            : StatCollector.translateToLocal("gui.oneblockultima.config.all");
                }
                break;

            case BUTTON_UNLOCK_CONDITIONS_BACK:
                changeView(VIEW_SET_DETAILS);
                break;

            case BUTTON_UNLOCK_CONDITIONS_SAVE:
                commitUnlockConditionsEditor();
                break;

            case BUTTON_UNLOCK_CONDITIONS_ADD:
                addNewUnlockCondition();
                break;

            case BUTTON_UNLOCK_CONDITIONS_DELETE:
                removeSelectedUnlockCondition();
                break;

            case BUTTON_UNLOCK_CONDITIONS_CYCLE_TYPE:
                cycleNewConditionType();
                break;

            case BUTTON_UNLOCK_CONDITIONS_CYCLE_SET:
                cycleNewConditionSet();
                break;

            case BUTTON_REMOVE_ENTRY:
                removeSelectedEntry();
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();

        drawRect(pad, pad, width - pad, height - pad, 0xCC22272E);
        drawRect(pad, pad, width - pad, pad + 1, 0xFF3A3F44);
        drawRect(pad, height - pad - 1, width - pad, height - pad, 0xFF3A3F44);
        drawRect(pad, pad, pad + 1, height - pad, 0xFF3A3F44);
        drawRect(width - pad - 1, pad, width - pad, height - pad, 0xFF3A3F44);

        switch (currentView)
        {
            case VIEW_SETS: drawSetsView(mouseX, mouseY); break;
            case VIEW_SET_DETAILS: drawSetDetailsView(mouseX, mouseY); break;
            case VIEW_ADD_ENTRY: drawAddEntryView(mouseX, mouseY); break;
            case VIEW_CONFIRM_DELETE: drawConfirmDeleteView(); break;
            case VIEW_EDIT_CURRENCY: drawEditCurrencyView(mouseX, mouseY); break;
            case VIEW_REQUIRED_MODS_EDITOR: drawRequiredModsEditorView(mouseX, mouseY); break;
            case VIEW_REQUIRED_MODS_ADD: drawRequiredModsAddView(mouseX, mouseY); break;
            case VIEW_UNLOCK_CONDITIONS: drawUnlockConditionsView(mouseX, mouseY); break;
        }

        if (!statusMessage.isEmpty())
        {
            int color = statusMessage.contains("error") || statusMessage.contains("failed") ? 0xFFFF4444 : 0xFFA0A0A0;
            drawCenteredString(fontRendererObj, statusMessage, width / 2, height - pad - textHeight, color);
        }

        if (suppressMouseUntilRelease && Mouse.isButtonDown(0))
        {
            for (GuiButton button : (List<GuiButton>)buttonList)
            {
                button.drawButton(mc, mouseX, mouseY);
            }
        }
        else
        {
            if (suppressMouseUntilRelease)
            {
                suppressMouseUntilRelease = false;
            }
            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    private void drawSetsView(int mouseX, int mouseY)
    {
        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.config.sets_title"), width / 2, pad + textHeight / 2, 0xFFFFFF);

        if (searchField != null) searchField.drawTextBox();

        int listX = pad + gap;
        assert searchField != null;
        int listY = searchField.yPosition + btnHeight + gap;
        int listWidth = width - pad * 2 - gap - scrollWidth;
        int listHeight = height - pad * 2 - textHeight - btnHeight - gap * 3;

        int visibleEntries = listHeight / entryHeight;
        if (visibleEntries < 1) visibleEntries = 1;
        int maxScroll = Math.max(0, filteredSets.size() - visibleEntries);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        drawRect(listX, listY, listX + listWidth - scrollWidth, listY + listHeight, 0xFF1A1F24);

        int startEntry = scrollOffset;
        int endEntry = Math.min(filteredSets.size(), startEntry + visibleEntries);

        for (int i = startEntry; i < endEntry; i++)
        {
            BlockSetConfig.BlockSetDefinition set = filteredSets.get(i);
            int entryX = listX + innerPadding;
            int entryY = listY + innerPadding + (i - startEntry) * entryHeight;
            int entryWidth = listWidth - innerPadding * 2 - scrollWidth;

            boolean isSelected = selectedSetIndex >= 0 && selectedSetIndex < sets.size() &&
                    sets.get(selectedSetIndex).id.equals(set.id);

            int bgColor = isSelected ? 0xFF3F5060 : (i % 2 == 0 ? 0xFF2A2F34 : 0xFF22272E);
            drawRect(entryX, entryY, entryX + entryWidth, entryY + entryHeight, bgColor);

            String name = getLocalizedSetName(set);
            fontRendererObj.drawString(name, entryX + gap, entryY + gap / 2, 0xFFFFFF);
            fontRendererObj.drawString(set.id, entryX + gap, entryY + gap / 2 + textHeight + innerPadding, 0xA0A0A0);

            String editLabel = StatCollector.translateToLocal("gui.oneblockultima.config.edit");
            String delLabel = StatCollector.translateToLocal("gui.oneblockultima.config.delete_set");
            int editWidth = fontRendererObj.getStringWidth(editLabel) + pad;
            int delWidth = fontRendererObj.getStringWidth(delLabel) + pad;

            int entryEnd = entryX + entryWidth;
            int editX = entryEnd - editWidth - delWidth - gap - pad;
            int delX = entryEnd - delWidth - pad;

            boolean editHover = mouseX >= editX && mouseX <= editX + editWidth &&
                    mouseY >= entryY && mouseY <= entryY + entryHeight;
            boolean delHover = mouseX >= delX && mouseX <= delX + delWidth &&
                    mouseY >= entryY && mouseY <= entryY + entryHeight;

            int editColor = editHover ? 0xFF6A7A8A : 0xFF3A4A5A;
            drawRect(editX, entryY + gap / 2, editX + editWidth, entryY + entryHeight - gap / 2, editColor);
            drawCenteredString(fontRendererObj, editLabel, editX + editWidth / 2, entryY + entryHeight / 2 - textHeight / 2, 0xFFFFFF);

            int delColor = delHover ? 0xFF6A3A3A : 0xFF3A2A2A;
            drawRect(delX, entryY + gap / 2, delX + delWidth, entryY + entryHeight - gap / 2, delColor);
            drawCenteredString(fontRendererObj, delLabel, delX + delWidth / 2, entryY + entryHeight / 2 - textHeight / 2, 0xFFFF4444);
        }

        if (filteredSets.size() > visibleEntries)
        {
            int scrollbarX = listX + listWidth - gap;
            int scrollbarY = listY + innerPadding;
            int scrollbarHeight = listHeight - innerPadding * 2;
            int thumbHeight = Math.max(10, scrollbarHeight * visibleEntries / filteredSets.size());
            int thumbY = scrollbarY + (scrollOffset * (scrollbarHeight - thumbHeight) / Math.max(1, maxScroll));

            drawRect(scrollbarX, scrollbarY, scrollbarX + scrollWidth, scrollbarY + scrollbarHeight, 0xFF2A2F34);
            drawRect(scrollbarX, thumbY, scrollbarX + scrollWidth, thumbY + thumbHeight, 0xFF7A7F84);
        }
    }

    private void drawSetDetailsView(int mouseX, int mouseY)
    {
        if (editingSet == null) return;

        String title = isNewSet ? StatCollector.translateToLocal("gui.oneblockultima.config.add_set") : StatCollector.translateToLocal("gui.oneblockultima.config.edit_set");
        drawCenteredString(fontRendererObj, title, width / 2, pad + textHeight / 2, 0xFFFFFF);

        int currentY = pad + textHeight + pad;

        drawString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.config.set_name") + ":", formMargin, currentY + btnHeight / 2 - textHeight / 2, 0xA0A0A0);
        setNameField.drawTextBox();

        currentY += btnHeight + gap;

        drawString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.config.set_id") + ":", formMargin, currentY + btnHeight / 2 - textHeight / 2, 0xA0A0A0);
        setIdField.drawTextBox();

        currentY += btnHeight + gap;
        drawString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.config.unlock_cost") + ":", formMargin, currentY + btnHeight / 2 - textHeight / 2, 0xA0A0A0);
        unlockCostField.drawTextBox();

        currentY += btnHeight + gap;
        if (editRequiredModsButton != null)
        {
            editRequiredModsButton.xPosition = formFieldX;
            editRequiredModsButton.yPosition = currentY;
            editRequiredModsButton.displayString = getRequiredModsButtonLabel(editingSet != null ? editingSet.requiredMods : null);
            editRequiredModsButton.drawButton(mc, mouseX, mouseY);
        }
        if (editUnlockConditionsButton != null)
        {
            editUnlockConditionsButton.xPosition = formFieldX + editRequiredModsButton.width + gap;
            editUnlockConditionsButton.yPosition = currentY;
            editUnlockConditionsButton.displayString = getUnlockConditionsButtonLabel(editingSet != null ? editingSet.unlockConditions : null);
            editUnlockConditionsButton.drawButton(mc, mouseX, mouseY);
        }

        int listY = currentY + btnHeight + gap;
        int listX = pad + gap;
        int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
        int listHeight = Math.max(80, height - listY - btnHeight - pad - gap - innerPadding * 2);

        drawRect(listX, listY, listX + listWidth, listY + listHeight, 0xFF1A1F24);

        int colWidth = (listWidth - innerPadding * 2) / 2;

        List<BlockDisplayEntry> blockDisplayEntries = buildBlockDisplayEntries();
        int blockDisplayCount = blockDisplayEntries.size();
        int mobCount = editingSet.mobs != null ? editingSet.mobs.size() : 0;
        int maxEntries = Math.max(blockDisplayCount, mobCount);
        int visibleEntries = listHeight / entryHeight;
        if (visibleEntries < 1) visibleEntries = 1;
        int maxScroll = Math.max(0, maxEntries - visibleEntries);
        if (entryScrollOffset > maxScroll) entryScrollOffset = maxScroll;

        for (int i = 0; i < visibleEntries && i + entryScrollOffset < maxEntries; i++)
        {
            int idx = i + entryScrollOffset;

            if (idx < blockDisplayCount)
            {
                BlockDisplayEntry displayEntry = blockDisplayEntries.get(idx);
                assert editingSet.blocks != null;
                BlockSetConfig.BlockElementDefinition entry = editingSet.blocks.get(displayEntry.blockIndex);
                int entryX = listX + innerPadding;
                int entryY = listY + innerPadding + i * entryHeight;

                boolean isSelected = (selectedBlockIndex == displayEntry.blockIndex && selectedBlockMeta == displayEntry.meta);
                int bgColor = isSelected ? 0xFF3F5060 : (i % 2 == 0 ? 0xFF2A2F34 : 0xFF22272E);
                drawRect(entryX, entryY, entryX + colWidth - innerPadding, entryY + entryHeight, bgColor);

                ItemStack stack = getItemStackFromEntry(entry, displayEntry.meta);
                Fluid entryFluid = getFluidForRegistry(entry.registry);
                if (entryFluid != null)
                {
                    renderFluidIcon(entryFluid, entryX + innerPadding, entryY + innerPadding, iconSize);
                }
                else if (stack != null && isInvisibleBlock(stack))
                {
                    renderBlockIconAs2D(((net.minecraft.item.ItemBlock) stack.getItem()).field_150939_a, stack.getItemDamage(), entryX + innerPadding, entryY + innerPadding, iconSize);
                }
                else if (stack != null)
                {
                    RenderHelper.enableGUIStandardItemLighting();
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), stack, entryX + innerPadding, entryY + innerPadding);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    RenderHelper.disableStandardItemLighting();
                }
                else
                {
                    drawRect(entryX, entryY, entryX + iconSize, entryY + iconSize, 0xFF444444);
                    drawString(fontRendererObj, "B", entryX + iconSize / 2 - textHeight / 4, entryY + iconSize / 2 - textHeight / 2, 0xFFFFFF);
                }

                String name = getLocalizedNameForBlock(entry, displayEntry.meta);
                int textX = entryX + iconSize + gap + innerPadding * 2;
                int maxNameWidth = colWidth - iconSize - gap * 4 - 70;
                String displayName = name;
                if (fontRendererObj.getStringWidth(displayName) > maxNameWidth)
                {
                    displayName = fontRendererObj.trimStringToWidth(displayName, maxNameWidth - fontRendererObj.getStringWidth("...")) + "...";
                }
                String levelLabel = StatCollector.translateToLocal("gui.oneblockultima.config.base_level") + ": " + entry.baseLevel;
                fontRendererObj.drawString(displayName, textX, entryY + innerPadding, 0xA0A0A0);
                int nameWidth = fontRendererObj.getStringWidth(displayName);
                fontRendererObj.drawString(levelLabel, textX + nameWidth + gap * 2, entryY + innerPadding, 0x707070);
                fontRendererObj.drawString(entry.registry + "  " + StatCollector.translateToLocal("gui.oneblockultima.chance") + ": " + entry.baseChance + "%", textX, entryY + innerPadding * 2 + textHeight, 0x808080);

                String currencyStr = String.valueOf(entry.currency);
                int editBtnWidth = entryHeight - innerPadding * 2;
                int coinSize = 10;
                int currencyWidth = fontRendererObj.getStringWidth(currencyStr);
                int currencyLabelWidth = coinSize + innerPadding + currencyWidth;
                int coinX = entryX + colWidth - innerPadding - editBtnWidth - gap - currencyLabelWidth;
                int coinY = entryY + innerPadding + (entryHeight - textHeight) / 2;
                int textY = entryY + innerPadding + (entryHeight - textHeight) / 2;
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(COIN_TEXTURE);
                drawModalRectWithCustomSizedTexture(coinX, coinY, 0, 0, coinSize, coinSize, coinSize, coinSize);
                fontRendererObj.drawString(currencyStr, coinX + coinSize + innerPadding, textY, 0xFFD700);

                int editCurrX = entryX + colWidth - innerPadding - editBtnWidth - gap / 2;
                boolean currHover = mouseX >= editCurrX && mouseX <= editCurrX + editBtnWidth &&
                        mouseY >= entryY && mouseY <= entryY + entryHeight;
                int currColor = currHover ? 0xFF6A7A8A : 0xFF3A4A5A;
                drawRect(editCurrX, entryY + gap / 2, editCurrX + editBtnWidth, entryY + entryHeight - gap / 2, currColor);
                drawCenteredString(fontRendererObj, "\u270E", editCurrX + editBtnWidth / 2, entryY + entryHeight / 2 - textHeight / 2, 0xFFFFFF);
            }

            if (idx < mobCount)
            {
                BlockSetConfig.MobElementDefinition entry = editingSet.mobs.get(idx);
                int entryX = listX + colWidth + innerPadding;
                int entryY = listY + innerPadding + i * entryHeight;

                boolean isSelected = (selectedMobIndex == idx);
                int bgColor = isSelected ? 0xFF3F5060 : (i % 2 == 0 ? 0xFF2A2F34 : 0xFF22272E);
                drawRect(entryX, entryY, entryX + colWidth - innerPadding, entryY + entryHeight, bgColor);

                try {
                    String entityName = entry.registry;
                    if (entityName.contains(":")) entityName = entityName.substring(entityName.indexOf(":") + 1);
                    String capitalizedName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
                    Entity entity = EntityList.createEntityByName(capitalizedName, mc.theWorld);
                    if (entity instanceof EntityLivingBase) {
                        int drawSize = iconSize;
                        int centerX = entryX + drawSize / 2 + innerPadding;
                        int centerY = entryY + innerPadding + drawSize * 3 / 4;
                        ModelUtil.drawEntityOnScreen(centerX, centerY, (EntityLivingBase)entity, drawSize);
                    } else {
                        drawRect(entryX + innerPadding, entryY + innerPadding, entryX + iconSize + innerPadding, entryY + iconSize + innerPadding, 0xFF444444);
                        drawString(fontRendererObj, "M", entryX + iconSize / 2 - textHeight / 4, entryY + iconSize / 2 - textHeight / 2, 0xFFFFFF);
                    }
                } catch (Exception ignored) {
                    drawRect(entryX + innerPadding, entryY + innerPadding, entryX + iconSize + innerPadding, entryY + iconSize + innerPadding, 0xFF444444);
                    drawString(fontRendererObj, "M", entryX + iconSize / 2 - textHeight / 4, entryY + iconSize / 2 - textHeight / 2, 0xFFFFFF);
                }

                String name = getLocalizedNameForMob(entry);
                int textX = entryX + iconSize + gap + innerPadding * 2;
                int maxMobNameWidth = colWidth - iconSize - gap * 6 - 40;
                String mobDisplayName = name;
                if (fontRendererObj.getStringWidth(mobDisplayName) > maxMobNameWidth)
                {
                    mobDisplayName = fontRendererObj.trimStringToWidth(mobDisplayName, maxMobNameWidth - fontRendererObj.getStringWidth("...")) + "...";
                }
                String mobLevelLabel = StatCollector.translateToLocal("gui.oneblockultima.config.base_level") + ": " + entry.baseLevel;
                fontRendererObj.drawString(mobDisplayName, textX, entryY + innerPadding, 0xA0A0A0);
                int mobNameWidth = fontRendererObj.getStringWidth(mobDisplayName);
                fontRendererObj.drawString(mobLevelLabel, textX + mobNameWidth + gap * 2, entryY + innerPadding, 0x707070);
                fontRendererObj.drawString(StatCollector.translateToLocal("gui.oneblockultima.chance") + ": " + entry.baseChance + "%", textX, entryY + textHeight + innerPadding * 2, 0x808080);

                int mobEditBtnWidth = entryHeight - innerPadding * 2;
                int mobEditX = entryX + colWidth - innerPadding - mobEditBtnWidth - gap / 2;
                boolean mobEditHover = mouseX >= mobEditX && mouseX <= mobEditX + mobEditBtnWidth &&
                        mouseY >= entryY && mouseY <= entryY + entryHeight;
                int mobEditColor = mobEditHover ? 0xFF6A7A8A : 0xFF3A4A5A;
                drawRect(mobEditX, entryY + gap / 2, mobEditX + mobEditBtnWidth, entryY + entryHeight - gap / 2, mobEditColor);
                drawCenteredString(fontRendererObj, "\u270E", mobEditX + mobEditBtnWidth / 2, entryY + entryHeight / 2 - textHeight / 2, 0xFFFFFF);
            }
        }

        if (maxEntries > visibleEntries)
        {
            int scrollbarX = listX + listWidth - gap;
            int scrollbarY = listY + innerPadding;
            int scrollbarHeight = listHeight - innerPadding * 2;
            int thumbHeight = Math.max(10, scrollbarHeight * visibleEntries / maxEntries);
            int thumbY = scrollbarY + (entryScrollOffset * (scrollbarHeight - thumbHeight) / Math.max(1, maxScroll));

            drawRect(scrollbarX, scrollbarY, scrollbarX + scrollWidth, scrollbarY + scrollbarHeight, 0xFF2A2F34);
            drawRect(scrollbarX, thumbY, scrollbarX + scrollWidth, thumbY + thumbHeight, 0xFF7A7F84);
        }
    }

    private void drawRequiredModsEditorView(int mouseX, int mouseY)
    {
        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.config.required_mods_title"), width / 2, pad + textHeight / 2, 0xFFFFFF);

        String summary = requiredModsEditorMods.isEmpty()
                ? StatCollector.translateToLocal("gui.oneblockultima.config.required_mods_empty")
                : StatCollector.translateToLocalFormatted("gui.oneblockultima.config.required_mods_selected", requiredModsEditorMods.size());
        drawString(fontRendererObj, summary, pad + gap, pad + gap * 3 + textHeight + btnHeight, 0xA0A0A0);

        int listX = pad + gap;
        int listY = pad + textHeight + pad + btnHeight + gap + textHeight + gap;
        int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
        int listHeight = height - listY - pad - btnHeight - gap * 2;

        drawRect(listX, listY, listX + listWidth, listY + listHeight, 0xFF1A1F24);

        List<RequiredModEntry> modsToShow = getCurrentRequiredModEntries();
        if (modsToShow.isEmpty())
        {
            drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.config.required_mods_empty"), width / 2, listY + listHeight / 2, 0x808080);
            return;
        }

        int visibleEntries = listHeight / entryHeight;
        if (visibleEntries < 1) visibleEntries = 1;
        int maxScroll = Math.max(0, modsToShow.size() - visibleEntries);
        if (requiredModsScrollOffset > maxScroll) requiredModsScrollOffset = maxScroll;

        int startEntry = requiredModsScrollOffset;
        int endEntry = Math.min(modsToShow.size(), startEntry + visibleEntries);

        for (int i = startEntry; i < endEntry; i++)
        {
            RequiredModEntry entry = modsToShow.get(i);
            boolean hovered = mouseX >= listX + innerPadding && mouseX <= listX + listWidth - innerPadding && mouseY >= listY + innerPadding + (i - startEntry) * entryHeight && mouseY <= listY + innerPadding + (i - startEntry) * entryHeight + entryHeight;
            boolean selected = selectedRequiredModsForRemoval.contains(entry.modId);
            int bgColor = selected ? 0xFF2F4F2F : (hovered ? 0xFF3F5060 : (i % 2 == 0 ? 0xFF2A2F34 : 0xFF22272E));
            drawRect(listX + innerPadding, listY + innerPadding + (i - startEntry) * entryHeight, listX + listWidth - innerPadding, listY + innerPadding + (i - startEntry) * entryHeight + entryHeight, bgColor);
            drawString(fontRendererObj, entry.displayName.isEmpty() ? entry.modId : entry.displayName, listX + gap, listY + gap + (i - startEntry) * entryHeight, 0xFFFFFF);
        }

        if (modsToShow.size() > visibleEntries)
        {
            int scrollbarX = listX + listWidth - gap;
            int scrollbarY = listY + innerPadding;
            int scrollbarHeight = listHeight - innerPadding * 2;
            int thumbHeight = Math.max(10, scrollbarHeight * visibleEntries / modsToShow.size());
            int thumbY = scrollbarY + (requiredModsScrollOffset * (scrollbarHeight - thumbHeight) / Math.max(1, maxScroll));
            drawRect(scrollbarX, scrollbarY, scrollbarX + scrollWidth, scrollbarY + scrollbarHeight, 0xFF2A2F34);
            drawRect(scrollbarX, thumbY, scrollbarX + scrollWidth, thumbY + thumbHeight, 0xFF7A7F84);
        }
    }

    private void drawRequiredModsAddView(int mouseX, int mouseY)
    {
        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.config.required_mods_add_title"), width / 2, pad + textHeight / 2, 0xFFFFFF);

        String summary = selectedRequiredModsToAdd.isEmpty()
                ? StatCollector.translateToLocal("gui.oneblockultima.config.required_mods_add_hint")
                : StatCollector.translateToLocalFormatted("gui.oneblockultima.config.required_mods_selected", selectedRequiredModsToAdd.size());
        drawString(fontRendererObj, summary, pad + gap, pad + textHeight + pad + btnHeight + gap / 2, 0xA0A0A0);

        int listX = pad + gap;
        int listY = pad + textHeight + pad + btnHeight + gap + textHeight + gap;
        int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
        int listHeight = height - pad * 2 - btnHeight - gap * 4 - textHeight - pad;

        drawRect(listX, listY, listX + listWidth, listY + listHeight, 0xFF1A1F24);

        List<RequiredModEntry> availableEntries = getAvailableRequiredModEntries();
        if (availableEntries.isEmpty())
        {
            drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.config.required_mods_empty"), width / 2, listY + listHeight / 2, 0x808080);
            return;
        }

        int visibleEntries = listHeight / entryHeight;
        if (visibleEntries < 1) visibleEntries = 1;
        int maxScroll = Math.max(0, availableEntries.size() - visibleEntries);
        if (requiredModsScrollOffset > maxScroll) requiredModsScrollOffset = maxScroll;

        int startEntry = requiredModsScrollOffset;
        int endEntry = Math.min(availableEntries.size(), startEntry + visibleEntries);

        for (int i = startEntry; i < endEntry; i++)
        {
            RequiredModEntry entry = availableEntries.get(i);
            boolean hovered = mouseX >= listX + innerPadding && mouseX <= listX + listWidth - innerPadding && mouseY >= listY + innerPadding + (i - startEntry) * entryHeight && mouseY <= listY + innerPadding + (i - startEntry) * entryHeight + entryHeight;
            boolean selected = selectedRequiredModsToAdd.contains(entry.modId);
            int bgColor = selected ? 0xFF2F4F2F : (hovered ? 0xFF3F5060 : (i % 2 == 0 ? 0xFF2A2F34 : 0xFF22272E));
            drawRect(listX + innerPadding, listY + innerPadding + (i - startEntry) * entryHeight, listX + listWidth - innerPadding, listY + innerPadding + (i - startEntry) * entryHeight + entryHeight, bgColor);
            drawString(fontRendererObj, entry.displayName.isEmpty() ? entry.modId : entry.displayName, listX + gap, listY + gap + (i - startEntry) * entryHeight, 0xFFFFFF);
        }

        if (availableEntries.size() > visibleEntries)
        {
            int scrollbarX = listX + listWidth - gap;
            int scrollbarY = listY + innerPadding;
            int scrollbarHeight = listHeight - innerPadding * 2;
            int thumbHeight = Math.max(10, scrollbarHeight * visibleEntries / availableEntries.size());
            int thumbY = scrollbarY + (requiredModsScrollOffset * (scrollbarHeight - thumbHeight) / Math.max(1, maxScroll));
            drawRect(scrollbarX, scrollbarY, scrollbarX + scrollWidth, scrollbarY + scrollbarHeight, 0xFF2A2F34);
            drawRect(scrollbarX, thumbY, scrollbarX + scrollWidth, thumbY + thumbHeight, 0xFF7A7F84);
        }
    }

    private int getAddEntryListY()
    {
        return Math.max(addLevelField != null ? addLevelField.yPosition + addLevelField.height + gap : 0,
                currentEntryType == EntryType.BLOCK && currencyField != null ? currencyField.yPosition + currencyField.height + gap : 0);
    }

    private void drawAddEntryView(int mouseX, int mouseY)
    {
        String title = currentEntryType == EntryType.BLOCK ?
                StatCollector.translateToLocal("gui.oneblockultima.config.add_block") :
                StatCollector.translateToLocal("gui.oneblockultima.config.add_mob");
        drawCenteredString(fontRendererObj, title, width / 2, pad + textHeight / 2, 0xFFFFFF);

        entrySearchField.drawTextBox();

        int labelGap = 4;

        if (addLevelField != null)
        {
            int levelLabelWidth = fontRendererObj.getStringWidth(StatCollector.translateToLocal("gui.oneblockultima.config.base_level") + ":");
            int labelX = addLevelField.xPosition - labelGap - levelLabelWidth;
            int textY = addLevelField.yPosition + addLevelField.height / 2 - textHeight / 2;
            drawString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.config.base_level") + ":", labelX, textY, 0xA0A0A0);
            addLevelField.drawTextBox();
        }

        if (addChanceField != null)
        {
            int chanceLabelWidth = fontRendererObj.getStringWidth(StatCollector.translateToLocal("gui.oneblockultima.chance") + ":");
            int labelX = addChanceField.xPosition - labelGap - chanceLabelWidth;
            int textY = addChanceField.yPosition + addChanceField.height / 2 - textHeight / 2;
            drawString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.chance") + ":", labelX, textY, 0xA0A0A0);
            addChanceField.drawTextBox();
        }

        if (currentEntryType == EntryType.BLOCK && currencyField != null)
        {
            int iconSize = 12;
            int iconX = currencyField.xPosition - iconSize - gap;
            int iconY = currencyField.yPosition + currencyField.height / 2 - iconSize / 2;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(COIN_TEXTURE);
            drawModalRectWithCustomSizedTexture(iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
            currencyField.drawTextBox();
        }

        int listY = getAddEntryListY();
        int listX = pad + gap;
        int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
        int listHeight = height - listY - pad - textHeight - btnHeight - gap * 3;

        drawRect(listX, listY, listX + listWidth, listY + listHeight, 0xFF1A1F24);

        if (searchResults.isEmpty())
        {
            String msg = StatCollector.translateToLocal("gui.oneblockultima.config.search.no_results");
            drawCenteredString(fontRendererObj, msg, width / 2, listY + listHeight / 2, 0x808080);
        }
        else
        {
            int visibleEntries = listHeight / entryHeight;
            if (visibleEntries < 1) visibleEntries = 1;
            int maxScroll = Math.max(0, searchResults.size() - visibleEntries);
            if (searchScrollOffset > maxScroll) searchScrollOffset = maxScroll;

            int startEntry = searchScrollOffset;
            int endEntry = Math.min(searchResults.size(), startEntry + visibleEntries);

            for (int i = startEntry; i < endEntry; i++)
            {
                SearchResult result = searchResults.get(i);
                int entryX = listX + innerPadding;
                int entryY = listY + innerPadding + (i - startEntry) * entryHeight;
                int entryWidth = listWidth - innerPadding * 2 - scrollWidth;

                boolean isHovered = mouseX >= entryX && mouseX <= entryX + entryWidth &&
                        mouseY >= entryY && mouseY <= entryY + entryHeight;

                int bgColor = isHovered ? 0xFF3F5060 : (i % 2 == 0 ? 0xFF2A2F34 : 0xFF22272E);
                drawRect(entryX, entryY, entryX + entryWidth, entryY + entryHeight, bgColor);

                if (result.stack != null && isInvisibleBlock(result.stack))
                {
                    renderBlockIconAs2D(((net.minecraft.item.ItemBlock) result.stack.getItem()).field_150939_a, result.stack.getItemDamage(), entryX + innerPadding, entryY + innerPadding, iconSize);
                }
                else if (result.stack != null)
                {
                    RenderHelper.enableGUIStandardItemLighting();
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), result.stack, entryX + innerPadding, entryY + innerPadding);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    RenderHelper.disableStandardItemLighting();
                }
                else if (result.isFluid && result.fluid != null)
                {
                    renderFluidIcon(result.fluid, entryX + innerPadding, entryY + innerPadding, iconSize);
                }
                else if (result.isMob && result.entityClass != null)
                {
                    try
                    {
                        Entity entity = (Entity) result.entityClass.getConstructor(net.minecraft.world.World.class)
                                .newInstance(Minecraft.getMinecraft().theWorld);
                        if (entity instanceof EntityLivingBase) {
                            int drawSize = iconSize;
                            int centerX = entryX + drawSize / 2 + innerPadding;
                            int centerY = entryY + innerPadding + drawSize * 3 / 4;
                            ModelUtil.drawEntityOnScreen(centerX, centerY, (EntityLivingBase)entity, drawSize);
                        }
                    }
                    catch (Exception ignored)
                    {
                        drawRect(entryX + innerPadding, entryY + innerPadding, entryX + iconSize + innerPadding, entryY + iconSize + innerPadding, 0xFF444444);
                        drawString(fontRendererObj, "M", entryX + iconSize / 2 - textHeight / 4, entryY + iconSize / 2 - textHeight / 2, 0xFFFFFF);
                    }
                }

                int textX = entryX + iconSize + gap + innerPadding * 2;
                fontRendererObj.drawString(result.name, textX, entryY + innerPadding, 0xFFFFFF);
                fontRendererObj.drawString(result.registry, textX, entryY + innerPadding + textHeight + 1, 0x808080);
            }

            if (searchResults.size() > visibleEntries)
            {
                int scrollbarX = listX + listWidth - gap;
                int scrollbarY = listY + innerPadding;
                int scrollbarHeight = listHeight - innerPadding * 2;
                int thumbHeight = Math.max(10, scrollbarHeight * visibleEntries / searchResults.size());
                int thumbY = scrollbarY + (searchScrollOffset * (scrollbarHeight - thumbHeight) / Math.max(1, maxScroll));

                drawRect(scrollbarX, scrollbarY, scrollbarX + scrollWidth, scrollbarY + scrollbarHeight, 0xFF2A2F34);
                drawRect(scrollbarX, thumbY, scrollbarX + scrollWidth, thumbY + thumbHeight, 0xFF7A7F84);
            }
        }

        String help = StatCollector.translateToLocal("gui.oneblockultima.config.search.help");
        drawString(fontRendererObj, help, pad + gap, height - pad - btnHeight - textHeight - gap * 2, 0x808080);
    }

    private void drawConfirmDeleteView()
    {
        String deleteName = deleteTargetIndex >= 0 && deleteTargetIndex < sets.size()
                ? getLocalizedSetName(sets.get(deleteTargetIndex))
                : "";
        String message = StatCollector.translateToLocalFormatted("gui.oneblockultima.config.confirm_delete_message", deleteName);
        drawCenteredString(fontRendererObj, message, width / 2, height / 2 - textHeight, 0xFFFFFF);
    }

    private void drawEditCurrencyView(int mouseX, int mouseY)
    {
        int centerX = width / 2;
        int centerY = height / 2;
        int fieldWidth = 100;
        int labelGap = 4;

        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.config.edit_currency"), centerX, pad + textHeight / 2, 0xFFFFFF);

        int levelLabelWidth = fontRendererObj.getStringWidth(StatCollector.translateToLocal("gui.oneblockultima.config.base_level") + ":");
        int chanceLabelWidth = fontRendererObj.getStringWidth(StatCollector.translateToLocal("gui.oneblockultima.chance") + ":");
        int currencyLabelWidth = fontRendererObj.getStringWidth(StatCollector.translateToLocal("gui.oneblockultima.config.currency") + ":");

        if (editLevelField != null)
        {
            int labelX = centerX - fieldWidth / 2 - labelGap - levelLabelWidth;
            int textY = editLevelField.yPosition + editLevelField.height / 2 - textHeight / 2;
            drawString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.config.base_level") + ":", labelX, textY, 0xA0A0A0);
            editLevelField.drawTextBox();
        }

        if (editChanceField != null)
        {
            int labelX = centerX - fieldWidth / 2 - labelGap - chanceLabelWidth;
            int textY = editChanceField.yPosition + editChanceField.height / 2 - textHeight / 2;
            drawString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.chance") + ":", labelX, textY, 0xA0A0A0);
            editChanceField.drawTextBox();
        }

        if (editCurrencyField != null && editingEntryType == EntryType.BLOCK)
        {
            int labelX = centerX - fieldWidth / 2 - labelGap - currencyLabelWidth;
            int textY = editCurrencyField.yPosition + editCurrencyField.height / 2 - textHeight / 2;
            drawString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.config.currency") + ":", labelX, textY, 0xA0A0A0);
            editCurrencyField.drawTextBox();
        }
    }

    private int getSetDetailsListY()
    {
        return pad + textHeight + pad + (btnHeight + gap) * 4 + gap;
    }

    private int getSetDetailsListHeight()
    {
        return height - pad * 2 - textHeight - btnHeight - gap * 3 - (btnHeight + gap) * 4 - gap * 2;
    }

    private boolean handleSetsViewClick(int mouseX, int mouseY)
    {
        int listX = pad + gap;
        int listY = searchField.yPosition + btnHeight + gap;
        int listWidth = width - pad * 2 - gap - scrollWidth;
        int listHeight = height - pad * 2 - textHeight - btnHeight - gap * 3;

        if (mouseX < listX || mouseX > listX + listWidth || mouseY < listY || mouseY > listY + listHeight)
        {
            return false;
        }

        int row = (mouseY - listY - innerPadding) / entryHeight;
        int index = scrollOffset + row;
        if (index < 0 || index >= filteredSets.size())
        {
            return false;
        }

        BlockSetConfig.BlockSetDefinition set = filteredSets.get(index);
        int entryX = listX + innerPadding;
        int entryY = listY + innerPadding + row * entryHeight;
        int entryWidth = listWidth - innerPadding * 2 - scrollWidth;

        String editLabel = StatCollector.translateToLocal("gui.oneblockultima.config.edit");
        String delLabel = StatCollector.translateToLocal("gui.oneblockultima.config.delete_set");
        int editWidth = fontRendererObj.getStringWidth(editLabel) + pad;
        int delWidth = fontRendererObj.getStringWidth(delLabel) + pad;

        int entryEnd = entryX + entryWidth;
        int editX = entryEnd - editWidth - delWidth - gap - pad;
        int delX = entryEnd - delWidth - pad;

        if (mouseX >= delX && mouseX <= delX + delWidth && mouseY >= entryY && mouseY <= entryY + entryHeight)
        {
            confirmDeleteSet(sets.indexOf(set));
            return true;
        }
        if (mouseX >= editX && mouseX <= editX + editWidth && mouseY >= entryY && mouseY <= entryY + entryHeight)
        {
            loadSetDetails(sets.indexOf(set));
            return true;
        }

        selectedSetIndex = sets.indexOf(set);
        return true;
    }

    private boolean handleSetDetailsViewClick(int mouseX, int mouseY)
    {
        if (editingSet == null)
        {
            return false;
        }

        int listY = getSetDetailsListY();
        int listX = pad + gap;
        int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
        int listHeight = getSetDetailsListHeight();
        int colWidth = (listWidth) / 2 - innerPadding;
        List<BlockDisplayEntry> blockDisplayEntries = buildBlockDisplayEntries();
        int blockDisplayCount = blockDisplayEntries.size();
        int mobCount = editingSet.mobs != null ? editingSet.mobs.size() : 0;

        if (mouseX < listX || mouseX > listX + listWidth || mouseY < listY || mouseY > listY + listHeight)
        {
            return false;
        }

        int row = (mouseY - listY - innerPadding) / entryHeight + entryScrollOffset;
        if (row < 0)
        {
            return false;
        }

        if (mouseX >= listX + innerPadding && mouseX < listX + innerPadding + colWidth - innerPadding * 2)
        {
            if (row >= blockDisplayCount)
            {
                return false;
            }

            BlockDisplayEntry displayEntry = blockDisplayEntries.get(row);
            selectedBlockIndex = displayEntry.blockIndex;
            selectedBlockMeta = displayEntry.meta;
            selectedMobIndex = -1;

            int entryX = listX + innerPadding;
            int entryY = listY + innerPadding + (row - entryScrollOffset) * entryHeight;
            int editBtnWidth = 24;
            int editCurrX = entryX + colWidth - innerPadding - editBtnWidth - gap / 2;
            if (mouseX >= editCurrX && mouseX <= editCurrX + editBtnWidth && mouseY >= entryY && mouseY <= entryY + entryHeight)
            {
                editBlock(displayEntry.blockIndex, EntryType.BLOCK);
            }
            return true;
        }

        if (mouseX >= listX + colWidth + innerPadding && mouseX < listX + listWidth - innerPadding)
        {
            if (row >= mobCount)
            {
                return false;
            }

            selectedMobIndex = row;
            selectedBlockIndex = -1;
            selectedBlockMeta = -1;

            int entryX = listX + colWidth + innerPadding;
            int entryY = listY + innerPadding + (row - entryScrollOffset) * entryHeight;
            int mobEditBtnWidth = entryHeight - innerPadding * 2;
            int mobEditX = entryX + colWidth - innerPadding - mobEditBtnWidth - gap / 2;
            if (mouseX >= mobEditX && mouseX <= mobEditX + mobEditBtnWidth && mouseY >= entryY && mouseY <= entryY + entryHeight)
            {
                editBlock(row, EntryType.MOB);
            }
            return true;
        }

        return false;
    }

    private boolean handleAddEntryViewClick(int mouseX, int mouseY)
    {
        int listX = pad + gap;
        int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
        int listY = getAddEntryListY();
        int listHeight = height - pad * 2 - textHeight - pad - btnHeight - pad - gap * 2 -
                (currentEntryType == EntryType.BLOCK ? btnHeight + gap : 0);

        if (mouseX < listX || mouseX > listX + listWidth || mouseY < listY || mouseY > listY + listHeight)
        {
            return false;
        }

        int row = (mouseY - listY - innerPadding) / entryHeight;
        int index = searchScrollOffset + row;
        if (index < 0 || index >= searchResults.size())
        {
            return false;
        }

        addEntryToCurrentSet(currentEntryType, searchResults.get(index));
        return true;
    }

    private boolean handleRequiredModsViewClick(int mouseX, int mouseY)
    {
        int listX = pad + gap;
        int listY = pad + textHeight + pad + btnHeight + gap + textHeight + gap;
        int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
        int listHeight = height - pad * 2 - btnHeight - gap * 4 - textHeight - pad;

        if (mouseX < listX || mouseX > listX + listWidth || mouseY < listY || mouseY > listY + listHeight)
        {
            return false;
        }

        int row = (mouseY - listY - innerPadding) / entryHeight;
        int index = requiredModsScrollOffset + row;
        List<RequiredModEntry> modsToShow = getCurrentRequiredModEntries();
        if (index < 0 || index >= modsToShow.size())
        {
            return false;
        }

        selectRequiredModForRemoval(modsToShow.get(index).modId);
        return true;
    }

    private boolean handleRequiredModsAddViewClick(int mouseX, int mouseY)
    {
        int listX = pad + gap;
        int listY = pad + textHeight + pad + btnHeight + gap + textHeight + gap;
        int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
        int listHeight = height - pad * 2 - btnHeight - gap * 4 - textHeight - pad;

        if (mouseX < listX || mouseX > listX + listWidth || mouseY < listY || mouseY > listY + listHeight)
        {
            return false;
        }

        int row = (mouseY - listY - innerPadding) / entryHeight;
        int index = requiredModsScrollOffset + row;
        List<RequiredModEntry> availableEntries = getAvailableRequiredModEntries();
        if (index < 0 || index >= availableEntries.size())
        {
            return false;
        }

        selectRequiredModToAdd(availableEntries.get(index).modId);
        return true;
    }

    private void handleTextFieldClicks(int mouseX, int mouseY, int mouseButton)
    {
        if (currentView == VIEW_ADD_ENTRY && entrySearchField != null)
        {
            entrySearchField.mouseClicked(mouseX, mouseY, mouseButton);
            if (addLevelField != null) addLevelField.mouseClicked(mouseX, mouseY, mouseButton);
            if (addChanceField != null) addChanceField.mouseClicked(mouseX, mouseY, mouseButton);
            if (currencyField != null) currencyField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        else if (searchField != null)
        {
            searchField.mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (currentView == VIEW_SET_DETAILS)
        {
            if (setNameField != null) setNameField.mouseClicked(mouseX, mouseY, mouseButton);
            if (setIdField != null) setIdField.mouseClicked(mouseX, mouseY, mouseButton);
            if (unlockCostField != null) unlockCostField.mouseClicked(mouseX, mouseY, mouseButton);
            if (requiredModsField != null) requiredModsField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        else if (currentView == VIEW_EDIT_CURRENCY)
        {
            if (editLevelField != null) editLevelField.mouseClicked(mouseX, mouseY, mouseButton);
            if (editChanceField != null) editChanceField.mouseClicked(mouseX, mouseY, mouseButton);
            if (editCurrencyField != null) editCurrencyField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        else if (currentView == VIEW_UNLOCK_CONDITIONS)
        {
            if (unlockConditionsLevelField != null) unlockConditionsLevelField.mouseClicked(mouseX, mouseY, mouseButton);
            if (unlockConditionsCountField != null) unlockConditionsCountField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (suppressNextMouseClick)
        {
            suppressNextMouseClick = false;
            return;
        }

        if (suppressMouseUntilRelease)
        {
            suppressMouseUntilRelease = false;
            return;
        }

        if (mouseButton == 0)
        {
            for (GuiButton button : new ArrayList<GuiButton>(buttonList))
            {
                if (button.mousePressed(mc, mouseX, mouseY))
                {
                    button.func_146113_a(mc.getSoundHandler());
                    actionPerformed(button);
                    return;
                }
            }

            if (currentView == VIEW_SET_DETAILS)
            {
                boolean handled = handleSetDetailsViewClick(mouseX, mouseY);
                if (handled)
                {
                    handleTextFieldClicks(mouseX, mouseY, mouseButton);
                    return;
                }
            }
            else if (currentView == VIEW_REQUIRED_MODS_EDITOR)
            {
                boolean handled = handleRequiredModsViewClick(mouseX, mouseY);
                if (handled)
                {
                    return;
                }
            }
            else if (currentView == VIEW_REQUIRED_MODS_ADD)
            {
                boolean handled = handleRequiredModsAddViewClick(mouseX, mouseY);
                if (handled)
                {
                    return;
                }
            }
            else if (currentView == VIEW_UNLOCK_CONDITIONS)
            {
                boolean handled = handleUnlockConditionsViewClick(mouseX, mouseY);
                if (handled)
                {
                    handleTextFieldClicks(mouseX, mouseY, mouseButton);
                    return;
                }
            }
            else if (currentView == VIEW_SETS)
            {
                boolean handled = handleSetsViewClick(mouseX, mouseY);
                if (handled)
                {
                    return;
                }
            }
            else if (currentView == VIEW_ADD_ENTRY)
            {
                boolean handled = handleAddEntryViewClick(mouseX, mouseY);
                if (handled)
                {
                    return;
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
        handleTextFieldClicks(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseMovedOrUp(int mouseX, int mouseY, int state)
    {
        if (state == 0)
        {
            suppressMouseUntilRelease = false;
            suppressNextMouseClick = false;
        }
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode)
    {
        super.keyTyped(typedChar, keyCode);

        if (searchField != null && searchField.isFocused())
        {
            searchField.textboxKeyTyped(typedChar, keyCode);
            searchQuery = searchField.getText();
            updateFilteredSets();
            return;
        }
        if (setNameField != null && setNameField.isFocused())
        {
            setNameField.textboxKeyTyped(typedChar, keyCode);
            if (isNewSet) savedNewSetName = setNameField.getText();
            return;
        }
        if (setIdField != null && setIdField.isFocused() && isNewSet)
        {
            setIdField.textboxKeyTyped(typedChar, keyCode);
            savedNewSetId = setIdField.getText();
            return;
        }
        if (unlockCostField != null && unlockCostField.isFocused())
        {
            unlockCostField.textboxKeyTyped(typedChar, keyCode);
            if (isNewSet) savedNewSetCost = unlockCostField.getText();
            return;
        }
        if (requiredModsField != null && requiredModsField.isFocused())
        {
            requiredModsField.textboxKeyTyped(typedChar, keyCode);
            if (isNewSet) savedNewSetMods = requiredModsField.getText();
            return;
        }
        if (entrySearchField != null && entrySearchField.isFocused())
        {
            entrySearchField.textboxKeyTyped(typedChar, keyCode);
            performSearch();
            return;
        }
        if (currencyField != null && currencyField.isFocused())
        {
            currencyField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        if (addLevelField != null && addLevelField.isFocused())
        {
            addLevelField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        if (addChanceField != null && addChanceField.isFocused())
        {
            addChanceField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        if (editLevelField != null && editLevelField.isFocused())
        {
            editLevelField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        if (editChanceField != null && editChanceField.isFocused())
        {
            editChanceField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        if (editCurrencyField != null && editCurrencyField.isFocused())
        {
            editCurrencyField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        if (unlockConditionsLevelField != null && unlockConditionsLevelField.isFocused())
        {
            if (keyCode == Keyboard.KEY_RETURN)
            {
                addNewUnlockCondition();
                return;
            }
            unlockConditionsLevelField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        if (unlockConditionsCountField != null && unlockConditionsCountField.isFocused())
        {
            if (keyCode == Keyboard.KEY_RETURN)
            {
                addNewUnlockCondition();
                return;
            }
            unlockConditionsCountField.textboxKeyTyped(typedChar, keyCode);
            return;
        }

        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            if (currentView == VIEW_ADD_ENTRY)
            {
                changeView(VIEW_SET_DETAILS);
            }
            else if (currentView == VIEW_REQUIRED_MODS_EDITOR)
            {
                changeView(VIEW_SET_DETAILS);
            }
            else if (currentView == VIEW_UNLOCK_CONDITIONS)
            {
                changeView(VIEW_SET_DETAILS);
            }
            else if (currentView == VIEW_EDIT_CURRENCY)
            {
                cancelCurrencyEdit();
            }
            else if (currentView == VIEW_SET_DETAILS || currentView == VIEW_CONFIRM_DELETE)
            {
                changeView(VIEW_SETS);
            }
            else
            {
                mc.displayGuiScreen(parent);
            }
        }
    }

    @Override
    public void handleMouseInput()
    {
        super.handleMouseInput();
        int d = Mouse.getEventDWheel();

        if (d != 0)
        {
            int delta = d > 0 ? -1 : 1;

            if (currentView == VIEW_SETS)
            {
                int listX = pad + gap;
                int listY = searchField.yPosition + btnHeight + gap;
                int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
                int listHeight = height - pad * 2 - textHeight - btnHeight - gap * 3;

                int mouseX = Mouse.getEventX() * width / mc.displayWidth;
                int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;

                if (mouseX >= listX && mouseX <= listX + listWidth &&
                        mouseY >= listY && mouseY <= listY + listHeight)
                {
                    int visibleEntries = listHeight / entryHeight;
                    if (visibleEntries < 1) visibleEntries = 1;
                    int maxScroll = Math.max(0, filteredSets.size() - visibleEntries);
                    scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + delta));
                }
            }
            else if (currentView == VIEW_SET_DETAILS && editingSet != null)
            {
                int listY = getSetDetailsListY();
                int listX = pad + gap;
                int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
                int listHeight = getSetDetailsListHeight();

                int mouseX = Mouse.getEventX() * width / mc.displayWidth;
                int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;

                if (mouseX >= listX && mouseX <= listX + listWidth &&
                        mouseY >= listY && mouseY <= listY + listHeight)
                {
                    int blockDisplayCount = buildBlockDisplayEntries().size();
                    int mobCount = editingSet.mobs != null ? editingSet.mobs.size() : 0;
                    int maxEntries = Math.max(blockDisplayCount, mobCount);
                    int visibleEntries = listHeight / entryHeight;
                    if (visibleEntries < 1) visibleEntries = 1;
                    int maxScroll = Math.max(0, maxEntries - visibleEntries);
                    entryScrollOffset = Math.max(0, Math.min(maxScroll, entryScrollOffset + delta));
                }
            }
            else if (currentView == VIEW_ADD_ENTRY)
            {
                int listX = pad + gap;
                int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
                int listY = getAddEntryListY();
                int listHeight = height - pad * 2 - textHeight - pad - btnHeight - pad - gap * 2 -
                        (currentEntryType == EntryType.BLOCK ? btnHeight + gap : 0);

                int mouseX = Mouse.getEventX() * width / mc.displayWidth;
                int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;

                if (mouseX >= listX && mouseX <= listX + listWidth &&
                        mouseY >= listY && mouseY <= listY + listHeight)
                {
                    int visibleEntries = listHeight / entryHeight;
                    if (visibleEntries < 1) visibleEntries = 1;
                    int maxScroll = Math.max(0, searchResults.size() - visibleEntries);
                    searchScrollOffset = Math.max(0, Math.min(maxScroll, searchScrollOffset + delta));
                }
            }
            else if (currentView == VIEW_REQUIRED_MODS_EDITOR || currentView == VIEW_REQUIRED_MODS_ADD)
            {
                int listX = pad + gap;
                int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
                int listY = pad + textHeight + pad + btnHeight + gap + textHeight + gap;
                int listHeight = height - pad * 2 - btnHeight - gap * 4 - textHeight - pad;

                int mouseX = Mouse.getEventX() * width / mc.displayWidth;
                int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;

                if (mouseX >= listX && mouseX <= listX + listWidth &&
                        mouseY >= listY && mouseY <= listY + listHeight)
                {
                    List<RequiredModEntry> mods = currentView == VIEW_REQUIRED_MODS_EDITOR
                            ? getCurrentRequiredModEntries()
                            : getAvailableRequiredModEntries();
                    int visibleEntries = listHeight / entryHeight;
                    if (visibleEntries < 1) visibleEntries = 1;
                    int maxScroll = Math.max(0, mods.size() - visibleEntries);
                    requiredModsScrollOffset = Math.max(0, Math.min(maxScroll, requiredModsScrollOffset + delta));
                }
            }
            else if (currentView == VIEW_UNLOCK_CONDITIONS)
            {
                int listX = pad + gap;
                int listY = getUnlockConditionsToolbarHeight();
                int listWidth = width - pad * 2 - gap * 2 - scrollWidth;
                int listHeight = height - listY - pad - btnHeight - gap;

                int mouseX = Mouse.getEventX() * width / mc.displayWidth;
                int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;

                if (mouseX >= listX && mouseX <= listX + listWidth &&
                        mouseY >= listY && mouseY <= listY + listHeight)
                {
                    int visibleEntries = listHeight / entryHeight;
                    if (visibleEntries < 1) visibleEntries = 1;
                    int maxScroll = Math.max(0, unlockConditionsEditorConditions.size() - visibleEntries);
                    unlockConditionsScrollOffset = Math.max(0, Math.min(maxScroll, unlockConditionsScrollOffset + delta));
                }
            }
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        if (searchField != null) searchField.updateCursorCounter();
        if (setNameField != null) setNameField.updateCursorCounter();
        if (setIdField != null) setIdField.updateCursorCounter();
        if (unlockCostField != null) unlockCostField.updateCursorCounter();
        if (requiredModsField != null) requiredModsField.updateCursorCounter();
        if (entrySearchField != null) entrySearchField.updateCursorCounter();
        if (currencyField != null) currencyField.updateCursorCounter();
        if (editCurrencyField != null) editCurrencyField.updateCursorCounter();
        if (unlockConditionsLevelField != null) unlockConditionsLevelField.updateCursorCounter();
        if (unlockConditionsCountField != null) unlockConditionsCountField.updateCursorCounter();

        if (statusTimer > 0)
        {
            statusTimer--;
            if (statusTimer == 0) statusMessage = "";
        }
    }

    private void renderBlockIconAs2D(net.minecraft.block.Block block, int meta, int x, int y, int size)
    {
        if (block == null) return;
        net.minecraft.util.IIcon icon = block.getIcon(2, meta);
        if (icon == null) return;

        mc.getTextureManager().bindTexture(net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture);
        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + size, 0.0D, minU, maxV);
        tessellator.addVertexWithUV(x + size, y + size, 0.0D, maxU, maxV);
        tessellator.addVertexWithUV(x + size, y, 0.0D, maxU, minV);
        tessellator.addVertexWithUV(x, y, 0.0D, minU, minV);
        tessellator.draw();
    }


    private boolean isInvisibleBlock(ItemStack stack)
    {
        if (stack == null || stack.getItem() == null) return false;
        if (stack.getItem() instanceof net.minecraft.item.ItemBlock)
        {
            net.minecraft.block.Block block = ((net.minecraft.item.ItemBlock) stack.getItem()).field_150939_a;
            return block != null && block.getRenderType() < 0;
        }
        return false;
    }

    protected void drawModalRectWithCustomSizedTexture(int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight)
    {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0.0D, u * f, (v + (float)height) * f1);
        tessellator.addVertexWithUV(x + width, y + height, 0.0D, (u + (float)width) * f, (v + (float)height) * f1);
        tessellator.addVertexWithUV(x + width, y, 0.0D, (u + (float)width) * f, v * f1);
        tessellator.addVertexWithUV(x, y, 0.0D, u * f, v * f1);
        tessellator.draw();
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }
}