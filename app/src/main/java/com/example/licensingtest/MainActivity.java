package com.example.licensingtest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    String dbName = "jmtest.db";
    int dbVersion = 3;
    private ListDataBase ldb;
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log의 tag 로 사용

    ArrayList<list_example> myList, myList2;
    MyListAdapter myListAdapter;
    ListView listView;
    TextView todayShow;
    EditText SearchText;

    //페이징 사용 변수들
    private boolean lastItemVisibleFlag = false;    // 리스트 스크롤이 마지막 셀(맨 바닥)로 이동했는지 체크할 변수
    private int page = 0;                           // 페이징변수. 초기 값은 0 이다.
    private final int OFFSET = 15;                  // 한 페이지마다 로드할 데이터 갯수.
    private ProgressBar progressBar;                // 데이터 로딩중을 표시할 프로그레스바
    private boolean mLockListView = false;          // 데이터 불러올때 중복안되게 하기위한 변수
    ////////////////

    String today;
    String jn, jc, sc, qualgbcd;
    int likecheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SearchText = (EditText) findViewById(R.id.EditText1);
        listView = (ListView) findViewById(R.id.ListView1);
        myList = new ArrayList<list_example>();
        myList2 = new ArrayList<list_example>();
        todayShow = (TextView) findViewById(R.id.MStoday);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        today = TodaySet();
        todayShow.setText(today);


        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        DBon();
                        ListSetting();

                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Collections.sort(myList);
                                        addMyList2();
                                        myListAdapter = new MyListAdapter(MainActivity.this, myList2);
                                        listView.setAdapter(myListAdapter);

                                    }
                                }

                        );

                    }
                }
        ).start();

        //페이징을위한 스크롤리스너 추가
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 1. OnScrollListener.SCROLL_STATE_IDLE : 스크롤이 이동하지 않을때의 이벤트(즉 스크롤이 멈추었을때).
                // 2. lastItemVisibleFlag : 리스트뷰의 마지막 셀의 끝에 스크롤이 이동했을때.
                // 3. mLockListView == false : 데이터 리스트에 다음 데이터를 불러오는 작업이 끝났을때.
                // 1, 2, 3 모두가 true일때 다음 데이터를 불러온다.
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag && mLockListView == false) {
                    // 화면이 바닦에 닿을때 처리
                    // 로딩중을 알리는 프로그레스바를 보인다.
                    progressBar.setVisibility(View.VISIBLE);

                    addMyList2();

                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                // firstVisibleItem : 화면에 보이는 첫번째 리스트의 아이템 번호.
                // visibleItemCount : 화면에 보이는 리스트 아이템의 갯수
                // totalItemCount : 리스트 전체의 총 갯수
                // 리스트의 갯수가 0개 이상이고, 화면에 보이는 맨 하단까지의 아이템 갯수가 총 갯수보다 크거나 같을때.. 즉 리스트의 끝일때. true
                lastItemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
            }
        });

    }

    //MyList 인덱스기준 20개씩 MyList2 ArrayList에 추가 함수
    void addMyList2() {
        // 리스트에 다음 데이터를 입력할 동안에 이 메소드가 또 호출되지 않도록 mLockListView 를 true로 설정한다.
        mLockListView = true;


        // 다음 20개의 데이터를 불러와서 리스트에 저장한다.
        for (int i = 0; i < OFFSET; i++) {
            if (myList.size() == myList2.size())
                break;
            else
                myList2.add(myList.get((page * OFFSET) + i));

        }

        // 2초 뒤 프로그레스바를 감추고 데이터를 갱신하고, 중복 로딩 체크하는 Lock을 했던 mLockListView변수를 풀어준다.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                page++;
                myListAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                mLockListView = false;
            }
        }, 100); // 2000 : 2초


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
        String sqlindex = "select * from student"; // 검생용
        Cursor indexC = db.rawQuery(sqlindex, null);

        while (indexC.moveToNext()) {


            jc = indexC.getString(1);
            qualgbcd = indexC.getString(2);
            jn = indexC.getString(3);
            sc = indexC.getString(4);
            likecheck = indexC.getInt(5);

            // S = 전문 자격       T = 기술 자격    sc에 넣기
            if (qualgbcd.equals("S"))
                sc = ExpertDateSet(sc);
            else if (qualgbcd.equals("T"))
                sc = TechnicianDateSet(jc);

            if (setComing(sc))
                myList.add(new list_example(jc, jn, sc, likecheck));
        }

        indexC.close();
    } // 리스트 셋팅 - 값을 불러 생성

    Boolean setComing(String sc) {
        String a = sc.substring(sc.indexOf("/") + 1, sc.indexOf("/") + 5);
        if (a.equals("9999")) {
            return false;
        } else
            return true;
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

    // 나중에 수정 20200115 로 고정됨
    String TodaySet() {
        Date today = new Date();
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
        return date.format(today);
    } // 나중에 수정 20200115 로 고정됨

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
        Log.d("Tag123", "체크값:" + c.getInt(0));
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
                if (checkLikeList(myList2.get(position).getJmCode())) { // return true;
                    insertLikeList(myList2.get(position).getJmCode());
                    myList2.get(position).setLikeCheck(1);
                    v.setBackgroundResource(R.drawable.star_on);
                    Toast.makeText(getApplicationContext(), "추가 성공", Toast.LENGTH_SHORT).show();
                } else { //return false;
                    deleteLikeList(myList2.get(position).getJmCode());
                    v.setBackgroundResource(R.drawable.star_off);
                    myList2.get(position).setLikeCheck(0);
                    Toast.makeText(getApplicationContext(), "삭제 성공", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }

    }

}


