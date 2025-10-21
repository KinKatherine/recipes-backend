package com.group.collectionofrecipes.enums;

public enum Unit {

    GRAM("гр."),
    KILOGRAM("кг."),
    MILLILITER("мл."),
    LITER("л."),
    PIECE("шт."),
    TEASPOON("ч. л."),
    TABLESPOON("сл. л."),
    CUP("ст."),
    PINCH("щеп."),
    TO_TASTE("по вкусу");


    private final String label;

    Unit(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}