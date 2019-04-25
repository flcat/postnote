package com.flcat.postnote;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpdateActivity extends Activity {
    private int TAKE_CAMERA = 1; //앱에서 카메라 호출시 반환해주는 값
    private int TAKE_GALLERY = 2; //앱에서 갤러리 호출시 반환해주는 값
    private Uri mImageCaptureUri; //로컬 이미지 Uri 주소 오프라인에서만 유효하다.
    private String selectedPath;
    private Uri returnImg;
    private Bitmap bm;
    private EditText et1; // 제목 에딧
    private EditText et2; // 본문 에딧
    private ImageView iv; //글쓰기 액티비티에서 이미지 출력
    private String imagePath;
    private TextView addressTextview; //위도 경도를 지오코드로 변환한 주소 표시
    String spTmp;
    private static final int MY_PERMISSION_REQUEST_STORAGE = 3;
    private double dlat,dlng; //MapsActivity에서 받아온 위도 lat 경도 lng 를 담을 변수
    private String slat,slng; //위도 경도 값을 WriteRequest로 보내기위해 형변환한 값을 담는 변수.
    public static int map_flag = 0;
    private String title,content,mUri,mThumbUri,lat,lng;
    private int num; // DB상에서 글 번호를 매기기 위한 변수.
    private Bitmap bitmap;
    // 사용자 위치 수신기
    private LocationManager locationManager;
    private LocationListener locationListener;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    Location currentLocation;
    //최소 gps 정보 업데이트 거리 10미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    int REQUEST_CODE_LOCATION=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("UpdateActivity", "onCreate");
        setContentView(R.layout.activity_update);
        iv = (ImageView) findViewById(R.id.imageView_update);
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        et1 = (EditText) findViewById(R.id.title_update);
        et2 = (EditText) findViewById(R.id.content_update);
        //addressTextview = (TextView)findViewById(R.id.adress_textview);

        //각 계정별로 DB를 관리하기위해 email을 유일한 키값으로 씀
        //로그인 한 이메일 값을 얻어옴
        final Intent intent = getIntent();
        final String email = intent.getStringExtra("email");
        Intent intent2 = getIntent();
                num = new Integer(intent2.getStringExtra("num"));
                title = intent2.getStringExtra("title");
                content = intent2.getStringExtra("content");
                mUri = intent2.getStringExtra("mUri");
                mThumbUri = intent.getStringExtra("mThumbUri");
                lat = intent2.getStringExtra("lat");
                lng = intent2.getStringExtra("lng");
                Log.e("UpdateActivity_좌표",title+"/"+content+"/"+mUri+"/"+mThumbUri+"/"+lat+"/"+lng);
                //

                if((lat != null && lng != null) || (lat != "" && lng != "")) {
                    dlat = Double.parseDouble(lat);
                    dlng = Double.parseDouble(lng);
                }
                //JSON data insert
                et1.setText(title);
                et2.setText(content);
                if(mUri != null && mUri != "" && mUri.length() > 5 ) {
                    Thread mThread = new Thread(){
                        @Override
                        public void run() {
                            try {
                                URL url = new URL(mUri);
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
                    };

                    mThread.start();

                    try{
                        mThread.join();
                        iv.setImageBitmap(bitmap);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if(map_flag == 0) {
            // 사용자의 위치 수신을 위한 세팅 //
            settingGPS();

            // 사용자의 현재 위치 //
            Location userLocation = getMyLocation();

            if( userLocation != null ) {
                // TODO 위치를 처음 얻어왔을 때 하고 싶은 것
                dlat = userLocation.getLatitude();
                dlng = userLocation.getLongitude();
                slat = dlat+"";
                slng = dlng+"";
                intent.putExtra("slat", slat);
                intent.putExtra("slng", slng);
                //MapsActivity 에서 값 제대로 넘겨받았는지 확인
            }
            Log.e("위도경도 잘됨?",slat+"/"+slng);
        }

        //Toast.makeText(getApplicationContext(), email, Toast.LENGTH_SHORT).show();
        et2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        findViewById(R.id.pic_btn_up).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                camera();
            }
        });

        findViewById(R.id.gallery_btn_up).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkGalleryPermission();

            }
        });

        findViewById(R.id.map_btn_up).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateActivity.this,MapsActivity.class);
                startActivityForResult(intent,5001);

            }
        });

        findViewById(R.id.save_btn_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //최대값 구해와야함
                num = Integer.parseInt(intent.getStringExtra("num"));
                String title = et1.getText().toString(); //WriteRequest에 값을 보내기 위해 edittext에 입력된값을 변수에 저장
                String content = et2.getText().toString();
                String mUri = "";
                String mThumbUri = "";

                if (mImageCaptureUri != null) {
                    uploadImage();
                    mUri = getRealPathFromURI(mImageCaptureUri); //mImageCaptureUri에 있는 Uri형 값을 getRealPathFrom Uri를 이용해 String 형태로 변환한뒤 mUri에 저장
                    mThumbUri = getThumbnailPath(mImageCaptureUri.toString());
                }

                //시간을 받아온다 (yyyy/MM/dd) 형태로
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String getTime = sdf.format(date);
                String snum = num+"";

                final ListViewItem tmp = new ListViewItem(email,title,content,mUri,mThumbUri,getTime,snum,slat,slng);

                if (title.length() == 0 && content.length() == 0 ) {
                    Toast toast = Toast.makeText(UpdateActivity.this, "비어있는 항목이 있습니다.", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    final Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");
                                if (success) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);
                                    builder.setMessage("메모 등록에 성공했습니다.")
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ((TodoActivity) TodoActivity.mContext).superadditem(tmp);
                                                    finish();
                                                }
                                            }).create().show();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);
                                    builder.setMessage("메모 등록에 실패했습니다.")
                                            .setNegativeButton("확인", null).create().show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    UpdateRequest updateRequest = new UpdateRequest(num, email, title, content, mUri, mThumbUri, getTime, slat, slng, responseListener);
                    Log.e("볼리",num + "/" + email + "/" + title + "/" + content + "/" + mUri + "/" + mThumbUri + "/" + getTime + "/" + slat + "/" +slng);
                    RequestQueue queue = Volley.newRequestQueue(UpdateActivity.this);
                    queue.add(updateRequest);

                }
                /* //쉐어드 입력중인 데이터 저장
                if ( title.length() == 0 && content.length() == 0 ) {
                    Toast toast = Toast.makeText(EditActivity.this, "내용을 입력해주세요.", Toast.LENGTH_SHORT);
                    toast.show();
                } else {

                    long now = System.currentTimeMillis();
                    Date date = new Date();
                    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd");
                    String strNow = sdfNow.format(date);

                    Intent intent = new Intent();
                    intent.putExtra("title", title);
                    intent.putExtra("content", content);
                    intent.putExtra("date", date);
                    setResult(5, intent);

                    ListViewItem tmp = new ListViewItem();

                    //생성자로 만들어 놓기
                    tmp.setTitle2(title);
                    tmp.setMemo(content);
                    tmp.setDate(strNow);


                    //이미지 없는경우를 위하여 널체크
                    if(mImageCaptureUri != null) {
                        tmp.setImageView(getRealPathFromURI(mImageCaptureUri));
                    }


                    if (TodoList.mContext != null) {
                        ((TodoList) TodoList.mContext).superadditem(tmp);
                    }

                    Toast toast = Toast.makeText(EditActivity.this, "제목 : " + title + "내용 :" + content + "저장 되었습니다.", Toast.LENGTH_SHORT);
                    toast.show();

                    et1.setText(title);
                    et2.setText(content);

                    SharedPreferences sp = getSharedPreferences("pref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.remove("");
                    editor.commit();

                    finish();
                }
               */

            }
        });
    }
    //받을 때 호출되는 콜백(실행한적 없는데 실행되는) 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == Activity.RESULT_OK) { //backpress button
            if (requestCode == TAKE_CAMERA) {
                iv.setImageURI(mImageCaptureUri);
                // Log.e("mImageCaptureUri",mImageCaptureUri.getPath().toString());
                String spResult = spTmp.substring(7, spTmp.length());
                //String tmp = getRealPathFromURI(mImageCaptureUri);
                Log.e("spTmp", spResult);
                data.putExtra("uri", spResult);
            } else if (requestCode == TAKE_GALLERY) {
                Log.d("test", "data : " + data);
                if (data != null) {
                    System.out.println("SELECT_Images");
                    Uri selectedImageUri = data.getData();
                    selectedPath = getRealPathFromURI(selectedImageUri);
                    returnImg = data.getData();
                    if ("com.google.android.apps.photos.contentprovider".equals(returnImg.getAuthority())) {
                        for (int i = 0; i < returnImg.getPathSegments().size(); i++) {
                            String temp = returnImg.getPathSegments().get(i);
                            if (temp.startsWith("content://") || temp.startsWith("uricontent://")) {
                                returnImg = Uri.parse(temp);
                                break;
                            }
                        }
                    }
                    mImageCaptureUri = data.getData();
                    Log.d("사진 실제 주소", mImageCaptureUri.getPath().toString());

                    //iv.setImageURI(mImageCaptureUri);
                    iv.setImageBitmap(getThumbNail(mImageCaptureUri));
                    Log.e("getThumbNail address",getThumbNail(mImageCaptureUri).toString());

                    String tmp = getRealPathFromURI(mImageCaptureUri);
                    //thumbnailTmp = getThumbnailPath(mImageCaptureUri+"");
                    //iv.setImageURI(Uri.parse(thumbnailTmp));
                    Log.e("mImageCaptureUri", tmp);
                    data.putExtra("uri", tmp);
                    //data.putExtra("mThumbUri", thumbnailTmp);
                }
            }
            if (slat != null && slng != null ) {
                dlat = Double.parseDouble(slat);
                dlng = Double.parseDouble(slng);
                addressTextview.setText(getAddress(getApplicationContext(), dlat, dlng));
                Toast.makeText(getApplicationContext(), "lat : " + slat + "\n lng :" + slng, Toast.LENGTH_SHORT).show();
            }
        } //back button


    }

    //위도경도 > 주소 지오코드
    public static String getAddress(Context mContext, double lat, double lng) {
        String nowAddress ="현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
                //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
                address = geocoder.getFromLocation(lat, lng, 1);

                if (address != null && address.size() > 0) {
                    // 주소 받아오기
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress  = currentLocationAddress;

                }
            }

        } catch (IOException e) {
            Toast.makeText(mContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show();

            e.printStackTrace();
        }
        return nowAddress;
    }

    private void checkGalleryPermission() {
        //갤러리 데이터 열 퍼미션 체크
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},1052);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK,  android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
            mImageCaptureUri = intent.getData();
            startActivityForResult(intent, TAKE_GALLERY);
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null );
        cursor.getPosition();
        cursor.moveToNext();
        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
        cursor.close();
        return path;
    }
    //비트맵 형식
    private Bitmap getThumbNail(Uri uri) {
        Log.d("test","from uri : "+uri);
        String[] filePathColumn = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.TITLE/*, MediaStore.Images.Media.ORIENTATION*/};
        ContentResolver cor = getContentResolver();
        //content 프로토콜로 리턴되기 때문에 실제 파일의 위치로 변환한다.
        Cursor cursor = cor.query(uri, filePathColumn, null, null, null);
        Bitmap thumbnail = null;
        if(cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            long ImageId = cursor.getLong(columnIndex);
            if(ImageId != 0) {
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                thumbnail = MediaStore.Images.Thumbnails.getThumbnail(
                        getContentResolver(), ImageId,
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        bmOptions);
            } else {
                Toast.makeText(this, "불러올수 없는 이미지 입니다.", Toast.LENGTH_LONG).show();
            }
            cursor.close();
        }
        return thumbnail;
    }

    //content:// 주소
    private String getThumbnailPath(String path) {
        String result = null;
        long imageId = -1;

        try
        {
            String[] projection = new String[] { MediaStore.MediaColumns._ID };
            String selection = MediaStore.MediaColumns.DATA + "=?";
            String[] selectionArgs = new String[] { path };
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst())
            {
                imageId = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            }
            cursor.close();

            cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(getContentResolver(), imageId, MediaStore.Images.Thumbnails.MINI_KIND, null);
            if (cursor != null && cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                result = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
            }
            cursor.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(result))
            result = path;
        Log.e("thumbnail address check",result);
        return result;
    }

    // 칼럼 이름과 그 칼럼의 내용 텍스트
    public String readColumns(Cursor cursor) {
        if (cursor.isBeforeFirst() || cursor.isAfterLast()) return "";
        //
        String rpt = "";
        for (String col:cursor.getColumnNames()) rpt += " ("+col+")"+cursor.getString(cursor.getColumnIndex(col));
        Log.e("FAT/FAT", rpt); // 로그 출력
        if (rpt.isEmpty()) return rpt;
        return rpt.substring(1);
    }

    //갤러리에 사진 추가
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mImageCaptureUri.getPath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    private void camera(){
        File imageStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/DCIM/Camera/");
        if (!imageStorageDir.exists()) {
            // Create AndroidExampleFolder at sdcard
            imageStorageDir.mkdirs();
        }

        File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");

        mImageCaptureUri = Uri.fromFile(file);
        galleryAddPic();
        // ImageView에 보여주기위해 사진파일의 절대 경로를 얻어온다.
        imagePath = file.getAbsolutePath();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { //camera permission 불가면 실행
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    // 사용자가 퍼미션을 작성해야 하는 이유에 대해 설명.
                    Toast.makeText(this, "카메라를 사용하려면 허용 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_REQUEST_STORAGE); //권한획득 팝업
            } else {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                startActivityForResult(intent, TAKE_CAMERA);
            }
        } else {

        }
    }

    private void uploadImage() {
        class UploadImage extends AsyncTask<Void, Void, String> {

            ProgressDialog uploading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                uploading = ProgressDialog.show(UpdateActivity.this, "Uploading File", "Please wait...", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                uploading.dismiss();
                //textViewResponse.setText(Html.fromHtml("<b>Uploaded at <a href='" + s + "'>" + s + "</a></b>"));
                //textViewResponse.setMovementMethod(LinkMovementMethod.getInstance());
            }

            @Override
            protected String doInBackground(Void... params) {
                FileUpload u = new FileUpload();
                //ThumbnailFileUpload tu = new ThumbnailFileUpload();
                String msg = u.uploadImage(selectedPath);


                return msg;
            }
        }
        UploadImage ui = new UploadImage();
        ui.execute();
    }

    /**
     * 사용자의 위치를 수신
     */
    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_LOCATION);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            // 수동으로 위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
                slat = dlat+"";
                slng = dlng+"";
                Log.d("Main", "longtitude=" + lng + ", latitude=" + lat);
            }
        }
        return currentLocation;
    }
    /**
     * GPS 를 받기 위한 매니저와 리스너 설정
     */
    private void settingGPS() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                dlat= location.getLatitude();
                dlng = location.getLongitude();
                // TODO 위도, 경도로 하고 싶은 것
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }
    /**
     * GPS 권한 응답에 따른 처리
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    boolean canReadLocation = false;
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // success!
                Location userLocation = getMyLocation();
                if( userLocation != null ) {
                    // todo 사용자의 현재 위치 구하기
                    dlat = userLocation.getLatitude();
                    dlng = userLocation.getLongitude();
                }
                canReadLocation = true;
            } else {
                // Permission was denied or request was cancelled
                canReadLocation = false;
            }
        }
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*
        SharedPreferences sp = getSharedPreferences("pref",MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        String title = et1.getText().toString();
        String content = et2.getText().toString();

        editor.putString("title",title);
        editor.putString("content",content);
        editor.commit();
        */
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
    @Override
    public void onStop() {
        super.onStop();
    }


}