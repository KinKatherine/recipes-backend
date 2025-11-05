package com.group.collectionofrecipes.exceptions;

public class NoRecipesFoundException extends RuntimeException {
    public NoRecipesFoundException(String message) {
        super(message);
    }
}
