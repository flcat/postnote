package com.flcat.postnote;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ItemView extends LinearLayout implements GestureDetector.OnGestureListener {

    Context mContext;
    TextView titleTextView; //타이틀 텍스트뷰
    TextView memoTextView; //메모 텍스트뷰
    TextView textView_Time; //시간관련 텍스트뷰
    ImageView thumbnail; // 썸네일 이미지
    TextView deletebtn;
    private GestureDetectorCompat gestureDetector;

    public ItemView(Context context) {
        super(context);

        mContext = context;
        init(); //만약 convert view재활용시 체크박스가 체크된다면 이를 통해 초기화 할수 있을것으로 예상됨
    }

    public ItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        init(); //만약 convert view재활용시 체크박스가 체크된다면 이를 통해 초기화 할수 있을것으로 예상됨
        gestureDetector = new GestureDetectorCompat(context,this);
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.listview_item, this, true);

        titleTextView = (TextView)findViewById(R.id.title_item);
        memoTextView = (TextView) findViewById(R.id.memo_item);
        thumbnail = (ImageView) findViewById(R.id.imageView1);

    }


    //컨텐츠_텍스트 관련 아이템 뷰 세팅
    public void setImageViewImage(String mThumbUri){
        if(thumbnail!=null){
            thumbnail.setImageURI(Uri.parse(mThumbUri));
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            final int SWIPE_THRESHOLD = 100;
            final int SWIPE_VELOCITY_THRESHOLD = 100;
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if(Math.abs(diffX) > Math.abs(diffY)){
                        if(Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(distanceX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0){
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                        result = true;
                    }
                    else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(distanceY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                    }
                    result = true;
                } catch (Exception e){
                    e.printStackTrace();
                }
                return  result;
            }

        public void onSwipeRight(){

        }
        public void onSwipeLeft(){
            deletebtn.setVisibility(VISIBLE);
            System.out.println("왼쪽으로 스와이프 했음.");
        }
        public void onSwipeBottom(){

        }
        public void onSwipeTop(){

        }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}