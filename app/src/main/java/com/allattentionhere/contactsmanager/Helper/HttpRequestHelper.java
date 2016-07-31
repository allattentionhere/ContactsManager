package com.allattentionhere.contactsmanager.Helper;

import android.content.Context;
import android.provider.Settings;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class HttpRequestHelper {

    public static String BASE_URL = "http://gojek-contacts-app.herokuapp.com";

    public void MakeJsonGetRequest(final String relative_uri, ArrayMap<String, String> getData, final Datacallback db, final Context c) {

        if (relative_uri == null) return;
        else {
            final String complete_url;
            if (getData != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(relative_uri).append("?").append("&");
                for (ArrayMap.Entry<String, String> entry : getData.entrySet()) {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                complete_url = BASE_URL + sb.toString();
            } else {
                complete_url = BASE_URL + relative_uri ;
            }
            Log.i("volley", complete_url);
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, complete_url, (JSONObject) null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("volley get string", response.toString());
                            Log.i("volley", complete_url);
                            db.onSuccess(response, relative_uri,"get");
                            // Check for info HTTP request
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO Auto-generated method stub
                            if (error.networkResponse != null && error.networkResponse.data != null) {
                                try {
                                    Log.i("volley", new String(error.networkResponse.data));
                                    db.onFailure(new JSONObject(new String(error.networkResponse.data)), relative_uri,"get");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (error instanceof TimeoutError) {
                                try {
                                    Log.i("volley", "TimeOutError");
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("fail", "TimeOutfail");
                                    db.onFailure(jsonObject, relative_uri,"get");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                try {
                                    Log.i("volley", "failed "+error.getMessage()+" | "+error.toString());
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("fail", "failed");
                                    db.onFailure(jsonObject, relative_uri,"get");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
            jsObjRequest.setShouldCache(false);
            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            jsObjRequest.setTag(db);
            MyApplication.Remotecalls.add(jsObjRequest);
        }
    }

    public void MakeJsonArrayRequest(final String relative_uri, ArrayMap<String, String> getData, final Datacallback db, Context c) {

        if (relative_uri == null) return;
        else {
            final String complete_url;
            if (getData != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(relative_uri).append("?");
                for (ArrayMap.Entry<String, String> entry : getData.entrySet()) {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                complete_url = BASE_URL + sb.toString();
            } else {
                complete_url = BASE_URL + relative_uri + "?" ;
            }
            Log.i("volley", complete_url);
            JsonArrayRequest jsArrayRequest = new JsonArrayRequest(Request.Method.GET, complete_url, (JSONArray) null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray jsonArray) {
                    if (jsonArray != null) Log.i("volley", jsonArray.toString());
                    try {
                        JSONObject j = new JSONObject();
                        j.put(relative_uri, jsonArray);
                        db.onSuccess(j, relative_uri,"getarray");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            Log.i("volley", new String(error.networkResponse.data));
                            db.onFailure(new JSONObject(new String(error.networkResponse.data)), relative_uri,"getarray");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (error instanceof TimeoutError) {
                        try {
                            Log.i("volley", "TimeOutError");
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("fail", "TimeOutfail");
                            db.onFailure(jsonObject, relative_uri,"getarray");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            jsArrayRequest.setShouldCache(false);
            jsArrayRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            jsArrayRequest.setTag(db);
            MyApplication.Remotecalls.add(jsArrayRequest);
        }
    }

    public void MakeJsonPostRequest(final String relative_uri, String postData, final Datacallback db, Context c) {

        if (relative_uri == null) return;
        else {
            try {
                JSONObject j = new JSONObject(postData);
                Log.i("volley", BASE_URL + relative_uri );
                if (postData != null) Log.i("volley", postData);
                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.POST, BASE_URL + relative_uri , j, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                db.onSuccess(response, relative_uri,"post");
                                if (response != null) Log.i("volley", response.toString());
                                Log.i("volley", BASE_URL + relative_uri );

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO Auto-generated method stub
                                Log.i("volley", "inside service slots error");
                                if (error.networkResponse != null && error.networkResponse.data != null) {
                                    try {
                                        Log.i("volley", new String(error.networkResponse.data));
                                        db.onFailure(new JSONObject(new String(error.networkResponse.data)), relative_uri,"post");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (error instanceof TimeoutError) {
                                    try {
                                        Log.i("volley", "TimeOutError");
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("fail", "fail");
                                        db.onFailure(jsonObject, relative_uri,"post");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                jsObjRequest.setShouldCache(false);
                jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                jsObjRequest.setTag(db);
                MyApplication.Remotecalls.add(jsObjRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void MakeJsonPutRequest(final String relative_uri, String putData, final Datacallback db, Context c) {

        if (relative_uri == null) return;
        else {
            try {
                JSONObject j = new JSONObject(putData);
                Log.i("volley", BASE_URL + relative_uri );
                if (putData != null) Log.i("volley", putData);
                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.PUT, BASE_URL + relative_uri , j, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                db.onSuccess(response, relative_uri,"put");
                                if (response != null) Log.i("volley", response.toString());
                                Log.i("volley", BASE_URL + relative_uri );

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO Auto-generated method stub
                                Log.i("volley", "inside service slots error");
                                if (error.networkResponse != null && error.networkResponse.data != null) {
                                    try {
                                        Log.i("volley", new String(error.networkResponse.data));
                                        db.onFailure(new JSONObject(new String(error.networkResponse.data)), relative_uri,"put");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (error instanceof TimeoutError) {
                                    try {
                                        Log.i("volley", "TimeOutError");
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("fail", "fail");
                                        db.onFailure(jsonObject, relative_uri,"put");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                jsObjRequest.setShouldCache(false);
                jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                jsObjRequest.setTag(db);
                MyApplication.Remotecalls.add(jsObjRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }





}
