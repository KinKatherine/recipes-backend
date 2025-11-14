package com.group.collectionofrecipes.utils;


public final class ApiConstants {

    public static final String FIELD_STATUS = "status";
    public static final String FIELD_SUCCESS = "success";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_ERROR = "error";
    public static final String ERROR_CATEGORY_NOT_FOUND = "Категория не найдена по id";
    public static final String ERROR_RECIPE_NOT_FOUND = "Рецепт не найден по id";
    public static final String ERROR_USER_NOT_FOUND = "Пользователь не найден по username: ";
    public static final String ERROR_COMMENT_NOT_FOUND = "Комментарий не найден по id: ";
    //пользователь
    public static final String AUTH_LOGIN_URI = "/api/v1/auth/login";
    public static final String AUTH_REGISTER_URI = "/api/v1/auth/register";
    public static final String EMAIL_VERIFICATION_URI = "/api/v1/verify";
    //категории
    public static final String CATEGORIES_URI = "/api/v1/categories";
    public static final String CATEGORY_RECIPES_URI = "/api/v1/categories/{categoryId}/recipes";
    //комментарии
    public static final String COMMENTS_URI = "/api/v1/comments";
    public static final String COMMENT_ID_URI = "/api/v1/comments/{commentId}";
    //рейтинги
    public static final String RATINGS_URI = "/api/v1/ratings";
    public static final String RATING_ID_URI = "/api/v1/ratings/{recipeId}";
    //рецепты
    public static final String RECIPE_OF_THE_DAY_URI = "/api/v1/recipes/recipe-of-the-day";
    public static final String RECIPE_RECENT_URI = "/api/v1/recipes/recent";
    public static final String RECIPE_ID_URI = "/api/v1/recipes/{recipeId}";
    public static final String RECIPES_FAVOURITE_URI = "/api/v1/recipes/favourites";
    public static final String RECIPES_CONFIRMED_URI = "/api/v1/recipes/my-recipes";
    public static final String RECIPES_UNCONFIRMED_URI = "/api/v1/recipes/unconfirmed";
    public static final String RECIPES_POPULAR_URI = "/api/v1/recipes/popular";
    public static final String RECIPES_URI = "/api/v1/recipes";
    public static final String RECIPES_USER_FAVOURITES_URI = "/api/v1/recipes/{id}";
    public static final String RECIPES_USER_ADDED_URI = "/api/v1/recipes/my-recipes";
    public static final Integer FIXED_PAGE_SIZE = 8;


    public static final String EMAIL_REGEX = "^(?=.{1,254}$)[A-Za-z0-9+_.-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?" +
            "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$";

    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_-]{3,20}$";



    private ApiConstants() {
    }
}