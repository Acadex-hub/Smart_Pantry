package com.richfield.smartpantry;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Host activity: bottom navigation switches between the Pantry, Cook (suggestions) and Settings fragments. */
public class MainActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment f;
            if (id == R.id.nav_pantry) f = new PantryFragment();
            else if (id == R.id.nav_cook) f = new SuggestionsFragment();
            else f = new SettingsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, f).commit();
            return true;
        });
        if (savedInstanceState == null) nav.setSelectedItemId(R.id.nav_pantry);
    }
}
