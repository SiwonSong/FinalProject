package kr.kookmin.finalproject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class DataBaseService {

    public DataBaseService() {

    }

    public void createTable(SQLiteDatabase db, String tableName) {
        try {
            String sql = "create table " + tableName + "(id integer primary key autoincrement, " + "time text, location text, memo text);";
            db.execSQL(sql);
        } catch (android.database.sqlite.SQLiteException e) {
            Log.d("Lab sqlite", "error: " + e);
        }
    }

    // Table 삭제
    public void totalRemove(SQLiteDatabase db, String tableName) {
        String sql = "drop table " + tableName;
        db.execSQL(sql);
        String sql2 = "create table " + tableName + "(id integer primary key autoincrement, " + "time text, location text, memo text);";
        db.execSQL(sql2);
    }

    // Data 추가
    public void insertData(SQLiteDatabase db, String tableName, String time, String location, String memo) {
        String sql = "insert into " + tableName + " values(NULL, '" + time + "', '" + location +"', '"+ memo + "');";
        db.execSQL(sql);
    }

    // Data 업데이트
    public void updateData(SQLiteDatabase db, String tableName, int index, String updateMemo) {
        String sql = "update " + tableName + " set memo = '" + updateMemo + "' where id = " + index + ";";
        db.execSQL(sql);
    }

    // Data 삭제
    public void removeData(SQLiteDatabase db, String tableName, int index) {
        String sql = "delete from " + tableName + " where id = " + index + ";";
        db.execSQL(sql);
    }

    // Data 읽기(꺼내오기)
    public int getId(SQLiteDatabase db, String tableName, String time) {
        String sql = "select * from " + tableName + " where time = '" + time + "';";
        Cursor result = db.rawQuery(sql, null);

        if (result.moveToFirst()) {
            int id = result.getInt(0);

            return id;
        }
        result.close();
        return -1;
    }

    public void selectData(SQLiteDatabase db, String tableName, int index, ArrayList<String> arrayList) {
        String sql = "select * from " + tableName + " where id = " + index + ";";
        Cursor result = db.rawQuery(sql, null);
        result.moveToFirst();

        for(int i = 1; i < 4; i++) {
            arrayList.add(result.getString(i));
        }

        result.close();

    }

    // 모든 Data 읽기
    public void selectAll(SQLiteDatabase db, String tableName, ArrayList<String> nameList) {
        String sql = "select * from " + tableName + ";";
        Cursor results = db.rawQuery(sql, null);
        results.moveToLast();

        while (!results.isBeforeFirst()) {
            int id = results.getInt(0);
            String time = results.getString(1);
            String location = results.getString(2);
            String memo = results.getString(3);
//          Toast.makeText(this, "index= " + id + " name=" + name, Toast.LENGTH_LONG).show();
            Log.d("lab_sqlite", "index= " + id + " time=" + time);

            nameList.add(time);

            results.moveToPrevious();
        }
        results.close();
    }

}

