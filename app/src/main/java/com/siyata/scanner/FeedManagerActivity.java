package com.siyata.scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class FeedManagerActivity extends Activity {
    private static final String TAG = "FeedManager";
    private LinearLayout feedListLayout;
    private List<String> feeds = new ArrayList<>();
    private File feedsFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create main layout
        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);

        // Title
        TextView title = new TextView(this);
        title.setText("Feed Manager");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 40);
        mainLayout.addView(title);

        // Add feed button
        Button addButton = new Button(this);
        addButton.setText("+ Add New Feed");
        addButton.setOnClickListener(v -> showAddFeedDialog());
        mainLayout.addView(addButton);

        // Feed list
        feedListLayout = new LinearLayout(this);
        feedListLayout.setOrientation(LinearLayout.VERTICAL);
        feedListLayout.setPadding(0, 40, 0, 0);
        mainLayout.addView(feedListLayout);

        // Close button
        Button closeButton = new Button(this);
        closeButton.setText("Close");
        closeButton.setOnClickListener(v -> finish());
        mainLayout.addView(closeButton);

        scrollView.addView(mainLayout);
        setContentView(scrollView);

        // Load feeds
        feedsFile = new File(getExternalFilesDir(null), "scanner_feeds.txt");
        loadFeeds();
    }

    private void loadFeeds() {
        feeds.clear();
        feedListLayout.removeAllViews();

        try {
            if (!feedsFile.exists()) {
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(feedsFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    feeds.add(line);
                    addFeedToUI(line);
                }
            }
            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "Error loading feeds", e);
            Toast.makeText(this, "Error loading feeds: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void addFeedToUI(String feedLine) {
        String[] parts = feedLine.split("\\|");
        if (parts.length < 2) return;

        String name = parts[0].trim();

        LinearLayout feedItem = new LinearLayout(this);
        feedItem.setOrientation(LinearLayout.HORIZONTAL);
        feedItem.setPadding(0, 20, 0, 20);

        TextView feedText = new TextView(this);
        feedText.setText(name);
        feedText.setTextSize(18);
        feedText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        feedItem.addView(feedText);

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setOnClickListener(v -> deleteFeed(feedLine));
        feedItem.addView(deleteButton);

        feedListLayout.addView(feedItem);
    }

    private void showAddFeedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Feed");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView nameLabel = new TextView(this);
        nameLabel.setText("Feed Name:");
        layout.addView(nameLabel);

        EditText nameInput = new EditText(this);
        nameInput.setHint("e.g. Omaha Police");
        layout.addView(nameInput);

        TextView descLabel = new TextView(this);
        descLabel.setText("Description:");
        descLabel.setPadding(0, 20, 0, 0);
        layout.addView(descLabel);

        EditText descInput = new EditText(this);
        descInput.setHint("e.g. Police Scanner");
        layout.addView(descInput);

        TextView urlLabel = new TextView(this);
        urlLabel.setText("Stream URL:");
        urlLabel.setPadding(0, 20, 0, 0);
        layout.addView(urlLabel);

        EditText urlInput = new EditText(this);
        urlInput.setHint("http://stream.example.com/audio.mp3");
        layout.addView(urlInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String desc = descInput.getText().toString().trim();
            String url = urlInput.getText().toString().trim();

            if (name.isEmpty() || url.isEmpty()) {
                Toast.makeText(this, "Name and URL are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (desc.isEmpty()) {
                desc = "Custom Feed";
            }

            String feedLine = name + " | " + desc + " | " + url;
            addFeed(feedLine);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addFeed(String feedLine) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(feedsFile, true));
            writer.write(feedLine);
            writer.newLine();
            writer.close();

            Toast.makeText(this, "Feed added!", Toast.LENGTH_SHORT).show();
            loadFeeds();
        } catch (Exception e) {
            Log.e(TAG, "Error adding feed", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteFeed(String feedLine) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Feed")
            .setMessage("Are you sure you want to delete this feed?")
            .setPositiveButton("Delete", (dialog, which) -> {
                try {
                    feeds.remove(feedLine);

                    // Rewrite file without deleted feed
                    BufferedWriter writer = new BufferedWriter(new FileWriter(feedsFile));
                    for (String feed : feeds) {
                        writer.write(feed);
                        writer.newLine();
                    }
                    writer.close();

                    Toast.makeText(this, "Feed deleted", Toast.LENGTH_SHORT).show();
                    loadFeeds();
                } catch (Exception e) {
                    Log.e(TAG, "Error deleting feed", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
