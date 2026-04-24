package com.example.borrowme.ui.dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.borrowme.R;

public class FigmaBorrowFeedFragment extends Fragment {

    private View btnLocationSelector;
    private View btnNotifications;
    private View searchContainer;
    private EditText etSearch;
    private View btnFilter;
    private View chipAllItems;
    private View chipTextbooks;
    private View chipElectronics;
    private TextView tvSeeAll;
    private View cardLabCoat;
    private View cardCalculator;
    private View cardKettle;
    private View cardStudyLamp;
    private ImageView btnFavoriteLabCoat;
    private ImageView btnFavoriteCalculator;
    private ImageView btnFavoriteKettle;
    private ImageView btnFavoriteStudyLamp;
    private View navFeed;
    private View navLendings;
    private View navRequests;
    private View navProfile;
    private View fabAddItem;

    public FigmaBorrowFeedFragment() {
        super(R.layout.fragment_figma_borrow_feed);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners();
    }

    private void initViews(@NonNull View root) {
        btnLocationSelector = root.findViewById(R.id.btn_location_selector);
        btnNotifications = root.findViewById(R.id.btn_notifications);
        searchContainer = root.findViewById(R.id.search_container);
        etSearch = root.findViewById(R.id.et_search);
        btnFilter = root.findViewById(R.id.btn_filter);
        chipAllItems = root.findViewById(R.id.chip_all_items);
        chipTextbooks = root.findViewById(R.id.chip_textbooks);
        chipElectronics = root.findViewById(R.id.chip_electronics);
        tvSeeAll = root.findViewById(R.id.tv_see_all);
        cardLabCoat = root.findViewById(R.id.card_lab_coat);
        cardCalculator = root.findViewById(R.id.card_calculator);
        cardKettle = root.findViewById(R.id.card_kettle);
        cardStudyLamp = root.findViewById(R.id.card_study_lamp);
        btnFavoriteLabCoat = root.findViewById(R.id.btn_favorite_lab_coat);
        btnFavoriteCalculator = root.findViewById(R.id.btn_favorite_calculator);
        btnFavoriteKettle = root.findViewById(R.id.btn_favorite_kettle);
        btnFavoriteStudyLamp = root.findViewById(R.id.btn_favorite_study_lamp);
        navFeed = root.findViewById(R.id.nav_feed);
        navLendings = root.findViewById(R.id.nav_lendings);
        navRequests = root.findViewById(R.id.nav_requests);
        navProfile = root.findViewById(R.id.nav_profile);
        fabAddItem = root.findViewById(R.id.fab_add_item);
    }

    private void setupListeners() {
        btnLocationSelector.setOnClickListener(v -> {
            // TODO: Handle location selector click.
        });

        btnNotifications.setOnClickListener(v -> {
            // TODO: Handle notifications click.
        });

        searchContainer.setOnClickListener(v -> {
            // TODO: Handle search container click.
        });

        etSearch.setOnClickListener(v -> {
            // TODO: Handle search input click.
        });

        btnFilter.setOnClickListener(v -> {
            // TODO: Handle filter click.
        });

        chipAllItems.setOnClickListener(v -> {
            // TODO: Handle All Items chip click.
        });

        chipTextbooks.setOnClickListener(v -> {
            // TODO: Handle Textbooks chip click.
        });

        chipElectronics.setOnClickListener(v -> {
            // TODO: Handle Electronics chip click.
        });

        tvSeeAll.setOnClickListener(v -> {
            // TODO: Handle See all click.
        });

        cardLabCoat.setOnClickListener(v -> {
            // TODO: Handle Lab Coat card click.
        });

        cardCalculator.setOnClickListener(v -> {
            // TODO: Handle Scientific Calculator card click.
        });

        cardKettle.setOnClickListener(v -> {
            // TODO: Handle Electric Kettle card click.
        });

        cardStudyLamp.setOnClickListener(v -> {
            // TODO: Handle Study Lamp card click.
        });

        btnFavoriteLabCoat.setOnClickListener(v -> {
            // TODO: Handle Lab Coat favorite click.
        });

        btnFavoriteCalculator.setOnClickListener(v -> {
            // TODO: Handle Scientific Calculator favorite click.
        });

        btnFavoriteKettle.setOnClickListener(v -> {
            // TODO: Handle Electric Kettle favorite click.
        });

        btnFavoriteStudyLamp.setOnClickListener(v -> {
            // TODO: Handle Study Lamp favorite click.
        });

        navFeed.setOnClickListener(v -> {
            // TODO: Handle Feed navigation click.
        });

        navLendings.setOnClickListener(v -> {
            // TODO: Handle Lendings navigation click.
        });

        navRequests.setOnClickListener(v -> {
            // TODO: Handle Requests navigation click.
        });

        navProfile.setOnClickListener(v -> {
            // TODO: Handle Profile navigation click.
        });

        fabAddItem.setOnClickListener(v -> {
            // TODO: Handle add item FAB click.
        });
    }
}
