package com.flcat.postnote;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class UpdateRequest extends StringRequest {

        final static private String URL = "http://flcat.vps.phps.kr/update.php";
        private Map<String, String> parameters;



    public UpdateRequest(int num, String email,String title, String content, String mUri, String mThumbUri,String date,String slat,String slng, Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("num",num+"");
        parameters.put("email", email);
        parameters.put("title", title);
        parameters.put("content", content);
        parameters.put("mUri", mUri);
        parameters.put("mThumbUri", mThumbUri);
        parameters.put("date", date);
        parameters.put("slat", slat);
        parameters.put("slng", slng);
    }

    @Override
    public Map<String, String> getParams(){
        return parameters;
    }
}
