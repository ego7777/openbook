package com.example.openbook.Chatting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.RequiresApi;


import com.example.openbook.DrawableMethod;
import com.example.openbook.R;
import com.example.openbook.View.TableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;



@RequiresApi(api = Build.VERSION_CODES.O)
public class ClientSocket extends Thread implements Serializable{

    private final String TAG = "ClientSocket";
    SocketAddress socketAddress;

    final int connection_timeout = 99999;
    public BufferedReader networkReader = null;
    public BufferedWriter networkWrite = null;

    String get_id;
    public static Socket socket;
    boolean loop;
    String line;

    Context context;

    public ArrayList<TableList> getTableList() {
        return tableList;
    }

    ArrayList<TableList> tableList;







    public ClientSocket(String ip, int port, String get_id, Context context, ArrayList<TableList> tableList) {
        //서버 ip 주소와 사용할 포트번호로 소켓 어드레스 객체 생성
        this.get_id = get_id;
        this.context = context;
        this.tableList = tableList;
        socketAddress = new InetSocketAddress(ip, port);
    }




    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        super.run();
        //
        try {
            /**
             * 1. 클라이언트 소켓생성
             */
            socket = new Socket();
            socket.setSoTimeout(connection_timeout);
            socket.setSoLinger(true, connection_timeout);


            /** 2. 소켓 연결
            /블록모드로 작동하는 connect() 메소드에서 반환되면 서버와 정상적으로 연결된 것
            */
            socket.connect(socketAddress, connection_timeout);
            Log.d(TAG, "소켓 연결 : " + socket.isConnected());


            //3. 데이터 입출력 메소드 설정
            OutputStreamWriter o = new OutputStreamWriter(socket.getOutputStream());
            //outputStream: 출력 스트림


            networkWrite = new BufferedWriter(o);
            Log.d(TAG, "run: " + networkWrite);
            //bufferedReader : 버퍼를 사용한 출력

            InputStreamReader i = new InputStreamReader(socket.getInputStream());
            //inputStream : 입력 스트림
            networkReader = new BufferedReader(i);
            //bufferedReader : 버퍼를 사용한 입력

            /**
             * 소켓이 생성이 되면 테이블 Id 값을 넘겨준다.
             */
            networkWrite.write(get_id+"_table");
            networkWrite.newLine();
            networkWrite.flush();
            Log.d(TAG, "id flush");

            loop = true;

            String myTable = get_id.replace("table", "");
            Log.d(TAG, "myTable :" + myTable);
            
            while(loop) {
                try {
//                    Log.d(TAG, "while start");
                    String line = networkReader.readLine();

                    Log.d(TAG, "new table :"  + line);

                    if (!line.equals(myTable)) {
                        updateTable(line);
                        Log.d(TAG, "update Table");

                    }else{
                        Log.d(TAG, "같음");

                    }

                    if(line == null){
                        break;
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    Log.d(TAG, "e :" + e);
                }
            }



        } catch (Exception e) {
//            loop = false;
            Log.d(TAG, "소켓을 생성하지 못했습니다.");
            Log.d(TAG, "Exception " + e);

        }

    }

    public void updateTable(String line){

        DrawableMethod drawableToBitmap = new DrawableMethod();

        Log.d(TAG, "tableList size :" + tableList.size());

        byte[] orderTableImage = drawableToBitmap.makeBitmap(context.getDrawable(R.drawable.table_boder_order));
        Log.d(TAG, "updateTable image :" + orderTableImage);

        tableList.get(Integer.parseInt(line)-1).setBytes(orderTableImage);
        Log.d(TAG, "updateTable: 이미지 byte로 넣기");
        tableList.get(Integer.parseInt(line)-1).setViewType(2);
        Log.d(TAG, "updateTable: viewType 2로 바꾸기");


    }

    public void quit(){
        loop =false;
        try{
            if(socket != null){
                socket.close();
                socket = null;
                Log.d(TAG, "quit: ");
            }


        }catch (IOException e){
            Log.d(TAG, "quit: e" + e);
        }
    }



}




