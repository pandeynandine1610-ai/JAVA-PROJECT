package com.smartbin.ui;

import com.smartbin.analytics.AnalyticsService;
import com.smartbin.model.ClassificationResult;
import com.smartbin.model.WasteItem;
import com.smartbin.service.ClassificationService;

import java.util.Optional;
import java.util.Scanner;

/**
 * Interactive console interface for the SmartBin Advisor.
 *
 * Simulates the feedback screen that would be mounted on a smart bin lid —
 * providing instant guidance at the moment of disposal, which is the key
 * intervention point for changing household recycling behaviour.
 *
 * Commands:
 *   <item name>   → classify an item
 *   history       → show items classified this session
 *   analytics     → show waste-stream analytics dashboard
 *   help          → list known items
 *   quit          → exit
 */
public class ConsoleUI {

    private static final String DIVIDER = "─".repeat(55);
    private static final String THIN    = "·".repeat(55);

    private final ClassificationService classificationService;
    private final AnalyticsService analyticsService;
    private final Scanner scanner;

    public ConsoleUI() {
        this.classificationService = new ClassificationService();
        this.analyticsService = new AnalyticsService();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printWelcome();

        while (true) {
            System.out.print("  Enter item name (or 'help', 'analytics', 'quit'): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            switch (input.toLowerCase()) {
                case "quit", "exit", "q" -> {
                    printGoodbye();
                    return;
                }
                case "analytics", "stats", "dashboard" -> printAnalytics();
                case "history"                          -> printHistory();
                case "help", "?"                        -> printHelp();
                default                                 -> handleClassification(input);
            }
        }
    }

    // ─── Command handlers ─────────────────────────────────────────────────────

    private void handleClassification(String input) {
        System.out.println();
        System.out.println("  " + THIN);
        System.out.println("  🔍 Scanning: \"" + input + "\"...");
        simulateScanDelay();

        Optional<ClassificationResult> resultOpt = classificationService.classify(input);

        if (resultOpt.isEmpty()) {
            System.out.println();
            System.out.println("  ❓ UNKNOWN ITEM");
            System.out.println("  ──────────────────────────────────────────────");
            System.out.println("  This item isn't in the knowledge base.");
            System.out.println("  Try: a more specific name (e.g., 'plastic bottle')");
            System.out.println("       or check your council's A–Z waste guide online.");
            System.out.println("  " + DIVIDER);
            System.out.println();
            return;
        }

        ClassificationResult result = resultOpt.get();
        WasteItem item = result.getItem();

        // Record in analytics
        analyticsService.record(item);

        // Print classification card
        System.out.println();
        System.out.println("  " + DIVIDER);
        System.out.printf("  %-20s  CONFIDENCE: %s%n",
                item.getName().toUpperCase(), item.getConfidencePercent());
        System.out.println("  " + DIVIDER);
        System.out.println();
        System.out.println("  " + item.getCategory().getBinLabel());
        System.out.println("  " + item.getCategory().getDescription());
        System.out.println();
        System.out.println("  📋 HOW TO DISPOSE:");
        System.out.println("     " + item.getDisposalInstruction());

        // Wish-cycle warning (priority display)
        if (result.isWishCycleDetected()) {
            System.out.println();
            System.out.println("  ┌─────────────────────────────────────────────────┐");
            System.out.println("  │ " + result.getWishCycleWarning());
            System.out.println("  └─────────────────────────────────────────────────┘");
        }

        // Tips
        if (!result.getTips().isEmpty()) {
            System.out.println();
            System.out.println("  💡 TIPS:");
            for (String tip : result.getTips()) {
                System.out.println("     • " + tip);
            }
        }

        // Special handling reminder
        if (result.isRequiresSpecialInstructions()) {
            System.out.println();
            System.out.println("  ⚠️  This item requires special disposal. Do NOT use");
            System.out.println("     standard household bins.");
        }

        System.out.println();
        System.out.println("  " + DIVIDER);
        System.out.printf("  Session total: %d items | Wish-cycles caught: %d%n",
                analyticsService.getStats().getTotalItems(),
                analyticsService.getStats().getWishCycleCount());
        System.out.println("  " + DIVIDER);
        System.out.println();
    }

    private void printAnalytics() {
        System.out.println();
        System.out.println("  " + DIVIDER);
        System.out.println("  📊 WASTE-STREAM ANALYTICS DASHBOARD");
        System.out.println("  " + DIVIDER);
        System.out.println();
        System.out.println(analyticsService.generateReport());
        System.out.println("  " + DIVIDER);
        System.out.println();
    }

    private void printHistory() {
        var history = analyticsService.getHistory();
        System.out.println();
        System.out.println("  " + DIVIDER);
        System.out.println("  📜 SESSION HISTORY");
        System.out.println("  " + DIVIDER);
        if (history.isEmpty()) {
            System.out.println("  No items classified yet.");
        } else {
            for (int i = 0; i < history.size(); i++) {
                WasteItem item = history.get(i);
                System.out.printf("  %2d. %-22s → %-18s (%s)%s%n",
                        i + 1,
                        item.getName(),
                        item.getCategory().getDisplayName(),
                        item.getConfidencePercent(),
                        item.isWishCycle() ? " ⚠️" : "");
            }
        }
        System.out.println("  " + DIVIDER);
        System.out.println();
    }

    private void printHelp() {
        System.out.println();
        System.out.println("  " + DIVIDER);
        System.out.println("  📚 SAMPLE ITEMS YOU CAN CLASSIFY");
        System.out.println("  " + DIVIDER);
        System.out.println("  ♻️  Recyclables  : plastic bottle, cardboard box, glass bottle,");
        System.out.println("                    newspaper, tin can, aluminium foil");
        System.out.println("  🟤 Organic      : banana peel, coffee grounds, food scraps,");
        System.out.println("                    egg shells, paper towel");
        System.out.println("  ⬛ General      : styrofoam, pizza box, nappy");
        System.out.println("  ⚠️  Hazardous    : battery, paint, motor oil, medicine, cfl bulb");
        System.out.println("  🖥️  E-Waste      : mobile phone, laptop, printer cartridge, led bulb");
        System.out.println("  🏥 Special      : broken glass, clothing, mattress");
        System.out.println();
        System.out.println("  Tip: Try wish-cycle items like 'plastic bag' or 'pizza box'!");
        System.out.println("  " + DIVIDER);
        System.out.println();
    }

    // ─── Display helpers ──────────────────────────────────────────────────────

    private void printWelcome() {
        System.out.println("  " + DIVIDER);
        System.out.println("  Welcome to SmartBin Advisor");
        System.out.println("  AI-Powered Waste Classification at Point of Disposal");
        System.out.println("  " + DIVIDER);
        System.out.println("  Type an item name to find out which bin it belongs in.");
        System.out.println("  Type 'help' to see sample items, or 'analytics' for stats.");
        System.out.println("  " + DIVIDER);
        System.out.println();
    }

    private void printGoodbye() {
        System.out.println();
        System.out.println("  " + DIVIDER);
        System.out.println("  Thanks for using SmartBin Advisor!");
        if (analyticsService.getStats().getTotalItems() > 0) {
            System.out.println();
            System.out.println("  Your final session stats:");
            System.out.println(analyticsService.generateReport());
        }
        System.out.println("  Together we can eliminate waste-stream dark data. 🌱");
        System.out.println("  " + DIVIDER);
    }

    /** Brief artificial delay — simulates AI model inference time. */
    private void simulateScanDelay() {
        try { Thread.sleep(350); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
