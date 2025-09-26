package com.group.collectionOfRecipes.services;

import com.group.collectionOfRecipes.dto.recipeDTO.CreateRecipeDTO;
import com.group.collectionOfRecipes.dto.recipeDTO.RecipeDTO;
import com.group.collectionOfRecipes.entities.Category;
import com.group.collectionOfRecipes.entities.Recipe;
import com.group.collectionOfRecipes.entities.User;
import com.group.collectionOfRecipes.mappers.RecipeMapper;
import com.group.collectionOfRecipes.repositories.CategoryRepository;
import com.group.collectionOfRecipes.repositories.RecipeRepository;
import com.group.collectionOfRecipes.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecipeService {

    private final LocalFileStorageService fileStorageService;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RecipeMapper recipeMapper;

    public List<RecipeDTO> findAllRecipes(String title) {
        List<Recipe> productList = (title != null ? recipeRepository.findByTitleContainingIgnoreCase(title) : recipeRepository.findAll());
        List<RecipeDTO> productDTOList = new ArrayList<>();
        for (Recipe i:productList)
        {
            productDTOList.add(recipeMapper.toRecipeDto(i));
        }
        return productDTOList;
    }

    public RecipeDTO findRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found by id: " + id));
        return recipeMapper.toRecipeDto(recipe);
    }

    public RecipeDTO saveRecipe(CreateRecipeDTO createRecipeDTO, MultipartFile image) {

        String imageName = fileStorageService.storeFile(image);

        Category category = categoryRepository.findById(createRecipeDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found by id: " + createRecipeDTO.getCategoryId()));

        User user = userRepository.findById(createRecipeDTO.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("User not found by id: " + createRecipeDTO.getAuthorId()));

        Recipe recipe = recipeMapper.toRecipeEntity(createRecipeDTO, user, category);
        recipe.setImage(imageName);
        return recipeMapper.toRecipeDto(recipeRepository.save(recipe));
    }

    public RecipeDTO deleteRecipe(Long id){
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found by id: " + id));
        recipeRepository.deleteById(id);
        return recipeMapper.toRecipeDto(recipe);
    }


    public RecipeDTO updateRecipe(Long id, CreateRecipeDTO createRecipeDTO) {

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found by id: " + id));

        recipe.setTitle(createRecipeDTO.getTitle());
        recipe.setDescription(createRecipeDTO.getDescription());
        recipe.setInstruction(createRecipeDTO.getInstruction());
        recipe.setCookingTime(createRecipeDTO.getCookingTime());
        recipe.setCountOfServings(createRecipeDTO.getCountOfServings());
        return recipeMapper.toRecipeDto(recipeRepository.save(recipe));
    }
}
