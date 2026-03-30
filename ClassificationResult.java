package com.smartbin.model;

import java.util.List;

/**
 * Encapsulates the full result of classifying a single waste item.
 *
 * Contains the primary classification, any alternative matches,
 * user-facing tips, and whether the item represents a wish-cycling
 * attempt (the key behavioural signal we want to capture).
 */
public class ClassificationResult {

    private final WasteItem item;
    private final List<String> tips;
    private final String wishCycleWarning;  // null if not a wish-cycle attempt
    private final boolean requiresSpecialInstructions;

    public ClassificationResult(WasteItem item,
                                List<String> tips,
                                String wishCycleWarning,
                                boolean requiresSpecialInstructions) {
        this.item = item;
        this.tips = List.copyOf(tips);
        this.wishCycleWarning = wishCycleWarning;
        this.requiresSpecialInstructions = requiresSpecialInstructions;
    }

    public WasteItem getItem()                        { return item; }
    public List<String> getTips()                     { return tips; }
    public String getWishCycleWarning()               { return wishCycleWarning; }
    public boolean isRequiresSpecialInstructions()    { return requiresSpecialInstructions; }
    public boolean isWishCycleDetected()              { return wishCycleWarning != null; }
}
