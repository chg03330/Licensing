package com.example.licensingtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Loading extends AppCompatActivity {

    String dbName = "jmtest.db";
    int dbVersion = 3;
    private ListDataBase ldb;
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log의 tag 로 사용
    String studentTN = "student"; // DB의 table 명
    String dateTN = "date"; // DB의 table 명


    TextView LoadT;

    String key = "g2KNHhe9AFR2%2FgfT0hrEl8iY%2FPiGp7AeAjnqHqSdzhA%2FxsgrTfele4KQ8nTorCekvke4qY2jrhVPOp63BDdHnA%3D%3D";

    String[] seriCDarry = {"07", "08", "09", "22", "24", "31", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "45", "46", "49", "50", "51", "52", "54",
            "56", "57", "59", "60", "61", "62", "63", "64", "66", "67", "72", "73"};

    String Check_T = "", Using_jmcd = "";

    boolean isGetDateData, isGetTechDateData;
    int GDD, TDD;

    Thread t1, t2, t3;
    int tbChecking = 0;
    int log = 0, PS = 0, PE = 0;
    int PS3 = 0, Pinsert = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);


        LoadT = (TextView) findViewById(R.id.LDloading);
        isGetDateData = false;
        isGetTechDateData = false;

        DBgo();
        Tbcheckgo();

        if (tbChecking == 0) {

            LoadT.setText("DB 값 생성 시작...");

            t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("thred1", "start");
                    getListData();
                    Log.d("thred1", "end1");
                }
            });

            t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("thred2", "start2");
                    int i = 0;
                    GDD = 0;
                    while (i < 36)
                        new Thread(new getDateDataRunnable(seriCDarry[i++])).start();
                    Log.d("thred2", "end 2");
                }
            });

            t3 = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("thred3", "start3");
                    Log.d("thred3", "Start SQL");
                    String sql = "select jmcd from student  where qualgbcd='T'";
                    Cursor C = db.rawQuery(sql, null);
                    Log.d("thred3", "End SQL");
                    TDD = 0;
                    while (C.moveToNext()) {
                        Log.d("thred3", "Next SQL" + ++PS3);
                        new Thread(new getTechDateDateRunnable(C.getString(0))).start();
                    }
                    Log.d("thred3", "end 3");
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {

                    t1.start();
                    t2.start();
                    try {
                        t1.join();
                        t3.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        finish();
                    }


                    try {
                        t3.join();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LoadT.setText("화면을 클릭하세요 !");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        finish();
                    }
                }
            }).start();


            Log.d("thred4", "start4");
            ContentValues values = new ContentValues();
            values.put("tbcheck", 1);
            db.insert("option", null, values);
            Log.d("thred4", "end4");
            Log.d("thred4", Thread.activeCount() + "");


        } else if (tbChecking == 1) {
            isGetTechDateData = true;
            isGetDateData = true;
            LoadT.setText("화면을 클릭하세요 !");
        }

    }

    //
    void DBgo() {
        ldb = new ListDataBase(this, dbName, null, dbVersion);
        try {
            db = ldb.getWritableDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(tag, "데이터 베이스를 열수 없음");
            finish();
        }
    } //DB 생성

    //
    void Tbcheckgo() {
        String sql = "select * from option ";    // 검생용// 작성한 코드 적용
        Cursor c = db.rawQuery(sql, null);       // sql 구문 실행
        while (c.moveToNext()) {
            tbChecking = c.getInt(1);
        }
    }   // 옵션 select 하기

    void getListData() {

        ContentValues values = new ContentValues();
        String queryUrl = "http://openapi.q-net.or.kr/api/service/rest/InquiryListNationalQualifcationSVC/getList?ServiceKey=" + key;
        try {
            URL url = new URL(queryUrl); //문자열로 된 요청 url을 URL 객체로 생성
            InputStream is = url.openStream(); // url위치로 인풋스트림 연결
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            // inputstream 으로부터 xml 입력받기
            xpp.setInput(new InputStreamReader(is, "UTF-8"));

            String tag;


            xpp.next();
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName(); // 태그 이름 얻어오기
                        if (tag.equals("item")) ; // 첫번째 검색결과
                        else if (tag.equals("jmcd")) {
                            xpp.next();
                            Using_jmcd = xpp.getText();
                            values.put("jmcd", Using_jmcd);
                        } else if (tag.equals("qualgbcd")) {
                            xpp.next();
                            Check_T = xpp.getText();
                            values.put("qualgbcd", Check_T);
                        } else if (tag.equals("jmfldnm")) {
                            xpp.next();
                            values.put("jmfldnm", xpp.getText());
                        } else if (tag.equals("seriescd")) {
                            xpp.next();
                            values.put("seriescd", xpp.getText());
                            //values.put("likelist",0);
                            db.insert(studentTN, null, values);
                        }
                        break;
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            LoadT.append("파싱 실패 \n");
        }

    } //List 넣기  -목록

    void getDateData(String arr) {
        ContentValues values = new ContentValues();
        String tag;
        values.put("seriescd", arr);
        String queryUrl = "http://openapi.q-net.or.kr/api/service/rest/InquiryTestDatesNationalProfessionalQualificationSVC/getList?ServiceKey=" + key + "&seriesCd=" + arr;

        try {
            Log.d("thred2", "Paxing Start" + ++PS);
            URL url = new URL(queryUrl); //문자열로 된 요청 url을 URL 객체로 생성
            InputStream is = url.openStream(); // url위치로 인풋스트림 연결
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8"));
            Log.d("thred2", "Paxing end" + ++PE);

            xpp.next();
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName(); // 태그 이름 얻어오기
                        if (tag.equals("item")) ; // 첫번째 검색결과
                        else if (tag.equals("description")) {
                            xpp.next();
                            values.put("description", xpp.getText());
                        } else if (tag.equals("examregstartdt")) {
                            xpp.next();
                            values.put("examregstartdt", xpp.getText());
                        } else if (tag.equals("examregenddt")) {
                            xpp.next();
                            values.put("examregenddt", xpp.getText());
                        } else if (tag.equals("examstartdt")) {
                            xpp.next();
                            values.put("examstartdt", xpp.getText());
                        } else if (tag.equals("examenddt")) {
                            xpp.next();
                            values.put("examenddt", xpp.getText());
                        } else if (tag.equals("passstartdt")) {
                            xpp.next();
                            values.put("passstartdt", xpp.getText());
                            Log.d("thred2", "DB insert" + ++log);
                            db.insert(dateTN, null, values);
                            Log.d("thred2", "DB Insert End");
                        }
                        break;
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            Log.d("testGDD","GDD ::: "+arr+"-seriesCode 파싱 실패 \n");
        }

    } // 일정 넣기-전문 자격

    void getTechDateDate(String jmcd) {
        ContentValues values = new ContentValues();
        String tag;
        String queryUrl = "http://openapi.q-net.or.kr/api/service/rest/InquiryTestInformationNTQSVC/getJMList?ServiceKey=" + key + "&jmCd=" + jmcd;

        values.put("jmcd", jmcd);


        Log.d("TEST", "" + queryUrl);
        try {
            Log.d("Thred 3", "Paxing Start");
            URL url = new URL(queryUrl); //문자열로 된 요청 url을 URL 객체로 생성
            InputStream is = url.openStream(); // url위치로 인풋스트림 연결
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8"));
            Log.d("Thred 3", "Paxing End");

            xpp.next();
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName(); // 태그 이름 얻어오기
                        if (tag.equals("item")) ; // 첫번째 검색결과
                        else if (tag.equals("docexamenddt")) {
                            xpp.next();
                            values.put("docexamenddt", xpp.getText());
                        } else if (tag.equals("docexamstartdt")) {
                            xpp.next();
                            values.put("docexamstartdt", xpp.getText());
                        } else if (tag.equals("docpassdt")) {
                            xpp.next();
                            values.put("docpassdt", xpp.getText());
                        } else if (tag.equals("docregenddt")) {
                            xpp.next();
                            values.put("docregenddt", xpp.getText());
                        } else if (tag.equals("docregstartdt")) {
                            xpp.next();
                            values.put("docregstartdt", xpp.getText());
                        } else if (tag.equals("implplannm")) {
                            xpp.next();
                            values.put("implplannm", xpp.getText());
                        } else if (tag.equals("pracexamenddt")) {
                            xpp.next();
                            values.put("pracexamenddt", xpp.getText());
                        } else if (tag.equals("pracexamstartdt")) {
                            xpp.next();
                            values.put("pracexamstartdt", xpp.getText());
                        } else if (tag.equals("pracpassstartdt")) {
                            xpp.next();
                            values.put("pracpassstartdt", xpp.getText());
                        } else if (tag.equals("pracregenddt")) {
                            xpp.next();
                            values.put("pracregenddt", xpp.getText());
                        } else if (tag.equals("pracregstartdt")) {
                            xpp.next();
                            values.put("pracregstartdt", xpp.getText());
                            Log.d("Thred 3", "DB insert" + ++Pinsert);
                            db.insert("techdate", null, values);

                        }
                        break;
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            Log.d("testTDD","TDD ::: "+jmcd+"-jmcd 파싱 실패 \n");
        }


    } // 일정 넣기- 기술자격


    public void NextScreen(View v) {
        Log.d("testTDD","isGDD = "+GDD+",isTDD ="+TDD);
        if (isGetDateData && isGetTechDateData) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    class getDateDataRunnable implements Runnable {
        String a;

        public getDateDataRunnable(String c) {
            a = c;
            GDD++;
        }

        @Override
        public void run() {
            getDateData(a);
            GDD--;
            Log.d("testTDD", "testGDD : "+GDD+", isGTDD :"+isGetDateData);
            if (GDD == 0) {
                isGetDateData = true;
                Log.d("testTDD", "testGDD : "+GDD+", isGTDD :"+isGetDateData);
            }
        }
    }

    class getTechDateDateRunnable implements Runnable {
        String a;

        public getTechDateDateRunnable(String c) {
            a = c;
            TDD++;
        }

        @Override
        public void run() {
            getTechDateDate(a);
            TDD--;
            Log.d("testTDD", "testTDD : "+TDD+", isGTDD :"+isGetTechDateData);
            if (TDD == 0) {
                isGetTechDateData = true;
                Log.d("testTDD", "testTDD : "+TDD+", isGTDD :"+isGetTechDateData);
            }
        }
    }


}
