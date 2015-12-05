package edu.temple.stockportfolio.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kevin on 11/17/15.
 */
public class StockModel implements Parcelable{
    private String shortName;
    private String name;
    private Double currentPrice;
    private Double openingPrice;
    private Double changePercent;
    private Integer volume;

    public StockModel(String shortName,String name,Double currentPrice,
                      Double openingPrice,Double changePercent,Integer volume){
        this.currentPrice = currentPrice;
        this.name = name;
        this.shortName = shortName;
        this.openingPrice = openingPrice;
        this.changePercent = changePercent;
        this.volume = volume;
    }

    protected StockModel(Parcel in) {
        shortName = in.readString();
        name = in.readString();
        currentPrice = in.readDouble();
        openingPrice = in.readDouble();
        changePercent = in.readDouble();
        volume = in.readInt();
    }

    public static final Creator<StockModel> CREATOR = new Creator<StockModel>() {
        @Override
        public StockModel createFromParcel(Parcel in) {
            return new StockModel(in);
        }

        @Override
        public StockModel[] newArray(int size) {
            return new StockModel[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getOpeningPrice() {
        return openingPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setOpeningPrice(double openingPrice) {
        this.openingPrice = openingPrice;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public void setChangePercent(Double changePercent) {
        this.changePercent = changePercent;
    }

    public Double getChangePercent() {
        return changePercent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(shortName);
        dest.writeString(name);
        dest.writeDouble(currentPrice);
        dest.writeDouble(openingPrice);
        dest.writeDouble(changePercent);
        dest.writeInt(volume);
    }
}
