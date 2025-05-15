package com.example.attendancechecker;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class RequestManager {
    String url;
    String method;
    RequestQueue queue ;
    public interface MyInterface{

    }
    public RequestManager(String url, Context context) {
        this.url = url;
        this.queue = Volley.newRequestQueue(context);
    }
    public void makeArrayRequest(){
        JsonArrayRequest
                jsonArrayRequest
                = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        response.toString();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        error.getCause();
                    }
                });
        queue.add(jsonArrayRequest);

    }
}
