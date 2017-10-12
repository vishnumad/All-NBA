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
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
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
    private int contentViewType;
    private PublishSubject<Highlight> viewClickSubject = PublishSubject.create();
    private PublishSubject<Highlight> shareClickSubject = PublishSubject.create();

    public HighlightAdapter(Context context, List<Highlight> highlights, int contentViewType) {
        this.context = context;
        this.highlights = highlights;
        this.contentViewType = contentViewType;
    }

    @Override
    public HighlightHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view;
        switch (contentViewType) {
            case Constants.HIGHLIGHTS_VIEW_SMALL:
                view = inflater.inflate(R.layout.row_highlight_small, parent, false);
                break;
            case Constants.HIGHLIGHTS_VIEW_LARGE:
                view = inflater.inflate(R.layout.row_highlight, parent, false);
                break;
            default:
                throw new IllegalStateException("Highlight view type is neither small nor large");
        }

        return new HighlightHolder(view);
    }

    @Override
    public void onBindViewHolder(HighlightHolder holder, int position) {
        holder.bindData(context, contentViewType, highlights.get(position), viewClickSubject,
                shareClickSubject);
    }

    @Override
    public int getItemViewType(int position) {
        return contentViewType;
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

    public void setContentViewType(int viewType) {
        contentViewType = viewType;
        notifyDataSetChanged();
    }

    private void preFetchImages(List<Highlight> highlights) {
        for (Highlight highlight : highlights) {
            if (StringUtils.Companion.isEmpty(highlight.getHdThumbnail())) {
                if (!StringUtils.Companion.isEmpty(highlight.getThumbnail())) {
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

        void bindData(Context context,
                      int contentViewType,
                      final Highlight highlight,
                      final PublishSubject<Highlight> viewClickSubject,
                      final PublishSubject<Highlight> shareClickSubject) {
            tvTitle.setText(highlight.getTitle());

            boolean thumbnailAvailable = true;
            if (!StringUtils.Companion.isEmpty(highlight.getHdThumbnail())) {
                Picasso.with(context).load(highlight.getHdThumbnail()).into(ivThumbnail);
            } else if (!StringUtils.Companion.isEmpty(highlight.getThumbnail())) {
                Picasso.with(context).load(highlight.getThumbnail()).into(ivThumbnail);
            } else {
                ivThumbnail.setImageDrawable(null);
                thumbnailAvailable = false;
            }

            // Set bball background visibility only for list type view.
            ivThumbnailUnavailable.setVisibility(contentViewType == Constants
                    .HIGHLIGHTS_VIEW_SMALL && !thumbnailAvailable ? VISIBLE : GONE);

            container.setOnClickListener(v -> viewClickSubject.onNext(highlight));

            ibShare.setOnClickListener(v -> shareClickSubject.onNext(highlight));
        }
    }
}
