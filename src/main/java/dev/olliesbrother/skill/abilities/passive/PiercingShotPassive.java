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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class PiercingShotPassive {

    private static final Identifier ARCHERY_ID =
            Identifier.of(
                    "skillforge",
                    "archery"
            );

    private PiercingShotPassive() {
    }

    public static float modifyArmor(
            LivingEntity target,
            DamageSource source,
            float vanillaArmor
    ) {

        if (!(target.getWorld()
                instanceof ServerWorld world)) {

            return vanillaArmor;
        }

        if (!source.isOf(
                DamageTypes.ARROW
        )) {
            return vanillaArmor;
        }

        if (!(source.getAttacker()
                instanceof ServerPlayerEntity player)) {

            return vanillaArmor;
        }

        if (player.isCreative()) {
            return vanillaArmor;
        }

        var archeryConfig =
                ConfigManager.get()
                        .archery;

        if (!archeryConfig.enabled) {
            return vanillaArmor;
        }

        var piercingConfig =
                archeryConfig
                        .passives
                        .piercingShot;

        if (!piercingConfig.enabled) {
            return vanillaArmor;
        }

        if (target
                instanceof ArmorStandEntity) {

            return vanillaArmor;
        }

        if (target
                instanceof ServerPlayerEntity
                && !archeryConfig.allowPlayerTargets) {

            return vanillaArmor;
        }

        /*
         * Nothing to pierce.
         */
        if (vanillaArmor <= 0.0f) {
            return vanillaArmor;
        }

        Skill archery =
                SkillRegistry.get(
                        ARCHERY_ID
                );

        if (archery == null) {
            return vanillaArmor;
        }

        SkillInstance archeryData =
                PlayerDataManager
                        .get(player)
                        .getSkill(archery);

        double chance =
                getPiercingShotChance(
                        archeryData.getLevel()
                );

        if (world.getRandom()
                .nextDouble() >= chance) {

            return vanillaArmor;
        }

        double ignoreFraction =
                getArmorIgnoreFraction();

        float effectiveArmor =
                (float) (
                        vanillaArmor
                                * (
                                1.0
                                        - ignoreFraction
                        )
                );

        player.sendMessage(
                Text.literal(
                        "§bPiercing Shot!"
                ),
                false
        );

        return Math.max(
                0.0f,
                effectiveArmor
        );
    }

    public static double getPiercingShotChance(
            int archeryLevel
    ) {

        var config =
                ConfigManager.get()
                        .archery
                        .passives
                        .piercingShot;

        int level =
                Math.max(
                        0,
                        archeryLevel
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

    public static double getArmorIgnoreFraction() {

        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        ConfigManager.get()
                                .archery
                                .passives
                                .piercingShot
                                .armorIgnoreFraction
                )
        );
    }
}