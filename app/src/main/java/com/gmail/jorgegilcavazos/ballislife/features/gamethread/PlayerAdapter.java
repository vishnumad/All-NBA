package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    private List<String> players;

    public PlayerAdapter(List<String> players) {
        this.players = players;
    }

    @Override
    public PlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_name_item, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlayerViewHolder holder, int position) {
        holder.tvPlayer.setText(players.get(position));
    }

    @Override
    public int getItemCount() {
        return null != players ? players.size() : 0;
    }

    public void setData(List<String> data) {
        players.clear();
        players.addAll(data);
        notifyDataSetChanged();
    }

    class PlayerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_player) TextView tvPlayer;

        public PlayerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
