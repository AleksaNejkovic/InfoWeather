package com.example.infoweatherapp.Class;

import java.util.Date;

public class Weather {
    protected Date dateTime;
    protected String iconPhrase;
    protected Double temperature;
    protected String icon;


    public Weather(Date dateTime, String iconPhrase, Double temperature,String icon) {
        this.dateTime = dateTime;
        this.iconPhrase = iconPhrase;
        this.temperature = temperature;
        this.icon = icon;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getIconPhrase() {
        return iconPhrase;
    }

    public void setIconPhrase(String iconPhrase) {
        this.iconPhrase = iconPhrase;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
