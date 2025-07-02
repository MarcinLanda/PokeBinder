package com.example.pokebinder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static com.example.pokebinder.BinderActivity.cards;
import static com.example.pokebinder.BinderActivity.setID;

public class PageFragment extends Fragment {
    private static final String ARG_PAGE_INDEX = "page_index";

    private RecyclerView recyclerView;
    private BinderAdapter adapter;
    private List<Card> pageCards = new ArrayList<>();
    private int pageIndex = -1;
    public CardLoader cardLoader;

    public static PageFragment newInstance(int position) {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_INDEX, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CardLoader) {
            cardLoader = (CardLoader) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement CardLoader");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pageIndex = getArguments().getInt(ARG_PAGE_INDEX);
        } else {
            Log.e("PageFragment", "Error: Arguments missing for page fragment.");
            pageIndex = 0;
        }

        int startIndex = pageIndex * 9;
        int endIndex = Math.min(startIndex + 9, cards.size());
        pageCards.clear();
        if (startIndex >= 0 && startIndex < cards.size()) {
            pageCards.addAll(cards.subList(startIndex, endIndex));
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.binder_page, container, false);
        if(!setID.equals("gigadex")){
            adapter = new BinderAdapter((AppCompatActivity) getActivity(), pageCards, this::openCard, cardLoader::chooseCard);
        } else {
            adapter = new BinderAdapter((AppCompatActivity) getActivity(), pageCards, this::openCard, null);
        }
        recyclerView = view.findViewById(R.id.recycler);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 columns
        recyclerView.setAdapter(adapter);
        return view;
    }

    private void openCard(int position) {
        Context context = getContext();
        if (context == null || pageCards.get(position).getCardID() == null) return;
        Intent intent = new Intent(context, CardActivity.class);
        intent.putExtra("card", pageCards.get(position).toString());
        startActivity(intent);
    }
}