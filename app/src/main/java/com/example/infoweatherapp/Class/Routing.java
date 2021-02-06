package com.example.infoweatherapp.Class;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

public class Routing {
    private static final String BASE_URL = "http://dataservice.accuweather.com/forecasts/v1/hourly/12hour/299758";

    public static String getRoute(String routePattern, Object ... params) {
        try {
            String relativeUrl = MessageFormat.format(routePattern, params);
            return new URL(new URL(BASE_URL), relativeUrl).toString();
        }
        catch (MalformedURLException e) {
            // TODO: log error somewhere
            // at the moment assuming all URLs to be correct, so swallow the exception
            return null;
        }
    }
}
