package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.dto.ingredientdto.CreateIngredientDTO;
import com.group.collectionofrecipes.dto.ingredientdto.IngredientDTO;
import com.group.collectionofrecipes.entities.Ingredient;
import com.group.collectionofrecipes.mappers.IngredientMapper;
import com.group.collectionofrecipes.repositories.IngredientRepository;
import com.group.collectionofrecipes.repositories.RecipeIngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


//ДОПИСАТЬ
@Service
@Slf4j
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    public List<IngredientDTO> findAllIngredients(String name) {

        log.info("Запрос на получение всех игредиентов");
        List<Ingredient> ingredients = (name != null ? ingredientRepository.findByNameContainingIgnoreCase(name) : ingredientRepository.findAll());
        List<IngredientDTO> ingredientDTOList = new ArrayList<>();
        for (Ingredient i : ingredients) {
            ingredientDTOList.add(ingredientMapper.toIngredientDto(i));
        }
        log.info("Найдено {} ингредиентов", ingredientDTOList.size());
        return ingredientDTOList;
    }

    public IngredientDTO saveIngredient(CreateIngredientDTO createIngredientDTO) {
        log.info("Запрос на создание нового ингредиента: {}", createIngredientDTO.getName());

        try {
            Ingredient ingredient = ingredientMapper.toIngredientEntity(createIngredientDTO);
            ingredient.setIsConfirmed(false);
            Ingredient savedIngredient = ingredientRepository.save(ingredient);
            log.info("Ингердиент успешно создан: ID={}, Name={}", savedIngredient.getId(), savedIngredient.getName());
            return ingredientMapper.toIngredientDto(savedIngredient);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при создании ингредиента: ингредиент '{}' уже существует", createIngredientDTO.getName());
            throw new IllegalArgumentException("Такой интердиент уже существует");
        } catch (Exception e) {
            log.error("Неожиданная ошибка при создании ингредиента: {}", e.getMessage());
            throw e;
        }
    }


}
