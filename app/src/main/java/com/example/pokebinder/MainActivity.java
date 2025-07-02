package com.example.pokebinder;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter;
import androidx.window.layout.DisplayFeature;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.WindowInfoTracker;
import androidx.window.layout.WindowLayoutInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SetAdapter adapter;
    private List<Set> setList = new ArrayList<>();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;

    private WindowInfoTrackerCallbackAdapter windowInfoTracker;
    private Consumer<WindowLayoutInfo> layoutInfoConsumer = new Consumer<WindowLayoutInfo>() {
        @Override
        public void accept(WindowLayoutInfo layoutInfo) {
            checkForUnfoldedState(layoutInfo);
        }
    };
    public static boolean unfolded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        windowInfoTracker = new WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(this));

    //RecyclerView
        recyclerView = findViewById(R.id.recycler);
        adapter = new SetAdapter(this, setList, this::openSet);
        recyclerView.setAdapter(adapter);
        gridLayoutManager = new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);
    //RecyclerView
        getSets(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Set icon to white if dark mode
        Configuration configuration = getResources().getConfiguration();
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if(currentNightMode == Configuration.UI_MODE_NIGHT_NO){
            menu.findItem(R.id.refresh).setIcon(R.drawable.refresh);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            int setSize = setList.size();
            setList.clear();
            adapter.notifyItemRangeRemoved(0, setSize);
            getSets(5);
            Toast.makeText(MainActivity.this, "Refreshed", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        windowInfoTracker.addWindowLayoutInfoListener(this, Runnable::run, layoutInfoConsumer);
        Log.d("FoldState", "WindowLayoutInfo listener added");
    }

    @Override
    protected void onStop() {
        super.onStop();
        windowInfoTracker.removeWindowLayoutInfoListener(layoutInfoConsumer);
        Log.d("FoldState", "WindowLayoutInfo listener removed");
    }

    private void checkForUnfoldedState(WindowLayoutInfo layoutInfo) {
        boolean currentlyUnfolded = false;
        for (DisplayFeature displayFeature : layoutInfo.getDisplayFeatures()) {
            if (displayFeature instanceof FoldingFeature) {
                FoldingFeature foldingFeature = (FoldingFeature) displayFeature;
                if (foldingFeature.getState() == FoldingFeature.State.FLAT) {
                    currentlyUnfolded = true;
                } else if (foldingFeature.getState() == FoldingFeature.State.HALF_OPENED) {
                    currentlyUnfolded = true;
                } else {
                    currentlyUnfolded = false;
                }
            }
        }

        if (currentlyUnfolded != unfolded) {
            unfolded = currentlyUnfolded;
            updateRecyclerViewLayout(); // Update the layout based on the new state
        }
        // runOnUiThread(() -> updateLayout(layoutInfo));
    }

    private void updateRecyclerViewLayout() {
        int spanCount = unfolded ? 6 : 3; // 6 columns when unfolded, 3 when folded

        if (gridLayoutManager.getSpanCount() != spanCount) {
            gridLayoutManager.setSpanCount(spanCount);
            //////////////////////////////////////////////////////////////////////////////////////////////////////////
        }
    }

    private void getSets(int i){
        String ip = getResources().getString(R.string.ip);
        String urlString = "http://" + ip + "/v2/sets";
        new Thread(() ->{
            try{
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String input;
                    StringBuilder response = new StringBuilder();
                    while ((input = in.readLine()) != null) {
                        response.append(input);
                    }
                    in.close();
                    mainHandler.post(() -> {
                        try {
                            updateSets(new JSONObject(response.toString()));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    mainHandler.post(() -> {
                        Toast.makeText(MainActivity.this, "Database is down", Toast.LENGTH_LONG).show();
                    });
                }
            } catch (IOException e){
                Log.e("Uh Oh Stinky Main", e + ": " + i);
                if(i < 5){
                    getSets( i + 1);
                }
            }
        }).start();
    }
    private void updateSets(JSONObject sets){
        try {
            JSONArray totSets = sets.getJSONArray("sets");
            setList.clear();
            List<Set> popSets = new ArrayList<>();
            List<Set> mcdSets = new ArrayList<>();
            setList.add(gigadex());
            for(int i = 0; i < totSets.length(); i++){
                JSONObject jSet = totSets.getJSONObject(i);
                JSONObject images = new JSONObject(jSet.getString("images"));
                String setImg = images.getString("logo");
                String name = jSet.getString("name");
                String id = jSet.getString("id");
                String releaseDate = jSet.getString("releaseDate");
                int total = jSet.getInt("total");
                Set set = new Set(setImg, name, id, releaseDate, total);

                //Add Mcdonalds and POP sets together at the end
                if(jSet.getString("id").startsWith("pop")){
                    popSets.add(set);
                } else if(jSet.getString("id").startsWith("mcd")){
                    mcdSets.add(set);
                } else {
                    setList.add(set);
                }
            }
            Collections.sort(setList);
            setList.addAll(popSets);
            setList.addAll(mcdSets);
            adapter.notifyItemRangeInserted(0, setList.size());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    private Set gigadex(){
        Set gigadex = new Set(null, "Gigadex", "gigadex", "0", 1025);
        return gigadex;
    }

    //Open the clicked set object
    private void openSet(int pos){
        Set clickedItem = setList.get(pos);
        Intent intent = new Intent(MainActivity.this, BinderActivity.class);
        intent.putExtra("setID", clickedItem.getSetID());
        startActivity(intent);
    }

}