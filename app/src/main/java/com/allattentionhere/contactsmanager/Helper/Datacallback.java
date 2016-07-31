package com.allattentionhere.contactsmanager.Helper;

import org.json.JSONObject;


public interface Datacallback {
     void onSuccess(JSONObject success, String uri,String method);
     void onFailure(JSONObject failure, String uri,String method);
}
