package ru.defea.oneblockultima.tile;

import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.block.ModBlocks;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.util.BlockUtil;
import ru.defea.oneblockultima.world.GeneratedBlockRegistry;

import java.util.*;

public class TileEntityOneBlockGenerator extends TileEntity
{
    private static final int NON_PLAYER_BREAK_COOLDOWN_TICKS = 20;

    private String selectedSetId;
    private long ownerIdMsb;
    private long ownerIdLsb;
    private boolean hasOwnerId = false;
    private final List<int[]> memberIds = new ArrayList<int[]>();
    private final List<PendingInvite> pendingInvites = new ArrayList<PendingInvite>();
    private boolean placedByPlayer = false;
    private boolean disableFluidGeneration = false;
    private boolean disableMobGeneration = false;
    private boolean disableChestGeneration = false;
    private boolean disableSaplingGeneration = false;
    private final Map<String, Integer> setLevels = new HashMap<String, Integer>();
    private long lastNonPlayerBreakTick = Long.MIN_VALUE;
    private boolean nonPlayerBreakCooldownActive = false;

    public TileEntityOneBlockGenerator()
    {
    }

    private static int[] uuidToMostLeast(UUID uuid)
    {
        if (uuid == null) return null;
        return new int[]{(int)(uuid.getMostSignificantBits() >> 32), (int)(uuid.getLeastSignificantBits() >> 32)};
    }

    private static UUID mostLeastToUUID(int most, int least)
    {
        return new UUID(((long) most) << 32, ((long) least) << 32);
    }

    public boolean canProcessNonPlayerBreak(long worldTick)
    {
        boolean active = isNonPlayerBreakCooldownActive(worldTick);
        return !active;
    }

    public boolean isNonPlayerBreakCooldownActive(long worldTick)
    {
        if (lastNonPlayerBreakTick == Long.MIN_VALUE)
        {
            nonPlayerBreakCooldownActive = false;
            return false;
        }

        boolean active = worldTick - lastNonPlayerBreakTick < NON_PLAYER_BREAK_COOLDOWN_TICKS;
        if (!active)
        {
            nonPlayerBreakCooldownActive = false;
            lastNonPlayerBreakTick = Long.MIN_VALUE;
        }
        return active;
    }

