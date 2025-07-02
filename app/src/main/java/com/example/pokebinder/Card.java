package com.example.pokebinder;

import org.json.JSONException;
import org.json.JSONObject;

public class Card implements Comparable<Card>{
    private String cardImageSmall;
    private String cardImageLarge;
    private String cardName;
    private String cardNumber;
    private String cardID;
    private String rarity;
    private String price;
    private Boolean owned;
    private Boolean gigaDex;
    private Boolean gigaDexSelect;

    public Card(String cardImageSmall, String cardImageLarge, String cardName, String cardNumber, String cardID, String rarity, String price, Boolean owned, Boolean gigaDex, Boolean gigaDexSelect) {
        this.cardImageSmall = cardImageSmall;
        this.cardImageLarge = cardImageLarge;
        this.cardName = cardName;
        this.cardNumber = cardNumber;
        this.cardID = cardID;
        this.rarity = rarity;
        this.price = price;
        this.owned = owned;
        this.gigaDex = gigaDex;
        this.gigaDexSelect = gigaDexSelect;
    }

    //Getters
    public String getCardImageSmall() {return cardImageSmall;}
    public String getCardImageLarge() {return cardImageLarge;}
    public String getCardName() {return cardName;}
    public String getCardNumber() {return cardNumber;}
    public String getCardID() {return cardID;}
    public String getRarity() {return rarity;}
    public String getPrice() {return price;}
    public Boolean getOwned() {return owned;}
    public Boolean getGigaDex() {return gigaDex;} //One of each pokemon
    public Boolean getGigaDexSelect() {return gigaDexSelect;} //Card that is selected when choosing which of a certain pokemon user has

    //Setters
    public void setOwned(Boolean owned) {this.owned = owned;}
    public Card getSetOwned(Boolean owned) {this.owned = owned; return this;}
    //Turn a gigaDexSelect to a Gigadex
    public Card updateGigaDex(String name, String number) {
        this.gigaDex = true;
        this.gigaDexSelect = false;
        this.cardName = name;
        this.cardNumber = number;
        return this;
    }

    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("cardImageSmall", cardImageSmall);
            json.put("cardImageLarge", cardImageLarge);
            json.put("cardName", cardName);
            json.put("cardNumber", cardNumber);
            json.put("cardID", cardID);
            json.put("rarity", rarity);
            json.put("price", price);
            json.put("owned", owned);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return json.toString();
    }
    @Override
    public int compareTo(Card o) {
        return Integer.compare(removeLetters(this.cardNumber.substring(0, this.cardNumber.indexOf('/'))), removeLetters(o.cardNumber.substring(0, o.cardNumber.indexOf('/'))));
    }

    public int removeLetters(String s){
        return Integer.parseInt(s.replaceAll("[^0-9]", ""));
    }
}
