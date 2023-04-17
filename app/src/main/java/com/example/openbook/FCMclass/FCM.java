package com.example.openbook.FCMclass;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.openbook.Activity.PopUp;
import com.example.openbook.Chatting.ChattingUI;

import com.example.openbook.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class FCM extends FirebaseMessagingService {

    String TAG = "FCM";
    String get_id;


    @Override
    protected Intent getStartCommandIntent(Intent originalIntent) {

        get_id = originalIntent.getStringExtra("get_id");

        Task<String> token = FirebaseMessaging.getInstance().getToken();
        token.addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    saveToken(get_id, task.getResult());
                }
            }
        });

        return super.getStartCommandIntent(originalIntent);
    }


    public void saveToken(String id, String token) {
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference conditionRef = mRootRef.child(id);

        UserData userData = new UserData();
        userData.userID = id;
        userData.fcmToken = token;
        Log.d(TAG, "saveToken: " + token);

        conditionRef.setValue(userData);
    }


    private RemoteViews getCustomDesign(String title, String message) {
        RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);
        remoteViews.setTextViewText(R.id.noti_title, title);
        remoteViews.setTextViewText(R.id.noti_message, message);
        remoteViews.setImageViewResource(R.id.logo, R.drawable.heart);
        return remoteViews;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // 푸시메시지 수신시 할 작업을 작성

        Log.d(TAG, "From: " + message.getFrom());
       

        // Check if message contains a notification payload.
        if (message.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + message.getNotification().getBody());
            Log.d(TAG, "Message Notification Title : " + message.getNotification().getTitle());
            showNotification(message.getNotification().getTitle(),
                    message.getNotification().getBody());
        }else if(message.getData() != null){
            Log.d(TAG, "onMessageReceived title: " + message.getData().get("title"));
            Log.d(TAG, "onMessageReceived body: " + message.getData().get("body"));
            Log.d(TAG, "onMessageReceived clickTable: " +message.getData().get("clickTable"));
            Log.d(TAG, "onMessageReceived ticket :" + message.getData().get("ticket"));

            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void run() {
                    showData(message.getData().get("title"),
                            message.getData().get("body"),
                            message.getData().get("clickTable"),
                            message.getData().get("ticket"));
                }
            },0);


        }


    }




    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void showData(String title, String body, String clickTable, String ticket){
        //popupActivity를 만들어서 띄우자
        Intent intent = new Intent(this, PopUp.class);
        intent.putExtra("notificationTitle", title);
        intent.putExtra("notificationBody", body);
        intent.putExtra("notificationClickTable", clickTable);
        if(ticket == "yesTicket"){
            intent.putExtra("profileTicket", ticket);
        }



        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        try {
            pendingIntent.send();

        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }


    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotification(String title, String message) {
        //팝업 터치시 이동할 액티비티를 지정합니다.

        Intent intent = new Intent(this, ChattingUI.class);
        //알림 채널 아이디 : 본인 하고싶으신대로...
        String channel_id = "openBook_fcm";
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setSmallIcon(R.drawable.heart)
                .setAutoCancel(true)
                .setOnlyAlertOnce(false)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder = builder.setContent(getCustomDesign(title, message));
        } else {
            builder = builder.setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.heart);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //알람 채널이 필요한 안드로이드 버전을 위한 코드

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, "OpenBook_FCM", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        //알람 표시
        notificationManager.notify(0, builder.build());


    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "onNewToken: " + token);

        //이 기기의 토큰이 바뀌었을 때 할 작업을 작성,
        // onNewToken에서는 서버로 변경된 키값을 전달
    }


}