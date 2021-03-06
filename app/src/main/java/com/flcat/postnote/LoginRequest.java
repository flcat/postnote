package com.flcat.postnote;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest {

    final static private String URL = "http://flcat.vps.phps.kr/Login.php";
    private Map<String, String> parameters;



    public LoginRequest(String email, String password, Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("password", password);
    }

    @Override
    public Map<String, String> getParams(){
        return parameters;
    }
}
