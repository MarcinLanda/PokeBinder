package com.example.pokebinder;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class BinderAdapter extends RecyclerView.Adapter<BinderAdapter.BinderViewHolder> {
    private AppCompatActivity activity;
    private List<Card> cards;
    private OnItemClickListener clickListener;
    private OnOwnedChangedListener ownedChangedListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public interface OnOwnedChangedListener{
        void onOwnedChanged(int position);
    }
    public BinderAdapter(AppCompatActivity activity, List<Card> cards, OnItemClickListener clickListener, OnOwnedChangedListener ownedChangedListener) {
        this.activity = activity;
        this.cards = cards;
        this.clickListener = clickListener;
        this.ownedChangedListener = ownedChangedListener;
    }

    @Override
    public void onBindViewHolder(@NonNull BinderAdapter.BinderViewHolder holder, int position) {
        Card card = cards.get(position);

        //Get the text color based on the theme
        TypedValue typedValue = new TypedValue();
        Context context = holder.itemView.getContext();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);

        holder.cardText.setTextColor(typedValue.data);
        holder.cardText.setText(card.getCardName());

        ImageView imageView = holder.cardImage;
        Glide.with(imageView.getContext()).load(card.getCardImageSmall()).apply(new RequestOptions()
                .placeholder(R.drawable.card_back)
                .error(R.drawable.card_back)).into(imageView);
        holder.itemView.setOnClickListener(v -> clickListener.onItemClick(position));
        if(card.getGigaDexSelect()){
            holder.haveCard.setVisibility(View.INVISIBLE);
            holder.whiteCircle.setVisibility(View.INVISIBLE);
            holder.cardText.setVisibility(View.GONE);
        } else if (card.getGigaDex()){
            holder.whiteCircle.setVisibility(View.INVISIBLE);
            holder.haveCard.setVisibility(View.INVISIBLE);
        } else if(card.getOwned()){
            holder.haveCard.setImageResource(R.drawable.card_true);
        }

        holder.haveCard.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition(); // Use getAdapterPosition()
            if (adapterPos != RecyclerView.NO_POSITION) {
                Card clickedCard = cards.get(adapterPos);
                if(!card.getGigaDex()){
                    clickedCard.setOwned(!card.getOwned());
                    if(clickedCard.getOwned()){
                        holder.haveCard.setImageResource(R.drawable.card_true);
                    } else {
                        holder.haveCard.setImageResource(R.drawable.card_false);
                    }
                }
                ownedChangedListener.onOwnedChanged(position);
            }
        });
    }

    @NonNull
    @Override
    public BinderAdapter.BinderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        return new BinderAdapter.BinderViewHolder(view);
    }

    @Override
    public int getItemCount() { return cards.size(); }

    static class BinderViewHolder extends RecyclerView.ViewHolder {
        ImageView cardImage;
        ImageView haveCard;
        ImageView whiteCircle;
        TextView cardText;

        public BinderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.cardImage);
            haveCard = itemView.findViewById(R.id.haveCard);
            whiteCircle = itemView.findViewById(R.id.whiteCircle);
            cardText = itemView.findViewById(R.id.cardText);
        }
    }
}