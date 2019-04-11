package com.flcat.postnote;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;


public class DeleteRequest extends StringRequest {

    final static private String URL = "http://flcat.vps.phps.kr/noteDel.php";
    private Map<String, String> parameters;

    public DeleteRequest(String num,String title, Response.Listener<String> listener){
        super(Request.Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("num",num);
        parameters.put("title",title);
    }



    @Override
    public Map<String, String> getParams(){
        return parameters;
    }
}
