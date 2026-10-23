package com.richfield.smartpantry;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Adapter for recipe cards. Used for both the strict list and the bonus "Almost There" list. */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.VH> {
    public interface Listener { void onClick(Matcher.Match m); }

    private final List<Matcher.Match> items = new ArrayList<>();
    private final Listener listener;
    public RecipeAdapter(Listener l) { listener = l; }

    public void submit(List<Matcher.Match> list) { items.clear(); items.addAll(list); notifyDataSetChanged(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_recipe, p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Matcher.Match m = items.get(pos);
        h.emoji.setText(m.recipe.emoji);
        h.name.setText(m.recipe.name);
        h.meta.setText("⏱ " + m.recipe.minutes + " min  ·  " + m.recipe.ingredients.size() + " ingredients");
        if (!m.missing.isEmpty()) {                       // Almost-There card: show what is missing
            tag(h.tag, "Missing: " + m.missing.get(0), "#E9A72B");
        } else if (m.expiringUsed > 0) {                  // Rescue badge for food that is about to expire
            tag(h.tag, "⏳ Uses " + m.expiringUsed + " expiring item" + (m.expiringUsed > 1 ? "s" : ""), "#E8573F");
        } else {
            h.tag.setVisibility(View.GONE);
        }
        h.itemView.setOnClickListener(v -> listener.onClick(m));
    }

    private void tag(TextView t, String text, String color) {
        t.setVisibility(View.VISIBLE); t.setText(text);
        t.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView emoji, name, meta, tag;
        VH(View v) {
            super(v);
            emoji = v.findViewById(R.id.tvEmoji); name = v.findViewById(R.id.tvName);
            meta = v.findViewById(R.id.tvMeta); tag = v.findViewById(R.id.tvTag);
        }
    }
}
