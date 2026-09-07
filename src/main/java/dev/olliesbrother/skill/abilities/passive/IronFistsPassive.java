package dev.olliesbrother.skill.abilities.passive;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.player.PlayerDataManager;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillInstance;
import dev.olliesbrother.skill.SkillRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class IronFistsPassive {

    private static final Identifier UNARMED_ID =
            Identifier.of(
                    "skillforge",
                    "unarmed"
            );

    private IronFistsPassive() {
    }

    public static float modifyDamage(
            LivingEntity target,
            DamageSource source,
            float originalDamage
    ) {

        /*
         * Server only.
         */
        if (!(target.getWorld()
                instanceof ServerWorld)) {

            return originalDamage;
        }

        /*
         * Only direct player melee attacks.
         */
        if (!source.isOf(
                DamageTypes.PLAYER_ATTACK
        )) {

            return originalDamage;
        }

        if (!(source.getAttacker()
                instanceof ServerPlayerEntity player)) {

            return originalDamage;
        }

        if (player.isCreative()) {
            return originalDamage;
        }

        var unarmedConfig =
                ConfigManager.get()
                        .unarmed;

        if (!unarmedConfig.enabled) {
            return originalDamage;
        }

        var ironFists =
                unarmedConfig
                        .passives
                        .ironFists;

        if (!ironFists.enabled) {
            return originalDamage;
        }

        /*
         * Main hand must genuinely be empty.
         *
         * Offhand items are fine.
         */
        if (!player
                .getMainHandStack()
                .isEmpty()) {

            return originalDamage;
        }

        if (target
                instanceof ArmorStandEntity) {

            return originalDamage;
        }

        if (target
                instanceof ServerPlayerEntity
                && !unarmedConfig.allowPlayerTargets) {

            return originalDamage;
        }

        Skill unarmed =
                SkillRegistry.get(
                        UNARMED_ID
                );

        if (unarmed == null) {
            return originalDamage;
        }

        SkillInstance unarmedData =
                PlayerDataManager
                        .get(player)
                        .getSkill(unarmed);

        double bonusDamage =
                getBonusDamage(
                        unarmedData.getLevel()
                );

        if (bonusDamage <= 0.0) {
            return originalDamage;
        }

        return (float) (
                originalDamage
                        + bonusDamage
        );
    }

    public static double getBonusDamage(
            int unarmedLevel
    ) {

        var config =
                ConfigManager.get()
                        .unarmed
                        .passives
                        .ironFists;

        int level =
                Math.max(
                        0,
                        unarmedLevel
                );

        double bonus =
                level
                        * Math.max(
                        0.0,
                        config.bonusDamagePerLevel
                );

        return Math.min(
                bonus,
                Math.max(
                        0.0,
                        config.maximumBonusDamage
                )
        );
    }
}