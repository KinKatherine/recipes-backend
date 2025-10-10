package com.group.collectionofrecipes.utils;


public final class ApiConstants {

    private ApiConstants() {
    }

    public static final String FIELD_STATUS = "status";
    public static final String FIELD_SUCCESS = "success";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_ERROR = "error";
    public static final String ERROR_CATEGORY_NOT_FOUND = "Категория не найдена по id";
    public static final String ERROR_RECIPE_NOT_FOUND = "Рецепт не найден по id";

    public static final String AUTH_URI = "/api/v1/auth/**";
    public static final String CATEGORIES_URI = "/api/v1/categories";
    public static final String RECIPES_URI = "/api/v1/recipes";
}