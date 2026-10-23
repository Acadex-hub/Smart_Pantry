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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/** Pantry list screen (READ) with entry points to Add (CREATE), Edit (UPDATE) and Delete. */
public class PantryFragment extends Fragment {
    private PantryAdapter adapter;
    private TextView tvHero, tvAlert, tvEmpty;

    public PantryFragment() { super(R.layout.fragment_pantry); }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        tvHero = v.findViewById(R.id.tvHero); tvAlert = v.findViewById(R.id.tvAlert); tvEmpty = v.findViewById(R.id.tvEmpty);
        RecyclerView rv = v.findViewById(R.id.rvPantry);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PantryAdapter(new PantryAdapter.Listener() {
            @Override public void onEdit(PantryItem item) {
                Intent i = new Intent(requireContext(), AddEditActivity.class);
                i.putExtra(AddEditActivity.EXTRA_ID, item.id);        // Intent carries the row id
                startActivity(i);
            }
            @Override public void onDelete(PantryItem item) { confirmDelete(item); }
        });
        rv.setAdapter(adapter);
        v.findViewById(R.id.fabAdd).setOnClickListener(x -> startActivity(new Intent(requireContext(), AddEditActivity.class)));
    }

    @Override public void onResume() { super.onResume(); refresh(); } // reload after returning from Add/Edit

    private void confirmDelete(PantryItem item) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove " + item.name + "?")
            .setMessage("It will be deleted from your pantry.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete", (d, w) -> { DatabaseHelper.get(requireContext()).deleteItem(item.id); refresh(); })
            .show();
    }

    private void refresh() {
        List<PantryItem> items = DatabaseHelper.get(requireContext()).getAllItems();
        adapter.submit(items);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        int soon = 0;
        for (PantryItem i : items) { Long d = i.daysLeft(); if (d != null && d >= 0 && d <= 3) soon++; }
        tvHero.setText("🧺  " + items.size() + (items.size() == 1 ? " ingredient" : " ingredients")
            + " in stock.\nEvery one you use is one less in the bin. 🌍");

        boolean alerts = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE).getBoolean("alert_expiring", true);
        if (alerts && soon > 0) { tvAlert.setVisibility(View.VISIBLE); tvAlert.setText("⏳ " + soon + " expiring within 3 days — cook them first!"); }
        else tvAlert.setVisibility(View.GONE);
    }
}
