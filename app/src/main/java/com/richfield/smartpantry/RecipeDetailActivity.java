package com.richfield.smartpantry;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.List;

/** Recipe detail: full ingredient list (✅ in pantry / ❌ missing) and numbered method. */
public class RecipeDetailActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "recipe_id";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        DatabaseHelper db = DatabaseHelper.get(this);
        Recipe r = db.getRecipe(getIntent().getLongExtra(EXTRA_ID, -1));
        if (r == null) { finish(); return; }

        ((TextView) findViewById(R.id.tvEmoji)).setText(r.emoji);
        ((TextView) findViewById(R.id.tvName)).setText(r.name);
        ((TextView) findViewById(R.id.tvMeta)).setText("⏱ " + r.minutes + " min  ·  " + r.ingredients.size() + " ingredients");

        // Re-run the matcher on this single recipe to know which ingredients are covered.
        List<String> missing = Matcher.match(db.getAllItems(), Collections.singletonList(r)).get(0).missing;

        LinearLayout box = findViewById(R.id.boxIngredients);
        for (Recipe.RecipeIngredient ri : r.ingredients) {
            boolean have = !missing.contains(ri.name);
            addLine(box, (have ? "✅  " : "❌  ") + Matcher.fmt(ri.qty) + " " + ri.unit + "  " + ri.name);
        }
        LinearLayout steps = findViewById(R.id.boxSteps);
        String[] parts = r.steps.split(";");
        for (int i = 0; i < parts.length; i++) addLine(steps, (i + 1) + ".  " + parts[i].trim());
    }

    private void addLine(LinearLayout parent, String text) {
        TextView t = new TextView(this);
        t.setText(text); t.setTextSize(16); t.setLineSpacing(0, 1.2f);
        t.setPadding(0, 8, 0, 8);
        parent.addView(t);
    }
}
