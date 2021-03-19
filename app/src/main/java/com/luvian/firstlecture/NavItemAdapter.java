package com.luvian.firstlecture;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.luvian.firstlecture.R;

public class NavItemAdapter extends LinearLayout {
    //어디서든 사용할 수 있게하려면
    TextView textView;

    public NavItemAdapter(Context context) {
        super(context);
        init(context);//인플레이션해서 붙여주는 역
    }

    public NavItemAdapter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    // 지금 만든 객체(xml 레이아웃)를 인플레이션화(메모리 객체화)해서 붙여줌
    // LayoutInflater를 써서 시스템 서비스를 참조할 수 있음
    // 단말이 켜졌을 때 기본적으로 백그라운드에서 실행시키는 것을 시스템 서비스라고 함
    private void init(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.nav_list,this, true);

        textView = findViewById(R.id.textView);
    }

    public void setName(String name){
        textView.setText(name);
    }

}
