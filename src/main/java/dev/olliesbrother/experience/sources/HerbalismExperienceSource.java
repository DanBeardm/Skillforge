package dev.olliesbrother.experience.sources;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.experience.ConfiguredBlockExperienceSource;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;

import java.util.Map;

public final class HerbalismExperienceSource
        extends ConfiguredBlockExperienceSource {

    public static final HerbalismExperienceSource INSTANCE =
            new HerbalismExperienceSource();

    private HerbalismExperienceSource() {
    }

    @Override
    protected Map<String, Double> getConfiguredSources() {

        return ConfigManager.get()
                .herbalism
                .experience;
    }

    @Override
    public double getExperience(
            BlockState state
    ) {

        /*
         * Standard crops:
         *
         * Wheat
         * Carrots
         * Potatoes
         * Beetroot
         *
         * CropBlock already knows its own maximum age.
         */
        if (state.getBlock()
                instanceof CropBlock cropBlock) {

            if (!cropBlock.isMature(state)) {
                return 0.0;
            }
        }

        /*
         * Nether Wart isn't a CropBlock,
         * so check its AGE manually.
         */
        if (state.getBlock()
                instanceof NetherWartBlock) {

            int age =
                    state.get(
                            NetherWartBlock.AGE
                    );

            if (age < NetherWartBlock.MAX_AGE) {
                return 0.0;
            }
        }

        /*
         * Cocoa also has its own AGE property.
         */
        if (state.getBlock()
                instanceof CocoaBlock) {

            int age =
                    state.get(
                            CocoaBlock.AGE
                    );

            if (age < CocoaBlock.MAX_AGE) {
                return 0.0;
            }
        }

        /*
         * If this isn't an age-based crop, or it
         * passed its maturity check, use the normal
         * configurable XP lookup.
         */
        return super.getExperience(state);
    }
}