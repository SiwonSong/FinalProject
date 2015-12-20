package kr.kookmin.finalproject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class DataBaseService {

    public DataBaseService() {

    }

    //DB Table을 생성하는 method
    public void createTable(SQLiteDatabase db, String tableName) {
        try {
            //데이터 내용은 id, title, time, location, memo (int, String, String, String, String)
            String sql = "create table " + tableName + "(id integer primary key autoincrement, " + "title text, time text, location text, memo text);";
            db.execSQL(sql);
        } catch (android.database.sqlite.SQLiteException e) {
            Log.d("Lab sqlite", "error: " + e);
        }
    }

    // Table에 있는 모든 내용을 삭제하는 method
    public void totalRemove(SQLiteDatabase db, String tableName) {
        //Table을 지우고 다시 만든다.
        String sql = "drop table " + tableName;
        db.execSQL(sql);
        String sql2 = "create table " + tableName + "(id integer primary key autoincrement, " + "title text, time text, location text, memo text);";
        db.execSQL(sql2);
    }

    // Data를 추가하기위한 Method
    public void insertData(SQLiteDatabase db, String tableName, String title, String time, String location, String memo) {
        String sql = "insert into " + tableName + " values(NULL, '" + title + "', '" + time + "', '" + location +"', '"+ memo + "');";
        db.execSQL(sql);
    }

    // Data 업데이트를 위한 Method. Title과 Memo만 변경 가능하도록 해놓았다.
    public void updateData(SQLiteDatabase db, String tableName, int index, String updateTitle, String updateMemo) {
        String sql = "update " + tableName + " set memo = '" + updateMemo + "' where id = " + index + ";";
        db.execSQL(sql);
        String sql2 = "update " + tableName + " set title = '" + updateTitle + "' where id = " + index + ";";
        db.execSQL(sql2);
    }

    // Data 삭제를 위한 Method. id를 값을 넣으면 해당 데이터들이 삭제된다.
    public void removeData(SQLiteDatabase db, String tableName, int index) {
        String sql = "delete from " + tableName + " where id = " + index + ";";
        db.execSQL(sql);
    }

    // id를 찾아주는 Method. 작성 시간을 바탕으로 id를 찾아낸다. 시간이 초단위로 기록되기때문에 겹칠 수 없다.
    public int getId(SQLiteDatabase db, String tableName, String time) {
        String sql = "select * from " + tableName + " where time = '" + time + "';";
        Cursor result = db.rawQuery(sql, null);

        //커서를 맨앞에 두어야한다.(시간으로 불러온 데이터는 1개 이기 때문)
        if (result.moveToFirst()) {
            int id = result.getInt(0);
            //id를 return한다.
            return id;
        }
        result.close();
        return -1;
    }

    //id로 Data를 불러와 ArrayList에 저장한다.
    public void selectData(SQLiteDatabase db, String tableName, int index, ArrayList<String> arrayList) {
        String sql = "select * from " + tableName + " where id = " + index + ";";
        Cursor result = db.rawQuery(sql, null);
        result.moveToFirst();

        for(int i = 1; i < 5; i++) {
            //(1번 title, 2번 time, 3번 location, 4번 Memo)
            arrayList.add(result.getString(i));
        }

        result.close();

    }

    // 모든 Data를 읽어와 ArrayList<DataSet>에 저장한다.
    public void selectAll(SQLiteDatabase db, String tableName, ArrayList<DataSet> dataSets) {
        String sql = "select * from " + tableName + ";";
        Cursor results = db.rawQuery(sql, null);
        //최근 저장된 순으로 불러오기 위해 커스를 마지막으로보냄
        results.moveToLast();

        //DataSet의 생성자는 id, title, time, location, memo를 저장한다.
        while (!results.isBeforeFirst()) {
            int id = results.getInt(0);
            String title = results.getString(1);
            String time = results.getString(2);
            String location = results.getString(3);
            String memo = results.getString(4);

            Log.d("lab_sqlite", "index= " + id + " time=" + time);
            //DataSet의 생성자는 id, title, time, location, memo를 저장한다.
            DataSet d = new DataSet(id, title, time, location, memo);
            dataSets.add(d);

            //커서를 맨 마지막에서 한칸씩 앞으로
            results.moveToPrevious();
        }
        results.close();
    }

}

