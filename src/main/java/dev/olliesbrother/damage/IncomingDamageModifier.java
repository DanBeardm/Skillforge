package dev.olliesbrother.damage;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

@FunctionalInterface
public interface IncomingDamageModifier {

    float modify(
            LivingEntity target,
            DamageSource source,
            float damage
    );
}