package dev.milow.aaigttracker.client.model;

import net.minecraft.network.chat.Component;

public enum CompletionVisibilityMode {
    SHOW_ALL(
            "screen.aaigttracker.visibility.show_all",
            "screen.aaigttracker.visibility.show_all_short",
            false,
            false
    ),
    HIDE_COMPLETED_CRITERIA(
            "screen.aaigttracker.visibility.hide_completed_criteria",
            "screen.aaigttracker.visibility.hide_completed_criteria_short",
            true,
            false
    ),
    HIDE_COMPLETED_CRITERIA_AND_ADVANCEMENTS(
            "screen.aaigttracker.visibility.hide_completed_criteria_and_advancements",
            "screen.aaigttracker.visibility.hide_completed_criteria_and_advancements_short",
            true,
            true
    );

    private final String translationKey;
    private final String shortTranslationKey;
    private final boolean hidesCompletedCriteria;
    private final boolean hidesCompletedAdvancements;

    CompletionVisibilityMode(
            String translationKey,
            String shortTranslationKey,
            boolean hidesCompletedCriteria,
            boolean hidesCompletedAdvancements
    ) {
        this.translationKey = translationKey;
        this.shortTranslationKey = shortTranslationKey;
        this.hidesCompletedCriteria = hidesCompletedCriteria;
        this.hidesCompletedAdvancements = hidesCompletedAdvancements;
    }

    public Component label() {
        return Component.translatable(this.translationKey);
    }

    public Component shortLabel() {
        return Component.translatable(this.shortTranslationKey);
    }

    public boolean hidesCompletedCriteria() {
        return this.hidesCompletedCriteria;
    }

    public boolean hidesCompletedAdvancements() {
        return this.hidesCompletedAdvancements;
    }

    public CompletionVisibilityMode next() {
        return switch (this) {
            case SHOW_ALL -> HIDE_COMPLETED_CRITERIA;
            case HIDE_COMPLETED_CRITERIA -> HIDE_COMPLETED_CRITERIA_AND_ADVANCEMENTS;
            case HIDE_COMPLETED_CRITERIA_AND_ADVANCEMENTS -> SHOW_ALL;
        };
    }
}
