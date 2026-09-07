package dev.olliesbrother.skill.abilities.passive;

import dev.olliesbrother.config.ConfigManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class GreenThumbPassive {

    private GreenThumbPassive() {
    }

    public static void tryReplant(
            ServerWorld world,
            ServerPlayerEntity player,
            BlockPos pos,
            BlockState harvestedState,
            int herbalismLevel
    ) {

        var config =
                ConfigManager.get()
                        .herbalism
                        .abilities
                        .greenThumb;

        if (!config.enabled) {
            return;
        }

        BlockState replantedState =
                createReplantedState(
                        harvestedState
                );

        /*
         * Not a crop Green Thumb knows how to replant.
         *
         * This excludes melons/pumpkins because their
         * stems remain naturally.
         */
        if (replantedState == null) {
            return;
        }

        double chance =
                getReplantChance(
                        herbalismLevel
                );

        if (world.getRandom().nextDouble()
                >= chance) {

            return;
        }

        /*
         * Make sure the crop can still legally exist
         * at this position.
         *
         * For example, wheat still needs farmland and
         * cocoa still needs a suitable supporting block.
         */
        if (!replantedState.canPlaceAt(
                world,
                pos
        )) {
            return;
        }

        /*
         * Replant directly at age 0.
         *
         * This does not go through BlockItem placement,
         * so our player-placed XP protection won't
         * incorrectly flag the crop.
         */
        boolean replanted =
                world.setBlockState(
                        pos,
                        replantedState,
                        Block.NOTIFY_ALL
                );

        if (!replanted) {
            return;
        }

        player.sendMessage(
                Text.literal(
                        "§aGreen Thumb! §eCrop replanted."
                ),
                true
        );
    }

    /*
     * Returns the freshly planted version of the
     * harvested crop.
     *
     * null means Green Thumb doesn't replant it.
     */
    private static BlockState createReplantedState(
            BlockState harvestedState
    ) {

        /*
         * Wheat, carrots, potatoes and beetroot.
         *
         * CropBlock.withAge(0) returns the freshly
         * planted state for that crop.
         */
        if (harvestedState.getBlock()
                instanceof CropBlock cropBlock) {

            return cropBlock.withAge(0);
        }

        /*
         * Nether Wart.
         */
        if (harvestedState.getBlock()
                instanceof NetherWartBlock) {

            return harvestedState.with(
                    NetherWartBlock.AGE,
                    0
            );
        }

        /*
         * Cocoa.
         *
         * Using harvestedState.with(...) rather than
         * the default state preserves the direction
         * the cocoa pod was facing.
         */
        if (harvestedState.getBlock()
                instanceof CocoaBlock) {

            return harvestedState.with(
                    CocoaBlock.AGE,
                    0
            );
        }

        return null;
    }

    /*
     * Shared calculation so /skills herbalism can
     * display exactly the same value used by gameplay.
     */
    public static double getReplantChance(
            int herbalismLevel
    ) {

        var config =
                ConfigManager.get()
                        .herbalism
                        .abilities
                        .greenThumb;

        int level =
                Math.max(
                        0,
                        herbalismLevel
                );

        double chance =
                config.baseReplantChance
                        + (
                        level
                                * config.chancePerLevel
                );

        chance =
                Math.min(
                        chance,
                        config.maxReplantChance
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