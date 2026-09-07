package dev.olliesbrother.mixin;

import dev.olliesbrother.damage.DamageModifierRegistry;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /*
     * Skillforge incoming damage pipeline.
     *
     * Examples currently include:
     *
     * - Axes: Critical Strike
     * - Unarmed: Iron Fists
     * - Acrobatics: Roll
     *
     * The mixin itself does not need to know
     * which abilities are registered.
     */
    @ModifyVariable(
            method = "damage",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private float skillforge$modifyIncomingDamage(
            float amount,
            DamageSource source
    ) {

        LivingEntity target =
                (LivingEntity) (Object) this;

        return DamageModifierRegistry
                .modifyIncomingDamage(
                        target,
                        source,
                        amount
                );
    }

    /*
     * Skillforge armor modification pipeline.
     *
     * Currently used by:
     *
     * - Archery: Piercing Shot
     */
    @Redirect(
            method = "applyArmorToDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/DamageUtil;getDamageLeft(Lnet/minecraft/entity/LivingEntity;FLnet/minecraft/entity/damage/DamageSource;FF)F"
            )
    )
    private float skillforge$modifyArmor(
            LivingEntity armorWearer,
            float damageAmount,
            DamageSource source,
            float armor,
            float armorToughness
    ) {

        float modifiedArmor =
                DamageModifierRegistry
                        .modifyArmor(
                                armorWearer,
                                source,
                                armor
                        );

        return DamageUtil.getDamageLeft(
                armorWearer,
                damageAmount,
                source,
                modifiedArmor,
                armorToughness
        );
    }
}