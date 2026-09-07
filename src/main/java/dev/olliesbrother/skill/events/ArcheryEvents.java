package dev.olliesbrother.skill.events;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.experience.CombatExperienceHelper;
import dev.olliesbrother.skill.ExperienceManager;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class ArcheryEvents {

    private static final Identifier ARCHERY_ID =
            Identifier.of(
                    "skillforge",
                    "archery"
            );

    private ArcheryEvents() {
    }

    public static void register() {

        ServerLivingEntityEvents.AFTER_DEATH.register(
                (killedEntity, damageSource) -> {

                    var config =
                            ConfigManager.get()
                                    .archery;

                    if (!config.enabled) {
                        return;
                    }

                    /*
                     * Only arrow damage.
                     *
                     * This excludes:
                     * - tridents
                     * - thrown items
                     * - fireballs
                     * - melee attacks
                     * - mob projectiles
                     */
                    if (!damageSource.isOf(
                            DamageTypes.ARROW
                    )) {
                        return;
                    }

                    /*
                     * DamageSource retains the attacker
                     * who fired the projectile.
                     *
                     * Therefore the player does not need
                     * to still be holding their bow when
                     * the arrow lands.
                     */
                    if (!(damageSource.getAttacker()
                            instanceof ServerPlayerEntity player)) {

                        return;
                    }

                    if (player.isCreative()) {
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
            net.minecraft.entity.LivingEntity killedEntity
    ) {

        var config =
                ConfigManager.get()
                        .archery;

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

        Skill archery =
                SkillRegistry.get(
                        ARCHERY_ID
                );

        if (archery == null) {
            return;
        }

        ExperienceManager.addExperience(
                player,
                archery,
                experience
        );
    }
}