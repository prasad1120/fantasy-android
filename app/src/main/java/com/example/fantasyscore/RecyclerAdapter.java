package com.example.fantasyscore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private Match[] matches;
    private int numberOfMatchesScoresUploaded;
    private OnItemClickListener onItemClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View view;
        OnItemClickListener onItemClickListener;

        ViewHolder(View view, OnItemClickListener onItemClickListener) {
            super(view);
            this.view = view;
            this.onItemClickListener = onItemClickListener;

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(getAdapterPosition());
        }
    }

    RecyclerAdapter(Match[] matches, int numberOfMatchesScoresUploaded, OnItemClickListener onItemClickListener) {
        this.matches = matches;
        this.numberOfMatchesScoresUploaded = numberOfMatchesScoresUploaded;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
        return new ViewHolder(row, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM  HH:mm");

        ConstraintLayout layout = holder.view.findViewById(R.id.cell_bg);
        TextView matchNumberTv = layout.findViewById(R.id.matchNumberTv);
        ImageView tickImg = layout.findViewById(R.id.tick_img);

        if (position + 1 > numberOfMatchesScoresUploaded) {
            tickImg.setVisibility(View.GONE);
            layout.setBackgroundColor(holder.view.getContext().getResources().getColor(R.color.backgroundColor));
        } else {
            tickImg.setVisibility(View.VISIBLE);
            layout.setBackgroundColor(holder.view.getContext().getResources().getColor(R.color.matchDoneColor));
        }

        if (isNumeric(matches[position].id)) {
            matchNumberTv.setText("#" + matches[position].id);
        } else {
            matchNumberTv.setText(matches[position].id);
            matchNumberTv.setTextColor(ContextCompat.getColor(holder.view.getContext(), R.color.secondaryTextColor));
        }

        ((TextView) layout.findViewById(R.id.timeTv)).setText(format.format(matches[position].time));

        TextView team1Tv = layout.findViewById(R.id.team1);
        team1Tv.setText(matches[position].t1);
        TextView team2Tv = layout.findViewById(R.id.team2);
        team2Tv.setText(matches[position].t2);

        ImageView t1Logo = layout.findViewById(R.id.t1Logo);
        t1Logo.setImageResource(getLogoResourceId(matches[position].t1));

        ImageView t2Logo = layout.findViewById(R.id.t2Logo);
        t2Logo.setImageResource(getLogoResourceId(matches[position].t2));
    }

    @Override
    public int getItemCount() {
        return matches.length;
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public static int getLogoResourceId(String teamName) {

        switch (teamName) {
            case "MI": return R.drawable.mi;
            case "RCB": return R.drawable.rcb;
            case "PK": return R.drawable.pk;
            case "CSK": return R.drawable.csk;
            case "KKR": return R.drawable.kkr;
            case "DC": return R.drawable.dc;
            case "SRH": return R.drawable.srh;
            case "RR": return R.drawable.rr;
            default: return R.drawable.question_mark;
        }
    }
}