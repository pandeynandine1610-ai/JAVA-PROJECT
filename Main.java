package com.smartbin;

import com.smartbin.ui.ConsoleUI;

/**
 * SmartBin Advisor - AI-powered household waste classification system.
 *
 * Entry point for the application. Launches the interactive console UI
 * which simulates real-time AI vision waste classification and provides
 * municipal waste-stream analytics.
 *
 * Course Project: Bring Your Own Project (BYOP)
 * Problem: "Dark Data" of Domestic Waste
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   SmartBin Advisor v1.0");
        System.out.println("   AI-Powered Waste Classification System");
        System.out.println("=========================================");
        System.out.println();

        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}
