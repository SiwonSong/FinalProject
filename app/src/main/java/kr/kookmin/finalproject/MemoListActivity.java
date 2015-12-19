package kr.kookmin.finalproject;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class MemoListActivity extends AppCompatActivity {

    ListView memoView;
    Thread viewThread;
    String address;

    Button bt_memo_write;
    Button bt_total_remove;

    SQLiteDatabase db;
    String dbName = "memoList.db";
    String tableName = "memoListTable";
    int dbMode = Context.MODE_PRIVATE;
    DataBaseService database;

    ArrayAdapter<String> baseAdapter;
    ArrayList<String> arrayList;

    boolean isRun;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_list);

        memoView = (ListView) findViewById(R.id.list_view);
        bt_memo_write = (Button) findViewById(R.id.bt_memo_write);
        bt_total_remove = (Button) findViewById(R.id.bt_total_remove);

        arrayList = new ArrayList<String>();
        baseAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_view_inner, arrayList);
        memoView.setAdapter(baseAdapter);

        memoView.setOnItemClickListener(mItemClickListener);


        db = openOrCreateDatabase(dbName,dbMode,null);
        database = new DataBaseService();

        database.selectAll(db, tableName, arrayList);


        Intent intent = getIntent();
        address = intent.getStringExtra("address");



    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bt_memo_write:
                Intent intent = new Intent(this, MemoWriteActivity.class);
                intent.putExtra("address", address);
                startActivity(intent);
                break;
            case R.id.bt_total_remove:
                database.totalRemove(db, tableName);
                break;
        }
    }



    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long l_position) {

            String time = (String)parent.getAdapter().getItem(position);
            //Toast.makeText(getApplicationContext(), time, Toast.LENGTH_SHORT).show();

            int id = database.getId(db, tableName, time);
            //Toast.makeText(getApplicationContext(), "id = "+ id, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MemoListActivity.this, MemoViewActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        isRun = true;

        viewThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isRun) {
                        handler.sendEmptyMessage(0);
                        Thread.sleep(500);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        viewThread.start();

    }

    @Override
    public void onStop() {
        super.onStop();
        threadStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 0:
                    viewThread();
                    break;

            }

        }
    };

    public void viewThread() {
        arrayList.clear();
        database.selectAll(db, tableName, arrayList);
        baseAdapter.notifyDataSetChanged();
    }

    public void threadStop() {
        try {
            isRun = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
