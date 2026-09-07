package dev.olliesbrother.skill.abilities.passive;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.player.PlayerDataManager;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillInstance;
import dev.olliesbrother.skill.SkillRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class RollPassive {

    private static final Identifier ACROBATICS_ID =
            Identifier.of(
                    "skillforge",
                    "acrobatics"
            );

    private RollPassive() {
    }

    public static float modifyDamage(
            LivingEntity target,
            DamageSource source,
            float originalDamage
    ) {

        /*
         * Roll only applies to players.
         */
        if (!(target
                instanceof ServerPlayerEntity player)) {

            return originalDamage;
        }

        if (!(player.getWorld()
                instanceof ServerWorld world)) {

            return originalDamage;
        }

        /*
         * Only vanilla fall damage.
         *
         * Elytra crashes, anvils, stalactites etc.
         * don't count.
         */
        if (!source.isOf(
                DamageTypes.FALL
        )) {

            return originalDamage;
        }

        if (player.isCreative()
                || player.isSpectator()) {

            return originalDamage;
        }

        var acrobatics =
                ConfigManager.get()
                        .acrobatics;

        if (!acrobatics.enabled) {
            return originalDamage;
        }

        var roll =
                acrobatics
                        .passives
                        .roll;

        if (!roll.enabled) {
            return originalDamage;
        }

        if (originalDamage
                < roll.minimumDamageToRoll) {

            return originalDamage;
        }

        Skill acrobaticsSkill =
                SkillRegistry.get(
                        ACROBATICS_ID
                );

        if (acrobaticsSkill == null) {
            return originalDamage;
        }

        SkillInstance acrobaticsData =
                PlayerDataManager
                        .get(player)
                        .getSkill(acrobaticsSkill);

        double chance =
                getRollChance(
                        acrobaticsData.getLevel()
                );

        if (world.getRandom()
                .nextDouble() >= chance) {

            return originalDamage;
        }

        double reduction =
                getDamageReductionFraction();

        float reducedDamage =
                (float) (
                        originalDamage
                                * (
                                1.0
                                        - reduction
                        )
                );

        /*
         * Chat rather than action bar so the
         * +Acrobatics XP message doesn't immediately
         * overwrite the Roll notification.
         */
        player.sendMessage(
                Text.literal(
                        "§aRoll! §eFall damage reduced by "
                                + formatPercent(reduction)
                                + "."
                ),
                false
        );

        return Math.max(
                0.0f,
                reducedDamage
        );
    }

    public static double getRollChance(
            int acrobaticsLevel
    ) {

        var config =
                ConfigManager.get()
                        .acrobatics
                        .passives
                        .roll;

        int level =
                Math.max(
                        0,
                        acrobaticsLevel
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

    public static double getDamageReductionFraction() {

        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        ConfigManager.get()
                                .acrobatics
                                .passives
                                .roll
                                .damageReductionFraction
                )
        );
    }

    private static String formatPercent(
            double value
    ) {

        double percent =
                value * 100.0;

        if (percent
                == Math.floor(percent)) {

            return ((int) percent) + "%";
        }

        return String.format(
                "%.1f%%",
                percent
        );
    }
}