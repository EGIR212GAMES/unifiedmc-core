package dev.unifiedmc.uapi.content;

import java.util.List;
import java.util.Objects;

/** Backend-neutral recipe definition. */
public interface UniversalRecipe extends UniversalContent {
    String recipeType();

    List<Ingredient> ingredients();

    Ingredient result();

    record Ingredient(UniversalIdentifier item, int count) {
        public Ingredient {
            Objects.requireNonNull(item, "item");
            if (count <= 0) {
                throw new IllegalArgumentException("count must be > 0");
            }
        }
    }
}
