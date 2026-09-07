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
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class CriticalStrikePassive {

    private static final Identifier AXES_ID =
            Identifier.of(
                    "skillforge",
                    "axes"
            );

    private CriticalStrikePassive() {
    }

    public static float modifyDamage(
            LivingEntity target,
            DamageSource source,
            float originalDamage
    ) {

        if (!(target.getWorld()
                instanceof ServerWorld world)) {

            return originalDamage;
        }

        /*
         * Only direct vanilla-style player melee attacks.
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

        var axesConfig =
                ConfigManager.get()
                        .axes;

        if (!axesConfig.enabled) {
            return originalDamage;
        }

        var criticalConfig =
                axesConfig
                        .passives
                        .criticalStrike;

        if (!criticalConfig.enabled) {
            return originalDamage;
        }

        if (target
                instanceof ArmorStandEntity) {

            return originalDamage;
        }

        if (target
                instanceof ServerPlayerEntity
                && !axesConfig.allowPlayerTargets) {

            return originalDamage;
        }

        /*
         * Must actually be attacking with an axe.
         */
        if (!player
                .getMainHandStack()
                .isIn(ItemTags.AXES)) {

            return originalDamage;
        }

        Skill axes =
                SkillRegistry.get(
                        AXES_ID
                );

        if (axes == null) {
            return originalDamage;
        }

        SkillInstance axesData =
                PlayerDataManager
                        .get(player)
                        .getSkill(axes);

        double chance =
                getCriticalStrikeChance(
                        axesData.getLevel()
                );

        if (world.getRandom()
                .nextDouble() >= chance) {

            return originalDamage;
        }

        double bonusMultiplier =
                Math.max(
                        0.0,
                        criticalConfig
                                .bonusDamageMultiplier
                );

        float modifiedDamage =
                (float) (
                        originalDamage
                                * (
                                1.0
                                        + bonusMultiplier
                        )
                );

        player.sendMessage(
                Text.literal(
                        "§6Critical Strike!"
                ),
                true
        );

        return modifiedDamage;
    }

    public static double getCriticalStrikeChance(
            int axesLevel
    ) {

        var config =
                ConfigManager.get()
                        .axes
                        .passives
                        .criticalStrike;

        int level =
                Math.max(
                        0,
                        axesLevel
                );

        double chance =
                config.baseChance
                        + (
                        level
                                * config.chancePerLevel
                );

        chance =
                Math.min(
                        chance,
                        config.maxChance
                );

        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        chance
                )
        );
    }

    public static double getDamageMultiplier() {

        return 1.0
                + Math.max(
                0.0,
                ConfigManager.get()
                        .axes
                        .passives
                        .criticalStrike
                        .bonusDamageMultiplier
        );
    }
}