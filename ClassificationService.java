package com.smartbin.service;

import com.smartbin.model.*;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Core service that classifies a waste item and returns a structured result.
 *
 * Simulates the output an AI vision model would produce at the point of
 * disposal. In a real IoT deployment, this service would:
 *   1. Receive an image frame from the bin-lid camera.
 *   2. Run it through a trained CNN (e.g., ResNet-50 fine-tuned on waste data).
 *   3. Return the top-1 prediction with confidence score.
 *
 * Here we replace the model inference with a knowledge-base lookup,
 * keeping the same interface so the switch would be transparent to callers.
 */
public class ClassificationService {

    private final WasteKnowledgeBase knowledgeBase;
    private final Random rng;

    public ClassificationService() {
        this.knowledgeBase = new WasteKnowledgeBase();
        this.rng = new Random();
    }

    /**
     * Classify a waste item by name.
     *
     * @param itemName the item the user wants to dispose of
     * @return ClassificationResult, or empty Optional if the item is unknown
     */
    public Optional<ClassificationResult> classify(String itemName) {
        Optional<WasteKnowledgeBase.Entry> entry = knowledgeBase.lookup(itemName);

        if (entry.isEmpty()) {
            return Optional.empty();
        }

        WasteKnowledgeBase.Entry e = entry.get();

        // Add ±3% realistic noise to the confidence score (simulates model variance)
        double noisedConfidence = e.baseConfidence() + (rng.nextGaussian() * 0.03);
        noisedConfidence = Math.max(0.50, Math.min(0.99, noisedConfidence));

        WasteItem item = new WasteItem(
                capitalise(itemName.trim()),
                e.category(),
                noisedConfidence,
                e.disposalInstruction(),
                e.isWishCycleTrap()
        );

        ClassificationResult result = new ClassificationResult(
                item,
                e.tips() != null ? e.tips() : List.of(),
                e.wishCycleWarning(),
                e.category() == WasteCategory.HAZARDOUS || e.category() == WasteCategory.SPECIAL
        );

        return Optional.of(result);
    }

    /** Returns the knowledge base (used by analytics to enumerate categories). */
    public WasteKnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }

    private String capitalise(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
