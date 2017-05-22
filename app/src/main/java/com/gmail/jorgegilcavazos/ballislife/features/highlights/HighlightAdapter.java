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
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class HighlightAdapter extends RecyclerView.Adapter<HighlightAdapter.HighlightHolder> {

    private Context context;
    private List<Highlight> highlights;
    private PublishSubject<Highlight> viewClickSubject = PublishSubject.create();
    private PublishSubject<Highlight> shareClickSubject = PublishSubject.create();

    public HighlightAdapter(Context context, List<Highlight> highlights) {
        this.context = context;
        this.highlights = highlights;
    }

    @Override
    public HighlightHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_highlight, parent, false);
        return new HighlightHolder(view);
    }

    @Override
    public void onBindViewHolder(HighlightHolder holder, int position) {
        holder.bindData(context, highlights.get(position), viewClickSubject, shareClickSubject);
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

    public void clearData() {
        highlights.clear();
        notifyDataSetChanged();
    }

    public void addData(List<Highlight> highlights) {
        this.highlights.addAll(highlights);
        notifyDataSetChanged();
    }

    public Observable<Highlight> getViewClickObservable() {
        return viewClickSubject;
    }

    public Observable<Highlight> getShareClickObservable() {
        return shareClickSubject;
    }

    static class HighlightHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.container) View container;
        @BindView(R.id.image_thumbnail) ImageView ivThumbnail;
        @BindView(R.id.text_title) TextView tvTitle;
        @BindView(R.id.button_share) ImageButton ibShare;

        public HighlightHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(Context context, final Highlight highlight,
                      final PublishSubject<Highlight> viewClickSubject,
                      final PublishSubject<Highlight> shareClickSubject) {
            tvTitle.setText(highlight.getTitle());

            if (highlight.getHdThumbnail() != null) {
                ivThumbnail.setVisibility(View.VISIBLE);
                Picasso.with(context)
                        .load(highlight.getHdThumbnail())
                        .into(ivThumbnail);
            } else {
                ivThumbnail.setVisibility(View.GONE);
            }

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewClickSubject.onNext(highlight);
                }
            });

            ibShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareClickSubject.onNext(highlight);
                }
            });
        }
    }
}
