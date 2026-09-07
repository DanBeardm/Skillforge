package dev.olliesbrother.skill.events;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.experience.sources.ExcavationExperienceSource;
import dev.olliesbrother.persistence.SkillforgeState;
import dev.olliesbrother.player.PlayerDataManager;
import dev.olliesbrother.skill.ExperienceManager;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillInstance;
import dev.olliesbrother.skill.SkillRegistry;
import dev.olliesbrother.skill.abilities.active.GigaDrillAbility;
import dev.olliesbrother.skill.abilities.passive.TreasureHunterPassive;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class ExcavationEvents {

    private static final Identifier EXCAVATION_ID =
            Identifier.of(
                    "skillforge",
                    "excavation"
            );

    private ExcavationEvents() {
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
                            .excavation
                            .enabled) {

                        return;
                    }

                    double experience =
                            ExcavationExperienceSource.INSTANCE
                                    .getExperience(state);

                    if (experience <= 0) {
                        return;
                    }

                    ItemStack tool =
                            serverPlayer
                                    .getMainHandStack();

                    if (ConfigManager.get()
                            .excavation
                            .requireCorrectTool) {

                        if (!tool.isIn(
                                ItemTags.SHOVELS
                        )) {
                            return;
                        }

                        if (!tool.isSuitableFor(
                                state
                        )) {
                            return;
                        }
                    }

                    Skill excavation =
                            SkillRegistry.get(
                                    EXCAVATION_ID
                            );

                    if (excavation == null) {
                        return;
                    }

                    SkillInstance instance =
                            PlayerDataManager
                                    .get(serverPlayer)
                                    .getSkill(
                                            excavation
                                    );

                    /*
                     * Every valid block, including Giga Drill's
                     * automatic blocks, awards Excavation XP.
                     */
                    ExperienceManager.addExperience(
                            serverPlayer,
                            excavation,
                            experience
                    );

                    /*
                     * Treasure Hunter only rolls on the block
                     * manually dug by the player.
                     *
                     * Giga Drill's additional eight blocks do NOT
                     * multiply treasure rolls.
                     */
                    if (!GigaDrillAbility
                            .isProcessing(serverPlayer)) {

                        TreasureHunterPassive
                                .tryFindTreasure(
                                        serverWorld,
                                        serverPlayer,
                                        pos,
                                        instance.getLevel()
                                );
                    }

                    /*
                     * Only the manually broken block can initiate
                     * the next 3x3.
                     */
                    if (GigaDrillAbility
                            .isActive(serverPlayer)
                            && !GigaDrillAbility
                            .isProcessing(serverPlayer)) {

                        GigaDrillAbility.breakArea(
                                serverWorld,
                                serverPlayer,
                                pos
                        );
                    }
                }
        );
    }
}