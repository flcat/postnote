package com.flcat.postnote;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;


public class CustomAdapter extends BaseAdapter{
    private Context context;
    private List<ListViewItem> items_current;
    private Activity parentActivity;
    public boolean delete_flag = false;
    ViewHolder holder = new ViewHolder();
    public static int num = 0;

    public CustomAdapter(Context context,List<ListViewItem> items_current, Activity parentActivity) {
        this.context = context;
        this.items_current = items_current;
        this.parentActivity = parentActivity;

    }

    static class ViewHolder {
        TextView title; //타이틀 텍스트뷰
        TextView content; //메모 텍스트뷰
        TextView textView_Time; //시간관련 텍스트뷰
        ImageView thumbnail; // 썸네일 이미지
        TextView deleteButton;
    }

    public void addItem(ListViewItem item) {
        items_current.add(0,item);
        notifyDataSetChanged();
    }

    public void get_current_item(String date)
    {
        /*
        items_current.clear(); //현재 월별 아이템 개수를 초기화시킴

        System.out.println("현재 보여지는 아이템이 초기화 되었습니다.");

        Iterator itr_all = items.iterator();

        while(itr_all.hasNext()){ //전체 아이템 개수에서 현재 월별 아이템을 받아옴
            ListViewItem tmp = (ListViewItem) itr_all.next();

            if(date.equals(tmp.getDate()))
            {
                items_current.add(tmp);
                notifyDataSetChanged();
                //System.out.println("현재 월별 아이템에" + tmp.getTitle() + "이 추가되었습니다.");
            }
        }
        */

    }

    @Override
    public int getCount() {
        return items_current.size();
    }

    @Override
    public Object getItem(int position) {
        return items_current.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = View.inflate(context, R.layout.listview_item, null);
        holder.title = (TextView) v.findViewById(R.id.title_item);
        holder.content = (TextView) v.findViewById(R.id.memo_item);
        holder.thumbnail = (ImageView) v.findViewById(R.id.imageView1);
        num = Integer.parseInt(items_current.get(position).getNum());
        Log.e("글번호",num+"썸네일주소"+items_current.get(position).getmThumbUri());
        holder.title.setText(items_current.get(position).getTitle());
        holder.content.setText(items_current.get(position).getContent());

        //썸네일
        if(items_current.get(position).getmThumbUri() != null || items_current.get(position).getmThumbUri() != "") {
            Glide.with(holder.thumbnail.getContext())
                    .load(items_current.get(position).getmThumbUri())
                    .crossFade()
                    .centerCrop()
                    .bitmapTransform(new CropCircleTransformation(holder.thumbnail.getContext()))
                    .into(holder.thumbnail);
        }
        //outofmemory... 썸네일을 불러오고 데이터를 20개씩 로딩하는걸로..
        //image.setImageURI(Uri.parse(items_current.get(position).getThumbnailUri()));
        v.setTag(items_current.get(position).getTitle());

        //삭제 버튼 기능 구현
        holder.deleteButton = (TextView) v.findViewById(R.id.delete_btn);
        holder.deleteButton.setVisibility(View.GONE);
        if( delete_flag == true ) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");
                                if (success) {
                                    items_current.remove(position);
                                    notifyDataSetChanged();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    DeleteRequest deleteRequest = new DeleteRequest(num+"",holder.title.getText().toString(),responseListener);
                    RequestQueue queue = Volley.newRequestQueue(parentActivity);
                    queue.add(deleteRequest);
                }
            });
        }
        else {
            notifyDataSetChanged();
        }

        return v;
    }
    public void saveData(Context mContext) {
        /*
        SharedPreferences sp = mContext.getSharedPreferences("MY_LOCAL_DB", mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        Iterator itr_all = items.iterator();

        editor.clear();
        editor.commit();
        String check = sp.getString("LOCAL_DB", "");

        int cnt = 0;

        while (itr_all.hasNext()) {
            ListViewItem tmp = (ListViewItem) itr_all.next();

            String title = tmp.getTitle();
            String memo = tmp.getMemo();
            String date = tmp.getDate();
            String uri = tmp.getImageView();

            String item_str_set = title + "__" + memo + "__" + date + "__" + uri;
            System.out.println("(" + cnt + ") " + item_str_set);
            String all_item_str_set = sp.getString("LOCAL_DB", "");

            if (all_item_str_set.equals("")) {
                all_item_str_set = all_item_str_set + item_str_set;
            } else {
                all_item_str_set = all_item_str_set + "◼" + item_str_set;
            }

            /* 유니코드 변환부 한글>유니코드는 변환되는데 유니코드>한글이 안됨..수리중..
            for(int i = 0 ; i < all_item_str_set.length() ; i++) {
                String uni_tmp = String.format("U+%04X", all_item_str_set.codePointAt(i));
                Log.e("유니코드로바뀜?",uni_tmp);
                String uni_han = Normalizer.normalize(uni_tmp,Normalizer.Form.NFC);
                Log.e("한글로 다시바뀜?",uni_han);
            }


            editor.putString("LOCAL_DB", all_item_str_set);
            editor.commit();

            System.out.println(all_item_str_set);

        }
        */
    }
    public void loadData(Context mContext) {
        /*
        SharedPreferences sp = mContext.getSharedPreferences("MY_LOCAL_DB", 0);
        String item_superstring = sp.getString("LOCAL_DB", "안들어옴");
        if (!item_superstring.equals("안들어옴")) {
            String[] itemValues = item_superstring.split("◼");

            for (int i = 0; i < itemValues.length; i++) {
                String tmp = itemValues[i];
                String[] tmp_item_val = tmp.split("__");
                String title = tmp_item_val[0];
                String memo = tmp_item_val[1];
                String date = tmp_item_val[2];
                String uri = tmp_item_val[3];
                items.add(new ListViewItem(title, memo,date,uri));
            }
        }
        */
    }
}