package org.flbohn.conversationsimulator_backend.learner.types;

public enum LearningLanguage {

    GERMAN("Deutsch"),
    ENGLISH("Englisch"),
    FRENCH("Französisch"),
    SPANISH("Spanisch");

    private final String languageValue;

    LearningLanguage(String languageValue) {
        this.languageValue = languageValue;
    }

    public String getLanguageValue() {
        return languageValue;
    }
}
