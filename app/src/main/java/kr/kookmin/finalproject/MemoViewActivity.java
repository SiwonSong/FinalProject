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

    //전 액티비티에서 받은 데이터베이스 id
    int id;
    //데이터를 담을 ArrayList
    ArrayList<String> arrayList;

    //데이터를 화면에 표시해주는 뷰
    TextView view_time;
    TextView view_location;
    TextView view_memo;
    TextView view_title;

    //이벤트 버튼
    Button bt_remove; //데이터 삭제
    Button bt_update_memo; //메모 수정
    Button bt_update_title; //제목 수정

    //데이터 베이스를 위한 변수
    SQLiteDatabase db;
    String dbName = "memoList.db";
    String tableName = "memoTable";
    int dbMode = Context.MODE_PRIVATE;
    DataBaseService database;

    //메모나 제목을 수정할 때 편하게 하기위해 메모와 제목을 따로저장
    String updateMemo;
    String updateTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_view);

        //뷰와 버튼 초기화
        view_title = (TextView) findViewById(R.id.view_title);
        view_time = (TextView) findViewById(R.id.view_time);
        view_location = (TextView) findViewById(R.id.view_location);
        view_memo = (TextView) findViewById(R.id.view_memo);
        bt_remove = (Button) findViewById(R.id.bt_remove);
        bt_update_memo = (Button) findViewById(R.id.bt_update_memo);
        bt_update_title = (Button) findViewById(R.id.bt_update_title);

        //이전에 인텐트로 보낸 데이터 id 받기
        Intent intent = getIntent();
        id = intent.getIntExtra("id", -1);

        arrayList = new ArrayList<String>();

        //데이터베이스 시작
        db = openOrCreateDatabase(dbName, dbMode, null);
        database = new DataBaseService();

        //받아온 id로 데이터 읽어오기
        readData();

        //제목과 메모는 따로 저장(수정시 편하게하기위해)
        updateTitle = arrayList.get(0);
        updateMemo = arrayList.get(3);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //데이터베이스 닫기
        db.close();
    }

    //버튼 이벤트 등록
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bt_remove: // 데이터 제거 id 값으로 데이터 제거
                database.removeData(db, tableName, id);
                Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                //이 엑티비티 종료
                finish();
                break;
            case R.id.bt_update_memo: //메모 수정
                updateMemo(); //수정을 위한 메쏘드
                break;
            case R.id.bt_update_title://제목 수정
                updateTitle();//수정을 위한 메쏘드
                break;
        }
    }

    //id 값으로 데이터를 읽어와 ArrayList에 저장
    public void readData() {

        //id로 데이터 불러와 ArrayList에 저장
        database.selectData(db, tableName, id, arrayList);

        //Toast.makeText(getApplicationContext(), y, Toast.LENGTH_SHORT).show();

        //각 뷰에 데이터들 삽입
        view_title.setText(arrayList.get(0));
        view_time.setText(arrayList.get(1));
        view_location.setText(arrayList.get(2));
        view_memo.setText(arrayList.get(3));

    }

    //메모 수정을 위한 메쏘드
    public void updateMemo() {

        //다이얼로그 활용
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("메모 수정");
        alert.setMessage("메모를 수정해 주세요.");

        //다이얼로그에 EditText 추가
        final EditText memo = new EditText(this);
        //EditText에 Memo데이터 삽입(수정을 쉽게)
        memo.setText(updateMemo);
        alert.setView(memo);

        //수정완료 버튼 이벤트
        alert.setPositiveButton("수정 완료", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //EditText에 있는 데이터를 updateMemo에 갱신
                updateMemo = memo.getText().toString();
                //데이터 베이스에 업데이트
                database.updateData(db, tableName, id, updateTitle, updateMemo);
                Toast.makeText(getApplicationContext(), "메모가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                //뷰에 바로 수정된 것이 보이도록 셋팅
                view_memo.setText(updateMemo);
            }
        });

        //취소버튼 이벤트
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //취소 버튼
            }
        });

        //다이얼로그 보이게
        alert.show();

    }

    //제목 수정을 위한 다이얼로그 이벤트
    public void updateTitle() {

        //다이얼로그 사용
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("제목 수정");
        alert.setMessage("제목를 수정해 주세요.");

        //EditText추가
        final EditText title = new EditText(this);
        //EditText에 updateTitle을 추가하여 수정이 쉽게만들기
        title.setText(updateTitle);
        alert.setView(title);

        //수정완료 버튼 이벤트
        alert.setPositiveButton("수정 완료", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //EditText에 있는 데이터 String으로 저장
                updateTitle = title.getText().toString();
                //데이터 베이스에 업데이트
                database.updateData(db, tableName, id, updateTitle, updateMemo);
                Toast.makeText(getApplicationContext(), "제목이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                //수정된 것이 바로 보이도록 뷰
                view_title.setText(updateTitle);
            }
        });

        //취소 버튼 이벤트
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //취소버튼
            }
        });

        //다이얼로그 보이기
        alert.show();
    }

}
