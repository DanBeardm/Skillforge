package dev.olliesbrother.skill.events;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.experience.sources.WoodcuttingExperienceSource;
import dev.olliesbrother.persistence.SkillforgeState;
import dev.olliesbrother.skill.ExperienceManager;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillRegistry;
import dev.olliesbrother.skill.abilities.active.TreeFellerAbility;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class WoodcuttingEvents {

    private static final Identifier WOODCUTTING_ID =
            Identifier.of(
                    "skillforge",
                    "woodcutting"
            );

    private WoodcuttingEvents() {
    }

    public static void register() {

        PlayerBlockBreakEvents.AFTER.register(
                (world, player, pos, state, blockEntity) -> {

                    if (!(world instanceof ServerWorld serverWorld)) {
                        return;
                    }

                    if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                        return;
                    }

                    /*
                     * Remove any player-placed marker.
                     *
                     * The same tracking system used by Mining
                     * works for Woodcutting too.
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
                            .woodcutting
                            .enabled) {

                        return;
                    }

                    double experience =
                            WoodcuttingExperienceSource.INSTANCE
                                    .getExperience(state);

                    if (experience <= 0) {
                        return;
                    }

                    ItemStack tool =
                            serverPlayer.getMainHandStack();

                    if (ConfigManager.get()
                            .woodcutting
                            .requireCorrectTool
                            && !tool.isIn(ItemTags.AXES)) {

                        return;
                    }

                    if (ConfigManager.get()
                            .woodcutting
                            .requireCorrectTool
                            && !tool.isSuitableFor(state)) {

                        return;
                    }

                    Skill woodcutting =
                            SkillRegistry.get(
                                    WOODCUTTING_ID
                            );

                    if (woodcutting == null) {
                        return;
                    }

                    ExperienceManager.addExperience(
                            serverPlayer,
                            woodcutting,
                            experience
                    );

                    /*
                     * Only the manually broken log starts Tree Feller.
                     * Automatically broken logs still award XP but
                     * PROCESSING prevents recursive Tree Feller calls.
                     */
                    if (!TreeFellerAbility.isProcessing(serverPlayer)) {

                        TreeFellerAbility.tryFellTree(
                                serverWorld,
                                serverPlayer,
                                pos,
                                state
                        );
                    }
                }
        );
    }
}