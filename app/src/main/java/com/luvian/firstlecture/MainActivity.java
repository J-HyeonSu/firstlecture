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

    //????????????????????????
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
         ???????????????, ????????? ?????????
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
         init ??????
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
        adapter.addItem(new NavItem("?????????"));
        adapter.addItem(new NavItem("?????????"));
        adapter.addItem(new NavItem("?????????"));
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), adapter.getItem(position)+"", Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_LONG).show();
            }
        });
    }

    class SingerAdapter extends BaseAdapter {
        //???????????? ??????????????? ??????, ????????? ???????????? ???????????????.
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

        // ???????????? ???????????? ???????????? ?????? ??????
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NavItemAdapter navItem = null;
            // ????????? ???????????? ??? ?????????
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
     ????????????
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
     ???????????????
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
     ????????????????????????
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
            dist = dist * 1.609344 * 1000;  // ?????? ????????? ??????
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
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); //???????????? ?????? ?????? ?????? ??????
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
     ?????????????????? init
     ***************************************************/
    private void setUpRecyclerView() {
        //recyclerview
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        //adapter
        itemList = new ArrayList<>(); //???????????????
        fillData();
        adapter = new ItemAdapter(itemList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL); //??????
        recyclerView.addItemDecoration(dividerItemDecoration);

        //?????????????????????
        //adapter.dataSetChanged(exampleList);

        //???????????? ????????? ??????
        adapter.setOnClickListener(this);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    /****************************************************
     ??????????????? ??????
     ***************************************************/
    private void fillData() {
        itemList = new ArrayList<>(); //???????????????
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "104"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "105"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "106"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "107"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "108"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "109"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "110"));

        itemList.add(new ItemModel(R.drawable.pings, "?????????", "203"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "204"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "205"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "206"));

        itemList.add(new ItemModel(R.drawable.pings, "?????????", "101"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "102"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "103"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "104"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "105"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "106"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "107"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "108"));

        itemList.add(new ItemModel(R.drawable.pings, "?????????", "201"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "202"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "203"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "204"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "205"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "206"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "207"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "208"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "209"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "210"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "211"));

        itemList.add(new ItemModel(R.drawable.pings, "?????????", "301"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "302"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "303"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "304"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "305"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "306"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "307"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "308"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "309"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "310"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "311"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "312"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "313"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "314"));

        itemList.add(new ItemModel(R.drawable.pings, "?????????", "401"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "402"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "403"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "404"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "405"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "406"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "407"));
        itemList.add(new ItemModel(R.drawable.pings, "?????????", "408"));

        itemList.add(new ItemModel(R.drawable.pings, "?????????", "??????"));
    }

    /****************************************************
     ?????? ??????????????? ??????
     ***************************************************/
    private void fillPoint() {
        //locationPoint.get(String).getKatechLat()
        //locationPoint.get(String).getKatechLon()
        locationPoint = new HashMap<String, TMapPoint>();
        locationPoint.put("?????????", new TMapPoint(37.37174, 127.92905));
        locationPoint.put("?????????", new TMapPoint(37.37162, 127.92831));
        locationPoint.put("?????????", new TMapPoint(37.36888, 127.93074));

    }

    /****************************************************
     ?????????????????? ???????????????
     ***************************************************/
    @Override
    public void onItemClicked(ItemModel model) {
        //Toast.makeText(this, "" + model.getText1(), Toast.LENGTH_SHORT).show();
        recyclerView.setVisibility(View.INVISIBLE);
        searchView.setQuery("", false);
        searchView.clearFocus();
        buttonHide(false);
        //??????????????? ????????????
        goal = new TMapMarkerItem();

        // ?????? ?????????
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.markerp);
        bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);

        goal.setIcon(bitmap); // ?????? ????????? ??????
        goal.setPosition(0.5f, 1.0f); // ????????? ???????????? ??????, ???????????? ??????
        goal.setTMapPoint(locationPoint.get(model.getText1())); // ????????? ?????? ??????
        goal.setName("goal"); // ????????? ????????? ??????
        isgoal = true;
        tMapView.addMarkerItem(model.getText1(), goal); // ????????? ?????? ??????

        FindCarPathTask task = new FindCarPathTask(getApplicationContext(), tMapView);
        task.execute(tmapGps.getLocation(), locationPoint.get(model.getText1()));

        //imgV.setImageResource(R.drawable.minju_1f_107);
        String txt;
        if(model.getText1().equals("?????????")){
            txt = "minju";
        }else if(model.getText1().equals("?????????")){
            txt = "changjo";
        }else if(model.getText1().equals("?????????")){
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
     ????????? init
     ***************************************************/
    private void setUpSearchView() {
        searchView = (SearchView) findViewById(R.id.searchView);
        //????????? ??????
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
     ???????????? ?????????
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
            Toast.makeText(getApplicationContext(), "????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() - time < 2000) {

            finish();
        }
    }


}


