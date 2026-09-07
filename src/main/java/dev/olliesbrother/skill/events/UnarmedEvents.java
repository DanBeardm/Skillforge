package dev.olliesbrother.skill.events;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.experience.CombatExperienceHelper;
import dev.olliesbrother.skill.ExperienceManager;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class UnarmedEvents {

    private static final Identifier UNARMED_ID =
            Identifier.of(
                    "skillforge",
                    "unarmed"
            );

    private UnarmedEvents() {
    }

    public static void register() {

        ServerEntityCombatEvents
                .AFTER_KILLED_OTHER_ENTITY
                .register(
                        (world, killer, killedEntity) -> {

                            if (!(killer
                                    instanceof ServerPlayerEntity player)) {
                                return;
                            }

                            if (player.isCreative()) {
                                return;
                            }

                            var config =
                                    ConfigManager.get()
                                            .unarmed;

                            if (!config.enabled) {
                                return;
                            }

                            /*
                             * Unarmed means the player's
                             * MAIN HAND is completely empty.
                             *
                             * Something in the offhand is fine.
                             * For example:
                             *
                             * Empty main hand + shield
                             * still counts as Unarmed.
                             */
                            if (!player
                                    .getMainHandStack()
                                    .isEmpty()) {

                                return;
                            }

                            if (killedEntity
                                    instanceof ArmorStandEntity) {

                                return;
                            }

                            if (killedEntity
                                    instanceof ServerPlayerEntity
                                    && !config.allowPlayerTargets) {

                                return;
                            }

                            awardExperience(
                                    player,
                                    killedEntity
                            );
                        }
                );
    }

    private static void awardExperience(
            ServerPlayerEntity player,
            LivingEntity killedEntity
    ) {

        var config =
                ConfigManager.get()
                        .unarmed;

        double experience =
                CombatExperienceHelper
                        .calculateExperience(
                                killedEntity,
                                config.experiencePerHealth,
                                config.minimumExperience,
                                config.maximumExperience
                        );

        if (experience <= 0) {
            return;
        }

        Skill unarmed =
                SkillRegistry.get(
                        UNARMED_ID
                );

        if (unarmed == null) {
            return;
        }

        ExperienceManager.addExperience(
                player,
                unarmed,
                experience
        );
    }
}