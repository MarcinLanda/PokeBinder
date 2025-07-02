package com.example.pokebinder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Set implements Comparable<Set>{
    private String setImage;
    private String setTitle;
    private String setID;
    private String releaseDate;
    private int cardTotal;


    public Set(String setImage, String setTitle, String setID, String releaseDate, int cardTotal) {
        this.setImage = setImage;
        this.setTitle = setTitle;
        this.setID = setID;
        this.releaseDate = releaseDate;
        this.cardTotal = cardTotal;
    }

    //Getters
    public String getSetImage() {return setImage;}
    public String getSetTitle() {return setTitle;}
    public String getSetID() {return setID;}
    public String getReleaseDate() {return releaseDate;}
    public int getCardTotal() {return cardTotal;}

    @Override
    public String toString() {
        return "Set{" +
                "setImage='" + setImage + '\'' +
                ", setTitle='" + setTitle + '\'' +
                ", setID='" + setID + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", cardTotal=" + cardTotal +
                '}';
    }

    @Override
    public int compareTo(Set o) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        try {
            LocalDate tDate = LocalDate.parse(this.getReleaseDate(), formatter);
            LocalDate oDate = LocalDate.parse(o.getReleaseDate(), formatter);
            return oDate.compareTo(tDate);

        } catch (Exception e) {
            System.err.println("Error parsing date: " + e.getMessage());
            return 0;
        }
    }
}
