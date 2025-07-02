package com.example.pokebinder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class BinderActivity extends AppCompatActivity implements BinderAdapter.OnItemClickListener, CardLoader{
    private BinderAdapter adapter;
    private MyPagerAdapter myPagerAdapter;
    private RecyclerView recyclerView;
    private RecyclerView gigaRecyclerView;
    private ViewPager2 viewPager;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public static List<Card> cardList = new ArrayList<>(); //List of all cards, doesnt change
    public static List<Card> cards = new ArrayList<>(); //List of all cards, changes with filters etc
    public List<Card> pageCards = new ArrayList<>();

    public static String setID;
    public static boolean gigadexSelect = false; //If the selection screen of the gigadex is being displayed
    public static boolean selecting = false;
    public boolean scrollTo = false; //Whether
    private String pokedexNum;
    private int pageNum = 0;
    private int pos = 0;
    private boolean vertical = false;
    private boolean horizontal = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binder);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //layoutInfoConsumer = this::checkForUnfoldedState;
        //checkForUnfoldedState(null);

        recyclerView = findViewById(R.id.recycler);
        gigaRecyclerView = findViewById(R.id.gigaRecycler);
        viewPager = findViewById(R.id.viewPager);
        vertical = true;

        Intent data = getIntent();
        String ip = getResources().getString(R.string.ip);

        if(data != null && data.hasExtra("setID")){
        //Get all of a specific set
            setSearchbar();
            setID = data.getStringExtra("setID");
            if(setID.equals("gigadex")){
                String urlString = "http://" + ip + "/v2/cards/" + setID;
                getCards(urlString, 0);
            } else {
                String urlString = "http://" + ip + "/v2/sets/" + setID + "/cards";
                getCards(urlString, 0);
            }
        }

        RecyclerView.ItemDecoration decoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = 25;
            }
        };
        gigaRecyclerView.addItemDecoration(decoration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_binder, menu);

        //Set icon to white if dark mode
        Configuration configuration = getResources().getConfiguration();
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if(currentNightMode == Configuration.UI_MODE_NIGHT_NO){
            menu.findItem(R.id.rotate).setIcon(R.drawable.rotate);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.rotate) {
            if (horizontal) {
                setVertical();
            } else if (vertical) {
                setHorizontal();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if(selecting){
            if(horizontal){
                setHorizontal();
            } else if (vertical) {
                setVertical();
            }
        } else {
            super.onBackPressed();
        }
    }

    private void setSearchbar(){
        //SearchBar & pageNum
        EditText search = findViewById(R.id.searchBar);
        EditText pageNum = findViewById(R.id.pageNum);
        search.setVisibility(View.INVISIBLE);
        findViewById(R.id.pageNums).setVisibility(View.INVISIBLE);
        cursorToEnd(search);
        cursorToEnd(pageNum);
        textChanged(search, 1);
        textChanged(pageNum, 2);
        setupUIForKeyboardDismissal(findViewById(R.id.binder));
        //SearchBarr & pageNum
    }

    /////@Override
    public void getCards(String urlString, int i){
        new Thread(() ->{
            try{
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    String input;
                    StringBuilder response = new StringBuilder();
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while((input = in.readLine()) != null){
                        response.append(input);
                    } in.close();
                    mainHandler.post(() -> {
                        try {
                            updateCards(new JSONObject(response.toString()));}
                        catch (JSONException e) { throw new RuntimeException(e); }
                    });

                }
            } catch (IOException e){
                //Log.e("Uh Oh Stinky Binder", "Count: " + i);
                if(i < 5){
                    getCards(urlString, i + 1);
                } else {
                    mainHandler.post(() -> {
                        Toast.makeText(BinderActivity.this, "Failed to connect to database", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    private void updateCards(JSONObject response){
        try {
            JSONArray totCards;
            ArrayList<Integer> indexArr = new ArrayList<Integer>();
            if(gigadexSelect){
                pageCards.clear(); cards.clear();
            } else {
                cardList.clear(); cards.clear();
            }

            if(response.has("cards")){
                totCards = new JSONArray(response.get("cards").toString());

                for(int i = 0; i < totCards.length(); i++) {
                    JSONObject card = new JSONObject(totCards.get(i).toString());
                    JSONObject image = new JSONObject(card.getString("images"));
                    String name = card.getString("name");
                    String number = card.getString("number");
                    String setTotal = card.getString("set_printedTotal");
                    String id = card.getString("id");
                    String rarity = card.getString("rarity");
                    String price = card.getString("tcgplayer");
                    int owned = card.getInt("have");

                    cards.add(new Card(image.getString("small"), image.getString("large"), name, (number + "/" + setTotal), id, rarity, price, owned == 1, false, gigadexSelect));
                    Collections.sort(cards);
                }
            } else {
                totCards = new JSONArray(response.get("gigadex").toString());
                for(int i = 0; i < totCards.length(); i++) {
                    JSONObject card = new JSONObject(totCards.get(i).toString());
                    String name = card.getString("name");

                    if (card.getString("images").equals("null")) {
                        cards.add(new Card(null, null, name, ((i+1) + "/" + (totCards.length())), null, null, null, false, true, false));
                    } else {
                        JSONObject image = new JSONObject(card.getString("images"));
                        String id = card.getString("id");
                        String rarity = card.getString("rarity");
                        String price = card.getString("tcgplayer");
                        int owned = card.getInt("have");        //// Maybe need to get pokedexNum from card? vvv
                        cards.add(new Card(image.getString("small"), image.getString("large"), name, ((i+1) + "/" + (totCards.length())), id, rarity, price, owned == 1, true, false));
                    }
                }
            }

            if(gigadexSelect){
                pageCards.addAll(cards);
            } else {
                cardList.addAll(cards);
            }

            if(gigadexSelect){
                setGigadex();
            } else if(horizontal){
                setHorizontal();
            } else if (vertical) {
                setVertical();
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        //Reset search bar
        EditText search = findViewById(R.id.searchBar);
        String text = search.getText().toString();
        search.setText("");
        search.setText(text);
        search.setSelection(search.getText().length());
    }

    public void setHorizontal(){
        vertical = false;
        horizontal = true;
        selecting = false;

        gigaRecyclerView.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        findViewById(R.id.searchBar).setVisibility(View.INVISIBLE);
        findViewById(R.id.pageNums).setVisibility(View.VISIBLE);

        myPagerAdapter = new MyPagerAdapter(this);
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setAdapter(myPagerAdapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                pageNum = position; ///
                ((EditText)findViewById(R.id.pageNum)).setText(String.valueOf(position + 1));
            }
        });
        ((TextView)findViewById(R.id.pageNumText)).setText("/ " + myPagerAdapter.getItemCount());
        myPagerAdapter.notifyItemRangeInserted(0, cards.size() - 1);
        viewPager.setCurrentItem(pageNum);
    }

    private void setVertical(){
        int span; if(MainActivity.unfolded){ span = 6; } else { span = 3; }
        vertical = true;
        horizontal = false;
        selecting = false;

        gigaRecyclerView.setVisibility(View.INVISIBLE);
        viewPager.setVisibility(View.INVISIBLE);
        findViewById(R.id.pageNums).setVisibility(View.INVISIBLE);
        findViewById(R.id.searchBar).setVisibility(View.VISIBLE);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, span, LinearLayoutManager.VERTICAL, false);
        if(setID.equals("gigadex")){
            adapter = new BinderAdapter(this, cards, this::openCard, this::openPkdx);
        } else {
            adapter = new BinderAdapter(this, cards, this::openCard, this::chooseCard);
        }
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(gridLayoutManager);

        adapter.notifyItemRangeInserted(0, cards.size() - 1);
        if(scrollTo){
            recyclerView.scrollToPosition(pos - 3);
            scrollTo = false;
        }
    }

    private void setGigadex(){
        int span; if(MainActivity.unfolded){ span = 6; } else { span = 3; }
        recyclerView.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        findViewById(R.id.pageNums).setVisibility(View.GONE);
        findViewById(R.id.searchBar).setVisibility(View.GONE);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, span, LinearLayoutManager.VERTICAL, false);
        adapter = new BinderAdapter(this, pageCards, this::chooseCard, null);
        gigaRecyclerView.setVisibility(View.VISIBLE);
        gigaRecyclerView.setAdapter(adapter);
        gigaRecyclerView.setLayoutManager(gridLayoutManager);

        adapter.notifyItemRangeInserted(0, cards.size() - 1);
        gigadexSelect = false;
        selecting = true;
    }

    private void openCard(int position) {
        //Card card = cards.get(position);
        if(!setID.equals("gigadex")){
            Intent intent = new Intent(this, CardActivity.class);
            intent.putExtra("card", cards.get(position).toString());
            startActivity(intent);
        } else {
            openPkdx(position);
        }
    }

    public void openPkdx(int position){
    //Get each of one specific pokemon
        gigadexSelect = true;
        scrollTo = true;
        pokedexNum = cards.get(position).getCardNumber().substring(0, cards.get(position).getCardNumber().indexOf("/"));
        pos = position;
        String urlString = "http://" + this.getResources().getString(R.string.ip) + "/v2/cards/" + pokedexNum;
        getCards(urlString, 0);
    }

    public void chooseCard(int position){
        AtomicReference<Boolean> complete = new AtomicReference<>(false);
        Thread thread = new Thread(() -> {
            try {
                URL url;
                String data;
                if (!setID.equals("gigadex")) {
                    url = new URL("http://" + this.getResources().getString(R.string.ip) + "/cards/have");
                    data = "{\"card_id\": \"" + cards.get(position).getCardID() + "\"}";
                } else {
                    url = new URL("http://" + this.getResources().getString(R.string.ip) + "/cards/gigadex");
                    data = "{\"card_id\": \"" + pageCards.get(position).getCardID() + "\","
                         + "\"pkdex_num\": \"" + pokedexNum + "\"}"; //setID
                }
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

                new DataOutputStream(connection.getOutputStream()).writeBytes(data);;

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    complete.set(true);
                } else {
                    Log.e("oopsie", "oopsie");
                }
            } catch (IOException e) {
                //Toast.makeText(BinderActivity.this, "Database is down", Toast.LENGTH_SHORT).show();
            }
        });
        try {
            thread.start();
            thread.join();;  // Block the current thread until the thread finishes
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//////////////////
        if(complete.get() && setID.equals("gigadex")){
            int num = Integer.parseInt(pokedexNum) - 1;
            Card card = cardList.get(num);
            cardList.set(num, pageCards.get(position).updateGigaDex(card.getCardName(), card.getCardNumber()));
            cards.set(pos, pageCards.get(position).updateGigaDex(card.getCardName(), card.getCardNumber()));
            adapter.notifyItemChanged(pos);
            if(horizontal){
                setHorizontal();
            } else if (vertical) {
                setVertical();
            }
        }
    }


    @Override
    public void onItemClick(int position) {
        openCard(position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.hasExtra("setID")) {
                    pageNum = (Integer.parseInt(data.getStringExtra("setID")) + 1) / 9;
                }
            }
        }
    }

    //When a specified EditText is clicked, the cursor appears at the end of the text
    private void cursorToEnd(EditText editText){
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //((EditText) v).setCursorVisible(false);
                    EditText editText = (EditText) v;
                    editText.post(new Runnable() {
                        @Override
                        public void run() {
                            editText.setSelection(editText.getText().length());
                        }
                    });
                }
            }
        });
    }

    private void textChanged(EditText text, int n){
        text.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override //When the search or pageNum text changes, update the displayed cards
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(n == 1 && vertical){
                    String searchText = s.toString();
                    cards.clear();
                    for (Card card : cardList) {
                        if (card.getCardName().toLowerCase().startsWith(searchText.toLowerCase())) {
                            cards.add(card);
                        }
                    }
                    if(adapter!= null){
                        adapter.notifyDataSetChanged();
                    }

                } else if (n == 2 && horizontal) {
                    String pageText = s.toString();
                    if (!pageText.isEmpty()) {
                        try {
                            int pageNumber = Integer.parseInt(pageText);
                            int totalPages = (int) Math.ceil((double) cards.size() / 9);
                            if (pageNumber >= 1 && pageNumber <= totalPages) {
                                EditText editText = findViewById(R.id.pageNum);
                                viewPager.setCurrentItem(pageNumber - 1);
                                editText.setSelection(editText.getText().length());
                            }
                        } catch (NumberFormatException e) {
                            Log.e("PageNum", "Invalid page number format", e);
                        }
                    }
                }

            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    public void setupUIForKeyboardDismissal(View backgroundView) {
        backgroundView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (findViewById(R.id.pageNum).isFocused() || findViewById(R.id.searchBar).isFocused()) {
                        findViewById(R.id.pageNum).clearFocus();
                        findViewById(R.id.searchBar).clearFocus();
                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    }
                }
                return false;
            }
        });
    }

    private static class MyPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {
        public MyPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }
        @NonNull
        @Override
        public Fragment createFragment(int position) { return PageFragment.newInstance(position); }
        @Override
        public int getItemCount() {
            int temp = 0;
            if(cards.size() % 9 > 0){
                temp = 1;
            }
            return cards.size() / 9 + temp;
        }


    }

}
