package com.smartbin.service;

import com.smartbin.model.WasteCategory;

import java.util.*;

/**
 * Knowledge base of waste items and their classifications.
 *
 * In production this would be backed by a trained image-classification
 * model (e.g., a CNN fine-tuned on labelled bin images). Here we model
 * that behaviour with a rule-based lookup table plus keyword matching —
 * demonstrating the same interface a real ML model would expose.
 *
 * Design pattern: Strategy — this class can be swapped for an ML
 * implementation without changing any other class.
 */
public class WasteKnowledgeBase {

    /**
     * Internal record pairing a category with classification metadata.
     */
    record Entry(WasteCategory category,
                 double baseConfidence,
                 String disposalInstruction,
                 boolean isWishCycleTrap,   // true = commonly mis-recycled
                 String wishCycleWarning,
                 List<String> tips) {}

    // ─── Lookup table ─────────────────────────────────────────────────────────
    // Key = normalised item name (lowercase, trimmed)
    private final Map<String, Entry> exactMatches = new LinkedHashMap<>();

    // Keyword fragments → category hints (used when exact match fails)
    private final Map<String, WasteCategory> keywordHints = new LinkedHashMap<>();

    public WasteKnowledgeBase() {
        loadExactMatches();
        loadKeywordHints();
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Look up an item by name.
     *
     * @param rawInput the item name as typed by the user
     * @return Optional<Entry> if a match (exact or keyword) is found
     */
    public Optional<Entry> lookup(String rawInput) {
        String key = normalise(rawInput);

        // 1. Exact match
        if (exactMatches.containsKey(key)) {
            return Optional.of(exactMatches.get(key));
        }

        // 2. Partial / contains match in known items
        for (Map.Entry<String, Entry> e : exactMatches.entrySet()) {
            if (key.contains(e.getKey()) || e.getKey().contains(key)) {
                // Reduce confidence slightly since it's not an exact match
                Entry base = e.getValue();
                Entry adjusted = new Entry(
                        base.category(),
                        Math.max(0.55, base.baseConfidence() - 0.12),
                        base.disposalInstruction(),
                        base.isWishCycleTrap(),
                        base.wishCycleWarning(),
                        base.tips()
                );
                return Optional.of(adjusted);
            }
        }

        // 3. Keyword hint match
        for (Map.Entry<String, WasteCategory> hint : keywordHints.entrySet()) {
            if (key.contains(hint.getKey())) {
                return Optional.of(buildKeywordEntry(hint.getValue()));
            }
        }

        return Optional.empty();
    }

    /** Normalises input for comparison: lowercase, trimmed, collapsed whitespace. */
    public String normalise(String input) {
        return input.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    // ─── Data loading ─────────────────────────────────────────────────────────

    private void loadExactMatches() {
        // ── Recyclables ────────────────────────────────────────────────────────
        add("plastic bottle", WasteCategory.RECYCLABLE, 0.97,
                "Rinse, remove cap (recycle separately), crush to save space.",
                false, null,
                List.of("Empty bottles only — liquid contamination ruins entire batches.",
                        "Remove labels if foil-backed; paper labels are fine to leave on."));

        add("cardboard box", WasteCategory.RECYCLABLE, 0.95,
                "Flatten before placing in blue bin. Remove tape and staples.",
                false, null,
                List.of("Wet or greasy cardboard (e.g., pizza boxes) is NOT recyclable.",
                        "Break down large boxes so they don't block bin sensors."));

        add("glass bottle", WasteCategory.RECYCLABLE, 0.94,
                "Rinse thoroughly. Lids and corks go in general waste.",
                false, null,
                List.of("Colour-sort where local rules require it.",
                        "Broken glass must be wrapped safely — never loose in the bin."));

        add("newspaper", WasteCategory.RECYCLABLE, 0.96,
                "Dry paper is fully recyclable. Keep dry and uncontaminated.",
                false, null,
                List.of("Shredded paper is often too small for sorting — bag it inside a paper bag."));

        add("tin can", WasteCategory.RECYCLABLE, 0.95,
                "Rinse clean. Leave labels on — they burn off in processing.",
                false, null,
                List.of("Aerosol cans are recyclable if completely empty."));

        add("aluminium foil", WasteCategory.RECYCLABLE, 0.88,
                "Clean foil only. Scrunch multiple pieces into a ball (>5cm) for sorting machines.",
                false, null,
                List.of("Foil contaminated with food goes in general waste."));

        add("plastic bag", WasteCategory.RECYCLABLE, 0.20,
                "NOT in kerbside recycling. Return to supermarket soft-plastic collection points.",
                true,
                "⚠️  WISH-CYCLE ALERT: Plastic bags jam sorting machinery and contaminate batches. " +
                        "Take to a soft-plastics drop-off point instead.",
                List.of("Many supermarkets have free soft-plastics recycling bins near the entrance.",
                        "Reuse plastic bags as many times as possible before recycling."));

        add("styrofoam", WasteCategory.GENERAL_WASTE, 0.93,
                "General waste only. Styrofoam is not accepted in kerbside recycling.",
                true,
                "⚠️  WISH-CYCLE ALERT: Styrofoam (EPS foam) is not recyclable in most councils. " +
                        "Check if a specialist drop-off exists in your area.",
                List.of("Some councils partner with specialist EPS recyclers — check your council website."));

        add("pizza box", WasteCategory.GENERAL_WASTE, 0.85,
                "Greasy boxes go in general waste. The oil contaminates paper recycling.",
                true,
                "⚠️  WISH-CYCLE ALERT: Greasy pizza boxes contaminate paper recycling batches. " +
                        "If only the lid is greasy, tear it off and recycle the clean base.",
                List.of("Tear off and recycle the clean parts — only bin the greasy sections."));

        // ── Organic ────────────────────────────────────────────────────────────
        add("banana peel", WasteCategory.ORGANIC, 0.98,
                "Compost/organic bin. Excellent for home composting too.",
                false, null,
                List.of("Fruit peels decompose quickly and add nitrogen to compost."));

        add("coffee grounds", WasteCategory.ORGANIC, 0.97,
                "Organic/compost bin, or add directly to garden soil.",
                false, null,
                List.of("Coffee grounds are loved by worms in worm farms.",
                        "Cafes often give away spent grounds free for gardeners."));

        add("food scraps", WasteCategory.ORGANIC, 0.96,
                "All cooked or uncooked food scraps in the organic bin.",
                false, null,
                List.of("Meat and dairy are accepted in council organics bins (unlike home compost).",
                        "Use a kitchen caddy with compostable liners to manage food scraps hygienically."));

        add("egg shells", WasteCategory.ORGANIC, 0.97,
                "Organic/compost bin. Excellent calcium source for compost.",
                false, null,
                List.of("Crush shells to speed up decomposition and deter slugs in gardens."));

        add("paper towel", WasteCategory.ORGANIC, 0.82,
                "Used paper towels with food or liquid go in organic/compost bin.",
                false, null,
                List.of("Clean, dry paper towels can be recycled. Soiled ones go to organics.",
                        "Switch to reusable cloths to reduce waste entirely."));

        // ── Hazardous ──────────────────────────────────────────────────────────
        add("battery", WasteCategory.HAZARDOUS, 0.99,
                "Take to a battery recycling point — hardware stores, libraries, or council depots.",
                true,
                "⚠️  HAZARD: Batteries contain toxic heavy metals. They can puncture and start fires " +
                        "in compactor trucks. NEVER put in any household bin.",
                List.of("Many supermarkets and hardware stores have free battery drop-off boxes.",
                        "Switch to rechargeable batteries to reduce disposal frequency."));

        add("paint", WasteCategory.HAZARDOUS, 0.98,
                "Council hazardous waste facility only. Never pour down drains.",
                false, null,
                List.of("Dry latex paint (leave lid off to dry) can often go in general waste once solid.",
                        "Donate usable paint to community groups or Habitat for Humanity ReStore."));

        add("motor oil", WasteCategory.HAZARDOUS, 0.99,
                "Auto workshop or council hazardous waste only. Highly toxic to waterways.",
                false, null,
                List.of("Many auto shops accept used motor oil for free recycling."));

        add("medicine", WasteCategory.HAZARDOUS, 0.97,
                "Return to a pharmacy for safe disposal through the MedReturn program.",
                false, null,
                List.of("Never flush medicine down the toilet — it enters the water supply.",
                        "Pharmacies are legally required to accept returned medication in most countries."));

        // ── E-Waste ────────────────────────────────────────────────────────────
        add("mobile phone", WasteCategory.E_WASTE, 0.99,
                "E-waste recycling centre or manufacturer take-back program.",
                false, null,
                List.of("Wipe data before recycling. Many manufacturers offer trade-in credit.",
                        "Working phones can be donated to domestic violence support services."));

        add("laptop", WasteCategory.E_WASTE, 0.99,
                "E-waste centre or manufacturer take-back. Contains valuable recoverable metals.",
                false, null,
                List.of("Remove and separately recycle the battery if possible.",
                        "Libraries and schools sometimes accept working donated laptops."));

        add("printer cartridge", WasteCategory.E_WASTE, 0.95,
                "Many office supply stores offer free cartridge recycling.",
                false, null,
                List.of("Refillable cartridges exist — reduces waste and saves money."));

        add("cfl bulb", WasteCategory.HAZARDOUS, 0.97,
                "Fluorescent bulbs contain mercury. Take to hardware store recycling point.",
                true,
                "⚠️  HAZARD: CFL bulbs contain mercury vapour. Never break or bin them.",
                List.of("Switch to LED — no mercury, last 25× longer, much cheaper to run."));

        add("led bulb", WasteCategory.E_WASTE, 0.90,
                "E-waste recycling. Contains circuit boards and small amounts of metals.",
                false, null,
                List.of("LEDs contain no mercury, but the electronics still need proper recycling."));

        // ── General Waste ──────────────────────────────────────────────────────
        add("nappy", WasteCategory.GENERAL_WASTE, 0.99,
                "Wrap and seal in a bag. General waste only.",
                false, null,
                List.of("Reusable cloth nappies can save hundreds of kilograms of landfill waste.",
                        "Some councils pilot compostable nappy collection — check locally."));

        add("broken glass", WasteCategory.SPECIAL, 0.95,
                "Wrap tightly in newspaper, tape shut, and label 'BROKEN GLASS' before binning.",
                false, null,
                List.of("Never place loose broken glass in any bin — safety risk to workers."));

        add("clothing", WasteCategory.SPECIAL, 0.90,
                "Donate wearable items to charity bins. Worn-out fabric goes to textile recyclers.",
                false, null,
                List.of("Many councils have textile recycling bins separate from general kerbside bins.",
                        "Brands like H&M and Zara offer in-store textile recycling vouchers."));

        add("mattress", WasteCategory.SPECIAL, 0.98,
                "Book a council bulk-waste collection. Mattresses can also be donated if in good condition.",
                false, null,
                List.of("Mattress components (springs, foam, fabric) are 80% recyclable by specialist recyclers."));
    }

    private void loadKeywordHints() {
        keywordHints.put("plastic", WasteCategory.RECYCLABLE);
        keywordHints.put("paper", WasteCategory.RECYCLABLE);
        keywordHints.put("card", WasteCategory.RECYCLABLE);
        keywordHints.put("metal", WasteCategory.RECYCLABLE);
        keywordHints.put("glass", WasteCategory.RECYCLABLE);
        keywordHints.put("bottle", WasteCategory.RECYCLABLE);
        keywordHints.put("can ", WasteCategory.RECYCLABLE);

        keywordHints.put("food", WasteCategory.ORGANIC);
        keywordHints.put("fruit", WasteCategory.ORGANIC);
        keywordHints.put("vegetable", WasteCategory.ORGANIC);
        keywordHints.put("veg", WasteCategory.ORGANIC);
        keywordHints.put("peel", WasteCategory.ORGANIC);
        keywordHints.put("garden", WasteCategory.ORGANIC);
        keywordHints.put("leaf", WasteCategory.ORGANIC);
        keywordHints.put("grass", WasteCategory.ORGANIC);

        keywordHints.put("battery", WasteCategory.HAZARDOUS);
        keywordHints.put("chemical", WasteCategory.HAZARDOUS);
        keywordHints.put("solvent", WasteCategory.HAZARDOUS);
        keywordHints.put("bleach", WasteCategory.HAZARDOUS);
        keywordHints.put("pesticide", WasteCategory.HAZARDOUS);

        keywordHints.put("phone", WasteCategory.E_WASTE);
        keywordHints.put("cable", WasteCategory.E_WASTE);
        keywordHints.put("charger", WasteCategory.E_WASTE);
        keywordHints.put("electronic", WasteCategory.E_WASTE);
        keywordHints.put("computer", WasteCategory.E_WASTE);
        keywordHints.put("tablet", WasteCategory.E_WASTE);

        keywordHints.put("furniture", WasteCategory.SPECIAL);
        keywordHints.put("tyre", WasteCategory.SPECIAL);
        keywordHints.put("tire", WasteCategory.SPECIAL);
        keywordHints.put("mattress", WasteCategory.SPECIAL);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void add(String key, WasteCategory category, double confidence,
                     String instruction, boolean wishCycleTrap,
                     String warning, List<String> tips) {
        exactMatches.put(key, new Entry(category, confidence, instruction,
                wishCycleTrap, warning, tips));
    }

    private Entry buildKeywordEntry(WasteCategory category) {
        return new Entry(
                category,
                0.65,   // lower confidence for keyword-only matches
                "See your council's waste guide for detailed instructions on this item type.",
                false, null,
                List.of("Tip: Enter a more specific item name for precise guidance.",
                        "When in doubt, check your local council's A–Z waste guide online.")
        );
    }
}
