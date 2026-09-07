package dev.olliesbrother.skill.abilities.passive;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.config.SkillforgeConfig;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class TreasureHunterPassive {

    private TreasureHunterPassive() {
    }

    public static void tryFindTreasure(
            ServerWorld world,
            ServerPlayerEntity player,
            BlockPos pos,
            int excavationLevel
    ) {

        var config =
                ConfigManager.get()
                        .excavation
                        .passives
                        .treasureHunter;

        if (!config.enabled) {
            return;
        }

        /*
         * Stage 1:
         *
         * Decide whether Treasure Hunter triggers at all.
         */
        double treasureChance =
                getTreasureChance(
                        excavationLevel
                );

        if (world.getRandom().nextDouble()
                >= treasureChance) {

            return;
        }

        /*
         * Stage 2:
         *
         * Build the pool of valid rewards currently
         * unlocked by the player's Excavation level.
         */
        List<SkillforgeConfig.TreasureRewardConfig> unlockedRewards =
                new ArrayList<>();

        double totalWeight = 0.0;

        for (SkillforgeConfig.TreasureRewardConfig reward
                : config.rewards) {

            if (excavationLevel
                    < reward.minimumLevel) {

                continue;
            }

            if (reward.weight <= 0) {
                continue;
            }

            Identifier itemId =
                    Identifier.tryParse(
                            reward.item
                    );

            if (itemId == null) {
                continue;
            }

            if (!Registries.ITEM
                    .containsId(itemId)) {

                continue;
            }

            unlockedRewards.add(
                    reward
            );

            totalWeight +=
                    reward.weight;
        }

        if (unlockedRewards.isEmpty()
                || totalWeight <= 0) {

            return;
        }

        /*
         * Pick exactly one reward using its weight.
         */
        double roll =
                world.getRandom()
                        .nextDouble()
                        * totalWeight;

        double runningWeight = 0.0;

        for (SkillforgeConfig.TreasureRewardConfig reward
                : unlockedRewards) {

            runningWeight +=
                    reward.weight;

            if (roll >= runningWeight) {
                continue;
            }

            dropReward(
                    world,
                    player,
                    pos,
                    reward
            );

            return;
        }
    }

    /*
     * Shared calculation so /skills excavation can
     * display the same chance the passive actually uses.
     */
    public static double getTreasureChance(
            int excavationLevel
    ) {

        var config =
                ConfigManager.get()
                        .excavation
                        .passives
                        .treasureHunter;

        int level =
                Math.max(
                        0,
                        excavationLevel
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

    private static void dropReward(
            ServerWorld world,
            ServerPlayerEntity player,
            BlockPos pos,
            SkillforgeConfig.TreasureRewardConfig reward
    ) {

        Identifier itemId =
                Identifier.tryParse(
                        reward.item
                );

        if (itemId == null
                || !Registries.ITEM.containsId(itemId)) {

            return;
        }

        Item item =
                Registries.ITEM.get(
                        itemId
                );

        int amount =
                Math.max(
                        1,
                        reward.amount
                );

        ItemStack treasure =
                new ItemStack(
                        item,
                        amount
                );

        Block.dropStack(
                world,
                pos,
                treasure
        );

        player.sendMessage(
                Text.literal(
                        "§6Treasure Hunter! §eFound "
                                + treasure
                                .getName()
                                .getString()
                                + "!"
                ),
                true
        );
    }
}