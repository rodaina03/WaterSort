# WaterSort AI – Situation Calculus Agent in Prolog

## 🧠 Overview

This project implements a **logic-based intelligent agent** to solve a simplified version of the **WaterSort puzzle** using **Situation Calculus** in **Prolog**. The agent uses reasoning with fluents, successor-state axioms, and iterative deepening search to plan actions and reach a goal configuration.

Developed as part of the *Introduction to Artificial Intelligence* course at the **German University in Cairo (Winter 2024 Term)**.

---

## 🎯 Problem Specification

- **Goal:** Have each bottle filled completely with only one color.
- **Assumptions:**
  - 3 bottles.
  - 2 colors.
  - 2 layers per bottle.
  - One layer can be poured at a time.
  - Single allowed action: `pour(i, j)` – pour from bottle `i` to bottle `j`.
  - Bottles are indexed from 1.

---

## 📁 Project Files

- `KB.pl`: The initial knowledge base (world state).
- `Watersort.pl`: Main Prolog file containing:
  - Fluents
  - Successor-state axioms
  - Goal predicate
  - Iterative Deepening Search implementation

---

## 🛠️ How to Run

1. Ensure you have [SWI-Prolog](https://www.swi-prolog.org/) installed.
2. Place `KB.pl` and `Watersort.pl` in the same directory.
3. Open a terminal and run:

```prolog
?- [Watersort].
?- ids(S, 1).
