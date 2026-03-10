Manifesto

---

# ReConnect: Product Strategy & Monetization Roadmap (2026)

## 1. Core Philosophy

**ReConnect** is a "Material Expressive" Personal CRM designed for high-value personal and professional relationships.

* **Privacy First:** Local-first architecture by default.
* **Design-Led:** Leveraging the 2026 Material 3 Expressive motion tokens and organic shapes.
* **Utility-Driven:** The core "Alerts" and "Nudges" are free, ensuring the app's primary purpose is accessible to everyone.

---

## 2. Feature Tiering

To ensure long-term sustainability without sacrificing the "soul" of the app, features are divided into three tiers: **Analog**, **Core Sync**, and **Professional Intelligence**.

### Tier 1: Analog (Free)

*Focus: The best "Local Notebook" experience.*

* **Unlimited People & Moments:** No cap on the number of contacts or notes stored locally.
* **Core Alerts:** Standard recurring reminders (Daily, Weekly, Monthly, Yearly).
* **Material Expressive UI:** Full access to the "Bento" dashboard and timeline views.
* **Manual Export:** Ability to export/import data via JSON/CSV for manual backups.

### Tier 2: Core Sync ($0.99/mo or $11.99/yr)

*Focus: Seamless multi-device convenience.*

* **Automated Supabase Sync:** Real-time cloud backup and multi-device synchronization.
* **Unlimited Text History:** Lifetime cloud storage of all notes and event metadata.
* **Cross-Platform Access:** Use ReConnect across Android, Tablet, and eventually Web.

### Tier 3: Professional Intelligence ($4.99/mo or $45.99/yr)

*Focus: AI-driven relationship superpowers.*

* **AI Prep Cards:** 3-bullet "Cheat Sheets" generated before a scheduled catch-up based on past notes.
* **Sentiment Timeline:** Visual health tracking of relationships based on NLP analysis of notes and interaction frequency.
* **Voice-to-Note:** AI transcription and summarization of voice memos directly into a person's timeline.
* **Location Nudges:** Alerts triggered when you are physically near a contact’s favorite spot or "Home Base."
* **Media Vault:** Cloud sync for photos, documents, and voice attachments (high-bandwidth storage).

---

## 3. The AI & Infrastructure Strategy

To keep overhead low and ensure the "Professional" tier remains profitable:

* **Ollama Hosting:** Deploying a CPU-optimized or on-demand GPU VM for LLM inference.
* **Local Processing:** Leveraging **MediaPipe/AICore** for basic NLP tasks on-device to reduce API/Server costs.
* **BYOK (Bring Your Own Key):** A "Developer Mode" option allowing users to input their own Gemini/OpenAI keys to unlock AI features without a subscription.

---

## 4. 2026 Growth Tactics

* **The "Founding Member" Lifetime Deal:** A one-time payment ($29–$49) for lifetime Sync + early AI features to generate upfront capital for Supabase Pro costs.
* **Zero Ads Policy:** Maintaining a premium, private environment to build trust and protect the "Expressive" aesthetic.
* **Regional Pricing:** Aggressive localized pricing for the Indian market (e.g., ₹29/mo for Sync) to drive high-volume adoption among students.

---

## 5. Technical Roadmap (Next Steps)

1. **Sync Engine:** Integrate **PowerSync** or **Supabase Realtime** with Room for offline-first reliability.
2. **Sentiment Engine:** Develop a basic Keyword/Frequency analyzer to test the "Sentiment Timeline" logic.
3. **Billing:** Implement **RevenueCat** to manage Google Play subscriptions and lifetime entitlements.

---

### How to use this file:

You can copy the content above and save it as `STRATEGY.md` in your project's root directory. It’s a perfect "North Star" for when you start feeling the "feature creep" or get stuck on a technical implementation!

**Would you like me to draft a "Product Hunt" style description based on this strategy for when you're ready to show the world?**
