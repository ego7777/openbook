package com.example.openbook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.openbook.Activity.PaymentSelect;
import com.example.openbook.Chatting.MessageDTO;
import com.example.openbook.Data.MyData;
import com.example.openbook.FCM.FCM;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.SuccessOrNot;
import com.google.gson.Gson;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TableDataManager {

    String TAG = "TableDataManagerTAG";
    FCM fcm;

    public void hideSystemUI(Activity activity){
        int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }

    public void setFcmService(int identifier){
        fcm = new FCM();
        fcm.getToken(identifier);
    }

    public void deleteProfile(String tableName, RetrofitService service, ResultCallback callback){
        Call<SuccessOrNot> call =service.deleteProfile(tableName);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SuccessOrNot> call, @NonNull Response<SuccessOrNot> response) {
                Log.d(TAG, "onResponse delete profile: " + response.body().getResult());
                if(response.isSuccessful()){
                    callback.onResultReceived(response.body().getResult());
                }else{
                    Log.d(TAG, "deleteProfile is not successful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessOrNot> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure deleteProfile: " + t.getMessage());
            }
        });

    }

    public void saveChatMessages(String sender,  String message, String tid,  RetrofitService service, ResultCallback callback){
        Call<SuccessOrNot> call = service.saveChatMessages(sender, message, tid);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SuccessOrNot> call, @NonNull Response<SuccessOrNot> response) {
                Log.d(TAG, "onResponse saveChatMessage: " + response.body().getResult());
                if(response.isSuccessful()){
                    callback.onResultReceived(response.body().getResult());
                }else{
                    Log.d(TAG, "saveChatMessages is not successful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessOrNot> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure saveChatMessages: " + t.getMessage());
            }
        });
    }

    public void setUseStop(Context context, MyData myData){
        Intent intent = new Intent(context, PaymentSelect.class);
        myData.init();
        intent.putExtra("myData", myData);
        context.startActivity(intent);
    }

    public void stopSocket(Context context, String id){
        Log.d(TAG, "stopSocket 호출");
        Gson gson = new Gson();
        Intent intent = new Intent("SendChattingData");
        MessageDTO messageDTO = new MessageDTO("", id, "disconnect", "");
        intent.putExtra("socketDisconnect", gson.toJson(messageDTO));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
