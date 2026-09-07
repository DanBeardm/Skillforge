package dev.olliesbrother.experience;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.Map;

public abstract class ConfiguredBlockExperienceSource
        implements BlockExperienceSource {

    protected abstract Map<String, Double> getConfiguredSources();

    @Override
    public double getExperience(BlockState state) {

        Map<String, Double> sources =
                getConfiguredSources();

        Identifier blockId =
                Registries.BLOCK.getId(
                        state.getBlock()
                );

        /*
         * Exact block IDs take priority.
         *
         * Example:
         *
         * "#minecraft:logs": 5
         * "minecraft:cherry_log": 10
         *
         * Cherry logs would give 10 XP.
         */
        Double exactExperience =
                sources.get(
                        blockId.toString()
                );

        if (exactExperience != null) {
            return exactExperience;
        }

        /*
         * Then check configured #tags.
         */
        for (Map.Entry<String, Double> entry
                : sources.entrySet()) {

            String key =
                    entry.getKey();

            if (!key.startsWith("#")) {
                continue;
            }

            Identifier tagId =
                    Identifier.tryParse(
                            key.substring(1)
                    );

            if (tagId == null) {
                continue;
            }

            TagKey<Block> tag =
                    TagKey.of(
                            RegistryKeys.BLOCK,
                            tagId
                    );

            if (state.isIn(tag)) {
                return entry.getValue();
            }
        }

        return 0.0;
    }
}