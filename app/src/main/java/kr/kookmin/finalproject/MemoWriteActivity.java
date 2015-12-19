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

    String address,time;

    Button bt_log_save;
    Button bt_log_view;
    TextView recent_address, recent_time;
    EditText memo;

    SQLiteDatabase db;
    String dbName = "memoList.db";
    String tableName = "memoListTable";
    int dbMode = Context.MODE_PRIVATE;
    DataBaseService database;

    boolean isRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_write);

        recent_address = (TextView) findViewById(R.id.recent_address);
        recent_time = (TextView) findViewById(R.id.recent_time);
        bt_log_save = (Button) findViewById(R.id.bt_log_save);
        bt_log_view =(Button) findViewById(R.id.bt_log_view);
        memo = (EditText) findViewById(R.id.memo);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");


        recent_address.setText(address);


        db = openOrCreateDatabase(dbName,dbMode,null);

        database = new DataBaseService();
        database.createTable(db, tableName);

    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bt_log_save:
                String insert_memo = memo.getText().toString();
                database.insertData(db, tableName, time, address, insert_memo);
                memo.setText("");
                Toast.makeText(getApplicationContext(), "메모가 저장되었습니다.", Toast.LENGTH_LONG).show();
                finish();
                break;
            case R.id.bt_log_view:
                Intent intent = new Intent(this, MemoListActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRun = true;
        Thread clockThread = new Thread(new Runnable() {
            public void run() {
                while (isRun) {
                    try {
                        handler.sendEmptyMessage(0);
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
        threadStop();
        db.close();
    }

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

    private void clockThread() {
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time = sdfNow.format(new Date(System.currentTimeMillis()));
        recent_time.setText(time);
    }

    public void threadStop() {
        try {
            isRun = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
