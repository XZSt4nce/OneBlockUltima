package ru.defea.oneblockultima.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import ru.defea.oneblockultima.OneBlockUltima;

import javax.annotation.Nullable;

public abstract class CustomPotion extends ItemPotion {
    private final PotionEffect[] potionEffects;

    public CustomPotion(String name, PotionEffect[] potionEffects) {
        setCreativeTab(OneBlockUltima.modTab);
        this.setRegistryName(name);
        this.setUnlocalizedName(name);
        this.potionEffects = potionEffects;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        // Добавляем эффекты при создании предмета
        addPotionEffectsToStack(stack);
        return super.initCapabilities(stack, nbt);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            ItemStack stack = new ItemStack(this);
            items.add(stack);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return net.minecraft.util.text.translation.I18n.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name").trim();
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        ItemStack resultStack = super.onItemUseFinish(stack, worldIn, entityLiving);

        if (!worldIn.isRemote) {
            for (PotionEffect potionEffect : potionEffects)
            {
                entityLiving.addPotionEffect(potionEffect);
            }
            entityLiving.addPotionEffect(new PotionEffect(MobEffects.POISON, 30 * 20, 2));
        }

        return resultStack;
    }

    public void addPotionEffectsToStack(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
        }

        NBTTagList effectsList = nbt.getTagList("CustomPotionEffects", Constants.NBT.TAG_COMPOUND);

        // Сохраняем эффект в NBT
        for (PotionEffect potionEffect : potionEffects)
        {
            NBTTagCompound effectTag = new NBTTagCompound();
            potionEffect.writeCustomPotionEffectToNBT(effectTag);
            effectsList.appendTag(effectTag);
        }

        nbt.setTag("CustomPotionEffects", effectsList);
        stack.setTagCompound(nbt);
    }
}
