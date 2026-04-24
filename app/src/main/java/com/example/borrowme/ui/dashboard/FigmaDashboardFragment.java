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

        bindClick(view, R.id.btn_notifications, "Notifications");
        bindClick(view, R.id.btn_view_details, "View details");
        bindClick(view, R.id.card_borrow, "Borrow");
        bindClick(view, R.id.card_lend, "Lend item");
        bindClick(view, R.id.btn_see_all, "See all requests");
        bindClick(view, R.id.card_request_hair_dryer, "Hair Dryer");
        bindClick(view, R.id.card_request_adapter, "HDMI Adapter");
        bindClick(view, R.id.fab_add_item, "Add item");
        bindClick(view, R.id.nav_home, "Home");
        bindClick(view, R.id.nav_lendings, "Lendings");
        bindClick(view, R.id.nav_requests, "Requests");
        bindClick(view, R.id.nav_profile, "Profile");
    }

    private void bindClick(@NonNull View parent, int viewId, @NonNull String label) {
        View target = parent.findViewById(viewId);
        if (target != null) {
            target.setOnClickListener(v ->
                    Toast.makeText(requireContext(), label, Toast.LENGTH_SHORT).show()
            );
        }
    }
}
