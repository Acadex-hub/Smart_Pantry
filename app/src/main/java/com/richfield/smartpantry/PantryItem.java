package com.richfield.smartpantry;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Model: one ingredient the user owns. */
public class PantryItem {
    public long id;
    public String name;
    public double qty;
    public String unit;
    public String expiry; // ISO date (yyyy-MM-dd) or empty string

    public PantryItem(long id, String name, double qty, String unit, String expiry) {
        this.id = id; this.name = name; this.qty = qty; this.unit = unit; this.expiry = expiry;
    }

    /** Days until expiry (negative = already expired), or null when no date is set. */
    public Long daysLeft() {
        if (expiry == null || expiry.isEmpty()) return null;
        return ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(expiry));
    }
}
