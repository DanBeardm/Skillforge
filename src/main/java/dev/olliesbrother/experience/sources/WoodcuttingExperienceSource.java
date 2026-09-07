package dev.olliesbrother.experience.sources;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.experience.ConfiguredBlockExperienceSource;

import java.util.Map;

public final class WoodcuttingExperienceSource
        extends ConfiguredBlockExperienceSource {

    public static final WoodcuttingExperienceSource INSTANCE =
            new WoodcuttingExperienceSource();

    private WoodcuttingExperienceSource() {
    }

    @Override
    protected Map<String, Double> getConfiguredSources() {

        return ConfigManager.get()
                .woodcutting
                .experience;
    }
}