package com.example.mobile_project;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Random;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

public class QuoteManager {

    private static final String QUOTES_URL = "https://zenquotes.io/api/random";

    public static void fetchRandomQuoteForSplash(Context context, TextView quoteTextView) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, "https://zenquotes.io/api/random", null,
                response -> {
                    try {
                        JSONObject quoteObject = response.getJSONObject(0);
                        String quote = quoteObject.getString("q");
                        String author = quoteObject.getString("a");

                        quoteTextView.setText("\"" + quote + "\"\n\n- " + author);
                    } catch (Exception e) {
                        e.printStackTrace();
                        quoteTextView.setText("Stay strong and keep going!");
                    }
                },
                error -> {
                    error.printStackTrace();
                    quoteTextView.setText("No motivation today, check your connection!");
                }
        );

        queue.add(jsonArrayRequest);
    }
}
