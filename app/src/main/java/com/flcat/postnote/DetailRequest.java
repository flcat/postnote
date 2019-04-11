package com.flcat.postnote;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class DetailRequest extends StringRequest {

    final static private String URL = "http://flcat.vps.phps.kr/Detail.php";
    private Map<String, String> parameters;

    public DetailRequest(String title, String content,String mUri, Response.Listener<String> listener){
        super(Request.Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("title",title);
        parameters.put("content",content);
        parameters.put("mUri",mUri);
    }



    @Override
    public Map<String, String> getParams(){
        return parameters;
    }
}
