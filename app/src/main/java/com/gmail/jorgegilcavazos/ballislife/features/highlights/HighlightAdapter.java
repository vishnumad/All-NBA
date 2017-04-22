package com.gmail.jorgegilcavazos.ballislife.features.highlights;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HighlightAdapter extends RecyclerView.Adapter<HighlightAdapter.HighlightHolder> {

    private Context context;
    private List<Highlight> highlights;

    public HighlightAdapter(Context context, List<Highlight> highlights) {
        this.context = context;
        this.highlights = highlights;
    }

    @Override
    public HighlightHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        return new HighlightHolder(inflater.inflate(R.layout.row_highlight, parent, false));
    }

    @Override
    public void onBindViewHolder(HighlightHolder holder, int position) {
        holder.bindData(context, highlights.get(position));
    }

    @Override
    public int getItemCount() {
        return null != highlights ? highlights.size() : 0;
    }

    public void setData(List<Highlight> highlights) {
        this.highlights.clear();
        this.highlights.addAll(highlights);
        notifyDataSetChanged();
    }

    public void addData(List<Highlight> highlights) {
        this.highlights.addAll(highlights);
        notifyDataSetChanged();
    }

    static class HighlightHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_thumbnail) ImageView ivThumbnail;
        @BindView(R.id.text_title) TextView tvTitle;

        public HighlightHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(Context context, Highlight highlight) {
            tvTitle.setText(highlight.getTitle());

            Picasso.with(context)
                    .load(highlight.getHdThumbnail())
                    .into(ivThumbnail);
        }
    }
}
