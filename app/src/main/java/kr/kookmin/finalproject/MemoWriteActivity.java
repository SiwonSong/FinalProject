package kr.kookmin.finalproject;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MemoWriteActivity extends AppCompatActivity {

    //기록하기 위한 주소 및 시간
    String address,time;

    //버튼 및 뷰
    Button bt_log_save; //저장버튼
    TextView recent_address, recent_time;
    EditText memo;
    EditText title;

    //데이터 베이스 사용을 위한 변수
    SQLiteDatabase db;
    String dbName = "memoList.db";
    String tableName = "memoTable";
    int dbMode = Context.MODE_PRIVATE;
    DataBaseService database;

    //쓰레드 종료를 위한 변수
    boolean isRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_write);

        //각 뷰 들 초기화
        recent_address = (TextView) findViewById(R.id.recent_address);
        recent_time = (TextView) findViewById(R.id.recent_time);
        bt_log_save = (Button) findViewById(R.id.bt_log_save);
        memo = (EditText) findViewById(R.id.memo);
        title = (EditText) findViewById(R.id.title);

        //인텐트로 주소 값 받아오기
        Intent intent = getIntent();
        address = intent.getStringExtra("address");

        //받아온 주소값 텓스트뷰로 보여주기
        recent_address.setText(address);

        //데이터베이스 시작
        db = openOrCreateDatabase(dbName,dbMode,null);

        database = new DataBaseService();

    }

    //저장 버튼을 위한 이벤트 등록
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bt_log_save://저장버튼
                //제목과 주소를 String으로 저장
                String insert_memo = memo.getText().toString();
                String insert_title = title.getText().toString();
                //데이터 베이스에 title, time, adress, memo 저장
                database.insertData(db, tableName,insert_title, time, address, insert_memo);
                //EditText 초기화
                memo.setText("");
                title.setText("");
                Toast.makeText(getApplicationContext(), "메모가 저장되었습니다.", Toast.LENGTH_LONG).show();
                //엑티비티 끝내기
                finish();
                break;
        }
    }

    //실시간으로 현재시간을 보기 위한 쓰레드
    @Override
    protected void onResume() {
        super.onResume();

        //쓰레드 시작을 위해
        isRun = true;

        Thread clockThread = new Thread(new Runnable() {
            public void run() {
                while (isRun) {
                    try {
                        handler.sendEmptyMessage(0);//handler에 0전달
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        clockThread.start();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Thread 정지와 데이터 베이스 닫기
        threadStop();
        db.close();
    }

    //쓰레드 핸들러
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 0:
                    clockThread();
                    break;
            }

        }
    };

    //실시간 쓰레드
    private void clockThread() {
        //시간을 읽어와 텍스트 뷰에 보여준다.
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time = sdfNow.format(new Date(System.currentTimeMillis()));
        recent_time.setText(time);
    }
    //쓰레드 정지를 위한 method
    public void threadStop() {
        try {
            isRun = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
