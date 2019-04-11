package com.flcat.postnote;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;


import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {

    final static private String URL = "http://flcat.vps.phps.kr/Register.php";
    private Map<String, String> parameters;



    public RegisterRequest(String email, String password, String name, String nick, Response.Listener<String> listener){
        //해당 URL에 파라메터들을 post 방식으로 전송
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("name", name);
        parameters.put("nick",nick);
    }

    @Override
    public Map<String, String> getParams(){
        return parameters;
    }
}
