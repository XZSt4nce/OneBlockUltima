package ru.defea.oneblockultima.item;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class ItemLiquidDeath extends CustomPotion {
    public ItemLiquidDeath() {
        super("liquid_death",
                new PotionEffect[]{
                        new PotionEffect(MobEffects.INSTANT_DAMAGE, 600 * 20, 123)
                });
    }
}
