package com.example.licensingtest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyListAdapter extends BaseAdapter {
    Context context;
    ArrayList<list_example> list_exampleArrayList;
    TextView jCodeV, jNameV, sCodeV, jNameV2;
    Button LikeB;
    LinearLayout LLn;

    String mToday="";


    public MyListAdapter(Context context, ArrayList<list_example> list_exampleArrayList) {
        this.context = context;
        this.list_exampleArrayList = list_exampleArrayList;
    }


    @Override
    public int getCount() {
        return this.list_exampleArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return list_exampleArrayList.get(position);
        //Object getItem(int position)은 현재 어떤 아이템인지를 알려주는 부분으로
        // 우리는 arraylist에 저장되있는 객체중 position에 해당하는것을 가져올것이므로
        // return this.list_itemArrayList.get(position)
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_layout, null);
        }
        LinearLayout list = (LinearLayout) convertView.findViewById(R.id.list);
        list.setBackgroundColor(Color.rgb(255, 255, 255));

        jNameV = (TextView) convertView.findViewById(R.id.jNameTV);
        jCodeV = (TextView) convertView.findViewById(R.id.jCodeTV);
        sCodeV = (TextView) convertView.findViewById(R.id.sCodeTV);
        LikeB = (Button) convertView.findViewById(R.id.LikeButton);
        LLn = (LinearLayout) convertView.findViewById(R.id.linearLayout3);
        jNameV2 = (TextView) convertView.findViewById(R.id.jNameTV2);

        mToday=TodaySet();

        StringBuffer a = new StringBuffer(list_exampleArrayList.get(position).getSeriCode().substring(0, list_exampleArrayList.get(position).getSeriCode().indexOf("/")));
        StringBuffer b = new StringBuffer(list_exampleArrayList.get(position).getSeriCode().substring(list_exampleArrayList.get(position).getSeriCode().indexOf("/") + 1));

        if (list_exampleArrayList.get(position).getJmName().indexOf("(") < 0) {
            jNameV.setText(list_exampleArrayList.get(position).getJmName());
            jNameV2.setText("");
        } else {
            StringBuffer c = new StringBuffer(list_exampleArrayList.get(position).getJmName().substring(list_exampleArrayList.get(position).getJmName().indexOf("(")));
            StringBuffer d = new StringBuffer(list_exampleArrayList.get(position).getJmName().substring(0, list_exampleArrayList.get(position).getJmName().indexOf("(")));
            jNameV.setText(d);
            jNameV2.setText(c);

        }
        jCodeV.setText(a);

        if(Integer.parseInt(list_exampleArrayList.get(position).getSeriCode().substring(list_exampleArrayList.get(position).getSeriCode().indexOf("/") + 1).replace(".","").substring(0,8))<=Integer.parseInt(mToday)&&
                Integer.parseInt(mToday)<=Integer.parseInt(list_exampleArrayList.get(position).getSeriCode().substring(list_exampleArrayList.get(position).getSeriCode().indexOf("/") + 1).replace(".","").substring(9)))
            sCodeV.setTextColor(Color.parseColor("#5FDAEF"));
        else
            sCodeV.setTextColor(Color.parseColor("#000000"));


        sCodeV.setText(b);


        LikeB.setTag(position);

        if (list_exampleArrayList.get(position).getLikeCheck() == 1) {
            LikeB.setBackgroundResource(R.drawable.star_on);
        } else {
            LikeB.setBackgroundResource(R.drawable.star_off);
        }

        //연결
        LLn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        context, // 현재화면의 제어권자 "  Adapter생성시, context를받아옵니다.
                        //ㅣ context가 startActivity를 사용할 수 있게 해줍니다.
                        ListDetail.class); // 다음넘어갈 화면

                // intent 객체에 데이터를 실어서 보내기
                // 리스트뷰 클릭시 인텐트 (Intent) 생성하고 position 값을 이용하여 인텐트로 넘길값들을 넘긴다
                // 객체 전달
                intent.putExtra("mylist", list_exampleArrayList.get(position));
                context.startActivity(intent);
            }
        });


        return convertView;
        //이 부분이 리스트뷰에서 아이템과 xml을 연결하여 화면에 표시해주는
        //가장 중요한 부분입니다. getView부분에서 반복문이 실행된다고 이해하시면 편하며 순차적으로 한칸씩 화면을 구성해주는 부분입니다.
        //(여기서 부터는 이해가 가지않으시면 패턴을 암기하시면됩니다.)
        //우선 convertView라는 파라미터를 메소드에서 주는데요 이 부분에 우리가 만든 item.xml을 불러와야합니다. 여기는 엑티비티가 아니므로 불러오기위한 약간의 절차가 필요한데요 그 때문에 위에서 저희가 Context를 생성자를 통해 받은것입니다.
        //LayoutInflater 클래스를 이용하면 다른 클래스에서도 xml을 가져올수있는데요

        //LayoutInflater.from(context).inflate("레이아웃.xml",null);
        //하면 View 클래스를 리턴해줍니다.
        //------------
        //첫번째 파라미터 position : ListView에 놓여질 목록의 위치.
        //두번째 파라미터 convertview : 리턴될 View로서 List의 한 함목의 View 객체(자세한 건 아래에 소개)
        //세번째 파라미터 parent : 이 Adapter 객체가 설정된 AdapterView객체(이 예제에서는 ListView)

    }
    String TodaySet() {
        Date today = new Date();
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
        return date.format(today);
    }

}
