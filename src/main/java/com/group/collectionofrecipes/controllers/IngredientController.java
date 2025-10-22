package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.dto.responsedto.ApiResponse;
import com.group.collectionofrecipes.enums.Unit;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Ingredients")
@Slf4j
@RestController
@RequiredArgsConstructor
public class IngredientController {

    @GetMapping("/api/v1/measures")
    public ApiResponse<List<String>> getStringUnits(){
        List<String> stringUnits = Arrays.stream(Unit.values()).map(Unit::getLabel).toList();
        return ApiResponse.success(stringUnits);
    }
}