    public void markNonPlayerBreak(long worldTick)
    {
        lastNonPlayerBreakTick = worldTick;
        nonPlayerBreakCooldownActive = true;
        if (worldObj != null && !worldObj.isRemote)
        {
            worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, ModBlocks.ONE_BLOCK_GENERATOR, NON_PLAYER_BREAK_COOLDOWN_TICKS);
        }
        markDirty();
    }

    public void tryGenerateBlock()
    {
        tryGenerateBlock(false);
    }

    public void tryGenerateBlock(boolean fromNonPlayerBreak)
    {
        if (worldObj != null && !worldObj.isRemote && isNonPlayerBreakCooldownActive(worldObj.getTotalWorldTime()))
        {
            return;
        }

        if (selectedSetId == null || selectedSetId.isEmpty())
        {
            selectedSetId = BlockSetConfig.get().getDefaultSetId();
        }

        BlockSetConfig.BlockSetDefinition set = BlockSetConfig.get().getSet(selectedSetId);
        if (set == null)
        {
            selectedSetId = BlockSetConfig.get().getDefaultSetId();
            set = BlockSetConfig.get().getSet(selectedSetId);
            if (set == null)
            {
                return;
            }
        }

        int level = resolveGenerationLevel();
        if (level <= 0)
        {
            selectedSetId = BlockSetConfig.get().getDefaultSetId();
            set = BlockSetConfig.get().getSet(selectedSetId);
            if (set == null)
            {
                return;
            }
            level = resolveGenerationLevel();
        }

        BlockSetConfig.SetLevelDefinition levelDefinition = set.getLevel(level);
        if (levelDefinition == null)
        {
            return;
        }

        BlockSetConfig.BlockEntryDefinition entry = pickGenerationEntry(levelDefinition);
        if (entry == null)
        {
            return;
        }

        int[] state = BlockUtil.toBlockAndMeta(entry);
        if (state == null)
        {
            return;
        }

        int targetX = xCoord;
        int targetY = yCoord + 1;
        int targetZ = zCoord;

        if (!BlockUtil.canReplaceForGeneration(worldObj, targetX, targetY, targetZ))
        {
            return;
        }

        GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(worldObj);
        if (registry.isGenerated(targetX, targetY, targetZ))
        {
            registry.remove(targetX, targetY, targetZ);
        }

        BlockUtil.placeBlockWithNBT(worldObj, targetX, targetY, targetZ, state[0], state[1], entry.nbtTags);
        registry.markGenerated(targetX, targetY, targetZ, xCoord, yCoord, zCoord, selectedSetId, entry.currency, level, entry.registry, entry.meta);
        if (worldObj != null && !worldObj.isRemote)
        {
            nonPlayerBreakCooldownActive = false;
            lastNonPlayerBreakTick = Long.MIN_VALUE;
        }
    }

    private BlockSetConfig.BlockEntryDefinition pickGenerationEntry(BlockSetConfig.SetLevelDefinition levelDefinition)
    {
        if (levelDefinition == null || levelDefinition.blocks == null || levelDefinition.blocks.isEmpty())
        {
            return null;
        }

        List<BlockSetConfig.BlockEntryDefinition> allowed = new ArrayList<BlockSetConfig.BlockEntryDefinition>();
        int totalChance = 0;
        for (BlockSetConfig.BlockEntryDefinition candidate : levelDefinition.blocks)
        {
            if (candidate == null || !isAllowedGenerationEntry(candidate))
            {
                continue;
            }
            totalChance += candidate.getChance();
            allowed.add(candidate);
        }

        if (allowed.isEmpty())
        {
            return null;
        }

        int roll = worldObj.rand.nextInt(Math.max(1, totalChance));
        int current = 0;
        for (BlockSetConfig.BlockEntryDefinition candidate : allowed)
        {
            current += candidate.getChance();
            if (roll < current)
            {
                return candidate;
            }
        }
        return allowed.get(allowed.size() - 1);
    }

    private boolean isAllowedGenerationEntry(BlockSetConfig.BlockEntryDefinition entry)
    {
        if (entry == null) return false;
        if (disableFluidGeneration && entry.isFluid()) return false;
        if (disableChestGeneration && isChestEntry(entry)) return false;
        if (disableSaplingGeneration && isSaplingEntry(entry)) return false;
        return true;
    }

    private boolean isChestEntry(BlockSetConfig.BlockEntryDefinition entry)
    {
        if (entry == null || entry.registry == null) return false;
        String registry = entry.registry.toLowerCase(Locale.ROOT);
        return registry.contains("chest") || registry.contains("barrel");
    }

    private boolean isSaplingEntry(BlockSetConfig.BlockEntryDefinition entry)
    {
        if (entry == null || entry.registry == null) return false;
        String registry = entry.registry.toLowerCase(Locale.ROOT);
        return registry.contains("sapling");
    }

    private int resolveGenerationLevel()
    {
        return getSetLevel(selectedSetId);
    }

    public int getSetLevel(String setId)
    {
        if (setId == null) return 0;
        Integer level = setLevels.get(setId);
        if (level != null) return level;
        BlockSetConfig config = BlockSetConfig.get();
        if (config == null) return 0;
        String defaultSetId = config.getDefaultSetId();
        return setId.equals(defaultSetId) ? 1 : 0;
    }

    public boolean upgradeSet(String setId, int cost, int maxLevel)
    {
        if (setId == null || cost < 0 || maxLevel <= 0) return false;
        int currentLevel = getSetLevel(setId);
        if (currentLevel >= maxLevel) return false;
        setLevels.put(setId, currentLevel + 1);
        markDirty();
        return true;
    }

    public Map<String, Integer> getSetLevels() { return setLevels; }

    public String getSelectedSetId()
    {
        if (selectedSetId == null || selectedSetId.isEmpty())
        {
            selectedSetId = BlockSetConfig.get().getDefaultSetId();
        }
        return selectedSetId;
    }

    public void setDisableFluidGeneration(boolean v) { this.disableFluidGeneration = v; markDirty(); }
    public boolean isDisableFluidGeneration() { return disableFluidGeneration; }
    public void setDisableMobGeneration(boolean v) { this.disableMobGeneration = v; markDirty(); }
    public boolean isDisableMobGeneration() { return disableMobGeneration; }
    public void setDisableChestGeneration(boolean v) { this.disableChestGeneration = v; markDirty(); }
    public boolean isDisableChestGeneration() { return disableChestGeneration; }
    public void setDisableSaplingGeneration(boolean v) { this.disableSaplingGeneration = v; markDirty(); }
    public boolean isDisableSaplingGeneration() { return disableSaplingGeneration; }

    public void setSelectedSetId(String id)
    {
        this.selectedSetId = id;
        markDirty();
        if (worldObj != null && !worldObj.isRemote)
        {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public boolean hasAccess(EntityPlayer player)
    {
        return player != null && hasAccess(player.getUniqueID());
    }

    public boolean hasAccess(UUID playerId)
    {
        if (playerId == null) return false;
        if (hasOwnerId && getOwnerId() != null && getOwnerId().equals(playerId)) return true;
        int[] idArr = uuidToMostLeast(playerId);
        if (idArr != null)
        {
            for (int[] member : memberIds)
            {
                if (member[0] == idArr[0] && member[1] == idArr[1]) return true;
            }
        }
        return false;
    }

    public boolean isOwner(EntityPlayer player)
    {
        return player != null && hasOwnerId && getOwnerId() != null && getOwnerId().equals(player.getUniqueID());
    }

    public boolean isFree() { return !hasOwnerId && memberIds.isEmpty(); }

    public boolean canBeClaimedBy(UUID playerId)
    {
        if (playerId == null || !isFree()) return false;
        if (worldObj == null || worldObj.isRemote) return false;
        for (Object obj : worldObj.loadedTileEntityList)
        {
            if (obj == this || !(obj instanceof TileEntityOneBlockGenerator)) continue;
            TileEntityOneBlockGenerator other = (TileEntityOneBlockGenerator) obj;
            if (other.hasAccess(playerId)) return false;
        }
        return true;
    }

    public boolean tryAssignOwnerIfEligible(UUID playerId)
    {
        if (!canBeClaimedBy(playerId)) return false;
        setOwnerId(playerId);
        return true;
    }

    public boolean ensureOwnership(UUID playerId)
    {
        if (playerId == null) return false;
        if (!isFree()) return true;
        return tryAssignOwnerIfEligible(playerId);
    }

    public boolean assignOwnerForPlacement(UUID playerId)
    {
        if (playerId == null) return false;
        if (!hasOwnerId && memberIds.isEmpty())
        {
            setOwnerId(playerId);
            return true;
        }
        if (hasOwnerId && getOwnerId() != null && getOwnerId().equals(playerId)) return true;
        return false;
    }

    public boolean isPlacedByPlayer() { return placedByPlayer; }
    public void setPlacedByPlayer(boolean v) { this.placedByPlayer = v; markDirty(); }

    public void addMember(UUID memberId)
    {
        if (memberId == null || hasAccess(memberId)) return;
        int[] arr = uuidToMostLeast(memberId);
        if (arr != null) memberIds.add(arr);
        markDirty();
    }

    public void addPendingInvite(UUID targetPlayerId, UUID senderPlayerId, int ticks)
    {
        if (targetPlayerId == null || senderPlayerId == null) return;
        Iterator<PendingInvite> it = pendingInvites.iterator();
        while (it.hasNext())
        {
            PendingInvite invite = it.next();
            if (invite.targetPlayerId.equals(targetPlayerId)) it.remove();
        }
        pendingInvites.add(new PendingInvite(targetPlayerId, senderPlayerId, ticks));
        markDirty();
    }

    public boolean acceptInvite(UUID targetPlayerId)
    {
        if (targetPlayerId == null) return false;
        Iterator<PendingInvite> it = pendingInvites.iterator();
        while (it.hasNext())
        {
            PendingInvite invite = it.next();
            if (invite.targetPlayerId.equals(targetPlayerId))
            {
                it.remove();
                if (worldObj != null && !worldObj.isRemote)
                {
                    for (Object obj : worldObj.loadedTileEntityList)
                    {
                        if (obj == this || !(obj instanceof TileEntityOneBlockGenerator)) continue;
                        TileEntityOneBlockGenerator other = (TileEntityOneBlockGenerator) obj;
                        if (other.hasAccess(targetPlayerId)) other.removeAccess(targetPlayerId);
                    }
                }
                if (!hasAccess(targetPlayerId)) addMember(targetPlayerId);
                if (!hasOwnerId) setOwnerId(invite.senderPlayerId);
                markDirty();
                return true;
            }
        }
        return false;
    }

    public boolean declineInvite(UUID targetPlayerId)
    {
        if (targetPlayerId == null) return false;
        Iterator<PendingInvite> it = pendingInvites.iterator();
        while (it.hasNext())
        {
            PendingInvite invite = it.next();
            if (invite.targetPlayerId.equals(targetPlayerId))
            {
                it.remove();
                markDirty();
                return true;
            }
        }
        return false;
    }

    public void tickInvites()
    {
        Iterator<PendingInvite> it = pendingInvites.iterator();
        while (it.hasNext())
        {
            PendingInvite invite = it.next();
            invite.ticksLeft--;
            if (invite.ticksLeft <= 0) it.remove();
        }
    }

    public List<PendingInvite> getPendingInvites() { return pendingInvites; }

    public void removeAccess(UUID playerId)
    {
        if (playerId == null) return;
        if (hasOwnerId && getOwnerId() != null && getOwnerId().equals(playerId))
        {
            clearOwnerId();
        }
        int[] arr = uuidToMostLeast(playerId);
        if (arr != null)
        {
            Iterator<int[]> it = memberIds.iterator();
            while (it.hasNext())
            {
                int[] m = it.next();
                if (m[0] == arr[0] && m[1] == arr[1]) it.remove();
            }
        }
        if (!hasOwnerId && memberIds.isEmpty()) placedByPlayer = false;
        markDirty();
    }

    public void clearOwnershipAndMembers()
    {
        clearOwnerId();
        memberIds.clear();
        placedByPlayer = false;
        markDirty();
    }

    public static class PendingInvite
    {
        public final UUID targetPlayerId;
        public final UUID senderPlayerId;
        public int ticksLeft;

        public PendingInvite(UUID target, UUID sender, int ticks)
        {
            this.targetPlayerId = target;
            this.senderPlayerId = sender;
            this.ticksLeft = ticks;
        }
    }

    public UUID getOwnerId()
    {
        if (!hasOwnerId) return null;
        return new UUID(ownerIdMsb, ownerIdLsb);
    }

    public void setOwnerId(UUID ownerId)
    {
        if (ownerId == null)
        {
            clearOwnerId();
        }
        else
        {
            this.ownerIdMsb = ownerId.getMostSignificantBits();
            this.ownerIdLsb = ownerId.getLeastSignificantBits();
            this.hasOwnerId = true;
        }
        this.memberIds.clear();
        markDirty();
    }

    private void clearOwnerId()
    {
        this.hasOwnerId = false;
        this.ownerIdMsb = 0;
        this.ownerIdLsb = 0;
        markDirty();
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString("selectedSetId", getSelectedSetId());
        compound.setBoolean("disableFluidGeneration", disableFluidGeneration);
        compound.setBoolean("disableMobGeneration", disableMobGeneration);
        compound.setBoolean("disableChestGeneration", disableChestGeneration);
        compound.setBoolean("disableSaplingGeneration", disableSaplingGeneration);
        compound.setBoolean("hasOwnerId", hasOwnerId);
        compound.setLong("ownerIdMsb", ownerIdMsb);
        compound.setLong("ownerIdLsb", ownerIdLsb);
        compound.setBoolean("placedByPlayer", placedByPlayer);
        compound.setLong("lastNonPlayerBreakTick", lastNonPlayerBreakTick);

        NBTTagList levelsTag = new NBTTagList();
        for (Map.Entry<String, Integer> entry : setLevels.entrySet())
        {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            NBTTagCompound levelTag = new NBTTagCompound();
            levelTag.setString("setId", entry.getKey());
            levelTag.setInteger("level", entry.getValue());
            levelsTag.appendTag(levelTag);
        }
        compound.setTag("setLevels", levelsTag);

        NBTTagList membersTag = new NBTTagList();
        for (int[] memberId : memberIds)
        {
            NBTTagCompound memberTag = new NBTTagCompound();
            memberTag.setInteger("most", memberId[0]);
            memberTag.setInteger("least", memberId[1]);
            membersTag.appendTag(memberTag);
        }
        compound.setTag("memberIds", membersTag);

        NBTTagList invitesTag = new NBTTagList();
        for (PendingInvite invite : pendingInvites)
        {
            if (invite == null || invite.targetPlayerId == null || invite.senderPlayerId == null) continue;
            NBTTagCompound inviteTag = new NBTTagCompound();
            int[] target = uuidToMostLeast(invite.targetPlayerId);
            int[] sender = uuidToMostLeast(invite.senderPlayerId);
            if (target != null && sender != null)
            {
                inviteTag.setInteger("targetMost", target[0]);
                inviteTag.setInteger("targetLeast", target[1]);
                inviteTag.setInteger("senderMost", sender[0]);
                inviteTag.setInteger("senderLeast", sender[1]);
            }
            inviteTag.setInteger("ticksLeft", invite.ticksLeft);
            invitesTag.appendTag(inviteTag);
        }
        compound.setTag("pendingInvites", invitesTag);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        selectedSetId = compound.getString("selectedSetId");
        disableFluidGeneration = compound.getBoolean("disableFluidGeneration");
        disableMobGeneration = compound.getBoolean("disableMobGeneration");
        disableChestGeneration = compound.getBoolean("disableChestGeneration");
        disableSaplingGeneration = compound.getBoolean("disableSaplingGeneration");
        hasOwnerId = compound.getBoolean("hasOwnerId");
        ownerIdMsb = compound.getLong("ownerIdMsb");
        ownerIdLsb = compound.getLong("ownerIdLsb");
        placedByPlayer = compound.getBoolean("placedByPlayer");
        lastNonPlayerBreakTick = compound.hasKey("lastNonPlayerBreakTick") ? compound.getLong("lastNonPlayerBreakTick") : Long.MIN_VALUE;

        setLevels.clear();
        if (compound.hasKey("setLevels"))
        {
            NBTTagList levelsTag = compound.getTagList("setLevels", 10);
            for (int i = 0; i < levelsTag.tagCount(); i++)
            {
                NBTTagCompound levelTag = (NBTTagCompound) levelsTag.getCompoundTagAt(i);
                if (levelTag.hasKey("setId") && levelTag.hasKey("level"))
                {
                    setLevels.put(levelTag.getString("setId"), levelTag.getInteger("level"));
                }
            }
        }

        memberIds.clear();
        if (compound.hasKey("memberIds"))
        {
            NBTTagList membersTag = compound.getTagList("memberIds", 10);
            for (int i = 0; i < membersTag.tagCount(); i++)
            {
                NBTTagCompound memberTag = (NBTTagCompound) membersTag.getCompoundTagAt(i);
                memberIds.add(new int[]{memberTag.getInteger("most"), memberTag.getInteger("least")});
            }
        }

        pendingInvites.clear();
        if (compound.hasKey("pendingInvites"))
        {
            NBTTagList invitesTag = compound.getTagList("pendingInvites", 10);
            for (int i = 0; i < invitesTag.tagCount(); i++)
            {
                NBTTagCompound inviteTag = (NBTTagCompound) invitesTag.getCompoundTagAt(i);
                UUID target = mostLeastToUUID(inviteTag.getInteger("targetMost"), inviteTag.getInteger("targetLeast"));
                UUID sender = mostLeastToUUID(inviteTag.getInteger("senderMost"), inviteTag.getInteger("senderLeast"));
                if (target != null && sender != null)
                {
                    pendingInvites.add(new PendingInvite(target, sender, inviteTag.getInteger("ticksLeft")));
                }
            }
        }
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.func_148857_g());
    }
}
