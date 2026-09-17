package com.siyata.scanner;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private List<RadioFeed> feeds;
    private int selectedPosition = 0;
    private OnFeedClickListener listener;

    public interface OnFeedClickListener {
        void onFeedClick(RadioFeed feed, int position);
    }

    public FeedAdapter(List<RadioFeed> feeds, OnFeedClickListener listener) {
        this.feeds = feeds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        RadioFeed feed = feeds.get(position);
        holder.feedName.setText((position == selectedPosition ? "▶ " : "  ") + feed.getName());
        holder.feedDescription.setText(feed.getDescription());
        
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.parseColor("#2E7D32"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#212121"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFeedClick(feed, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return feeds.size();
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public RadioFeed getSelectedFeed() {
        if (selectedPosition >= 0 && selectedPosition < feeds.size()) {
            return feeds.get(selectedPosition);
        }
        return null;
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {
        TextView feedName;
        TextView feedDescription;

        FeedViewHolder(View itemView) {
            super(itemView);
            feedName = itemView.findViewById(R.id.feed_name);
            feedDescription = itemView.findViewById(R.id.feed_description);
        }
    }
}
