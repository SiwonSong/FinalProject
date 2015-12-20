package kr.kookmin.finalproject;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MemoListActivity extends AppCompatActivity {

    //데이터를 받아보기 위한 변수들(ListView)
    ListView memoView;
    Thread viewThread;
    String address;

    //이벤트를 위한 버튼
    Button bt_memo_write;//메모작성
    Button bt_total_remove;//전체 데이터 제거

    //DataBase를 위한 변수들
    SQLiteDatabase db;
    String dbName = "memoList.db";
    String tableName = "memoTable";
    int dbMode = Context.MODE_PRIVATE;
    DataBaseService database;

    //DataSet Array
    ArrayList<DataSet> dataSets;

    //ListView에 Layout을 삽입하기위한 어뎁터
    DataAdapter m_adapter;

    //Thread 종료를 위한 변수
    boolean isRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_list);

        //버튼 및 View 초기화
        memoView = (ListView) findViewById(R.id.list_view);
        bt_memo_write = (Button) findViewById(R.id.bt_memo_write);
        bt_total_remove = (Button) findViewById(R.id.bt_total_remove);

        //ListView에 클릭을 가능하게 하기 위해
        memoView.setOnItemClickListener(mItemClickListener);

        //Database open
        db = openOrCreateDatabase(dbName,dbMode,null);
        database = new DataBaseService();

        //테이블 생성
        database.createTable(db, tableName);

        //전 액티비티에서 받아온 주소를 adress에 저장
        Intent intent = getIntent();
        address = intent.getStringExtra("address");

        //DataSet에 DB에 있는 데이터 모두 저장
        dataSets = new ArrayList<DataSet>();
        database.selectAll(db, tableName, dataSets);

        //어텝터 초기화
        m_adapter = new DataAdapter(this, R.layout.activity_memo_list, dataSets);

        //어뎁터 적용
        memoView.setAdapter(m_adapter);
    }

    //버튼이벤트를 등록
    public void onClick(View v) {
        switch(v.getId()) {
            //메모를 작성하는 엑티비티 시작
            case R.id.bt_memo_write:
                Intent intent = new Intent(this, MemoWriteActivity.class);
                //주소를 인텐트로 보낸다.
                intent.putExtra("address", address);
                startActivity(intent);
                break;
            case R.id.bt_total_remove:
                //Database에 있는 모든 데이터 삭제(테이블 삭제하고 다시 생성)
                database.totalRemove(db, tableName);
                Toast.makeText(getApplicationContext(), "메모가 전부 삭제되었습니다.", Toast.LENGTH_LONG).show();
                break;
        }
    }

    //List 뷰에 각 아이템마다 레이아웃을 삽입시키기 위한 클래스
    private class DataAdapter extends ArrayAdapter<DataSet> {

        private ArrayList<DataSet> dataSets;

        public DataAdapter(Context context, int textViewResourceId, ArrayList<DataSet> dataSets) {
            super(context, textViewResourceId, dataSets);
            this.dataSets = dataSets;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //view에 list_view_inner레이아웃 삽입
                view = inflater.inflate(R.layout.list_view_inner, null);
            }
            DataSet m_data = dataSets.get(position);
            if (m_data != null) {
                //레이아웃에 있는 텍스트 뷰 초기화
                TextView time = (TextView) view.findViewById(R.id.inner_time);
                TextView location = (TextView) view.findViewById(R.id.inner_location);
                TextView title = (TextView) view.findViewById(R.id.inner_title);

                //텍스트 뷰에 각 데이터 삽입 DataSet에 있는 Method 사용
                time.setText(m_data.getTimeSet());
                location.setText(m_data.getLocationSet());
                title.setText(m_data.getTitleSet());

            }
            //뷰를 반환한다.
            return view;
        }
    }


    //ListView에 각 아이템들을 위한 버튼이벤트
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

        //onItemClick method 사용
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long l_position) {
            //parent 는 컨테이너, view는 각 아이템들, position을 list위치
            //시간을 저장하고 있는 뷰의 Data를 가져온다.
            String time =((TextView) view.findViewById(R.id.inner_time)).getText().toString();

            //가져온 시간으로 데이터베이스에서 id를 찾는다
            int id = database.getId(db, tableName, time);
            //id와 함께 memoView 엑티비티로 인텐트
            Intent intent = new Intent(MemoListActivity.this, MemoViewActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //쓰레드 시작을 위해
        isRun = true;

        //view 쓰레드. 데이터를 레이아웃에 갱신시키기 위해 쓰레드 사용
        viewThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isRun) {
                        //핸들러에 0을 보낸다.
                        handler.sendEmptyMessage(0);
                        Thread.sleep(500);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //쓰레드 시작
        viewThread.start();

    }

    @Override
    public void onStop() {
        super.onStop();
        //쓰레드 종료
        threadStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //데이터베이스 닫기
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

    //핸들러에 의해 시작
    public void viewThread() {
        //데이터 초기화
        dataSets.clear();
        //모든 데이터 읽어오기
        database.selectAll(db, tableName, dataSets);
        //변화된 데이터 갱신
        m_adapter.notifyDataSetChanged();
    }

    //쓰레드 종료를 위한 method
    public void threadStop() {
        try {
            isRun = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
