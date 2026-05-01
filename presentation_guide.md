# BorrowMe: Peer-to-Peer Item Sharing Platform
## Project Presentation & Technical Documentation

---

### 1. Project Overview
**BorrowMe** is a community-centric mobile application designed to facilitate the sharing of resources within a localized group (e.g., college hostels or residential societies). It enables users to list items they own for lending and borrow items from others, promoting a "circular economy" and reducing waste/redundant purchases.

### 2. Core Tech Stack
Our application is built on a robust and scalable modern mobile stack:
- **Platform:** Native Android (Minimum SDK 24+)
- **Programming Language:** Java (migrated for maximum compatibility and performance)
- **Backend-as-a-Service (BaaS):** Firebase
- **Image Processing & Hosting:** 
    - **ImgBB API:** For high-speed item image hosting.
    - **Glide:** For efficient image caching and loading.
- **UI Architecture:** Material Design 3 (M3) with custom animations.

---

### 3. Firebase Architecture & Connectivity
The app uses a decentralized serverless architecture powered by Google Firebase.

#### A. Authentication
- **Mechanism:** Firebase Authentication using Email and Password.
- **Process:**
    - User enters credentials in `LoginActivity` or `SignupActivity`.
    - `mAuth.signInWithEmailAndPassword()` or `createUserWithEmailAndPassword()` is called.
    - Upon success, a unique **UID** is generated which acts as the Primary Key for all data relations.
- **Security:** Session management is handled automatically by the Firebase SDK.

#### B. Firestore (NoSQL Database)
The heart of our data is stored in **Cloud Firestore**. We use three primary collections:

1.  **`users`**:
    - **Key Data:** `fullName`, `email`, `hostel`, `reputationScore`, `activeLendCount`.
    - **Purpose:** Manages user identity and their trust within the community.
2.  **`items`**:
    - **Key Data:** `title`, `description`, `imageUrl`, `ownerId`, `status` (AVAILABLE/BORROWED), `maxBorrowDays`.
    - **Connection:** Linked to `users` via `ownerId`.
3.  **`requests`**:
    - **Key Data:** `borrowerId`, `lenderId`, `itemId`, `status` (PENDING/APPROVED/RETURNED).
    - **Connection:** The bridge between a user and an item.

---

### 4. How Data is Saved (Data Flow)
When a user lends an item via `AddItemActivity`:
1.  **Image Upload:** The image is first uploaded to **ImgBB** (via `ImgBBUploader.java`). This returns a public URL.
2.  **Document Creation:** A new Map object is created containing the item details and the ImgBB URL.
3.  **Firestore Transaction:** 
    - The item is added to the `items` collection.
    - Simultaneously, the user's `activeLendCount` is incremented in the `users` document using `FieldValue.increment(1)`.
4.  **Real-time Updates:** Firestore listeners in `HomeActivity` and `FeedActivity` automatically refresh the UI to show the new item.

---

### 5. Application Workflow (Screens)
The app consists of **10 distinct screens**, each serving a specific purpose:

1.  **Splash Screen (`SplashActivity`):** Initial branding and auth-check logic (routes to Login or Home).
2.  **Login Screen (`LoginActivity`):** Secure gateway with password visibility toggle and reset options.
3.  **Signup Screen (`SignupActivity`):** Onboarding new users into the community.
4.  **Home Dashboard (`HomeActivity`):** 
    - Displays **Reputation Score** with dynamic animations.
    - Shows a "Quick Glance" at active requests.
    - Main navigation hub.
5.  **Feed / Browse (`FeedActivity`):** Searchable list of all items available in the community.
6.  **Add Item / Lend (`AddItemActivity`):** Form-based listing with camera/gallery integration for item photos.
7.  **Item Details (`ItemDetailActivity`):** Detailed view of an item with a "Request to Borrow" action.
8.  **User Profile (`ProfileActivity`):** View stats, edit profile details, and logout.
9.  **My Lendings (`MyLendingsActivity`):** Specialized view for managing items the user has listed.
10. **Requests Management (`RequestsManagementActivity`):** The control center for approving incoming requests and tracking outgoing ones.

---

### 6. Team & Work Division
Our team collaborated on different modules to ensure a cohesive and functional application:

| Member | Feature Module | Screens / Responsibilities |
| :--- | :--- | :--- | 
| **Vedansh** | Authentication & Onboarding | `SplashActivity`, `LoginActivity`, `SignupActivity` |
| **Vishal** | Core Dashboard & Discovery | `HomeActivity` (Dashboard), `FeedActivity` (Community Feed) |
| **Vishesh** | Item Management | `AddItemActivity` (Lend), `ItemDetailActivity` (Item Info) |
| **Yash** | Borrowing System | `RequestsManagementActivity`, Request Logic & Real-time Sync |
| **Vivek** | Profile & Inventory | `ProfileActivity`, `MyLendingsActivity` (Inventory Management) |

---

### 7. Key Innovations
- **Trust System:** A reputation score calculated based on lending history and return punctuality.
- **Dual Status Management:** Real-time tracking of whether an item is currently with the owner or a borrower.
- **Hybrid Storage:** Using ImgBB for item images to optimize storage costs while keeping Firestore for structured data.

---

### 8. References (How things are connected)
- **User -> Item:** `ownerId` in `items` collection refers to `uid` in `users` collection.
- **Request -> User:** `borrowerId` and `lenderId` in `requests` collection refer to `uid` in `users`.
- **Request -> Item:** `itemId` in `requests` collection refers to `documentID` in `items`.
- **Activity -> Activity:** Navigation is managed via `Intents` with `Extras` (e.g., passing `itemId` to `ItemDetailActivity`).
