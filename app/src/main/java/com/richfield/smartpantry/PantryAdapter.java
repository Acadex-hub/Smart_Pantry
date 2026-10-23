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

/** Custom adapter binding PantryItem rows to item_pantry.xml, with colour-coded expiry chips. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.VH> {
    public interface Listener { void onEdit(PantryItem item); void onDelete(PantryItem item); }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    public PantryAdapter(Listener l) { listener = l; }

    public void submit(List<PantryItem> list) { items.clear(); items.addAll(list); notifyDataSetChanged(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_pantry, p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        PantryItem i = items.get(pos);
        h.emoji.setText(Matcher.emojiFor(i.name));
        h.name.setText(i.name);
        h.qty.setText(Matcher.fmt(i.qty) + " " + i.unit);

        Long d = i.daysLeft();
        int color; String label;
        if (d == null)      { color = Color.parseColor("#9E9A8E"); label = "No date"; }
        else if (d < 0)     { color = Color.parseColor("#E8573F"); label = "Expired"; }
        else if (d <= 3)    { color = Color.parseColor("#E9A72B"); label = d == 0 ? "Today!" : d + "d left"; }
        else                { color = Color.parseColor("#4C8B52"); label = d + "d left"; }
        h.expiry.setText(label);
        h.expiry.setBackgroundTintList(ColorStateList.valueOf(color));

        h.itemView.setOnClickListener(v -> listener.onEdit(i));
        h.delete.setOnClickListener(v -> listener.onDelete(i));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView emoji, name, qty, expiry, delete;
        VH(View v) {
            super(v);
            emoji = v.findViewById(R.id.tvEmoji); name = v.findViewById(R.id.tvName); qty = v.findViewById(R.id.tvQty);
            expiry = v.findViewById(R.id.tvExpiry); delete = v.findViewById(R.id.btnDelete);
        }
    }
}
