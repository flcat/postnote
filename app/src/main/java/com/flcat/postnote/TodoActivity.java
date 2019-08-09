package com.flcat.postnote;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.flcat.postnote.R.layout.activity_main;

public class TodoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,AbsListView.OnScrollListener {
    FirebaseAuth mFirebaseAuth;
    public static FirebaseUser mFirebaseUser;
    DrawerLayout drawer = null;
    public static Context mContext;
    String num;
    public static String notelist;
    final String TAG = TodoActivity.class.getName();
    TextView nick;
    String date_str;
    String userEmail;
    String userPhoto;
    String stUid;
    public static Bitmap bitmap;
    private ListView listview;
    private boolean lastItemVisibleFlag = false; // 리스트 스크롤이 마지막 아이템으로 이동했는지 체크하는 변수

    private int page = 0; // 페이징 변수
    private int count = 0;
    private int currentPage = 1;
    private int previousTotal = 0;
    private int totPageCount = 0;
    private boolean loading = true;

    private ProgressBar progressBar;
    private boolean mLockListView = false; // 데이터 로드시 중복체크하는 변수
    private CustomAdapter adapter;
    private List<ListViewItem> items_current;
    //종료버튼 backpress button
    private final long FINISH_INTERVAL_TIME = 2000;
    private long   backPressedTime = 0;

    //팹버튼
    boolean fab_flag = false;
    FloatingActionButton fab = null;
    FloatingActionButton fab1 = null;
    FloatingActionButton fab2 = null;
    FloatingActionButton fab3 = null;

    FrameLayout.LayoutParams layoutParams;
    FrameLayout.LayoutParams layoutParams2;
    FrameLayout.LayoutParams layoutParams3;

    //팹버튼 애니메이션
    Animation show_fab_1 = null;
    Animation hide_fab_1 = null;
    Animation show_fab_2 = null;
    Animation hide_fab_2 = null;
    Animation show_fab_3 = null;
    Animation hide_fab_3 = null;

