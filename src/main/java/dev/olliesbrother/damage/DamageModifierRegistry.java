package dev.olliesbrother.damage;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

import java.util.ArrayList;
import java.util.List;

public final class DamageModifierRegistry {

    private static final List<IncomingDamageModifier>
            INCOMING_DAMAGE_MODIFIERS =
            new ArrayList<>();

    private static final List<ArmorModifier>
            ARMOR_MODIFIERS =
            new ArrayList<>();

    private DamageModifierRegistry() {
    }

    public static void registerIncoming(
            IncomingDamageModifier modifier
    ) {

        INCOMING_DAMAGE_MODIFIERS.add(
                modifier
        );
    }

    public static void registerArmor(
            ArmorModifier modifier
    ) {

        ARMOR_MODIFIERS.add(
                modifier
        );
    }

    public static float modifyIncomingDamage(
            LivingEntity target,
            DamageSource source,
            float originalDamage
    ) {

        float damage =
                originalDamage;

        for (IncomingDamageModifier modifier
                : INCOMING_DAMAGE_MODIFIERS) {

            damage =
                    modifier.modify(
                            target,
                            source,
                            damage
                    );

            /*
             * Nothing should ever make incoming
             * damage negative.
             */
            damage =
                    Math.max(
                            0.0f,
                            damage
                    );
        }

        return damage;
    }

    public static float modifyArmor(
            LivingEntity target,
            DamageSource source,
            float originalArmor
    ) {

        float armor =
                originalArmor;

        for (ArmorModifier modifier
                : ARMOR_MODIFIERS) {

            armor =
                    modifier.modify(
                            target,
                            source,
                            armor
                    );

            armor =
                    Math.max(
                            0.0f,
                            armor
                    );
        }

        return armor;
    }

    public static int getIncomingModifierCount() {

        return INCOMING_DAMAGE_MODIFIERS.size();
    }

    public static int getArmorModifierCount() {

        return ARMOR_MODIFIERS.size();
    }
}