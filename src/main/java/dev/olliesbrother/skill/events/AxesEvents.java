package dev.olliesbrother.skill.events;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.experience.CombatExperienceHelper;
import dev.olliesbrother.skill.ExperienceManager;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class AxesEvents {

    private static final Identifier AXES_ID =
            Identifier.of(
                    "skillforge",
                    "axes"
            );

    private AxesEvents() {
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
                                            .axes;

                            if (!config.enabled) {
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

                            ItemStack weapon =
                                    player.getMainHandStack();

                            if (!weapon.isIn(
                                    ItemTags.AXES
                            )) {
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
                        .axes;

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

        Skill axes =
                SkillRegistry.get(
                        AXES_ID
                );

        if (axes == null) {
            return;
        }

        ExperienceManager.addExperience(
                player,
                axes,
                experience
        );
    }
}