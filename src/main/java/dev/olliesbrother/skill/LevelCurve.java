package dev.olliesbrother.skill;

public final class LevelCurve {

    private LevelCurve() {
    }

    public static double getRequiredExperience(int level) {
        return 100 + ((level - 1) * 25);
    }
}