# BorrowMe: 3-Minute Presentation Script

This script covers the essential "Borrowing Cycle" using two separate user IDs.

---

## **Part 1: Introduction (0:00 - 0:30)**

**Action:** Open the app to the **Splash Screen** and then **Login Screen**.

**Script:**
> "Good morning/afternoon everyone. Today, we're presenting **BorrowMe**, a peer-to-peer item-sharing platform built for local communities like college hostels. 
> 
> Our app solves the problem of redundant purchases by allowing users to lend what they have and borrow what they need. We've used **Java** for the Android app, **Firebase** for our backend, and **ImgBB** for efficient image hosting. 
> 
> Let's jump into a live demonstration of our core flow: The Borrowing Request."

---

## **Part 2: User A - The Borrower (0:30 - 1:45)**

**Action:** Login as **User A**.

**Script:**
> "I'm logging in as **User A**, who needs a specific item. On the Home Dashboard, you can see my **Reputation Score**—this is our trust mechanism that ensures users return items on time.
>
> Now, I'll go to the **Community Feed**. I can see everything available in my hostel."

**Action:** Navigate to **FeedActivity**, scroll, and click on an item (e.g., "Scientific Calculator").

**Script:**
> "I found a 'Scientific Calculator' listed by another student. I'll click 'View Details' to check its description and how long I can borrow it for. 
>
> Everything looks good. I'll hit **'Request to Borrow'**. This instantly creates a pending request in our Firestore database."

**Action:** Click the request button, wait for the toast confirmation, then go to **Profile** and **Logout**.

**Script:**
> "The request is sent! Now, I'll log out so we can see what the lender sees on their end."

---

## **Part 3: User B - The Lender (1:45 - 2:45)**

**Action:** Login as **User B** (the owner of the calculator).

**Script:**
> "Now I'm logging in as **User B**. As a lender, I get a real-time update when someone wants to borrow my items. 
>
> I'll navigate to **'Requests Management'** from the dashboard."

**Action:** Navigate to **RequestsManagementActivity**. Locate the incoming request from User A.

**Script:**
> "Here, I can see the pending request from User A. I can see their name and which item they want. 
> 
> I'll click **'Approve'**. Notice what happens: 
> 1. The request status updates to 'APPROVED'.
> 2. The item's status in the feed will now show as 'BORROWED'.
> 3. User B's lending count increases, boosting their reputation."

---

## **Part 4: Conclusion (2:45 - 3:00)**

**Action:** Back to **HomeActivity**.

**Script:**
> "And that is the complete cycle. Our system handles everything from real-time status updates to image processing and trust-based scoring. 
> 
> BorrowMe isn't just an app; it's a way to build a more sustainable and collaborative community. Thank you!"

---

## **Technical Tips for the Presenter**
1.  **Pre-fill IDs:** Have the login credentials for User A and User B ready (on a sticky note or clipboard).
2.  **Item Setup:** Ensure User B already has an item listed (like a "Scientific Calculator") before you start.
3.  **Speed:** Practice the transition between Login and Feed so it feels snappy.
4.  **Reputation Check:** Point to the Reputation Score animation on the Home screen—it's a great visual "WOW" factor.
