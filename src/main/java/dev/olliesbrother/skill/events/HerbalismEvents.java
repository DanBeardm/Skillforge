package dev.olliesbrother.skill.events;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.experience.sources.HerbalismExperienceSource;
import dev.olliesbrother.persistence.SkillforgeState;
import dev.olliesbrother.player.PlayerDataManager;
import dev.olliesbrother.skill.ExperienceManager;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillInstance;
import dev.olliesbrother.skill.SkillRegistry;
import dev.olliesbrother.skill.abilities.passive.GreenThumbPassive;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class HerbalismEvents {

    private static final Identifier HERBALISM_ID =
            Identifier.of(
                    "skillforge",
                    "herbalism"
            );

    private HerbalismEvents() {
    }

    public static void register() {

        PlayerBlockBreakEvents.AFTER.register(
                (world, player, pos, state, blockEntity) -> {

                    if (!(world
                            instanceof ServerWorld serverWorld)) {

                        return;
                    }

                    if (!(player
                            instanceof ServerPlayerEntity serverPlayer)) {

                        return;
                    }

                    /*
                     * Important for things such as manually
                     * placed melon and pumpkin blocks.
                     */
                    boolean playerPlaced =
                            SkillforgeState
                                    .get(serverWorld.getServer())
                                    .isPlayerPlaced(
                                            serverWorld,
                                            pos
                                    );

                    if (playerPlaced) {
                        return;
                    }

                    if (serverPlayer.isCreative()) {
                        return;
                    }

                    if (!ConfigManager.get()
                            .herbalism
                            .enabled) {

                        return;
                    }

                    /*
                     * This returns 0 for immature crops.
                     */
                    double experience =
                            HerbalismExperienceSource.INSTANCE
                                    .getExperience(state);

                    if (experience <= 0) {
                        return;
                    }

                    Skill herbalism =
                            SkillRegistry.get(
                                    HERBALISM_ID
                            );

                    if (herbalism == null) {
                        return;
                    }

                    SkillInstance herbalismData =
                            PlayerDataManager
                                    .get(serverPlayer)
                                    .getSkill(herbalism);

                    ExperienceManager.addExperience(
                            serverPlayer,
                            herbalism,
                            experience
                    );

                    /*
                     * Try Green Thumb after the crop has been
                     * successfully harvested and XP awarded.
                     */
                    GreenThumbPassive.tryReplant(
                            serverWorld,
                            serverPlayer,
                            pos,
                            state,
                            herbalismData.getLevel()
                    );

                    ExperienceManager.addExperience(
                            serverPlayer,
                            herbalism,
                            experience
                    );
                }
        );
    }
}