package com.example.licensingtest;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class ListDetail extends Activity {

    String dbName = "jmtest.db";
    int dbVersion = 3;
    private ListDataBase ldb;
    private SQLiteDatabase db;
    String tag = "SQLite";

    Thread d1, d2;

    Intent intent;
    String jmcdIntent, cd, qualgbcd;
    int likecheck;
    String key = "g2KNHhe9AFR2%2FgfT0hrEl8iY%2FPiGp7AeAjnqHqSdzhA%2FxsgrTfele4KQ8nTorCekvke4qY2jrhVPOp63BDdHnA%3D%3D";
    TextView At, Ct, Dt, Et;
    Button LikeB;
    EditText SearchText;

    list_example m;

    StringBuffer feesSB, howSB;
    String converhow = "시험 정보가 없습니다.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_detail);

        At = (TextView) findViewById(R.id.Text1_Text);
        Ct = (TextView) findViewById(R.id.Text3_Text);
        Dt = (TextView) findViewById(R.id.Text4_Text);
        Et = (TextView) findViewById(R.id.Text5_Text);
        LikeB = (Button) findViewById(R.id.LikeButton);

        SearchText = (EditText) findViewById(R.id.EditText1);

        intent = getIntent(); // 보내온 Intent를 얻는다
        DBon();

        m = (list_example) intent.getSerializableExtra("mylist");

        jmcdIntent = m.getJmCode();
        At.setText(m.getJmName());
        likecheck = m.getLikeCheck();

        if (likecheck == 0)
            LikeB.setBackgroundResource(R.drawable.star_off);
        else
            LikeB.setBackgroundResource(R.drawable.star_on);

        DataSetting(jmcdIntent);


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

    void DataSetting(String jmcd) {
        String sql = "select * from student  where jmcd ='" + jmcd + "'";
        Cursor C = db.rawQuery(sql, null);
        while (C.moveToNext()) {
            qualgbcd = C.getString(2);
            cd = C.getString(4);

            // qualgbcd = 기술,전문 판별      jmcd = 기술일시 사용    cd = 전문일시 사용
            calendar(qualgbcd, jmcd, cd);

            d1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    fees(); //수수료
                    runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {
                                          Ct.setText(feesSB);
                                      }
                                  }
                    );
                    d2.start();
                }
            }
            );
            d2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    howget();
                    runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {
                                          Et.setText(converhow);
                                      }
                                  }
                    );
                }
            }
            );
            d1.start();

        }

    }


    void fees() {
        String queryUrl = "http://openapi.q-net.or.kr/api/service/rest/InquiryTestInformationNTQSVC/getFeeList?ServiceKey=" + key + "&jmCd=" + jmcdIntent;
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
                        else if (tag.equals("contents")) {
                            xpp.next();
                            feesSB = new StringBuffer("\n" + xpp.getText() + "\n");
                        }
                        break;
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            feesSB = new StringBuffer("파싱 실패 \n");
        }


    } // 수수료

    void howget() {
        String queryUrl = "http://openapi.q-net.or.kr/api/service/rest/InquiryInformationTradeNTQSVC/getList?ServiceKey=" + key + "&jmCd=" + jmcdIntent;
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
                        else if (tag.equals("contents")) {
                            xpp.next();
                            converhow = xpp.getText();
                            //converhow.replace("시행처","시행바보");
                        }
                        break;
                }
                eventType = xpp.next();
            }
            Log.d("converhow","body="+converhow.indexOf("BODY"));
            if (converhow.equals(null) || converhow.equals(""))
                converhow = "자료가 없습니다";
            else {

                if(converhow.indexOf("BODY")==0 && converhow.indexOf("&#9312")>=0){
                    Log.d("converhow","body="+converhow.indexOf("1)"));
                    converhow=converhow.substring(converhow.indexOf("&#9312")).replace("&#9312;","①").replace("&#9313;","\n②")
                            .replace("&#9314;","\n③").replace("&#9315;","\n④").replace("&#9316;","\n⑤")
                            .replace("&middot;","·").replace("&rarr;","→");
                }
                else if(converhow.indexOf("BODY")==0 && converhow.indexOf("□")>=0) {
                    converhow=converhow.substring(converhow.indexOf("□")).replace("&lt;","<")
                            .replace("&middot;","·").replace("&rarr;","→").replace("○","\n○")
                            .replace("1.","\n1.").replace("2.","\n2.").replace("3.","\n3.").replace("4.","\n4.");
                } else if(converhow.indexOf("BODY")==0 && converhow.indexOf("&lt;")>=0){
                    converhow=converhow.substring(converhow.indexOf("&lt;")).replace("&lt;","<")
                            .replace("&middot;","·").replace("&rarr;","→");
                }
                else if(converhow.indexOf("BODY")==0 && converhow.indexOf("○")>=0){
                    converhow=converhow.substring(converhow.indexOf("○")).replace("&lt;","<")
                            .replace("&middot;","·").replace("&rarr;","→").replace("○","\n○");
                }
                else if(converhow.indexOf("BODY")<=0 && converhow.indexOf("}",1)>=0){
                    converhow=converhow.substring(converhow.indexOf("}",150)+1).replace("&lt;","<")
                            .replace("&middot;","·").replace("&rarr;","→");
                }
                else if(converhow.indexOf("BODY")<=0)
                converhow=converhow.replace("②","\n②").replace("③","\n③").replace("④","\n④")
                        .replace("⑤","\n⑤").replace("⑥","\n⑥").replace("⑦","\n⑦").replace("1)","\n1)")
                        .replace("2)","\n2)").replace(" 3)","\n3)").replace("4)","\n4)").replace("5)","\n5)")
                        .replace("6)","\n6)");
            }


        } catch (Exception e) {
            howSB = new StringBuffer("파싱 실패 \n");
        }


    } // 수수료

    void calendar(String qualgbcd, String jmcd, String seriCd) {
        if (qualgbcd.equals("S")) {
            StringBuffer sb = new StringBuffer();
            String sql = "select * from date  where seriescd ='" + seriCd + "'";
            Cursor c = db.rawQuery(sql, null);
            while (c.moveToNext()) {

                Dt.append("\n");
                StringBuffer startdt = new StringBuffer(c.getString(3)).insert(4, ". ").insert(8, ". ");
                StringBuffer enddt = new StringBuffer(c.getString(4)).insert(4, ". ").insert(8, ". ");

                Dt.append(c.getString(2) + "\n     접수 기간 : " + startdt + " ~ " + enddt + "\n");

                String CheckA = c.getString(5);
                String CheckB = c.getString(6);

                startdt = new StringBuffer(CheckA).insert(4, ". ").insert(8, ". ");
                enddt = new StringBuffer(CheckB).insert(4, ". ").insert(8, ". ");
                if (CheckA.equals(CheckB))
                    Dt.append("     시험 일정 : " + startdt + "\n");
                else
                    Dt.append("     시험 일정 : " + startdt + " ~ " + enddt + "\n");

                startdt = new StringBuffer(c.getString(7)).insert(4, ". ").insert(8, ". ");
                Dt.append("     합격자 발표 : " + startdt + "\n\n");
            }
        } else {
            StringBuffer sb = new StringBuffer();
            String sql = "select * from techdate  where jmcd ='" + jmcd + "'";
            Cursor c = db.rawQuery(sql, null);
            while (c.moveToNext()) {

                String CheckA = c.getString(3);
                String CheckB = c.getString(4);

                try{

                    Dt.append("\n");
                    StringBuffer startdt = new StringBuffer(CheckA).insert(4, ". ").insert(8, ". ");
                    StringBuffer enddt = new StringBuffer(CheckB).insert(4, ". ").insert(8, ". ");

                    Dt.append(c.getString(2) + "\n(필기)\n     접수 기간 : " + startdt + " ~ " + enddt + "\n");

                    CheckA = c.getString(5);
                    CheckB = c.getString(6);

                    startdt = new StringBuffer(CheckA).insert(4, ". ").insert(8, ". ");
                    enddt = new StringBuffer(CheckB).insert(4, ". ").insert(8, ". ");
                    if (CheckA.equals(CheckB))
                        Dt.append("     시험 일정 : " + startdt + "\n");
                    else
                        Dt.append("     시험 일정 : " + startdt + " ~ " + enddt + "\n");

                    startdt = new StringBuffer(c.getString(7)).insert(4, ". ").insert(8, ". ");
                    Dt.append("     합격자 발표 : " + startdt + "\n");

                }catch (NullPointerException e){
                    Dt.append("(필기) - 일정이 없습니다   \n");
                }
                StringBuffer startdt = new StringBuffer(c.getString(8)).insert(4, ". ").insert(8, ". ");
                StringBuffer enddt = new StringBuffer(c.getString(9)).insert(4, ". ").insert(8, ". ");
                Dt.append("(실기)\n     접수 기간 : " + startdt + " ~ " + enddt + "\n");

                startdt = new StringBuffer(c.getString(10)).insert(4, ". ").insert(8, ". ");
                enddt = new StringBuffer(c.getString(11)).insert(4, ". ").insert(8, ". ");
                Dt.append("     시험 일정 : " + startdt + " ~ " + enddt + "\n");

                startdt = new StringBuffer(c.getString(12)).insert(4, ". ").insert(8, ". ");
                Dt.append("     합격자 발표 : " + startdt + "\n\n");

            }
        }

    } //날짜 셋팅

    //즐찾DB값존재여부
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
            //즐겨찾기버튼
            case R.id.LikeButton: {
                if (checkLikeList(m.getJmCode())) { // return true;
                    insertLikeList(m.getJmCode());
                    LikeB.setBackgroundResource(R.drawable.star_on);
                    m.setLikeCheck(1);
                    Toast.makeText(getApplicationContext(), "추가 성공", Toast.LENGTH_SHORT).show();
                } else { //return false;
                    deleteLikeList(m.getJmCode());
                    LikeB.setBackgroundResource(R.drawable.star_off);
                    m.setLikeCheck(0);
                    Toast.makeText(getApplicationContext(), "삭제 성공", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }

    }


}// end of onCreate
