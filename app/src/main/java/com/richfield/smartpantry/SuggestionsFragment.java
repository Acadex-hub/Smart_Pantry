package com.richfield.smartpantry;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** "Suggested Recipes" screen: runs the strict matcher against the live pantry. */
public class SuggestionsFragment extends Fragment {
    private RecipeAdapter strictAdapter, almostAdapter;
    private TextView tvHero, tvEmpty, tvAlmostHeader;
    private RecyclerView rvAlmost;

    public SuggestionsFragment() { super(R.layout.fragment_suggestions); }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        tvHero = v.findViewById(R.id.tvHero); tvEmpty = v.findViewById(R.id.tvEmpty);
        tvAlmostHeader = v.findViewById(R.id.tvAlmostHeader); rvAlmost = v.findViewById(R.id.rvAlmost);

        RecyclerView rv = v.findViewById(R.id.rvStrict);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        strictAdapter = new RecipeAdapter(this::open);
        rv.setAdapter(strictAdapter);

        rvAlmost.setLayoutManager(new LinearLayoutManager(requireContext()));
        almostAdapter = new RecipeAdapter(this::open);
        rvAlmost.setAdapter(almostAdapter);
    }

    @Override public void onResume() { super.onResume(); refresh(); }

    private void refresh() {
        DatabaseHelper db = DatabaseHelper.get(requireContext());
        List<Matcher.Match> all = Matcher.match(db.getAllItems(), db.getAllRecipes());

        List<Matcher.Match> strict = new ArrayList<>(), almost = new ArrayList<>();
        for (Matcher.Match m : all) {
            if (m.missing.isEmpty()) strict.add(m);          // STRICT: nothing missing
            else if (m.missing.size() == 1) almost.add(m);   // BONUS: exactly one missing, kept separate
        }
        // Food-waste twist: recipes that rescue expiring ingredients float to the top.
        Collections.sort(strict, (a, c) -> c.expiringUsed - a.expiringUsed);

        strictAdapter.submit(strict);
        tvEmpty.setVisibility(strict.isEmpty() ? View.VISIBLE : View.GONE);
        tvHero.setText(strict.isEmpty()
            ? "Nothing cookable yet 🌱\nAdd a few more ingredients to your pantry."
            : "🎉  You can cook " + strict.size() + (strict.size() == 1 ? " meal" : " meals") + " right now.\nNo shopping trip needed!");

        boolean showAlmost = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE).getBoolean("show_almost", true);
        boolean visible = showAlmost && !almost.isEmpty();
        tvAlmostHeader.setVisibility(visible ? View.VISIBLE : View.GONE);
        rvAlmost.setVisibility(visible ? View.VISIBLE : View.GONE);
        almostAdapter.submit(visible ? almost : new ArrayList<>());
    }

    private void open(Matcher.Match m) {
        Intent i = new Intent(requireContext(), RecipeDetailActivity.class);
        i.putExtra(RecipeDetailActivity.EXTRA_ID, m.recipe.id);
        startActivity(i);
    }
}
