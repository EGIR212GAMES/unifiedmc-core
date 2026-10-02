package dev.unifiedmc.content.ir.definitions;

import dev.unifiedmc.content.ir.ContentDefinition;
import dev.unifiedmc.content.ir.ContentDefinitionKind;
import dev.unifiedmc.uapi.content.UniversalIdentifier;
import dev.unifiedmc.uapi.content.UniversalRecipe;
import java.util.List;
import java.util.Map;

public record ContentRecipeDefinition(
        UniversalIdentifier id,
        String recipeType,
        List<UniversalRecipe.Ingredient> ingredients,
        UniversalRecipe.Ingredient result,
        Map<String, String> properties)
        implements ContentDefinition {
    public ContentRecipeDefinition {
        if (recipeType == null || recipeType.isBlank()) {

            throw new IllegalArgumentException("recipeType must not be blank");
        }
        ingredients = List.copyOf(ingredients);
        properties = Map.copyOf(properties);
    }

    @Override
    public ContentDefinitionKind kind() {
        return ContentDefinitionKind.RECIPE;
    }
}
