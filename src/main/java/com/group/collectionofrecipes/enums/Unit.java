package com.group.collectionofrecipes.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Unit {

    GRAM("гр."),
    KILOGRAM("кг."),
    MILLILITER("мл."),
    LITER("л."),
    PIECE("шт."),
    TEASPOON("ч. л."),
    TABLESPOON("ст. л."),
    CUP("ст."),
    PINCH("щеп."),
    TO_TASTE("по вкусу"),
    SHEAF("пучок"),
    CLOVE("зубч.");



    private final String label;

    Unit(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Unit findByLabel(String label) {
        if (label == null) {
            return null;
        }
        for (Unit unit : Unit.values()) {
            if (unit.label.equalsIgnoreCase(label) || unit.name().equalsIgnoreCase(label)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Неизвестная единица измерения: " + label);
    }
}