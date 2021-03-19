package com.luvian.firstlecture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.luvian.firstlecture.R;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.PI;

public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback, ItemAdapter.onItemListener {

    private static String apiKey = "apiKey";
    private TMapGpsManager tmapGps = null;
    private boolean trackMode = true;
    private TMapView tMapView = null;
    LinearLayout linearLayoutTmap;

    private ItemAdapter adapter;
    private List<ItemModel> itemList;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private Button gpsButton, comButton, mapButton, dismapButton;
    private boolean gpsPress, comPress, mapPress = false;

    //뒤로가기버튼시간
    private long time = 0;

    private HashMap<String, TMapPoint> locationPoint;
    private TMapMarkerItem goal;

    private DrawerLayout drawerLayout;
    private View drawerView;
    private View drawerViewR;

    private String[] spinArr;
    private boolean isgoal = false;
    ImageView imgV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /***
         네비게이션, 스피너 작업중
         ***/
        spinArr = getResources().getStringArray(R.array.minju);
        Spinner spinner = findViewById(R.id.floorSpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, spinArr);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        imgV = findViewById(R.id.floorImageView);
        imgV.setImageResource(R.drawable.minju_1f_107);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerView = (View) findViewById(R.id.drawerView);
        drawerLayout.setDrawerListener(listener);

        drawerViewR = (View) findViewById(R.id.drawerViewRight);


        /****************************************************
         init 함수
         ***************************************************/
        setUpTMapView();
        setUpRecyclerView();
        setUpSearchView();
        setUpButton();
        fillPoint();
        setUpNavAdapter();

        setTheme(R.style.AppTheme);

    }

    private void setUpNavAdapter(){
        ListView listView = findViewById(R.id.navListView);
        final SingerAdapter adapter = new SingerAdapter();
        adapter.addItem(new NavItem("민주관"));
        adapter.addItem(new NavItem("창조관"));
        adapter.addItem(new NavItem("예술관"));
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), adapter.getItem(position)+"", Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "준비중입니다", Toast.LENGTH_LONG).show();
            }
        });
    }

    class SingerAdapter extends BaseAdapter {
        //데이터가 들어가있지 않고, 어떻게 담을지만 정의해뒀다.
        ArrayList<NavItem> items = new ArrayList<NavItem>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(NavItem item){
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        // 어댑터가 데이터를 관리하고 뷰도 만듬
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NavItemAdapter navItem = null;
            // 코드를 재사용할 수 있도록
            if(convertView == null) {
                navItem = new NavItemAdapter(getApplicationContext());
            } else {
                navItem = (NavItemAdapter)convertView;
            }
            NavItem item = items.get(position);
            navItem.setName(item.getName());
            return navItem;
        }
    }

    /****************************************************
     버튼설정
     ***************************************************/
    private void setUpButton() {
        gpsButton = (Button) findViewById(R.id.gpsButton);
        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gpsPress) {
                    tMapView.setCenterPoint(tmapGps.getLocation().getLongitude(), tmapGps.getLocation().getLatitude());
                    gpsButton.setBackground(getResources().getDrawable(R.drawable.bg_gpsbtn_pressed));
                    gpsPress = true;
                } else {


                }
            }
        });

        comButton = (Button) findViewById(R.id.comButton);
        comButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!comPress) {
                    tMapView.setCompassMode(true);
                    tMapView.setSightVisible(true);
                    comButton.setBackground(getResources().getDrawable(R.drawable.bg_compbtn_pressed));
                    comPress = true;
                } else {
                    tMapView.setCompassMode(false);
                    tMapView.setSightVisible(false);
                    comButton.setBackground(getResources().getDrawable(R.drawable.bg_compbtn));
                    comPress = false;
                }

            }
        });

        mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerViewR);
            }
        });

        dismapButton = (Button)findViewById(R.id.dismapButton);
        dismapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerView);
            }
        });

    }

    /****************************************************
     버튼숨기기
     ***************************************************/
    private void buttonHide(boolean h) {
        if (h) {
            gpsButton.setVisibility(View.INVISIBLE);
            comButton.setVisibility(View.INVISIBLE);
            dismapButton.setVisibility(View.INVISIBLE);
        } else {
            gpsButton.setVisibility(View.VISIBLE);
            comButton.setVisibility(View.VISIBLE);
            dismapButton.setVisibility(View.VISIBLE);

        }
    }

    /****************************************************
     네비게이션드로워
     ***************************************************/
    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
        }

        @Override
        public void onDrawerStateChanged(int newState) {
        }
    };



    /****************************************************
     gps
     ***************************************************/
    @Override
    public void onLocationChange(Location location) {
        if (trackMode) {
            tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        }
        if(isgoal){

            double radius = 40;
            double dis = GetDistanceBetweenPoints(location.getLongitude(), location.getLatitude(), goal.getTMapPoint().getLongitude(),goal.getTMapPoint().getLatitude());
            //Toast.makeText(this, dis+"", Toast.LENGTH_SHORT).show();
            if(dis < radius){
                drawerLayout.openDrawer(drawerView);
            }

        }
    }

    private double GetDistanceBetweenPoints(double lat1, double lon1, double lat2, double lon2)
    {
        double theta, dist;
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            theta = lon1 - lon2;
            dist = sin(ConvertDecimalDegreesToRadians(lat1)) * sin(ConvertDecimalDegreesToRadians(lat2)) +
                    cos(ConvertDecimalDegreesToRadians(lat1)) * cos(ConvertDecimalDegreesToRadians(lat2)) *
                            cos(ConvertDecimalDegreesToRadians(theta));
            dist = acos(dist);
            dist = ConvertRadiansToDecimalDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344 * 1000;  // 미터 단위로 변환
            return dist;
        }
    }

    private double ConvertDecimalDegreesToRadians(double deg)
    {
        return (deg * PI / 180);
    }
    private double ConvertRadiansToDecimalDegrees(double rad)
    {
        return (rad * 180 / PI);
    }

    /****************************************************
     Tmap init
     ***************************************************/
    private void setUpTMapView() {
        linearLayoutTmap = (LinearLayout) findViewById(R.id.linearLayoutTmap);
        tMapView = new TMapView(this);

        tMapView.setSKTMapApiKey(apiKey);
        linearLayoutTmap.addView(tMapView);


        tMapView.setCompassMode(false);
        tMapView.setIconVisibility(true);

        tMapView.setZoomLevel(15);
        tMapView.setMapType(tMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(tMapView.LANGUAGE_KOREAN);

        tmapGps = new TMapGpsManager(MainActivity.this);
        tmapGps.setMinTime(1000);
        tmapGps.setMinDistance(5);
        //tmapGps.setProvider(tmapGps.NETWORK_PROVIDER);
        tmapGps.setProvider(tmapGps.GPS_PROVIDER);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); //위치권한 탐색 허용 관련 내용
            }
            return;
        }

        tmapGps.OpenGps();

        tMapView.setTrackingMode(true);
        tMapView.setSightVisible(false);

        tMapView.setCenterPoint(37.3735, 127.92874);

        tMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                Log.e("tmap", "click");
                if (tMapView.getCenterPoint() != tmapGps.getLocation()) {
                    gpsPress = false;
                    gpsButton.setBackground(getResources().getDrawable(R.drawable.bg_gpsbtn));
                }
                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                return false;
            }
        });
    }

    /****************************************************
     리사이클러뷰 init
     ***************************************************/
    private void setUpRecyclerView() {
        //recyclerview
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        //adapter
        itemList = new ArrayList<>(); //샘플테이터
        fillData();
        adapter = new ItemAdapter(itemList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL); //밑줄
        recyclerView.addItemDecoration(dividerItemDecoration);

        //데이터셋변경시
        //adapter.dataSetChanged(exampleList);

        //어댑터의 리스너 호출
        adapter.setOnClickListener(this);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    /****************************************************
     강의실위치 추가
     ***************************************************/
    private void fillData() {
        itemList = new ArrayList<>(); //샘플테이터
        itemList.add(new ItemModel(R.drawable.pings, "민주관", "104"));
        itemList.add(new ItemModel(R.drawable.pings, "민주관", "105"));
        itemList.add(new ItemModel(R.drawable.pings, "민주관", "106"));
        itemList.add(new ItemModel(R.drawable.pings, "민주관", "107"));
        itemList.add(new ItemModel(R.drawable.pings, "민주관", "108"));
        itemList.add(new ItemModel(R.drawable.pings, "민주관", "109"));
        itemList.add(new ItemModel(R.drawable.pings, "민주관", "110"));

        itemList.add(new ItemModel(R.drawable.pings, "민주관", "203"));
        itemList.add(new ItemModel(R.drawable.pings, "민주관", "204"));
        itemList.add(new ItemModel(R.drawable.pings, "민주관", "205"));
        itemList.add(new ItemModel(R.drawable.pings, "민주관", "206"));

        itemList.add(new ItemModel(R.drawable.pings, "예술관", "101"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "102"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "103"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "104"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "105"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "106"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "107"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "108"));

        itemList.add(new ItemModel(R.drawable.pings, "예술관", "201"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "202"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "203"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "204"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "205"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "206"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "207"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "208"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "209"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "210"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "211"));

        itemList.add(new ItemModel(R.drawable.pings, "예술관", "301"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "302"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "303"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "304"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "305"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "306"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "307"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "308"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "309"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "310"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "311"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "312"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "313"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "314"));

        itemList.add(new ItemModel(R.drawable.pings, "예술관", "401"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "402"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "403"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "404"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "405"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "406"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "407"));
        itemList.add(new ItemModel(R.drawable.pings, "예술관", "408"));

        itemList.add(new ItemModel(R.drawable.pings, "창조관", "식당"));
    }

    /****************************************************
     지도 위도경도값 추가
     ***************************************************/
    private void fillPoint() {
        //locationPoint.get(String).getKatechLat()
        //locationPoint.get(String).getKatechLon()
        locationPoint = new HashMap<String, TMapPoint>();
        locationPoint.put("예술관", new TMapPoint(37.37174, 127.92905));
        locationPoint.put("민주관", new TMapPoint(37.37162, 127.92831));
        locationPoint.put("창조관", new TMapPoint(37.36888, 127.93074));

    }

    /****************************************************
     리사이클러뷰 클릭이벤트
     ***************************************************/
    @Override
    public void onItemClicked(ItemModel model) {
        //Toast.makeText(this, "" + model.getText1(), Toast.LENGTH_SHORT).show();
        recyclerView.setVisibility(View.INVISIBLE);
        searchView.setQuery("", false);
        searchView.clearFocus();
        buttonHide(false);
        //마커그리고 경로표시
        goal = new TMapMarkerItem();

        // 마커 아이콘
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.markerp);
        bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);

        goal.setIcon(bitmap); // 마커 아이콘 지정
        goal.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
        goal.setTMapPoint(locationPoint.get(model.getText1())); // 마커의 좌표 지정
        goal.setName("goal"); // 마커의 타이틀 지정
        isgoal = true;
        tMapView.addMarkerItem(model.getText1(), goal); // 지도에 마커 추가

        FindCarPathTask task = new FindCarPathTask(getApplicationContext(), tMapView);
        task.execute(tmapGps.getLocation(), locationPoint.get(model.getText1()));

        //imgV.setImageResource(R.drawable.minju_1f_107);
        String txt;
        if(model.getText1().equals("민주관")){
            txt = "minju";
        }else if(model.getText1().equals("창조관")){
            txt = "changjo";
        }else if(model.getText1().equals("예술관")){
            txt = "art";
        }else{
            txt="";
        }



        txt += "_";
        
        String txt2 = model.getText2().substring(0,1);



        if(txt2.equals("1")){
            txt+="1f_";
            txt+=model.getText2();
        }else if(txt2.equals("2")){
            txt+="2f_";
            txt+=model.getText2();
        }
        else if(txt2.equals("3")){
            txt+="3f_";
            txt+=model.getText2();
        }
        else if(txt2.equals("4")){
            txt+="4f_";
            txt+=model.getText2();
        }else if(txt2.equals("5")){
            txt+="5f_";
            txt+=model.getText2();
        }else{
            txt+="1f_kitchen";
        }






        imgV.setImageResource(this.getResources().getIdentifier(txt, "drawable", this.getPackageName()));

        dismapButton.setVisibility(View.VISIBLE);


    }

    /****************************************************
     서치뷰 init
     ***************************************************/
    private void setUpSearchView() {
        searchView = (SearchView) findViewById(R.id.searchView);
        //서치뷰 필터
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    buttonHide(true);
                    recyclerView.setVisibility(View.VISIBLE);
                }

            }
        });
    }


    /****************************************************
     뒤로가기 이벤트
     ***************************************************/
    @Override
    public void onBackPressed() {

        if (recyclerView.getVisibility() == View.VISIBLE) {
            recyclerView.setVisibility(View.INVISIBLE);
            searchView.setQuery("", false);
            searchView.clearFocus();
            buttonHide(false);
        } else if (drawerLayout.isDrawerOpen(drawerView)) {
            drawerLayout.closeDrawer(drawerView);
        } else if(drawerLayout.isDrawerOpen(drawerViewR)){
                drawerLayout.closeDrawer(drawerViewR);
        } else if (System.currentTimeMillis() - time >= 2000) {
            tMapView.removeAllMarkerItem();
            tMapView.removeAllTMapPolyLine();
            isgoal=false;
            dismapButton.setVisibility(View.INVISIBLE);
            time = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "한번더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() - time < 2000) {

            finish();
        }
    }


}


