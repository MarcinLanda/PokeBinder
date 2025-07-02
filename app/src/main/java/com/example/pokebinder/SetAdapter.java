package com.example.pokebinder;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class SetAdapter extends RecyclerView.Adapter<SetAdapter.SetViewHolder>{
    private Context context;
    private List<Set> setList;
    private OnItemClickListener clickListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public SetAdapter(Context context, List<Set> setList, OnItemClickListener clickListener) {
        this.context = context;
        this.setList = setList;
        this.clickListener = clickListener;
    }
    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        Set set = setList.get(position);

        //Get the text color based on the theme
        TypedValue typedValue = new TypedValue();
        Context context = holder.itemView.getContext();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);

        holder.setTitle.setText(set.getSetTitle());
        holder.setTitle.setTextColor(typedValue.data);

        ImageView imageView = holder.setLogo;
        if(set.getSetID().equals("gigadex")) {
            imageView.setImageResource(R.drawable.gigadex);
        } else {
            Glide.with(imageView.getContext()).load(set.getSetImage()).apply(new RequestOptions()
                    .placeholder(R.drawable.question_dark)
                    .error(R.drawable.error_dark)).into(imageView);
        }

        holder.itemView.setOnClickListener(v -> clickListener.onItemClick(position));
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_view, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public int getItemCount(){
        return setList.size();
    }

    static class SetViewHolder extends RecyclerView.ViewHolder {
        ImageView setLogo;
        TextView setTitle;
        public SetViewHolder(@NonNull View itemView) {
            super(itemView);
            setLogo = itemView.findViewById(R.id.setImage);
            setTitle = itemView.findViewById(R.id.setTitle);
        }
    }
}
