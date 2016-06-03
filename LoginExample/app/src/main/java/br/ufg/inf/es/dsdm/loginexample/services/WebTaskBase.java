package br.ufg.inf.es.dsdm.loginexample.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import br.ufg.inf.es.dsdm.loginexample.R;

public abstract class WebTaskBase extends AsyncTask<Void, Void, Void> {

    public static final int RESPONSE_OK = 200;
    public static final int RESPONSE_INVALID_REQUEST = 403;
    private static int TIMEOUT = 15;
    private static String BASE_URL = "http://private-c8b4eb-infnews.apiary-mock.com/";

    private String serviceURL;
    private Context context;
    private Error error;
    private String responseString;
    private String image;
    private int responseCode;
    private boolean silent;


    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public WebTaskBase(Context context, String serviceURL) {
        this.serviceURL = serviceURL;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if(!isOnline(context)){
            error = new Error(context.getString(R.string.error_connection));
            responseString = null;
            return null;
        }

        doRegularCall();

        return null;
    }

    private void doRegularCall() {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, getRequestBody());

        client.setConnectTimeout(TIMEOUT, TimeUnit.SECONDS);
        client.setReadTimeout(TIMEOUT, TimeUnit.SECONDS);

        Request request = new Request.Builder()
                .url(BASE_URL + serviceURL)
                .post(body)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            responseCode = response.code();
            responseString =  response.body().string();
        } catch (IOException e) {
            error = new Error(context.getString(R.string.error_connection));
        }
    }

    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(error!= null && !silent){
            EventBus.getDefault().post(error);
        }else{

            switch (getResponseCode()){
                case RESPONSE_OK:
                    try {
                        JSONObject responseJSON = new JSONObject(responseString);
                        String errorMessage = responseJSON.getString("erro");
                        if(!silent){
                            EventBus.getDefault().post(new Error(errorMessage));
                        }
                    } catch (JSONException e) {
                        handleResponse(responseString);
                    } catch (NullPointerException e) {
                        handleResponse("");
                    }
                    break;

                case RESPONSE_INVALID_REQUEST:
                    EventBus.getDefault().post(new Error(context.getString(R.string.error_request)));
                    break;
            }
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public abstract void handleResponse(String response);

    public abstract String getRequestBody();

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Context getContext() {
        return context;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }
}
