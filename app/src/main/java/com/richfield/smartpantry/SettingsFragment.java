package com.richfield.smartpantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;

/** Settings screen: two persisted toggles stored in SharedPreferences. */
public class SettingsFragment extends Fragment {
    public SettingsFragment() { super(R.layout.fragment_settings); }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        SharedPreferences sp = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        MaterialSwitch swExp = v.findViewById(R.id.swExpiring), swAlmost = v.findViewById(R.id.swAlmost);
        swExp.setChecked(sp.getBoolean("alert_expiring", true));
        swAlmost.setChecked(sp.getBoolean("show_almost", true));
        swExp.setOnCheckedChangeListener((s, on) -> sp.edit().putBoolean("alert_expiring", on).apply());
        swAlmost.setOnCheckedChangeListener((s, on) -> sp.edit().putBoolean("show_almost", on).apply());
    }
}