    //프래그먼트
    FragmentTransaction ft;
    Fragment fragment;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activity_main);
        mContext = this;
        mFirebaseAuth = LoginActivity.mAuth;
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            // Fixme : 로그인이 필요합니다.
            //Toast.makeText(this, "직접 가입 사용자",Toast.LENGTH_SHORT).show();
        } else {
            //mUsername = mFirebaseUser.getDisplayName();
            /*
            if(mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
            */
        }
        Intent intent = getIntent();
        notelist = intent.getStringExtra("noteList");
        userEmail = intent.getStringExtra("email");
        userPhoto = intent.getStringExtra("userphoto");
        stUid = intent.getStringExtra("key");


        SharedPreferences sharedPreferences = getSharedPreferences("email",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("uid",mFirebaseUser.getUid());
        editor.putString("email",userEmail);
        editor.putString("userphoto",userPhoto);
        editor.apply();

        Log.e("TodoActivity", "온 크리에이트");
        listview = (ListView)findViewById(R.id.list11);
        items_current = new ArrayList<ListViewItem>();

        /*
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            */
                adapter = new CustomAdapter(TodoActivity.this,items_current,TodoActivity.this);
                listview.setAdapter(adapter);
                listview.setOnScrollListener(this);
                getitem();
                //progressBar.setVisibility(View.GONE);
            //}
        //});

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ListViewItem item = (ListViewItem) adapter.getItem(position);

                //select position 확인
                //Toast.makeText(getApplicationContext(), "Item #" + item + " 클릭됨.", Toast.LENGTH_SHORT).show();
                num = item.getNum();
                String title = item.getTitle();
                String content = item.getContent();
                String mUri = item.getmUri();
                String mThumbUri = item.getmThumbUri();
                String lat = item.getLat();
                String lng = item.getLng();

                Log.e("선택된아이템", content);

                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);

                intent.putExtra("mode", "modify");
                intent.putExtra("num",num);
                intent.putExtra("email",userEmail);
                intent.putExtra("title", title);
                intent.putExtra("content", content);
                intent.putExtra("mUri",mUri);
                intent.putExtra("mThumbUri",mThumbUri);
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);

                Log.e("Todo_mUri_ur_value",mUri);

                startActivityForResult(intent, 1001);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //햄버거
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.addDrawerListener(toggle);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) { }

            public void onDrawerOpened(View drawerView) {
                //Util.hideKeyboard(MainActivity.et_search); // 혹시 키보드가 열려 있으면 닫는다.
            }

            @Override
            public void onDrawerClosed(View drawerView) { }

            @Override
            public void onDrawerStateChanged(int newState) { }
        });
        toggle.syncState();

        //네비게이션 뷰
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navi_header_view = navigationView.getHeaderView(0);

        //유저 사진
        final ImageView navi_header_userPhoto = (ImageView)navi_header_view.findViewById(R.id.nav_header_userphoto);

        // Write a message to the database
        // Read from the database
        Thread mThread = new Thread() {
            @Override
            public void run() {
                if(userPhoto != null && userPhoto != "") {
                    try {
                        URL url = new URL(userPhoto);

                        //웹에서 이미지 가져온뒤 imageView에 지정할 비트맵을 만든다
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.connect();

                        InputStream is = conn.getInputStream();
                        bitmap = BitmapFactory.decodeStream(is);

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        mThread.start();

        try {
            // 메인 Thread는 별도의 작업 Thread가 작업을 완료할 때까지 대기해야한다.
            // Join()을 호출하여 별도의 작업 Thread가 종료될때까지 메인 Thread가 기다리게 함
            mThread.join();

            //작업 Thread에서 이미지를 불러오는 작업을 완료한 뒤
            // UI 작업을 할 수 있는 메인 Thread에서 이미지뷰에 이미지를 지정한다.
            navi_header_userPhoto.setImageBitmap(bitmap);
        } catch (InterruptedException e) {
            Log.e("구글 유저 이미지","에러");
            e.printStackTrace();
        }

        //햄버거버튼 userphoto clicklistener
        navi_header_userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        TextView navi_header_id_text = (TextView)navi_header_view.findViewById(R.id.nav_header_email_textview);
        navi_header_id_text.setText(userEmail);


        fab = (FloatingActionButton) findViewById(R.id.fab);


            fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    //세부 팹버튼 정의
                    fab1 = (FloatingActionButton) findViewById(R.id.fab_1);
                    fab2 = (FloatingActionButton) findViewById(R.id.fab_2);
                    fab3 = (FloatingActionButton) findViewById(R.id.fab_3);

                    layoutParams = (FrameLayout.LayoutParams) fab1.getLayoutParams();
                    layoutParams2 = (FrameLayout.LayoutParams) fab2.getLayoutParams();
                    layoutParams3 = (FrameLayout.LayoutParams) fab3.getLayoutParams();
                    //팹버튼 애니메이션
                    show_fab_1 = AnimationUtils.loadAnimation(getApplication(), R.anim.fab1_show);
                    hide_fab_1 = AnimationUtils.loadAnimation(getApplication(), R.anim.fab1_hide);
                    show_fab_2 = AnimationUtils.loadAnimation(getApplication(), R.anim.fab2_show);
                    hide_fab_2 = AnimationUtils.loadAnimation(getApplication(), R.anim.fab2_hide);
                    show_fab_3 = AnimationUtils.loadAnimation(getApplication(), R.anim.fab3_show);
                    hide_fab_3 = AnimationUtils.loadAnimation(getApplication(), R.anim.fab3_hide);
            if(fab_flag == false){
                    showfabWH();
                } else {
                    hidefabWH();
                }

                //글쓰기
                fab1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TodoActivity.this, WriteActivity.class);
                        intent.putExtra("email", userEmail);
                        startActivityForResult(intent,6001);
                        //작업 후 팹버튼 최소화
                        hidefabWH();
                    }
                });
                //다이얼로그 캘린더
                fab2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TodoActivity.this);

                        alertDialogBuilder.setTitle("달력");
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        Context context = new ContextThemeWrapper(TodoActivity.this, android.R.style.Theme_Material_Dialog);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            // API 24 이상일 경우 시스템 기본 테마 사용
                            context = TodoActivity.this;
                        }
                        DatePickerDialog datePickerDialog = new DatePickerDialog(context, dateSetListener, year, month, day);
                        datePickerDialog.show();
                        //작업 후 팹버튼 최소화
                        hidefabWH();
                    }
                });
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                //구글 맵 클러스터링
                fab3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TodoActivity.this, MapsClusterActivity.class);
                    startActivityForResult(intent,7001);
                    hidefabWH();
                }
            });
            }
        });
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Toast.makeText(TodoActivity.this, year + "-" + monthOfYear + "-" + dayOfMonth, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onBackPressed() {

        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;
        if(intervalTime >= 0 && FINISH_INTERVAL_TIME >= intervalTime) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setTitle("앱 종료");

            alertDialogBuilder.setMessage("앱을 종료하시겠습니까?")
                    .setCancelable(false)
                    .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TodoActivity.super.onBackPressed();
                            moveTaskToBack(true);
                            TodoActivity.this.finish();
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(0);
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();

    }
        else {
            backPressedTime = tempTime;

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if( adapter.delete_flag == false) {
                adapter.delete_flag = true;
                adapter.notifyDataSetChanged();
            } else if ( adapter.delete_flag == true) {
                adapter.delete_flag = false;
                adapter.notifyDataSetChanged();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        ft = getSupportFragmentManager().beginTransaction();
        fragment = new SimpleFragment();
        ft.hide(fragment);
        int id = item.getItemId();
        Bundle bundle = new Bundle();
        bundle.putString("userphoto",userPhoto);

        switch (id){
            case R.id.nav_all_memo:
                ft.hide(fragment);
                fab.setVisibility(View.VISIBLE);
                break;

            case R.id.nav_book:
                break;

            case R.id.nav_friends:
                fragment = new FriendsFragment();
                fab.setVisibility(View.GONE);
                if(fab_flag == true) {
                    hidefabWH();
                }
                break;
            case R.id.nav_manage:
                fragment = new UserInfoFragment();
                fragment.setArguments(bundle);
                break;
            /*
            case R.id.nav_send:
                fragment = new ChatFragment();
                fab.setVisibility(View.GONE);
                if(fab_flag == true) {
                    hidefabWH();
                }
                break;
            //단체 채팅
            */
            case R.id.logout_btn:
                //finish();
                new AlertDialog.Builder(this)
                        .setTitle("로그아웃").setMessage("로그아웃 하시겠습니까?")
                        .setPositiveButton("로그아웃", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                LoginActivity.user = null;
                                mFirebaseUser = null;
                                mFirebaseAuth.signOut();
                                Intent i = new Intent(TodoActivity.this, LoginActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(i);
                                Toast.makeText(mContext, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })
                        .show();
                break;
        }

        if (fragment != null & id != R.id.logout_btn) {
            ft.replace(R.id.content_fragment_layout, fragment);
            ft.commit();
        }

        //drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // 1. OnScrollListener.SCROLL_STATE_IDLE : 스크롤이 이동하지 않을때의 이벤트(즉 스크롤이 멈추었을때).
        // 2. lastItemVisibleFlag : 리스트뷰의 마지막 셀의 끝에 스크롤이 이동했을때.
        // 3. mLockListView == false : 데이터 리스트에 다음 데이터를 불러오는 작업이 끝났을때.
        // 1, 2, 3 모두가 true일때 다음 데이터를 불러온다.
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag && mLockListView == false) {
            // 화면이 바닦에 닿을때 처리
            // 로딩중을 알리는 프로그레스바를 보인다.
            //progressBar.setVisibility(View.VISIBLE);
            Log.e("스크롤되고있다","스크롤");
            // 다음 데이터를 불러온다.
            getitem();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // firstVisibleItem : 화면에 보이는 첫번째 리스트의 아이템 번호.
        // visibleItemCount : 화면에 보이는 리스트 아이템의 갯수
        // totalItemCount : 리스트 전체의 총 갯수
        /*
        // 리스트의 갯수가 0개 이상이고, 화면에 보이는 맨 하단까지의 아이템 갯수가 총 갯수보다 크거나 같을때.. 즉 리스트의 끝일때. true
        lastItemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
        int lastItemVisiblePosition = view.getLastVisiblePosition();
        */
        Log.e("position", "--firstItem:" + firstVisibleItem + "  visibleItemCount:" + visibleItemCount + "  totalItemCount:" + totalItemCount + "  pageCount:" + totPageCount);
        int total = firstVisibleItem + visibleItemCount;

        // totalItemCount : 페이지 총 아이템 갯수
        //firstItem : 리스트 첫번째 아이템 posion
        // visibleItemCount : 화면에 보이는 아이템 갯수

        if (loading) { //
            Log.e("position", "totalItemCount :" + totalItemCount + "     previousTotal :" + previousTotal);
            if (totalItemCount > previousTotal) {  // 20 , 40 >
                loading = false;
                previousTotal = totalItemCount;
                //currentPage++;
            }
        }
        if (totPageCount > 0) {
            Log.e("position", "total%20 :" + total % 20 + "        loading=" + loading);
            if (totPageCount == currentPage) {
                //Toast.makeText(MyInfoAddressSearchResultActivity.this, "마지막 페이지 입니다.",Toast.LENGTH_SHORT).show();
            } else if (!loading && (total) % 20 == 0 && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleItemCount)) {  //페이지당 20개씩 노출함으로 20으로 나누고 나머지값이 0으로 떨어지면 다음 페이지 호출
                currentPage = currentPage + 1;
                loading = true;
            }
        }
    }

    private void getitem(){
        // 리스트에 다음 데이터를 입력할 동안에 이 메소드가 또 호출되지 않도록 mLockListView 를 true로 설정한다.
        mLockListView = true;
        // 다음 20개의 데이터를 불러와서 리스트에 저장한다.
        try{

            JSONObject jsonObject = new JSONObject(notelist);
            final JSONArray jsonArray = jsonObject.getJSONArray("response");

            String email,title, content, mUri, mThumbUri, date, lat, lng;

            //아이템 갯수 결정
            for (count = 0; count < jsonArray.length() ;count++) {
            //while(count < jsonArray.length()){

                    JSONObject object = jsonArray.getJSONObject(count);
                    email = object.getString("email");
                    title = object.getString("title");
                    content = object.getString("content");
                    mUri = object.getString("mUri");
                    mThumbUri = object.getString("mThumbUri");
                    date = object.getString("date");
                    num = object.getString("num");
                    lat = object.getString("lat");
                    lng = object.getString("lng");

                    ListViewItem item = new ListViewItem(email, title, content, mUri, mThumbUri, date, num, lat, lng);
                    items_current.add(item);

            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    page++;
                    adapter.notifyDataSetChanged();
                    //progressBar.setVisibility(View.GONE);
                    mLockListView = false;
                }
            },1000);
        } catch (Exception e){
            e.printStackTrace();
        }

    }


    class BackgroundTask extends AsyncTask<Void, Void, String>
    {
        String target;

        @Override
        protected void onPreExecute() {
            target = "http://flcat.vps.phps.kr/Detail.php";
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                URL url = new URL(target);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); //하나씩 읽어옴
                String temp;
                StringBuilder stringBuilder = new StringBuilder();
                while ((temp = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(temp + "\n");

                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            Intent intent = new Intent(TodoActivity.this, DetailActivity.class);
            intent.putExtra("Detail",result);
            TodoActivity.this.startActivity(intent);
        }
    }
    public void superadditem(ListViewItem item){
        this.adapter.addItem(item);
        this.adapter.notifyDataSetChanged();
    }
    //팹버튼 보이기시 위치 설정
    public void showfabWH(){
        //팹버튼 나타내기 위치설정
        layoutParams.rightMargin += (int) (fab1.getWidth() * 1.7);
        //layoutParams.bottomMargin += (int) (fab1.getHeight() * 0.25);

        layoutParams2.rightMargin += (int) (fab2.getWidth() * 1.13);
        layoutParams2.bottomMargin += (int) (fab2.getHeight() * 1.13);

        //layoutParams3.rightMargin += (int) (fab3.getWidth() * 0.25);
        layoutParams3.bottomMargin += (int) (fab3.getHeight() * 1.7);
        fab1.setLayoutParams(layoutParams);
        fab1.startAnimation(show_fab_1);
        fab1.setClickable(true);

        fab2.setLayoutParams(layoutParams2);
        fab2.startAnimation(show_fab_2);
        fab2.setClickable(true);

        fab3.setLayoutParams(layoutParams3);
        fab3.startAnimation(show_fab_3);
        fab3.setClickable(true);

        fab_flag = true;
    }
    //팹버튼 숨기기시 위치 설정
    public void hidefabWH(){
        //팹버튼 숨기기 위치설정
        layoutParams.rightMargin -= (int) (fab1.getWidth() * 1.7);
        //layoutParams.bottomMargin -= (int) (fab1.getHeight() * 0.25);

        layoutParams2.rightMargin -= (int) (fab2.getWidth() * 1.13);
        layoutParams2.bottomMargin -= (int) (fab2.getHeight() * 1.13);

        //layoutParams3.rightMargin -= (int) (fab3.getWidth() * 0.25);
        layoutParams3.bottomMargin -= (int) (fab3.getHeight() * 1.7);

        fab1.setLayoutParams(layoutParams);
        fab1.startAnimation(hide_fab_1);
        fab1.setClickable(false);

        fab2.setLayoutParams(layoutParams2);
        fab2.startAnimation(hide_fab_2);
        fab2.setClickable(false);

        fab3.setLayoutParams(layoutParams3);
        fab3.startAnimation(hide_fab_3);
        fab3.setClickable(false);
        fab_flag = false;
    }
}
