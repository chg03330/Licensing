
package com.example.licensingtest;


import android.util.Log;

import java.io.Serializable;

public class list_example implements Comparable<list_example>, Serializable {

    private String jmName;
    private String jmCode;
    private String seriCode;
    private int likecheck;

    public list_example(String jmCode, String jmName, String seriCode, int likecheck) {
        this.jmCode = jmCode;
        this.jmName = jmName;
        this.seriCode = seriCode;
        this.likecheck = likecheck;
    }

    public String getJmCode() {
        return this.jmCode;
    }

    public String getJmName() {
        return this.jmName;
    }

    public String getSeriCode() {
        return this.seriCode;
    }

    public int getLikeCheck() {
        return this.likecheck;
    }

    public void setLikeCheck(int a) {
        this.likecheck = a;
    }

    //정렬을 위한 정렬함수
    public int compareTo(list_example _list) {
        String this_a = this.seriCode.substring(this.seriCode.indexOf("/") + 1, this.seriCode.indexOf("/") + 11);
        String list_a = _list.seriCode.substring(_list.seriCode.indexOf("/") + 1, _list.seriCode.indexOf("/") + 11);

        this_a = this_a.replace(".", "").replace(" ", "");
        list_a = list_a.replace(".", "").replace(" ", "");

        if (Integer.valueOf(this_a) < Integer.valueOf(list_a)) {

            return -1; //이렇게 하면 오름 차순

        } else if (Integer.valueOf(this_a).equals(Integer.valueOf(list_a))) {
            return 0;
        } else {
            return 1; //이렇게 하면 오름 차순
        }
    }

}