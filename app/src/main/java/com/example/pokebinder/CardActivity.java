package com.example.pokebinder;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class CardActivity  extends AppCompatActivity {
    Card card;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        try {
            JSONObject cardString = new JSONObject(getIntent().getStringExtra("card"));
            card = new Card(cardString.getString("cardImageSmall"), cardString.getString("cardImageLarge"), cardString.getString("cardName"), cardString.getString("cardNumber"), cardString.getString("cardID"), cardString.getString("rarity"), cardString.getString("price"), cardString.getBoolean("owned"), false, false);

            JSONObject prices = new JSONObject(card.getPrice()).getJSONObject("prices");
            String url = new JSONObject(card.getPrice()).getString("url");
            Log.e("CardActivity", prices.toString());
            String price;
            if(prices.has("normal")){
                price = "$" + prices.getJSONObject("normal").getString("market");
            } else if(prices.has("holofoil")){
                price = "$" + prices.getJSONObject("holofoil").getString("market");
            } else {
                price = "No value found";
            }

            ImageView imageView = findViewById(R.id.card);
            Glide.with(imageView.getContext()).load(card.getCardImageLarge()).apply(new RequestOptions().error(R.drawable.error_dark)).into(imageView);
            ((TextView)findViewById(R.id.name)).setText(card.getCardName());
            ((TextView)findViewById(R.id.number)).setText(card.getCardNumber() + " " + card.getRarity());
            ((TextView)findViewById(R.id.price)).setText(price);

            findViewById(R.id.tcgplayer).setOnClickListener(v -> Log.d("URL", "URL: " + url));
        } catch (JSONException e) {
            Log.e("CardActivity", card.getPrice());
            throw new RuntimeException(e);
        }
    }
}
