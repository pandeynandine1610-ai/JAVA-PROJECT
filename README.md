# JAVA-PROJECT

# SmartBin Advisor 🗑️♻️

> **AI-powered waste classification at the point of disposal** — providing instant guidance to households and real-time waste-stream analytics to municipalities.

---

## Table of Contents

- [Problem Statement](#problem-statement)
- [What This Project Does](#what-this-project-does)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [How to Build & Run](#how-to-build--run)
- [How to Use](#how-to-use)
- [Running the Tests](#running-the-tests)
- [Design Decisions](#design-decisions)
- [Future Extensions](#future-extensions)

---

## Problem Statement

Municipalities have **no real-time data** on what households are actually throwing away until waste reaches a sorting facility — by which point contamination has often already rendered recyclable batches unusable.

The root cause is **"wish-cycling"**: people place non-recyclables in recycling bins *hoping* they'll be recycled. This good intention causes real harm — one contaminated item can ruin an entire sorting batch.

> **SmartBin Advisor** simulates an AI-vision system mounted at the household bin that intercepts this moment: classifying the item *before* it enters the bin, giving the resident immediate, actionable guidance, and feeding anonymised stream data to a city analytics layer.

---

## What This Project Does

| Feature | Description |
|---|---|
| **Item Classification** | Type any waste item name; get the correct bin, confidence score, and disposal instructions |
| **Wish-Cycle Detection** | High-priority alerts for items commonly mis-recycled (plastic bags, batteries, pizza boxes) |
| **Session Analytics** | Tracks recycling rate, landfill diversion rate, and wish-cycle frequency |
| **Sustainability Badge** | Gamified score (Bronze → Platinum) to encourage behaviour change |
| **Municipal Dashboard View** | ASCII analytics report simulating data a city would receive in real-time |

### Sample Session

```
  Enter item name: plastic bag

  🔍 Scanning: "plastic bag"...

  ───────────────────────────────────────────────────────
  PLASTIC BAG              CONFIDENCE: 20%
  ───────────────────────────────────────────────────────

  ♻️  BLUE BIN  ← INCORRECT for this item

  📋 HOW TO DISPOSE:
     NOT in kerbside recycling. Return to supermarket
     soft-plastic collection points.

  ┌─────────────────────────────────────────────────────┐
  │ ⚠️ WISH-CYCLE ALERT: Plastic bags jam sorting       │
  │ machinery and contaminate batches. Take to a        │
  │ soft-plastics drop-off point instead.               │
  └─────────────────────────────────────────────────────┘

  💡 TIPS:
     • Many supermarkets have free soft-plastics recycling bins.
     • Reuse plastic bags as many times as possible.
```

---

## Project Structure

```
SmartBinAdvisor/
│
├── src/
│   ├── main/java/com/smartbin/
│   │   ├── Main.java                          # Entry point
│   │   ├── model/
│   │   │   ├── WasteItem.java                 # Domain object: a single classified item
│   │   │   ├── WasteCategory.java             # Enum: RECYCLABLE, ORGANIC, HAZARDOUS, etc.
│   │   │   ├── ClassificationResult.java      # Result wrapper (item + tips + warnings)
│   │   │   └── SessionStats.java              # Aggregated session metrics
│   │   ├── service/
│   │   │   ├── ClassificationService.java     # Core classification logic
│   │   │   └── WasteKnowledgeBase.java        # Item lookup table (simulates ML model)
│   │   ├── analytics/
│   │   │   └── AnalyticsService.java          # Session-level waste-stream analytics
│   │   └── ui/
│   │       └── ConsoleUI.java                 # Interactive console interface
│   │
│   └── test/java/com/smartbin/
│       ├── ClassificationServiceTest.java     # Unit tests: classification correctness
│       └── AnalyticsServiceTest.java          # Unit tests: analytics aggregation
│
├── pom.xml                                    # Maven build configuration
├── README.md
└── PROJECT_REPORT.md
```

---

## Prerequisites

| Tool | Version |
|---|---|
| Java JDK | 21 or later |
| Apache Maven | 3.9 or later |

**Check your versions:**

```bash
java -version
mvn -version
```

**Install on Ubuntu/Debian:**

```bash
sudo apt install openjdk-21-jdk maven
```

**Install on macOS (with Homebrew):**

```bash
brew install openjdk@21 maven
```

**Install on Windows:**

Download from [adoptium.net](https://adoptium.net) (JDK) and [maven.apache.org](https://maven.apache.org/download.cgi) (Maven).

---

## How to Build & Run

### Option A — Build a runnable JAR (recommended)

```bash
# 1. Clone the repository
git clone https://github.com/YOUR_USERNAME/SmartBinAdvisor.git
cd SmartBinAdvisor

# 2. Build
mvn clean package -DskipTests

# 3. Run
java -jar target/SmartBinAdvisor.jar
```

### Option B — Compile and run without packaging

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.smartbin.Main"
```

---

## How to Use

Once running, type any household waste item name and press **Enter**:

| Command | Description |
|---|---|
| `plastic bottle` | Classify any item by name |
| `help` | Show a list of sample items to try |
| `analytics` | View the waste-stream analytics dashboard |
| `history` | List all items classified in this session |
| `quit` | Exit the application |

**Try these items to explore the system:**

```
plastic bottle    → Recycling with high confidence
battery           → Hazardous — wish-cycle alert!
banana peel       → Organic/compost
plastic bag       → Wish-cycle alert + special instructions
mobile phone      → E-waste centre
pizza box         → Wish-cycle alert (greasy = not recyclable)
```

---

## Running the Tests

```bash
mvn test
```

Tests cover:
- Correct category assignment for all major item types
- Wish-cycle detection for known mis-recycled items
- Confidence score bounds (always 0.0–1.0)
- Unknown item handling (graceful empty Optional)
- Input normalisation (case-insensitive, whitespace-trimmed)
- Analytics aggregation (recycling rate, diversion rate, wish-cycle rate)
- History list immutability

---

## Design Decisions

### Why a knowledge-base instead of a real ML model?

The `WasteKnowledgeBase` is explicitly designed to mirror the interface a trained image-classification CNN would expose. The `ClassificationService` calls `lookup()` — which in production would be replaced by `model.predict(imageFrame)`. This separation (Strategy pattern) means the switch to a real model requires no changes outside that single class.

### Why Optional for classification results?

Using `Optional<ClassificationResult>` forces every caller to explicitly handle the "unknown item" case, preventing null pointer exceptions and making the API self-documenting.

### Why records for knowledge base entries?

Java 21 records give immutable, compact data containers with auto-generated constructors, `equals`, and `toString` — ideal for knowledge base entries that should never be mutated after loading.

### Why simulate confidence score noise?

Adding ±3% Gaussian noise to confidence scores (within bounds) simulates the natural variance of a real ML model inference, making the output feel authentic and prompting discussion about confidence thresholds in production.

---

## Future Extensions

| Extension | Description |
|---|---|
| Real ML model | Integrate TensorFlow Lite or ONNX Runtime for actual image classification |
| Camera input | Use OpenCV to capture frames from a webcam/IoT camera |
| REST API | Expose classification endpoint so smart bins can POST item images |
| Municipal dashboard | Push session stats to a time-series DB (InfluxDB) with a Grafana frontend |
| Council localisation | Load council-specific rules from JSON config (rules differ by postcode) |
| Mobile companion app | Android/iOS app for citizens to scan items at home before bin day |
