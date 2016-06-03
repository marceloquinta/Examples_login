package br.ufg.inf.es.dsdm.loginexample.services;

import android.content.Context;
import android.util.TypedValue;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import br.ufg.inf.es.dsdm.loginexample.R;
import br.ufg.inf.es.dsdm.loginexample.model.User;

public class WebTaskLogin extends WebTaskBase{

    private static String SERVICE_URL = "login";
    private String email;
    private String password;

    public WebTaskLogin(Context context, String email, String password){
        super(context, SERVICE_URL);
        this.email = email;
        this.password = password;
    }

    @Override
    public void handleResponse(String response) {
        User user = new User();
        try {
            JSONObject responseAsJSON = new JSONObject(response);
            String name = responseAsJSON.getString("name");
            user.setName(name);
            String username = responseAsJSON.getString("username");
            user.setUsername(username);
            String photoUrl = responseAsJSON.getString("photoURL");
            user.setPhotoURL(photoUrl);
            EventBus.getDefault().post(user);

        } catch (JSONException e) {
            if(!isSilent()){
                EventBus.getDefault().post(new Error(getContext().getString(R.string.label_error_invalid_response)));
            }
        }
    }

    private User readUser(JSONObject userAsJSON)  throws  JSONException{
        User user = new User();
        user.setName(userAsJSON.getString("name"));
        user.setUsername(userAsJSON.getString("username"));
        user.setPhotoURL(userAsJSON.getString("photoURL"));
        return user;
    }

    @Override
    public String getRequestBody(){
        Map<String,String> requestMap = new HashMap<>();
        requestMap.put("email", email);
        requestMap.put("password", password);

        JSONObject json = new JSONObject(requestMap);
        String jsonString = json.toString();

        return  jsonString;
    }

}
