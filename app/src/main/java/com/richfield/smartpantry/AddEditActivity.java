package com.richfield.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.util.Locale;

/** Add / Edit ingredient form with input validation. Edit mode is triggered by an Intent extra. */
public class AddEditActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "item_id";
    private static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    private TextInputLayout tilName, tilQty;
    private TextInputEditText etName, etQty;
    private Spinner spUnit;
    private MaterialButton btnExpiry;
    private TextView btnClear;
    private String expiry = "";
    private long editId = -1;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        tilName = findViewById(R.id.tilName); tilQty = findViewById(R.id.tilQty);
        etName = findViewById(R.id.etName); etQty = findViewById(R.id.etQty);
        spUnit = findViewById(R.id.spUnit); btnExpiry = findViewById(R.id.btnExpiry); btnClear = findViewById(R.id.btnClearExpiry);
        spUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UNITS));

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());
        btnExpiry.setOnClickListener(v -> pickDate());
        btnClear.setOnClickListener(v -> { expiry = ""; showExpiry(); });
        findViewById(R.id.btnSave).setOnClickListener(v -> save());

        editId = getIntent().getLongExtra(EXTRA_ID, -1);
        if (editId != -1) {                                     // EDIT mode: pre-fill the form
            PantryItem item = DatabaseHelper.get(this).getItem(editId);
            if (item != null) {
                ((TextView) findViewById(R.id.tvTitle)).setText("Edit ingredient");
                etName.setText(item.name); etQty.setText(Matcher.fmt(item.qty));
                for (int i = 0; i < UNITS.length; i++) if (UNITS[i].equals(item.unit)) spUnit.setSelection(i);
                expiry = item.expiry == null ? "" : item.expiry;
            }
        }
        showExpiry();
    }

    private void showExpiry() {
        btnExpiry.setText(expiry.isEmpty() ? "📅 Expiry date (optional)" : "📅 Expires " + expiry);
        btnClear.setVisibility(expiry.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void pickDate() {
        LocalDate d = expiry.isEmpty() ? LocalDate.now() : LocalDate.parse(expiry);
        new DatePickerDialog(this, (view, y, m, day) -> {
            expiry = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, day);
            showExpiry();
        }, d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth()).show();
    }

    /** Validates input, then inserts (Create) or updates (Update) the row. */
    private void save() {
        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        String qtyText = etQty.getText() == null ? "" : etQty.getText().toString().trim();
        boolean ok = true;

        if (name.isEmpty()) { tilName.setError("Please enter an ingredient name"); ok = false; }
        else if (!name.matches(".*[A-Za-z].*")) { tilName.setError("Name must contain letters"); ok = false; }
        else tilName.setError(null);

        double qty = 0;
        try { qty = Double.parseDouble(qtyText); } catch (NumberFormatException e) { /* handled below */ }
        if (qty <= 0) { tilQty.setError("Enter a quantity greater than 0"); ok = false; }
        else tilQty.setError(null);

        if (!ok) return;

        PantryItem item = new PantryItem(editId, name, qty, UNITS[spUnit.getSelectedItemPosition()], expiry);
        DatabaseHelper db = DatabaseHelper.get(this);
        if (editId == -1) db.insertItem(item); else db.updateItem(item);
        Toast.makeText(this, editId == -1 ? "Added to pantry ✅" : "Updated ✅", Toast.LENGTH_SHORT).show();
        finish();
    }
}
