package com.smartbin.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single waste item submitted for classification.
 *
 * Captures the item name, classification result, confidence score,
 * and timestamp of disposal — forming the core data unit for
 * both user feedback and municipal analytics.
 */
public class WasteItem {

    private final String name;
    private final WasteCategory category;
    private final double confidenceScore;  // 0.0 – 1.0
    private final LocalDateTime timestamp;
    private final String disposalInstruction;
    private final boolean isWishCycle;     // true if user tried to recycle something non-recyclable

    public WasteItem(String name, WasteCategory category, double confidenceScore,
                     String disposalInstruction, boolean isWishCycle) {
        this.name = Objects.requireNonNull(name, "Item name cannot be null");
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        this.confidenceScore = clamp(confidenceScore, 0.0, 1.0);
        this.disposalInstruction = Objects.requireNonNull(disposalInstruction);
        this.isWishCycle = isWishCycle;
        this.timestamp = LocalDateTime.now();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getName() { return name; }
    public WasteCategory getCategory() { return category; }
    public double getConfidenceScore() { return confidenceScore; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDisposalInstruction() { return disposalInstruction; }
    public boolean isWishCycle() { return isWishCycle; }

    /** Returns confidence as a human-readable percentage string, e.g. "87%". */
    public String getConfidencePercent() {
        return String.format("%.0f%%", confidenceScore * 100);
    }

    @Override
    public String toString() {
        return String.format("WasteItem{name='%s', category=%s, confidence=%s, wishCycle=%b}",
                name, category, getConfidencePercent(), isWishCycle);
    }
}
