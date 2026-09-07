package dev.olliesbrother.skill.abilities.passive;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.player.PlayerDataManager;
import dev.olliesbrother.player.PlayerSkillData;
import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillInstance;
import dev.olliesbrother.skill.SkillRegistry;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public final class ProspectorPassive {

    private static final Identifier MINING_ID =
            Identifier.of("skillforge", "mining");

    private ProspectorPassive() {
    }

    public static void tryApply(
            ServerWorld world,
            ServerPlayerEntity player,
            BlockPos pos,
            BlockState state,
            BlockEntity blockEntity
    ) {

        var config =
                ConfigManager.get()
                        .mining
                        .passives
                        .prospector;

        if (!config.enabled) {
            return;
        }

        // Prospector should only work on ores.
        if (!state.isIn(ConventionalBlockTags.ORES)) {
            return;
        }

        Skill mining = SkillRegistry.get(MINING_ID);

        if (mining == null) {
            return;
        }

        PlayerSkillData playerData =
                PlayerDataManager.get(player);

        SkillInstance miningData =
                playerData.getSkill(mining);

        double chance =
                miningData.getLevel()
                        * config.chancePerLevel;

        chance = Math.min(
                chance,
                config.maxChance
        );

        // Keep badly configured values between 0% and 100%.
        chance = Math.max(
                0.0,
                Math.min(1.0, chance)
        );

        if (world.getRandom().nextDouble() >= chance) {
            return;
        }

        ItemStack tool =
                player.getMainHandStack().copy();

        List<ItemStack> drops =
                Block.getDroppedStacks(
                        state,
                        world,
                        pos,
                        blockEntity,
                        player,
                        tool
                );

        ItemStack bonusDrop = ItemStack.EMPTY;

        for (ItemStack drop : drops) {

            if (!drop.isEmpty()) {
                bonusDrop = drop.copy();
                bonusDrop.setCount(1);
                break;
            }
        }

        if (bonusDrop.isEmpty()) {
            return;
        }

        Block.dropStack(
                world,
                pos,
                bonusDrop
        );

        player.sendMessage(
                Text.literal("§6Prospector! §eBonus drop!"),
                true
        );
    }
}