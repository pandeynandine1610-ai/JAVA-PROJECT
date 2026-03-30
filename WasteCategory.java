package com.smartbin.model;

/**
 * Enumeration of all supported waste disposal categories.
 *
 * Each category maps to a physical bin colour used by most municipalities,
 * and carries a short description shown to the end-user.
 */
public enum WasteCategory {

    RECYCLABLE(
            "Recyclable",
            "♻️  BLUE BIN",
            "Clean and dry recyclables — paper, cardboard, glass, metal, hard plastics (#1, #2, #5)."
    ),
    ORGANIC(
            "Organic / Compost",
            "🟤 BROWN BIN",
            "Food scraps, garden waste, uncoated paper towels, coffee grounds."
    ),
    GENERAL_WASTE(
            "General Waste",
            "⬛ BLACK BIN",
            "Non-recyclable, non-organic waste. Goes to landfill — minimize this category."
    ),
    HAZARDOUS(
            "Hazardous",
            "⚠️  HAZARDOUS DROP-OFF",
            "Batteries, paints, chemicals, fluorescent bulbs. Never in regular bins."
    ),
    E_WASTE(
            "E-Waste",
            "🖥️  E-WASTE CENTRE",
            "Electronics, cables, phones, printers. Contains recoverable materials."
    ),
    SPECIAL(
            "Special Handling",
            "🏥 SPECIAL COLLECTION",
            "Medical sharps, large furniture, tyres. Requires council collection booking."
    );

    private final String displayName;
    private final String binLabel;
    private final String description;

    WasteCategory(String displayName, String binLabel, String description) {
        this.displayName = displayName;
        this.binLabel = binLabel;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getBinLabel()    { return binLabel; }
    public String getDescription() { return description; }
}
