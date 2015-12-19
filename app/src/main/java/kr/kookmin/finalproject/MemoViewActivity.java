package kr.kookmin.finalproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MemoViewActivity extends AppCompatActivity {

    int id;
    ArrayList<String> arrayList;

    TextView view_time;
    TextView view_location;
    TextView view_memo;

    Button bt_remove;
    Button bt_update;

    SQLiteDatabase db;
    String dbName = "memoList.db";
    String tableName = "memoListTable";
    int dbMode = Context.MODE_PRIVATE;
    DataBaseService database;

    String updateMemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_view);

        view_time = (TextView) findViewById(R.id.view_time);
        view_location = (TextView) findViewById(R.id.view_location);
        view_memo = (TextView) findViewById(R.id.view_memo);
        bt_remove = (Button) findViewById(R.id.bt_remove);
        bt_update = (Button) findViewById(R.id.bt_update);

        Intent intent = getIntent();
        id = intent.getIntExtra("id", -1);

        arrayList = new ArrayList<String>();

        db = openOrCreateDatabase(dbName, dbMode, null);
        database = new DataBaseService();

        readData();

        updateMemo = arrayList.get(2);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bt_remove:
                database.removeData(db, tableName, id);
                Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case R.id.bt_update:
                updateDialog();
                break;
        }
    }

    public void readData() {

        database.selectData(db, tableName, id, arrayList);

        //Toast.makeText(getApplicationContext(), y, Toast.LENGTH_SHORT).show();

        view_time.setText(arrayList.get(0));
        view_location.setText(arrayList.get(1));
        view_memo.setText(arrayList.get(2));

    }

    public void updateDialog() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("메모 수정");
        alert.setMessage("메모를 수정해 주세요.");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setText(updateMemo);
        alert.setView(input);

        alert.setPositiveButton("수정 완료", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                updateMemo = input.getText().toString();
                database.updateData(db, tableName, id, updateMemo);
                Toast.makeText(getApplicationContext(), "메모가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                view_memo.setText(updateMemo);
            }
        });


        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

        alert.show();

    }
}
