package dev.olliesbrother.experience;

import net.minecraft.block.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BlockExperienceSourceRegistry {

    private static final List<BlockExperienceSource> SOURCES =
            new ArrayList<>();

    private BlockExperienceSourceRegistry() {
    }

    public static void register(
            BlockExperienceSource source
    ) {

        if (SOURCES.contains(source)) {
            return;
        }

        SOURCES.add(source);
    }

    public static boolean awardsExperience(
            BlockState state
    ) {

        for (BlockExperienceSource source : SOURCES) {

            if (source.awardsExperience(state)) {
                return true;
            }
        }

        return false;
    }

    public static List<BlockExperienceSource> getAll() {
        return Collections.unmodifiableList(
                SOURCES
        );
    }
}