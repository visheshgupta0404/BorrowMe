# 🎤 Presentation Script: Authentication & Onboarding (BorrowMe)

## **Slide 1: Introduction**
"Hello everyone, I’m Vedansh. I worked on the **Authentication and Onboarding** module for BorrowMe. This is the first touchpoint for any user entering our ecosystem, so my focus was on making it secure, university-specific, and visually premium."

---

## **Slide 2: Splash Screen (The Entry Experience)**
"When you open the app, you're greeted by our **Splash Screen**. 
*   **Technically:** It’s not just a logo; it’s a session manager. It uses **Firebase Auth** to check if a user is already signed in.
*   **The Flow:** If you're a returning user, it takes you straight to the Home dashboard. If you're new, it guides you to the Signup flow.
*   **UI Detail:** I implemented a smooth **Linear Progress Indicator** to give a sense of 'loading the campus network,' enhancing the initial user experience."

---

## **Slide 3: Registration & Profile Setup (SignupActivity)**
"Next is the **Signup Screen**. Since BorrowMe is built for students, we have strict security here.
*   **Domain Validation:** We only allow registration from university emails (like `@bmu.edu.in`). If a user tries a generic Gmail, the app blocks it to keep the community safe.
*   **Hostel Integration:** During signup, users select their specific Hostel block (like Bhagat Singh or Kalpana Chawla). This helps in calculating the 'proximity' for borrowing items later.
*   **Database:** Once you click 'Create Account,' your profile is instantly synced to **Cloud Firestore** with an initial 'Reputation Score' of 100."

---

## **Slide 4: Secure Login (LoginActivity)**
"For returning users, the **Login Screen** provides a secure gateway.
*   **Features:** I included a password visibility toggle and a 'Forgot Password' flow that triggers a reset email directly from Firebase.
*   **Auto-Healing:** If for some reason a user has an account but their profile document is missing in the database, the Login logic detects this and 'heals' the account by recreating the document."

---

## **Slide 5: Technical Tech Stack Summary**
"To summarize the tech behind these screens:
1.  **Firebase Auth:** For secure, encrypted login sessions.
2.  **Firestore:** For storing real-time user metadata.
3.  **Material Design 3:** For the UI components like the rounded buttons and circular profile images.
4.  **Java & XML:** The core architecture used for the logic and layouts."

---

## **Slide 6: Conclusion**
"By streamlining this onboarding process, we ensure that every user on BorrowMe is verified, localized to a hostel, and ready to start sharing. Thank you!"
