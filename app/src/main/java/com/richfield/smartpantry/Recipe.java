package com.richfield.smartpantry;

import java.util.ArrayList;
import java.util.List;

/** Model: a recipe with its required ingredients and method steps. */
public class Recipe {
    public long id;
    public String name, emoji, steps; // steps are separated by ';'
    public int minutes;
    public List<RecipeIngredient> ingredients = new ArrayList<>();

    public static class RecipeIngredient {
        public String name, unit;
        public double qty;
        public RecipeIngredient(String name, double qty, String unit) { this.name = name; this.qty = qty; this.unit = unit; }
    }
}
