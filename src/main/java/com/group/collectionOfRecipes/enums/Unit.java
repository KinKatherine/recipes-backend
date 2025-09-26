package com.group.collectionOfRecipes.enums;

public enum Unit {

    GRAM("гр"),
    KILOGRAM("кг"),
    MILLILITER("мл"),
    LITER("л"),
    PIECE("шт"),
    TEASPOON("ч. ложка"),
    TABLESPOON("сл. ложка"),
    CUP("стакан"),
    PINCH("щепотка"),
    TO_TASTE("по вкусу");


    private final String label;

    Unit(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}