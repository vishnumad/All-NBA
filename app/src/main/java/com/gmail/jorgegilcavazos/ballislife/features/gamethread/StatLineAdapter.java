package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.model.StatLine;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.NORMAL;

public class StatLineAdapter extends RecyclerView.Adapter<StatLineAdapter.StatLineViewHolder> {

    private List<StatLine> statLines;

    public StatLineAdapter(List<StatLine> statLines) {
        this.statLines = statLines;
    }

    @Override
    public StatLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.box_score_row, parent, false);
        return new StatLineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StatLineViewHolder holder, int position) {
        StatLine statLine = statLines.get(position);

        int fgp;
        if (statLine.getFga() == 0) {
            fgp = 0;
        } else {
            fgp = 100 * statLine.getFgm() / statLine.getFga();
        }

        holder.tvMin.setTypeface(null, NORMAL);
        holder.tvPts.setTypeface(null, NORMAL);
        holder.tvReb.setTypeface(null, NORMAL);
        holder.tvAst.setTypeface(null, NORMAL);
        holder.tvBlk.setTypeface(null, NORMAL);
        holder.tvStl.setTypeface(null, NORMAL);
        holder.tvTo.setTypeface(null, NORMAL);
        holder.tvPf.setTypeface(null, NORMAL);
        holder.tvFg.setTypeface(null, NORMAL);
        holder.tvFgp.setTypeface(null, NORMAL);

        holder.tvMin.setText(String.valueOf(statLine.getMin()));
        holder.tvPts.setText(String.valueOf(statLine.getPts()));
        holder.tvReb.setText(String.valueOf(statLine.getReb()));
        holder.tvAst.setText(String.valueOf(statLine.getAst()));
        holder.tvBlk.setText(String.valueOf(statLine.getBlk()));
        holder.tvStl.setText(String.valueOf(statLine.getStl()));
        holder.tvTo.setText(String.valueOf(statLine.getTov()));
        holder.tvPf.setText(String.valueOf(statLine.getPf()));
        holder.tvFg.setText(statLine.getFgm() + "/" + statLine.getFga());
        holder.tvFgp.setText(fgp + "%");
    }

    @Override
    public int getItemCount() {
        return null != statLines ? statLines.size() : 0;
    }

    public void setData(List<StatLine> data) {
        statLines.clear();
        statLines.addAll(data);
        notifyDataSetChanged();
    }

    class StatLineViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_min) TextView tvMin;
        @BindView(R.id.text_pts) TextView tvPts;
        @BindView(R.id.text_reb) TextView tvReb;
        @BindView(R.id.text_ast) TextView tvAst;
        @BindView(R.id.text_blk) TextView tvBlk;
        @BindView(R.id.text_stl) TextView tvStl;
        @BindView(R.id.text_to) TextView tvTo;
        @BindView(R.id.text_pf) TextView tvPf;
        @BindView(R.id.text_fg) TextView tvFg;
        @BindView(R.id.text_fgp) TextView tvFgp;

        public StatLineViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
