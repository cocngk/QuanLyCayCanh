package com.example.btl;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

/** Shared bottom navigation behavior for the main app screens. */
public class BottomNavHelper {
    public static void setup(Activity activity) {
        setup(activity, R.id.navPlants);
    }

    public static void setup(Activity activity, int selectedTabId) {
        setTabClick(activity, R.id.navHome, MainActivity.class);
        setTabClick(activity, R.id.navPlants, PlantManagementActivity.class);
        setTabClick(activity, R.id.navBills, InvoiceManagementActivity.class);
        setTabClick(activity, R.id.navStats, Statistics.class);
        setTabClick(activity, R.id.navAccount, Account.class);

        selectTab(activity, selectedTabId);
    }

    private static void setTabClick(
            Activity activity, int tabId, Class<?> destination) {
        View tab = activity.findViewById(tabId);
        if (tab == null) return;

        tab.setOnClickListener(v -> {
            if (!destination.isInstance(activity)) {
                open(activity, destination);
            }
        });
    }

    private static void selectTab(Activity activity, int selectedTabId) {
        int[] tabIds = {
                R.id.navHome, R.id.navPlants, R.id.navBills, R.id.navStats, R.id.navAccount
        };
        int green = ContextCompat.getColor(activity, R.color.green_primary);
        int muted = ContextCompat.getColor(activity, R.color.text_secondary);
        for (int tabId : tabIds) {
            LinearLayout tab = activity.findViewById(tabId);
            if (tab == null) continue;
            boolean selected = tabId == selectedTabId;
            tab.setSelected(selected);
            tab.setBackgroundResource(selected
                    ? R.drawable.bg_nav_selected : android.R.color.transparent);
            for (int i = 0; i < tab.getChildCount(); i++) {
                android.view.View child = tab.getChildAt(i);
                if (child instanceof ImageView) {
                    ((ImageView) child).setColorFilter(selected ? green : muted);
                } else if (child instanceof TextView) {
                    TextView label = (TextView) child;
                    label.setTextColor(selected ? green
                            : ContextCompat.getColor(activity, R.color.text_primary));
                    label.setTypeface(Typeface.DEFAULT,
                            selected ? Typeface.BOLD : Typeface.NORMAL);
                }
            }
        }
    }

    private static void open(Activity activity, Class<?> page) {
        Intent intent = new Intent(activity, page);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        if (activity instanceof AddEditPlantActivity
                || activity instanceof CreateInvoiceActivity) {
            activity.finish();
        }
    }
}
