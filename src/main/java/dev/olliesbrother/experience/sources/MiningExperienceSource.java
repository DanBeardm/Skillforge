package dev.olliesbrother.experience.sources;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.experience.ConfiguredBlockExperienceSource;

import java.util.Map;

public final class MiningExperienceSource
        extends ConfiguredBlockExperienceSource {

    public static final MiningExperienceSource INSTANCE =
            new MiningExperienceSource();

    private MiningExperienceSource() {
    }

    @Override
    protected Map<String, Double> getConfiguredSources() {

        return ConfigManager.get()
                .mining
                .experience;
    }
}