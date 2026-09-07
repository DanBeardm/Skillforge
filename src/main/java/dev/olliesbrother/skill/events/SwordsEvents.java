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

public final class SwordsEvents {

    private static final Identifier SWORDS_ID =
            Identifier.of(
                    "skillforge",
                    "swords"
            );

    private SwordsEvents() {
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

                            if (!ConfigManager.get()
                                    .swords
                                    .enabled) {
                                return;
                            }

                            /*
                             * Do not let armour stands become
                             * free Swords XP.
                             */
                            if (killedEntity
                                    instanceof ArmorStandEntity) {
                                return;
                            }

                            /*
                             * PvP XP is disabled by default.
                             */
                            if (killedEntity
                                    instanceof ServerPlayerEntity
                                    && !ConfigManager.get()
                                    .swords
                                    .allowPlayerTargets) {

                                return;
                            }

                            ItemStack weapon =
                                    player.getMainHandStack();

                            if (!weapon.isIn(
                                    ItemTags.SWORDS
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
                        .swords;

        double experience =
                CombatExperienceHelper.calculateExperience(
                        killedEntity,
                        config.experiencePerHealth,
                        config.minimumExperience,
                        config.maximumExperience
                );

        if (experience <= 0) {
            return;
        }

        Skill swords =
                SkillRegistry.get(
                        SWORDS_ID
                );

        if (swords == null) {
            return;
        }

        ExperienceManager.addExperience(
                player,
                swords,
                experience
        );
    }
}