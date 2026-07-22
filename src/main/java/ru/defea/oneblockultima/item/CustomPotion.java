package ru.defea.oneblockultima.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.defea.oneblockultima.OneBlockUltima;

import java.util.List;

public abstract class CustomPotion extends Item
{
    private final PotionEffect[] potionEffects;
    private final String name;

    public CustomPotion(String name, PotionEffect[] potionEffects)
    {
        super();
        this.potionEffects = potionEffects;
        this.name = name;
        setCreativeTab(OneBlockUltima.modTab);
        setUnlocalizedName("oneblockultima." + name);
        setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (!world.isRemote)
        {
            for (PotionEffect effect : potionEffects)
            {
                player.addPotionEffect(effect);
            }
        }
        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltipList, boolean advanced)
    {
        for (PotionEffect effect : potionEffects)
        {
            tooltipList.add(effect.getEffectName());
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register)
    {
        this.itemIcon = register.registerIcon(OneBlockUltima.MODID + ":item_" + name);
    }
}
