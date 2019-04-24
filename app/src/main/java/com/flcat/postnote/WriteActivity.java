package com.flcat.postnote;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WriteActivity extends Activity {
    private int TAKE_CAMERA = 1; //앱에서 카메라 호출시 반환해주는 값
    private int TAKE_GALLERY = 2; //앱에서 갤러리 호출시 반환해주는 값
    private Uri mImageCaptureUri; //로컬 이미지 Uri 주소
    private String selectedPath;
    private String selectedThumbnailPath;
    private String spTmp;
    private Uri returnImg;
    private EditText et1; // 제목 에딧
    private EditText et2; // 본문 에딧
    private ImageView iv; //글쓰기 액티비티에서 이미지 출력
    private String imagePath;
    private TextView addressTextview; //위도 경도를 지오코드로 변환한 주소 표시
    String mUri;
    String mThumbUri;
    private static final int MY_PERMISSION_REQUEST_STORAGE = 3;
    private double dlat,dlng; //MapsActivity에서 받아온 위도 lat 경도 lng 를 담을 변수
    private String slat=null,slng=null; //위도 경도 값을 WriteRequest로 보내기위해 형변환한 값을 담는 변수.
    public static int map_flag = 0;
    private int num; // DB상에서 글 번호를 매기기 위한 변수.
    // 사용자 위치 수신기
    private LocationManager locationManager;
    private LocationListener locationListener;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    Location currentLocation;
    //최소 gps 정보 업데이트 거리 10미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    int REQUEST_CODE_LOCATION=0;
    //임시저장선언
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("WriteActivity", "onCreate");
        setContentView(R.layout.activity_write);
        iv = (ImageView) findViewById(R.id.imageView1);
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        et1 = (EditText) findViewById(R.id.title_edit);
        et2 = (EditText) findViewById(R.id.content_edit);
        addressTextview = (TextView)findViewById(R.id.adress_textview);

        //각 계정별로 DB를 관리하기위해 email을 유일한 키값으로 씀
        //로그인 한 이메일 값을 얻어옴
        final Intent intent = getIntent();
        final String email = intent.getStringExtra("email");

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        //임시저장
        sp = getSharedPreferences("pref", MODE_PRIVATE);
        editor = sp.edit();

        if(map_flag == 0) {
            // 사용자의 위치 수신을 위한 세팅 //
            settingGPS();
            getMyLocation();
            // 사용자의 현재 위치 //
            //Location userLocation = getMyLocation();

            Log.e("위도경도 잘됨?",slat+"/"+slng);
        }

        //Toast.makeText(getApplicationContext(), email, Toast.LENGTH_SHORT).show();
        et2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        findViewById(R.id.write_pic_btn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //수정전 카메라 호출
                camera();
                //수정후 카메라 호출
                //Intent cameraintent = new Intent(getApplicationContext(), CameraActivity.class);
                //mImageCaptureUri = cameraintent.getData();
                //startActivityForResult(cameraintent,TAKE_CAMERA);
            }
        });

        findViewById(R.id.write_gallery_btn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkGalleryPermission();

            }
        });

        findViewById(R.id.map_btn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WriteActivity.this,MapsActivity.class);
                startActivityForResult(intent,5001);

            }
        });

        findViewById(R.id.write_save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //최대값 구해와야함
                num = 0;
                String title = et1.getText().toString(); //WriteRequest에 값을 보내기 위해 edittext에 입력된값을 변수에 저장
                String content = et2.getText().toString();
                //if (mImageCaptureUri != null) {
                thumbnailUploadImage();
                Log.e("writeActivity_thumbnail","ok"+FileUpload.fileName+FileUpload.stUploadtime+ "." + FileUpload.fileExtension);
                uploadImage();
                Log.e("writeActivity_image","ok2"+FileUpload.fileName+FileUpload.stUploadtime+ "." + FileUpload.fileExtension);
                editor.putString("mUri","http://flcat.vps.phps.kr/uploads/images"+FileUpload.fileName+FileUpload.stUploadtime+ "." + FileUpload.fileExtension);
                editor.putString("mThumbUri","http://flcat.vps.phps.kr/uploads/thumbnails"+FileUpload.fileName+FileUpload.stUploadtime+ "." + FileUpload.fileExtension);
                editor.commit();
                Log.e("파일명",FileUpload.fileName+FileUpload.stUploadtime+ "." + FileUpload.fileExtension);

                //시간을 받아온다 (yyyy/MM/dd) 형태로
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String getTime = sdf.format(date);
                String snum = num+"";

                final ListViewItem tmp = new ListViewItem(email,title,content,mUri,mThumbUri,getTime,snum,slat,slng);

                if (title.length() == 0 && content.length() == 0 ) {
                    Toast toast = Toast.makeText(WriteActivity.this, "비어있는 항목이 있습니다.", Toast.LENGTH_SHORT);
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
                                    AlertDialog.Builder builder = new AlertDialog.Builder(WriteActivity.this);
                                    builder.setMessage("메모 등록에 성공했습니다.")
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ((TodoActivity) TodoActivity.mContext).superadditem(tmp);
                                                    finish();
                                                }
                                            }).create().show();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(WriteActivity.this);
                                    builder.setMessage("메모 등록에 실패했습니다.")
                                            .setNegativeButton("확인", null).create().show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    mUri = sp.getString("mUri","");
                    mThumbUri = sp.getString("mThumbUri","");
                    slat = sp.getString("slat","");
                    slng = sp.getString("slng","");
                    WriteRequest writeRequest = new WriteRequest(num, email, title, content, mUri, mThumbUri, getTime, dlat+"", dlng+"", responseListener);
                    Log.e("볼리",num + "/" + email + "/" + title + "/" + content + "/" + mUri + "/" + mThumbUri + "/" + getTime + "/" + slat + "/" +slng);
                    RequestQueue queue = Volley.newRequestQueue(WriteActivity.this);
                    queue.add(writeRequest);
                }
                //쉐어드 초기화
                //editor.clear();
                //editor.commit();
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
        if (requestCode == TAKE_CAMERA) {
            if(resultCode == Activity.RESULT_OK) { //backpress button
                iv.setImageURI(mImageCaptureUri);
                Log.e("camera_mImageCaptureUri",mImageCaptureUri.getPath().toString());
                String spResult = spTmp.substring(7, spTmp.length());
                String tmp = getRealPathFromURI(mImageCaptureUri);
                Log.e("spTmp", spResult);
                data.putExtra("uri", spResult);
                mImageCaptureUri = data.getData();
                Log.d("사진 실제 주소", mImageCaptureUri.getPath().toString());
                //썸네일
                iv.setImageBitmap(resize(getApplicationContext(),mImageCaptureUri,360));
                mThumbUri = getImageUri(getApplicationContext(),resize(getApplicationContext(),mImageCaptureUri,60)).toString();
                selectedThumbnailPath =  getRealPathFromURI(Uri.parse(mThumbUri));
                Log.e("mImageCaptureUri", tmp);
                Log.e("selectedThumbnailPath",selectedThumbnailPath);
                data.putExtra("mUri", tmp);
                data.putExtra("mThumbUri", selectedThumbnailPath);
            }
        } else if (requestCode == TAKE_GALLERY) {
            if(resultCode == Activity.RESULT_OK) { //backpress button
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
                    //썸네일
                    iv.setImageBitmap(resize(getApplicationContext(),mImageCaptureUri,360));
                    mThumbUri = getImageUri(getApplicationContext(),resize(getApplicationContext(),mImageCaptureUri,60)).toString();
                    selectedThumbnailPath =  getRealPathFromURI(Uri.parse(mThumbUri));
                    //Log.e("getThumbNail address",getThumbNail(mImageCaptureUri).toString());
                    String tmp = getRealPathFromURI(mImageCaptureUri);
                    Log.e("mImageCaptureUri", tmp);
                    Log.e("selectedThumbnailPath",selectedThumbnailPath);
                    data.putExtra("mUri", tmp);
                    data.putExtra("mThumbUri", selectedThumbnailPath);
                }
            }
        }
        else if (requestCode == 5001 || resultCode == Activity.RESULT_OK ) {
                dlat = Double.parseDouble(data.getStringExtra("slat"));
                dlng = Double.parseDouble(data.getStringExtra("slng"));
                editor.putString("slat",dlat+"");
                editor.putString("slng",dlng+"");
                editor.commit();
                addressTextview.setText(getAddress(getApplicationContext(), dlat, dlng));
                Toast.makeText(getApplicationContext(), "lat : " + dlat + "\n lng :" + dlng, Toast.LENGTH_SHORT).show();
        } else if (resultCode == Activity.RESULT_CANCELED){
            //반환값 없을 경우
        }
    } //back button
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

    private void camera(){
        File imageStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/DCIM/Camera/");
        if (!imageStorageDir.exists()) {
            // Create AndroidExampleFolder at sdcard
            imageStorageDir.mkdirs();
        }

        File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");

        //mImageCaptureUri = Uri.fromFile(file);
        //galleryAddPic();
        // ImageView에 보여주기위해 사진파일의 절대 경로를 얻어온다.
        imagePath = file.getAbsolutePath();

        //사용자의 os가 마시멜로우 이상인지 체크.
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
                mImageCaptureUri = intent.getData();
                startActivityForResult(intent, TAKE_CAMERA);
            }
        } else {
            //사용자 os가 마시멜로우 이하일 경우
            //임시
            Toast.makeText(this, "마시멜로우 os 이하에서는 카메라를 이용할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkGalleryPermission() {
        //사용자의 os가 마시멜로우 이상인지 체크.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //갤러리 데이터 열 퍼미션 체크
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1052);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                mImageCaptureUri = intent.getData();
                startActivityForResult(intent, TAKE_GALLERY);
            }
        } else {
            //사용자 os가 마시멜로우 이하일 경우
            //임시
            Toast.makeText(this, "마시멜로우 os 이하에서는 갤러리를 이용할 수 없습니다.", Toast.LENGTH_SHORT).show();
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

    private Bitmap resize(Context context,Uri uri,int resize){
        Bitmap resizeBitmap=null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1번

            int width = options.outWidth;
            int height = options.outHeight;
            int samplesize = 1;

            while (true) {//2번
                if (width / 2 < resize || height / 2 < resize)
                    break;
                width /= 2;
                height /= 2;
                samplesize *= 2;
            }

            options.inSampleSize = samplesize;
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); //3번
            resizeBitmap=bitmap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return resizeBitmap;
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    //업로드 AsyncTask
    private void uploadImage() {
        class UploadImage extends AsyncTask<Void, Void, String> {
            ProgressDialog uploading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                uploading = ProgressDialog.show(WriteActivity.this, "Uploading File", "Please wait...", false, false);
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
                String msg = u.uploadImage(selectedPath);
                return msg;
            }
        }
        UploadImage ui = new UploadImage();
        ui.execute();
    }

    //Thumbnail 업로드 AsyncTask
    private void thumbnailUploadImage() {
        class ThumbnailUploadImage extends AsyncTask<Void, Void, String> {

            ProgressDialog uploading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //uploading = ProgressDialog.show(WriteActivity.this, "Uploading File", "Please wait...", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //uploading.dismiss();
                //textViewResponse.setText(Html.fromHtml("<b>Uploaded at <a href='" + s + "'>" + s + "</a></b>"));
                //textViewResponse.setMovementMethod(LinkMovementMethod.getInstance());
            }

            @Override
            protected String doInBackground(Void... params) {
                ThumbnailFileUpload tu = new ThumbnailFileUpload();
                String msg2 = tu.uploadImage(selectedThumbnailPath);


                return msg2;
            }
        }
        ThumbnailUploadImage tui = new ThumbnailUploadImage();
        tui.execute();
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
                slat = lat+"";
                slng = lng+"";
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
        SharedPreferences sp = getSharedPreferences("pref",MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        String title = et1.getText().toString();
        String content = et2.getText().toString();

        editor.putString("title",title);
        editor.putString("content",content);
        editor.commit();
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