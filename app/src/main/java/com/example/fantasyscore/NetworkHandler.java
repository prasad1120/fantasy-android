package com.example.fantasyscore;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.fantasyscore.Secrets.WEBAPP_URL;

public class NetworkHandler {

    static ArrayList<String> teams = new ArrayList<String>() {
        {
            add("Devilliersfc");
            add("Targaryenss");
            add("Elsido United");
            add("Charkop Falcons");
            add("Shubhamdevilliers");
            add("aditya117");
            add("DAIVA117ST");
        }
    };

    static void postData(Context context, final HashMap<String, String> params, Response.Listener<String> listener) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, WEBAPP_URL, listener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("MainActivity", error.getLocalizedMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        int socketTimeOut = 10000;

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(context);

        queue.add(stringRequest);
    }
}
