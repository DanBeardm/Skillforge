package dev.olliesbrother.skill.abilities.passive;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.player.PlayerDataManager;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillInstance;
import dev.olliesbrother.skill.SkillRegistry;
import dev.olliesbrother.skill.events.FishingEvents;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;

public final class AnglersFortunePassive {

    private static final Identifier FISHING_ID =
            Identifier.of(
                    "skillforge",
                    "fishing"
            );

    private AnglersFortunePassive() {
    }

    public static void tryUpgradeCatch(
            ServerPlayerEntity player,
            ItemStack rod,
            FishingBobberEntity bobber,
            Collection<ItemStack> fishingLoot
    ) {

        var config =
                ConfigManager.get()
                        .fishing
                        .passives
                        .anglersFortune;

        if (!config.enabled) {
            return;
        }

        if (player.isCreative()) {
            return;
        }

        if (!(bobber.getWorld()
                instanceof ServerWorld world)) {

            return;
        }

        if (fishingLoot == null
                || fishingLoot.isEmpty()) {

            return;
        }

        Skill fishing =
                SkillRegistry.get(
                        FISHING_ID
                );

        if (fishing == null) {
            return;
        }

        SkillInstance fishingData =
                PlayerDataManager
                        .get(player)
                        .getSkill(fishing);

        int level =
                fishingData.getLevel();

        FishingEvents.CatchType catchType =
                FishingEvents.classifyCatch(
                        fishingLoot
                );

        /*
         * Treasure is already the best category.
         */
        if (catchType
                == FishingEvents.CatchType.TREASURE) {

            return;
        }

        /*
         * Junk -> Fish
         */
        if (catchType
                == FishingEvents.CatchType.JUNK) {

            double chance =
                    getJunkToFishChance(level);

            if (world.getRandom()
                    .nextDouble() >= chance) {

                return;
            }

            if (replaceLoot(
                    world,
                    player,
                    rod,
                    bobber,
                    fishingLoot,
                    LootTables.FISHING_FISH_GAMEPLAY
            )) {

                player.sendMessage(
                        Text.literal(
                                "§6Angler's Fortune! §eJunk upgraded to Fish!"
                        ),
                        false
                );
            }

            return;
        }

        /*
         * Fish -> Treasure
         */
        if (catchType
                == FishingEvents.CatchType.FISH) {

            if (config.requireOpenWaterForTreasure
                    && !bobber.isInOpenWater()) {

                return;
            }

            double chance =
                    getFishToTreasureChance(
                            level
                    );

            if (world.getRandom()
                    .nextDouble() >= chance) {

                return;
            }

            if (replaceLoot(
                    world,
                    player,
                    rod,
                    bobber,
                    fishingLoot,
                    LootTables.FISHING_TREASURE_GAMEPLAY
            )) {

                player.sendMessage(
                        Text.literal(
                                "§6Angler's Fortune! §eFish upgraded to Treasure!"
                        ),
                        false
                );
            }
        }
    }

    /*
     * Generate replacement loot directly from one of
     * Minecraft's normal fishing sub-tables.
     */
    private static boolean replaceLoot(
            ServerWorld world,
            ServerPlayerEntity player,
            ItemStack rod,
            FishingBobberEntity bobber,
            Collection<ItemStack> existingLoot,
            RegistryKey<LootTable> lootTableKey
    ) {

        LootTable lootTable =
                world.getServer()
                        .getReloadableRegistries()
                        .getLootTable(
                                lootTableKey
                        );

        LootContextParameterSet parameters =
                new LootContextParameterSet.Builder(
                        world
                )
                        .add(
                                LootContextParameters.ORIGIN,
                                bobber.getPos()
                        )
                        .add(
                                LootContextParameters.TOOL,
                                rod
                        )
                        .add(
                                LootContextParameters.THIS_ENTITY,
                                bobber
                        )
                        .luck(
                                player.getLuck()
                        )
                        .build(
                                LootContextTypes.FISHING
                        );

        List<ItemStack> replacementLoot =
                lootTable.generateLoot(
                        parameters
                );

        if (replacementLoot.isEmpty()) {
            return false;
        }

        /*
         * We're modifying the same vanilla loot collection
         * that FishingBobberEntity will subsequently spawn.
         */
        existingLoot.clear();
        existingLoot.addAll(
                replacementLoot
        );

        return true;
    }

    public static double getJunkToFishChance(
            int fishingLevel
    ) {

        var config =
                ConfigManager.get()
                        .fishing
                        .passives
                        .anglersFortune;

        return calculateChance(
                fishingLevel,
                config.junkToFishBaseChance,
                config.junkToFishChancePerLevel,
                config.junkToFishMaxChance
        );
    }

    public static double getFishToTreasureChance(
            int fishingLevel
    ) {

        var config =
                ConfigManager.get()
                        .fishing
                        .passives
                        .anglersFortune;

        return calculateChance(
                fishingLevel,
                config.fishToTreasureBaseChance,
                config.fishToTreasureChancePerLevel,
                config.fishToTreasureMaxChance
        );
    }

    private static double calculateChance(
            int level,
            double baseChance,
            double chancePerLevel,
            double maxChance
    ) {

        int safeLevel =
                Math.max(
                        0,
                        level
                );

        double chance =
                baseChance
                        + (
                        safeLevel
                                * chancePerLevel
                );

        chance =
                Math.min(
                        chance,
                        maxChance
                );

        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        chance
                )
        );
    }
}