package dev.olliesbrother.experience.sources;

import dev.olliesbrother.config.ConfigManager;
import dev.olliesbrother.experience.ConfiguredBlockExperienceSource;

import java.util.Map;

public final class ExcavationExperienceSource
        extends ConfiguredBlockExperienceSource {

    public static final ExcavationExperienceSource INSTANCE =
            new ExcavationExperienceSource();

    private ExcavationExperienceSource() {
    }

    @Override
    protected Map<String, Double> getConfiguredSources() {

        return ConfigManager.get()
                .excavation
                .experience;
    }
}