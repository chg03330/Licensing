package com.example.licensingtest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ListDataBase extends SQLiteOpenHelper {
    public ListDataBase(Context context, String name,
                        SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table student" +
                "(_id integer primary key autoincrement," +
                "jmcd text,qualgbcd text, jmfldnm text, seriescd text, likecheck integer default 0);";
        db.execSQL(sql);
        sql = "create table techdate" +
                "(_id integer primary key autoincrement," +
                "jmcd text, implplannm text, docregstartdt integer,docregenddt integer,docexamstartdt integer,docexamenddt integer,docpassdt integer," +
                "pracregstartdt integer,pracregenddt integer,pracexamstartdt integer,pracexamenddt integer,pracpassstartdt integer);";
        db.execSQL(sql);
        sql = "create table date" +
                "(_id integer primary key autoincrement," +
                "seriescd text, description text, examregstartdt integer,examregenddt integer,examstartdt integer,examenddt integer,passstartdt integer);";
        db.execSQL(sql);
        sql = "create table option" +
                "(_id integer primary key autoincrement," +
                "tbcheck integer);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "drop table if exists student;";
        db.execSQL(sql);
        onCreate(db); // 다시 테이블 생성
    }


}
