package ru.defea.oneblockultima.item;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class ItemSuperPoison extends CustomPotion {
    public ItemSuperPoison() {
        super("super_poison",
                new PotionEffect[]{
                        new PotionEffect(MobEffects.POISON, 90 * 20, 3),
                        new PotionEffect(MobEffects.WITHER, 30 * 20, 2),
                });
    }
}
