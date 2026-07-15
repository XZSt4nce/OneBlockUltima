package ru.defea.oneblockultima.item;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class ItemNaturalPoison extends CustomPotion {
    public ItemNaturalPoison() {
        super("natural_poison",
                new PotionEffect[]{
                        new PotionEffect(MobEffects.POISON, 30 * 20, 1)
                });
    }
}
