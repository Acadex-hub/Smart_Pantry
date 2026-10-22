package com.richfield.smartpantry;

import java.util.*;

/**
 * THE STRICT-MATCHING ENGINE (core business logic).
 * A recipe is "makeable" only if EVERY ingredient is in the pantry in at least the required quantity.
 * Robustness: names are normalised (case, spacing, plurals) and quantities are converted to base units
 * (g / ml / pieces), so "Tomatoes" == "tomato" and 0.5 kg covers 200 g.
 */
public class Matcher {

    /** Result of testing one recipe against the pantry. */
    public static class Match {
        public Recipe recipe;
        public List<String> missing = new ArrayList<>(); // ingredients not covered
        public int expiringUsed = 0;                      // how many expiring-soon items this recipe rescues
    }

    /** Lower-case, trim and singularise the last word: "Tomatoes" -> "tomato". */
    public static String normalize(String s) {
        String[] w = s.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").split(" ");
        w[w.length - 1] = singular(w[w.length - 1]);
        return String.join(" ", w);
    }

    private static String singular(String w) {
        if (w.length() <= 3) return w;
        if (w.endsWith("ies")) return w.substring(0, w.length() - 3) + "y";
        if (w.endsWith("oes")) return w.substring(0, w.length() - 2);
        if (w.endsWith("ss") || w.endsWith("us")) return w;
        if (w.endsWith("s")) return w.substring(0, w.length() - 1);
        return w;
    }

    /** Unit family: mass, volume or count (units from different families are not comparable). */
    static String family(String u) {
        switch (u.toLowerCase(Locale.ROOT)) {
            case "g": case "kg": return "mass";
            case "ml": case "l": case "tsp": case "tbsp": case "cup": return "vol";
            default: return "count";
        }
    }

    /** Multiplier converting a unit into its family's base unit (g, ml, pcs). */
    static double factor(String u) {
        switch (u.toLowerCase(Locale.ROOT)) {
            case "kg": case "l": return 1000;
            case "tsp": return 5;
            case "tbsp": return 15;
            case "cup": return 240;
            default: return 1;
        }
    }

    /** Tests every recipe against the pantry. Callers filter: missing.isEmpty() = strict suggestion. */
    public static List<Match> match(List<PantryItem> pantry, List<Recipe> recipes) {
        Map<String, Double> stock = new HashMap<>();
        Set<String> expiring = new HashSet<>();
        for (PantryItem p : pantry) {
            String key = normalize(p.name) + "|" + family(p.unit);
            stock.merge(key, p.qty * factor(p.unit), Double::sum); // duplicates are added together
            Long d = p.daysLeft();
            if (d != null && d >= 0 && d <= 3) expiring.add(normalize(p.name));
        }
        List<Match> out = new ArrayList<>();
        for (Recipe r : recipes) {
            Match m = new Match();
            m.recipe = r;
            for (Recipe.RecipeIngredient ri : r.ingredients) {
                String n = normalize(ri.name);
                Double have = stock.get(n + "|" + family(ri.unit));
                if (have == null || have + 1e-9 < ri.qty * factor(ri.unit)) m.missing.add(ri.name);
                else if (expiring.contains(n)) m.expiringUsed++;
            }
            out.add(m);
        }
        return out;
    }

    /** Pretty number: 2.0 -> "2", 0.5 -> "0.5". */
    public static String fmt(double d) { return d == Math.rint(d) ? String.valueOf((long) d) : String.valueOf(d); }

    private static final String[][] EMOJI = {
        {"egg","🥚"},{"tomato","🍅"},{"butter","🧈"},{"cheese","🧀"},{"milk","🥛"},{"pasta","🍝"},{"garlic","🧄"},
        {"onion","🧅"},{"rice","🍚"},{"flour","🌾"},{"sugar","🍬"},{"bread","🍞"},{"potato","🥔"},{"carrot","🥕"},
        {"oil","🫒"},{"salt","🧂"},{"banana","🍌"},{"tuna","🐟"},{"bean","🫘"},{"chickpea","🫘"},{"lentil","🫘"},
        {"oat","🥣"},{"tortilla","🫓"},{"soy","🥢"},{"apple","🍎"},{"chicken","🍗"},{"lemon","🍋"}
    };
    /** Emoji "icon" for an ingredient, chosen by keyword. */
    public static String emojiFor(String name) {
        String n = normalize(name);
        for (String[] e : EMOJI) if (n.contains(e[0])) return e[1];
        return "🥫";
    }
}
