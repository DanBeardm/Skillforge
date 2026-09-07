package dev.olliesbrother.experience;

import net.minecraft.block.BlockState;

public interface BlockExperienceSource {

    double getExperience(BlockState state);

    default boolean awardsExperience(BlockState state) {
        return getExperience(state) > 0;
    }
}