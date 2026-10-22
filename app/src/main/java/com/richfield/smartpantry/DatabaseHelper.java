package com.richfield.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite persistence layer. Tables: pantry (user CRUD), recipes + recipe_ingredients (seeded on first run).
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper instance;
    public static synchronized DatabaseHelper get(Context c) {
        if (instance == null) instance = new DatabaseHelper(c.getApplicationContext());
        return instance;
    }
    private DatabaseHelper(Context c) { super(c, "smart_pantry.db", null, 1); }

    // name|emoji|minutes|steps (;-separated)|ingredient:qty:unit,...
    private static final String[] SEED = {
        "Tomato Egg Scramble|🍳|10|Beat the eggs.;Fry tomato in butter for 2 minutes.;Add eggs and stir until just set.|egg:2:pcs,tomato:1:pcs,butter:10:g",
        "Cheese Omelette|🧀|10|Whisk eggs with milk.;Melt butter in a pan.;Pour in eggs, add cheese and fold.|egg:3:pcs,cheese:50:g,butter:10:g,milk:30:ml",
        "Garlic Butter Pasta|🍝|15|Boil pasta until al dente.;Sizzle garlic in butter.;Toss together with salt.|pasta:200:g,garlic:2:pcs,butter:30:g,salt:1:tsp",
        "Simple Tomato Pasta|🍅|20|Boil the pasta.;Simmer chopped tomato, onion and garlic for 10 minutes.;Mix and serve.|pasta:200:g,tomato:3:pcs,onion:1:pcs,garlic:1:pcs",
        "Egg Fried Rice|🍚|15|Cook rice and let it cool.;Scramble egg and set aside.;Fry rice with onion, add egg and soy sauce.|rice:200:g,egg:2:pcs,onion:1:pcs,soy sauce:1:tbsp",
        "Fluffy Pancakes|🥞|20|Mix flour, milk, egg and sugar.;Rest for 5 minutes.;Fry ladles of batter in butter until golden.|flour:150:g,milk:250:ml,egg:1:pcs,sugar:1:tbsp,butter:10:g",
        "Cheese Toastie|🥪|8|Butter the bread.;Fill with cheese.;Toast in a pan until golden.|bread:2:pcs,cheese:40:g,butter:10:g",
        "French Toast|🍞|10|Whisk egg, milk and sugar.;Dip the bread.;Fry in butter until golden.|bread:4:pcs,egg:2:pcs,milk:100:ml,butter:10:g,sugar:1:tsp",
        "Buttery Mash|🥔|25|Peel and boil the potatoes.;Drain and mash with butter and milk.;Season with salt.|potato:4:pcs,butter:30:g,milk:60:ml,salt:1:tsp",
        "Roast Veg Tray|🥕|35|Chop carrots, potatoes and onion.;Toss with oil and salt.;Roast 30 minutes at 200°C.|carrot:3:pcs,potato:3:pcs,onion:1:pcs,oil:2:tbsp,salt:1:tsp",
        "Veggie Rice Bowl|🥣|25|Cook the rice.;Sauté carrot, onion and garlic.;Stir in soy sauce and serve over rice.|rice:200:g,carrot:2:pcs,onion:1:pcs,garlic:1:pcs,soy sauce:1:tbsp",
        "Comfort Lentil Soup|🍲|40|Sauté onion and carrot.;Add lentils and 1 litre of water.;Simmer 30 minutes and season.|lentil:200:g,onion:1:pcs,carrot:1:pcs,salt:1:tsp",
        "Tuna Pasta|🐟|15|Boil the pasta.;Flake tuna and mix with oil and garlic.;Combine and serve.|pasta:200:g,tuna:1:pcs,garlic:1:pcs,oil:1:tbsp",
        "Banana Oat Porridge|🍌|10|Simmer oats in milk for 5 minutes.;Top with sliced banana and sugar.|oat:60:g,milk:250:ml,banana:1:pcs,sugar:1:tsp",
        "Bean Quesadilla|🌮|15|Mash the beans.;Spread on a tortilla with cheese.;Fold and toast both sides.|tortilla:2:pcs,bean:200:g,cheese:60:g",
        "Cheesy Rice Bake|🧀|30|Mix cooked rice with egg, cheese and milk.;Bake 20 minutes at 180°C.|rice:200:g,egg:2:pcs,cheese:80:g,milk:100:ml",
        "Fried Potato & Egg|🍟|20|Slice and fry potatoes in oil.;Crack eggs on top and cover until set.;Season.|potato:3:pcs,egg:2:pcs,oil:2:tbsp,salt:1:tsp",
        "Chickpea Tomato Stew|🫘|25|Fry onion and garlic.;Add tomatoes and chickpeas.;Simmer 15 minutes.|chickpea:400:g,tomato:3:pcs,onion:1:pcs,garlic:2:pcs"
    };

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, qty REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, emoji TEXT, minutes INTEGER, steps TEXT)");
        db.execSQL("CREATE TABLE recipe_ingredients(id INTEGER PRIMARY KEY AUTOINCREMENT, recipe_id INTEGER, name TEXT, qty REAL, unit TEXT, FOREIGN KEY(recipe_id) REFERENCES recipes(id))");
        seed(db); // pre-load recipes on first run
    }

    @Override public void onUpgrade(SQLiteDatabase db, int o, int n) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS pantry");
        onCreate(db);
    }

    private void seed(SQLiteDatabase db) {
        for (String line : SEED) {
            String[] p = line.split("\\|");
            ContentValues rv = new ContentValues();
            rv.put("name", p[0]); rv.put("emoji", p[1]); rv.put("minutes", Integer.parseInt(p[2])); rv.put("steps", p[3]);
            long rid = db.insert("recipes", null, rv);
            for (String ing : p[4].split(",")) {
                String[] x = ing.split(":");
                ContentValues iv = new ContentValues();
                iv.put("recipe_id", rid); iv.put("name", x[0]); iv.put("qty", Double.parseDouble(x[1])); iv.put("unit", x[2]);
                db.insert("recipe_ingredients", null, iv);
            }
        }
    }

    // ---------- Pantry CRUD ----------
    private ContentValues toValues(PantryItem i) {
        ContentValues v = new ContentValues();
        v.put("name", i.name); v.put("qty", i.qty); v.put("unit", i.unit); v.put("expiry", i.expiry);
        return v;
    }
    public long insertItem(PantryItem i) { return getWritableDatabase().insert("pantry", null, toValues(i)); }                       // Create
    public int updateItem(PantryItem i) { return getWritableDatabase().update("pantry", toValues(i), "id=?", new String[]{String.valueOf(i.id)}); } // Update
    public int deleteItem(long id) { return getWritableDatabase().delete("pantry", "id=?", new String[]{String.valueOf(id)}); }     // Delete

    private PantryItem readItem(Cursor c) {
        return new PantryItem(c.getLong(0), c.getString(1), c.getDouble(2), c.getString(3), c.getString(4));
    }
    public List<PantryItem> getAllItems() {                                                                                           // Read
        List<PantryItem> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,qty,unit,expiry FROM pantry ORDER BY name COLLATE NOCASE", null)) {
            while (c.moveToNext()) list.add(readItem(c));
        }
        return list;
    }
    public PantryItem getItem(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,qty,unit,expiry FROM pantry WHERE id=?", new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? readItem(c) : null;
        }
    }

    // ---------- Recipes (read-only) ----------
    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase d = getReadableDatabase();
        try (Cursor c = d.rawQuery("SELECT id,name,emoji,minutes,steps FROM recipes ORDER BY name", null)) {
            while (c.moveToNext()) {
                Recipe r = new Recipe();
                r.id = c.getLong(0); r.name = c.getString(1); r.emoji = c.getString(2); r.minutes = c.getInt(3); r.steps = c.getString(4);
                try (Cursor ic = d.rawQuery("SELECT name,qty,unit FROM recipe_ingredients WHERE recipe_id=?", new String[]{String.valueOf(r.id)})) {
                    while (ic.moveToNext()) r.ingredients.add(new Recipe.RecipeIngredient(ic.getString(0), ic.getDouble(1), ic.getString(2)));
                }
                list.add(r);
            }
        }
        return list;
    }
    public Recipe getRecipe(long id) {
        for (Recipe r : getAllRecipes()) if (r.id == id) return r;
        return null;
    }
}
