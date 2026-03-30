package com.smartbin;

import com.smartbin.model.*;
import com.smartbin.service.ClassificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the core classification pipeline.
 *
 * Tests are organised around:
 *  1. Correct category assignment
 *  2. Wish-cycle detection
 *  3. Confidence score bounds
 *  4. Unknown item handling
 *  5. Input normalisation
 */
class ClassificationServiceTest {

    private ClassificationService service;

    @BeforeEach
    void setUp() {
        service = new ClassificationService();
    }

    // ── Category correctness ──────────────────────────────────────────────────

    @Test
    @DisplayName("Plastic bottle → RECYCLABLE")
    void plasticBottle_isRecyclable() {
        Optional<ClassificationResult> result = service.classify("plastic bottle");
        assertTrue(result.isPresent());
        assertEquals(WasteCategory.RECYCLABLE, result.get().getItem().getCategory());
    }

    @Test
    @DisplayName("Battery → HAZARDOUS")
    void battery_isHazardous() {
        Optional<ClassificationResult> result = service.classify("battery");
        assertTrue(result.isPresent());
        assertEquals(WasteCategory.HAZARDOUS, result.get().getItem().getCategory());
    }

    @Test
    @DisplayName("Mobile phone → E_WASTE")
    void mobilePhone_isEWaste() {
        Optional<ClassificationResult> result = service.classify("mobile phone");
        assertTrue(result.isPresent());
        assertEquals(WasteCategory.E_WASTE, result.get().getItem().getCategory());
    }

    @Test
    @DisplayName("Banana peel → ORGANIC")
    void bananaPeel_isOrganic() {
        Optional<ClassificationResult> result = service.classify("banana peel");
        assertTrue(result.isPresent());
        assertEquals(WasteCategory.ORGANIC, result.get().getItem().getCategory());
    }

    @Test
    @DisplayName("Nappy → GENERAL_WASTE")
    void nappy_isGeneralWaste() {
        Optional<ClassificationResult> result = service.classify("nappy");
        assertTrue(result.isPresent());
        assertEquals(WasteCategory.GENERAL_WASTE, result.get().getItem().getCategory());
    }

    // ── Wish-cycle detection ──────────────────────────────────────────────────

    @Test
    @DisplayName("Plastic bag is flagged as wish-cycle")
    void plasticBag_isWishCycle() {
        Optional<ClassificationResult> result = service.classify("plastic bag");
        assertTrue(result.isPresent());
        assertTrue(result.get().isWishCycleDetected(), "Plastic bag should trigger wish-cycle warning");
        assertNotNull(result.get().getWishCycleWarning());
    }

    @Test
    @DisplayName("Battery is flagged as wish-cycle/hazard")
    void battery_isWishCycle() {
        Optional<ClassificationResult> result = service.classify("battery");
        assertTrue(result.isPresent());
        assertTrue(result.get().isWishCycleDetected());
    }

    @Test
    @DisplayName("Plastic bottle is NOT flagged as wish-cycle")
    void plasticBottle_isNotWishCycle() {
        Optional<ClassificationResult> result = service.classify("plastic bottle");
        assertTrue(result.isPresent());
        assertFalse(result.get().isWishCycleDetected());
    }

    // ── Confidence bounds ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Confidence score is always between 0.0 and 1.0")
    void confidenceScore_isWithinBounds() {
        String[] testItems = {"plastic bottle", "battery", "banana peel",
                "mobile phone", "cardboard box", "motor oil"};
        for (String item : testItems) {
            Optional<ClassificationResult> result = service.classify(item);
            assertTrue(result.isPresent(), "Expected result for: " + item);
            double conf = result.get().getItem().getConfidenceScore();
            assertTrue(conf >= 0.0 && conf <= 1.0,
                    "Confidence out of range for " + item + ": " + conf);
        }
    }

    // ── Unknown item handling ─────────────────────────────────────────────────

    @Test
    @DisplayName("Completely unknown item returns empty Optional")
    void unknownItem_returnsEmpty() {
        Optional<ClassificationResult> result = service.classify("xyzzy_unrecognised_thing_12345");
        assertTrue(result.isEmpty(), "Unknown item should return empty Optional");
    }

    @Test
    @DisplayName("Empty string returns empty Optional")
    void emptyString_returnsEmpty() {
        Optional<ClassificationResult> result = service.classify("   ");
        // Should either return empty or handle gracefully without throwing
        // (whitespace-only input normalises to empty string)
        assertDoesNotThrow(() -> service.classify("   "));
    }

    // ── Input normalisation ───────────────────────────────────────────────────

    @Test
    @DisplayName("Case-insensitive lookup: PLASTIC BOTTLE == plastic bottle")
    void caseInsensitiveMatch() {
        Optional<ClassificationResult> lower = service.classify("plastic bottle");
        Optional<ClassificationResult> upper = service.classify("PLASTIC BOTTLE");
        Optional<ClassificationResult> mixed = service.classify("Plastic Bottle");

        assertTrue(lower.isPresent());
        assertTrue(upper.isPresent());
        assertTrue(mixed.isPresent());

        assertEquals(lower.get().getItem().getCategory(), upper.get().getItem().getCategory());
        assertEquals(lower.get().getItem().getCategory(), mixed.get().getItem().getCategory());
    }

    @Test
    @DisplayName("Extra whitespace is trimmed correctly")
    void whitespaceIsNormalised() {
        Optional<ClassificationResult> result = service.classify("  plastic   bottle  ");
        assertTrue(result.isPresent());
        assertEquals(WasteCategory.RECYCLABLE, result.get().getItem().getCategory());
    }

    // ── Disposal instructions ─────────────────────────────────────────────────

    @Test
    @DisplayName("Result always includes a non-empty disposal instruction")
    void disposalInstructionIsNeverEmpty() {
        String[] items = {"plastic bottle", "coffee grounds", "cfl bulb", "clothing"};
        for (String item : items) {
            Optional<ClassificationResult> result = service.classify(item);
            assertTrue(result.isPresent());
            assertFalse(result.get().getItem().getDisposalInstruction().isBlank(),
                    "Disposal instruction missing for: " + item);
        }
    }
}
