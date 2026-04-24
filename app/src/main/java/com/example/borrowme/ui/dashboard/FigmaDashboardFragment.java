package com.example.borrowme.ui.dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.borrowme.R;

public class FigmaDashboardFragment extends Fragment {

    private View btnNotifications;
    private View btnViewDetails;
    private View cardBorrow;
    private View cardLend;
    private View btnSeeAll;
    private View cardRequestHairDryer;
    private View cardRequestAdapter;
    private View navHome;
    private View navLendings;
    private View navRequests;
    private View navProfile;
    private View fabAddItem;

    public FigmaDashboardFragment() {
        super(R.layout.fragment_figma_dashboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View root = view.findViewById(R.id.figma_dashboard_root);
        View bottomBar = view.findViewById(R.id.bottom_navigation_bar);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            bottomBar.setPadding(
                    bottomBar.getPaddingLeft(),
                    bottomBar.getPaddingTop(),
                    bottomBar.getPaddingRight(),
                    systemBars.bottom
            );
            return insets;
        });

        initViews(view);
        setupListeners();
    }

    private void initViews(@NonNull View root) {
        btnNotifications = root.findViewById(R.id.btn_notifications);
        btnViewDetails = root.findViewById(R.id.btn_view_details);
        cardBorrow = root.findViewById(R.id.card_borrow);
        cardLend = root.findViewById(R.id.card_lend);
        btnSeeAll = root.findViewById(R.id.btn_see_all);
        cardRequestHairDryer = root.findViewById(R.id.card_request_hair_dryer);
        cardRequestAdapter = root.findViewById(R.id.card_request_adapter);
        navHome = root.findViewById(R.id.nav_home);
        navLendings = root.findViewById(R.id.nav_lendings);
        navRequests = root.findViewById(R.id.nav_requests);
        navProfile = root.findViewById(R.id.nav_profile);
        fabAddItem = root.findViewById(R.id.fab_add_item);
    }

    private void setupListeners() {
        bindClick(btnNotifications, "Notifications");
        bindClick(btnViewDetails, "View details");
        bindClick(cardBorrow, "Borrow");
        bindClick(cardLend, "Lend item");
        bindClick(btnSeeAll, "See all requests");
        bindClick(cardRequestHairDryer, "Hair Dryer");
        bindClick(cardRequestAdapter, "HDMI Adapter");
        bindClick(fabAddItem, "Add item");
        bindClick(navHome, "Home");
        bindClick(navLendings, "Lendings");
        bindClick(navRequests, "Requests");
        bindClick(navProfile, "Profile");
    }

    private void bindClick(@Nullable View target, @NonNull String label) {
        if (target == null) {
            return;
        }
        target.setOnClickListener(v ->
                Toast.makeText(requireContext(), label, Toast.LENGTH_SHORT).show()
        );
    }
}
