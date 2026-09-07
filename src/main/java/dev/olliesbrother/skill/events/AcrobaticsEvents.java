package dev.olliesbrother.skill.events;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.skill.ExperienceManager;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AcrobaticsEvents {

    private static final Identifier ACROBATICS_ID =
            Identifier.of(
                    "skillforge",
                    "acrobatics"
            );

    /*
     * Effective health immediately before a fall:
     *
     * normal health + absorption health
     *
     * This lets us calculate what was actually lost
     * after Minecraft finishes processing the fall.
     */
    private static final Map<UUID, Float>
            PRE_FALL_HEALTH =
            new HashMap<>();

    private AcrobaticsEvents() {
    }

    public static void register() {

        /*
         * Record the player's state immediately before
         * Minecraft processes fall damage.
         *
         * Returning true means Skillforge does not
         * cancel or otherwise interfere with the fall.
         */
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
                (entity, source, amount) -> {

                    if (!(entity
                            instanceof ServerPlayerEntity player)) {

                        return true;
                    }

                    if (!source.isOf(
                            DamageTypes.FALL
                    )) {

                        return true;
                    }

                    if (player.isCreative()
                            || player.isSpectator()) {

                        return true;
                    }

                    float effectiveHealth =
                            player.getHealth()
                                    + player.getAbsorptionAmount();

                    PRE_FALL_HEALTH.put(
                            player.getUuid(),
                            effectiveHealth
                    );

                    return true;
                }
        );

        /*
         * AFTER_DAMAGE is not called if the entity was
         * killed, which conveniently means lethal falls
         * don't award Acrobatics XP.
         */
        ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (entity,
                 source,
                 baseDamageTaken,
                 damageTaken,
                 blocked) -> {

                    if (!(entity
                            instanceof ServerPlayerEntity player)) {

                        return;
                    }

                    if (!source.isOf(
                            DamageTypes.FALL
                    )) {

                        return;
                    }

                    Float healthBefore =
                            PRE_FALL_HEALTH.remove(
                                    player.getUuid()
                            );

                    if (healthBefore == null) {
                        return;
                    }

                    if (player.isCreative()
                            || player.isSpectator()) {

                        return;
                    }

                    if (!ConfigManager.get()
                            .acrobatics
                            .enabled) {

                        return;
                    }

                    float healthAfter =
                            player.getHealth()
                                    + player.getAbsorptionAmount();

                    double actualDamage =
                            Math.max(
                                    0.0,
                                    healthBefore
                                            - healthAfter
                            );

                    awardExperience(
                            player,
                            actualDamage
                    );
                }
        );

        /*
         * AFTER_DAMAGE doesn't run for lethal falls,
         * so clear any stored entry when the player dies.
         */
        ServerLivingEntityEvents.AFTER_DEATH.register(
                (entity, source) -> {

                    if (entity
                            instanceof ServerPlayerEntity player) {

                        PRE_FALL_HEALTH.remove(
                                player.getUuid()
                        );
                    }
                }
        );
    }

    private static void awardExperience(
            ServerPlayerEntity player,
            double damageTaken
    ) {

        var config =
                ConfigManager.get()
                        .acrobatics;

        if (damageTaken
                < config.minimumDamageForExperience) {

            return;
        }

        double experience =
                damageTaken
                        * config.experiencePerDamage;

        experience =
                Math.min(
                        experience,
                        config.maximumExperiencePerFall
                );

        if (experience <= 0.0) {
            return;
        }

        Skill acrobatics =
                SkillRegistry.get(
                        ACROBATICS_ID
                );

        if (acrobatics == null) {
            return;
        }

        ExperienceManager.addExperience(
                player,
                acrobatics,
                experience
        );
    }
}