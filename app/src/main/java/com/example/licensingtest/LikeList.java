package com.example.licensingtest;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LikeList extends Activity {

    String dbName = "jmtest.db";
    int dbVersion = 3;
    private ListDataBase ldb;
    private SQLiteDatabase db;
    //    final static String TAG = "sqlite";
    String tag = "SQLite"; // Log의 tag 로 사용


    ArrayList<list_example> myList;
    MyListAdapter myListAdapter;
    ListView listView;
    String today;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.like_layout);
        this.setFinishOnTouchOutside(true);

        //해당액티비티 크기, 위치조정
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels * 0.7); // Display 사이즈의 90%
        getWindow().getAttributes().width = width;
        getWindow().getAttributes().gravity = Gravity.RIGHT;

        //해당 액티비티 배경 투명도설정 -> 바탕화면 보이게
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        //액티비티 레이아웃 배경설정:하얀색 /
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.basic);
        rl.setBackgroundColor(Color.rgb(255, 255, 255));


        listView = (ListView) findViewById(R.id.ListView1);
        Button bt_c = (Button) findViewById(R.id.CloseLikeListButton);
        bt_c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        today = TodaySet();
        myList = new ArrayList<list_example>();
        DBon();
        ListSetting();


        myListAdapter = new MyListAdapter(LikeList.this, myList);
        listView.setAdapter(myListAdapter);


    }

    void DBon() {
        ldb = new ListDataBase(this, dbName, null, dbVersion);
        try {
            db = ldb.getWritableDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(tag, "데이터 베이스를 열수 없음");
            finish();
        }
    } //DB 생성

    void ListSetting() {
        try {
            String jc, jn, sc, qualgbcd;
            int likecheck;
            String sqlindex = "select * from student where likecheck = 1;"; // 검생용
            Cursor indexC = db.rawQuery(sqlindex, null);
            if (indexC.getCount() != 0)
                while (indexC.moveToNext()) {
                    jc = indexC.getString(1);
                    qualgbcd = indexC.getString(2);
                    jn = indexC.getString(3);
                    sc = indexC.getString(4);
                    likecheck = indexC.getInt(5);

                    if (qualgbcd.equals("S"))
                        sc = ExpertDateSet(sc);
                    else if (qualgbcd.equals("T"))
                        sc = TechnicianDateSet(jc);

                    myList.add(new list_example(jc, jn, sc, likecheck));
                }
            indexC.close();
        } catch (Exception e) {
            Log.d("TAG123", "안됨");
        }

    }

    String ExpertDateSet(String sd) {
        StringBuffer dateT = new StringBuffer();


        String Start, End = "0", AgoEnd = "0";
        String sqldate = "select * from date where seriescd = " + sd;
        Cursor dateC = db.rawQuery(sqldate, null);

        while (dateC.moveToNext()) {

            Start = dateC.getString(3);
            End = dateC.getString(4);

            if (!dateC.isNull(3)) {

                if ((Integer.parseInt(Start) <= Integer.parseInt(today) && Integer.parseInt(today) <= Integer.parseInt(End)) ||
                        (Integer.parseInt(AgoEnd) <= Integer.parseInt(today) && Integer.parseInt(today) <= Integer.parseInt(Start))) {
                    dateT.setLength(0);
                    //날짜 띄어쓰기
                    StringBuffer startdt = new StringBuffer(Start).insert(4, ".").insert(7, ".");
                    StringBuffer enddt = new StringBuffer(End).insert(4, ".").insert(7, ".");
                    //결합
                    dateT.append(dateC.getString(2) + "/" + startdt + "~" + (enddt));
                    break;
                }
                AgoEnd = End;

            }
        }
        if (Integer.parseInt(today) <= Integer.parseInt(End)) {
            dateC.close();
            return dateT.toString();
        } else {
            dateC.close();
            return "없음/9999.99.99";
        }

    }// 전문 자격증 날짜 출력

    String TechnicianDateSet(String jmcd) {
        StringBuffer sb = new StringBuffer();

        String StartDoc, EndDoc, StartPrac, EndPrac = "0", AgoStart = "0", AgoEnd = "0";
        int AgoPosition = 0;
        Boolean AgoTag = false; // False = Doc / True = Prac

        String sql = "select * from techdate  where jmcd ='" + jmcd + "'";
        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext()) {

            StartDoc = c.getString(3);
            EndDoc = c.getString(4);
            StartPrac = c.getString(8);
            EndPrac = c.getString(9);


            if (c.isNull(3)) { //StartDoc ==NULL
                if (c.isNull(8)) { // StartPrac == NULL
                    Log.d("TagTechDate_Main", "기간 확인 불가 코드:" + jmcd);
                    return "없음/9999.99.99";
                } else { // StartPrac != NULL
                    int sp = Integer.valueOf(StartPrac);
                    int ep = Integer.valueOf(EndPrac);
                    int td = Integer.valueOf(today);

                    if ((sp <= td && td <= ep) || td <= sp) {
                        AgoStart = StartPrac;
                        AgoEnd = EndPrac;
                        AgoPosition = c.getPosition();
                        AgoTag = true;
                        break;
                    }
                }
            } else { // StartDoc != NULL
                if (c.isNull(8)) { // StartPrac == NULL
                    int sd = Integer.valueOf(StartDoc);
                    int ed = Integer.valueOf(EndDoc);
                    int td = Integer.valueOf(today);

                    if ((sd <= td && td <= ed) || td <= sd) {
                        AgoStart = StartDoc;
                        AgoEnd = EndDoc;
                        AgoPosition = c.getPosition();
                        AgoTag = false;
                        break;
                    }

                } else { // StartPrac != NULL
                    int sp = Integer.valueOf(StartPrac);
                    int ep = Integer.valueOf(EndPrac);
                    int sd = Integer.valueOf(StartDoc);
                    int ed = Integer.valueOf(EndDoc);
                    int td = Integer.valueOf(today);

                    if (td <= sd || (sd <= td && td <= ed)) {
                        AgoStart = StartDoc;
                        AgoEnd = EndDoc;
                        AgoPosition = c.getPosition();
                        AgoTag = false;
                        break;
                    } else if ((sp <= td && td <= ep) || td <= sp) {
                        AgoStart = StartPrac;
                        AgoEnd = EndPrac;
                        AgoPosition = c.getPosition();
                        AgoTag = true;
                        break;
                    }
                }
            }
        }
        if ("0".equals(AgoStart)) {
            return "없음/9999.99.99";
        } else {
            StringBuffer as = new StringBuffer(AgoStart).insert(4, ".").insert(7, ".");
            StringBuffer ae = new StringBuffer(AgoEnd).insert(4, ".").insert(7, ".");
            c.moveToPosition(AgoPosition);
            if (AgoTag) {
                sb.append(c.getString(2) + "@실기" + "/" + as + "~" + ae);
            } else {
                sb.append(c.getString(2) + "@필기" + "/" + as + "~" + ae);
            }
            return sb.toString();
        }
    }


    String TodaySet() {
        Date today = new Date();
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");

        return date.format(today);
    }

    //리스트 갱신
    public void reSet() {
        myList.clear();
        ListSetting();
        myListAdapter.notifyDataSetChanged();
    }

    public void mOnClick(View v) {
        switch (v.getId()) {

            //즐겨찾기버튼
            case R.id.LikeButton: {
                int position = (int) v.getTag();
                if (checkLikeList(myList.get(position).getJmCode())) { // return true;
                    insertLikeList(myList.get(position).getJmCode());
                    myList.get(position).setLikeCheck(1);
                    v.setBackgroundResource(R.drawable.star_on);
                    Toast.makeText(getApplicationContext(), "추가 성공", Toast.LENGTH_SHORT).show();
                } else { //return false;
                    deleteLikeList(myList.get(position).getJmCode());
                    v.setBackgroundResource(R.drawable.star_off);
                    myList.get(position).setLikeCheck(0);
                    reSet();
                    Toast.makeText(getApplicationContext(), "삭제 성공", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    public void insertLikeList(String a) {
        db.execSQL("update student set likecheck=1 where jmcd ='" + a + "';");
    }

    public void deleteLikeList(String a) {
        db.execSQL("update student set likecheck=0 where jmcd ='" + a + "';");
    }

    //즐찾DB값존재여부
    public boolean checkLikeList(String a) {
        String sql = "select likecheck from student where jmcd = '" + a + "'";
        Cursor c = db.rawQuery(sql, null);
        c.moveToNext();
        if (c.getInt(0) == 0) {
            c.close();
            return true;
        } else {
            c.close();
            return false;
        }
    }

    //오버라이드
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_slide_out_right, R.anim.anim_slide_out_right);
    }


}
