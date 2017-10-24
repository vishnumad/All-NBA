package com.gmail.jorgegilcavazos.ballislife.features.highlights;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;
import com.gmail.jorgegilcavazos.ballislife.features.model.HighlightViewType;
import com.gmail.jorgegilcavazos.ballislife.util.StringUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class HighlightAdapter extends RecyclerView.Adapter<HighlightAdapter.HighlightHolder> {
    private Context context;
    private List<Highlight> highlights;
    private HighlightViewType highlightViewType;
    private PublishSubject<Highlight> viewClickSubject = PublishSubject.create();
    private PublishSubject<Highlight> shareClickSubject = PublishSubject.create();

    public HighlightAdapter(
            Context context,
            List<Highlight> highlights,
            HighlightViewType highlightViewType) {
        this.context = context;
        this.highlights = highlights;
        this.highlightViewType = highlightViewType;
    }

    @Override
    public HighlightHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view;
        if (viewType == HighlightViewType.LARGE.getValue()) {
            view = inflater.inflate(R.layout.row_highlight, parent, false);
        } else {
            view = inflater.inflate(R.layout.row_highlight_small, parent, false);
        }
        return new HighlightHolder(view);
    }

    @Override
    public void onBindViewHolder(HighlightHolder holder, int position) {
        holder.bindData(context, highlightViewType, highlights.get(position), viewClickSubject,
                        shareClickSubject);
    }

    @Override
    public int getItemViewType(int position) {
        return highlightViewType.getValue();
    }

    @Override
    public int getItemCount() {
        return null != highlights ? highlights.size() : 0;
    }

    public void setData(List<Highlight> highlights) {
        this.highlights.clear();
        this.highlights.addAll(highlights);
        preFetchImages(highlights);
        notifyDataSetChanged();
    }

    public void addData(List<Highlight> highlights) {
        this.highlights.addAll(highlights);
        preFetchImages(highlights);
        notifyDataSetChanged();
    }

    public void setContentViewType(HighlightViewType viewType) {
        this.highlightViewType = viewType;
        notifyDataSetChanged();
    }

    private void preFetchImages(List<Highlight> highlights) {
        for (Highlight highlight : highlights) {
            if (StringUtils.Companion.isNullOrEmpty(highlight.getHdThumbnail())) {
                if (!StringUtils.Companion.isNullOrEmpty(highlight.getThumbnail())) {
                    Picasso.with(context).load(highlight.getThumbnail()).fetch();
                }
            } else {
                Picasso.with(context).load(highlight.getHdThumbnail()).fetch();
            }
        }
    }

    public Observable<Highlight> getViewClickObservable() {
        return viewClickSubject;
    }

    public Observable<Highlight> getShareClickObservable() {
        return shareClickSubject;
    }

    static class HighlightHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.container)
        View container;
        @BindView(R.id.image_thumbnail)
        ImageView ivThumbnail;
        @BindView(R.id.image_thumbnail_unavailable)
        ImageView ivThumbnailUnavailable;
        @BindView(R.id.text_title)
        TextView tvTitle;
        @BindView(R.id.button_share)
        ImageButton ibShare;

        public HighlightHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(Context context, HighlightViewType contentViewType,
                      final Highlight highlight,
                      final PublishSubject<Highlight> viewClickSubject,
                      final PublishSubject<Highlight> shareClickSubject) {
            tvTitle.setText(highlight.getTitle());

            boolean thumbnailAvailable = true;
            if (!StringUtils.Companion.isNullOrEmpty(highlight.getHdThumbnail())) {
                Picasso.with(context).load(highlight.getHdThumbnail()).into(ivThumbnail);
            } else if (!StringUtils.Companion.isNullOrEmpty(highlight.getThumbnail())) {
                Picasso.with(context).load(highlight.getThumbnail()).into(ivThumbnail);
            } else {
                Picasso.with(context).cancelRequest(ivThumbnail);
                ivThumbnail.setImageDrawable(null);
                thumbnailAvailable = false;
            }

            // Set bball background visibility only for list type view.
            ivThumbnailUnavailable.setVisibility(
                    contentViewType == HighlightViewType.SMALL && !thumbnailAvailable ? VISIBLE :
                            GONE);

            container.setOnClickListener(v -> viewClickSubject.onNext(highlight));

            ibShare.setOnClickListener(v -> shareClickSubject.onNext(highlight));
        }
    }
}
