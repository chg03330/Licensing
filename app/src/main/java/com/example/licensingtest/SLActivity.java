package com.example.licensingtest;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

//SearchListActivity
public class SLActivity extends Activity {

    String dbName = "jmtest.db";
    int dbVersion = 3;
    private ListDataBase ldb;
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log의 tag 로 사용

    String name;
    EditText SearchText;
    ListView listview;
    TextView textview;

    ArrayList<list_example> myList;
    MyListAdapter myListAdapter;

    String today;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_search);

        myList = new ArrayList<list_example>();

        SearchText = (EditText) findViewById(R.id.EditText1);
        listview = (ListView) findViewById(R.id.ListView1);
        textview = (TextView) findViewById(R.id.MStoday);
        textview.setText(today = TodaySet());
        name = First();
        name = SChecking(name);
        if ("".equals(name)) {
            listview.setVisibility(View.INVISIBLE);
            textview.setText("검색어를 입력하지 않았습니다.");
        } else {
            DBon();
            ListSetting(name);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    myListAdapter = new MyListAdapter(SLActivity.this, myList);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listview.setAdapter(myListAdapter);
                        }
                    });
                }
            }).start();

        }


    }

    String First() {
        Intent intent = getIntent();
        return intent.getExtras().getString("search");
    }

    String SChecking(String a) {

        String b = a.replaceAll(" ", "");
        if ("".equals(b)) {
            return "";
        } else
            return b;
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
        Log.d("Tag123", "값:" + c.getInt(0));
        if (c.getInt(0) == 0)
            return true;
        else
            return false;
    }


    public void mOnClick(View v) {
        String a = SearchText.getText().toString();
        switch (v.getId()) {
            case R.id.SearchButton: {
                Intent intent = new Intent(getApplicationContext(), SLActivity.class);
                intent.putExtra("search", a);

                startActivity(intent);
                break;
            }
            case R.id.OpenLikeListButton: {
                Intent intent = new Intent(getApplicationContext(), LikeList.class);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_in_right);
                break;
            }
            case R.id.Logo: {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
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
                    Toast.makeText(getApplicationContext(), "삭제 성공", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }

    }


    void ListSetting(String name) {
        String sqlindex = "select * from student where jmfldnm Like '%" + name + "%'"; // 검생용
        Cursor indexC = db.rawQuery(sqlindex, null);

        if (indexC.getCount() == 0)
            textview.setText(name + "에 대한 결과가 없습니다.");
        else {
            while (indexC.moveToNext()) {
                String jn, jc, sc, qualgbcd;
                int likecheck;
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
        }
        indexC.close();
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
            c.close();
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
            c.close();
            return sb.toString();
        }
    }


    String TodaySet() {
        Date today = new Date();
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
        return date.format(today);

    } // 나중에 수정 20200115 로 고정됨
}
