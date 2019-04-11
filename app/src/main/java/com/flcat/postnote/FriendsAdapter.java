package com.flcat.postnote;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder>{
    private Context context;
    Fragment fragment;
    FragmentTransaction ft;
    List<Friends> mFriends;
    public static String stPhoto;
    String stEmail;
    public static String chatRoomUid;
    String friendUid;
    String uid;
    private AdapterView.OnItemClickListener mCallback;
    String TAG = getClass().getSimpleName();


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public TextView nickname;
        public TextView email;
        public ImageView userphoto;
        public Button btnChat;
        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.mTextView);
            nickname = itemView.findViewById(R.id.nick_item);
            userphoto = itemView.findViewById(R.id.userphoto);
            email = itemView.findViewById(R.id.email_item);
            btnChat = itemView.findViewById(R.id.chat_button);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FriendsAdapter(List<Friends> friends) {
        this.mFriends = friends;
    }

    @NonNull
    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friendlist_item, parent, false);
        uid =  FirebaseAuth.getInstance().getCurrentUser().getUid(); //채팅을 요구하는 아이디 즉, 단말기에 로그인된 아이디

        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FriendsAdapter.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        // && getItemViewType(position)!=left_again
        //if (!mFriends.get(position).getEmail().equals(stEmail)) {
            holder.nickname.setText(mFriends.get(position).getEmail());
        //}
        //Log.e("이메일",mFriends.get(position).getEmail());
        stPhoto = mFriends.get(position).getUserphoto();
        if(TextUtils.isEmpty(stPhoto)) {

        } else {
            Glide.with(holder.itemView.getContext())
                    .load(stPhoto)
                    .crossFade()
                    .centerCrop()
                    .bitmapTransform(new CropCircleTransformation(holder.itemView.getContext()))
                    .into(holder.userphoto);
        }

        holder.btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent intent = new Intent(context, ChatFragment.class);
                //context.startActivity(intent);
                friendUid = mFriends.get(position).getUid(); //채팅을 당하는 아이디
                ft = ((AppCompatActivity)TodoActivity.mContext).getSupportFragmentManager().beginTransaction();
                fragment = new ChatFragment();
                Bundle args = new Bundle();
                //Log.e("아이디값",stFriendId);
                args.putString("friendUid",friendUid);
                fragment.setArguments(args);
                ft.replace(R.id.content_fragment_layout,fragment);
                ft.commit();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mFriends.size();
    }
    //채팅방 중복체크
    void checkChatRoom(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).orderByChild("users/"+uid)
                .equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //chatrooms 하부 경로를 받아옴
                        for(DataSnapshot item : dataSnapshot.getChildren()){

                            String value2 = dataSnapshot.getValue().toString();
                            Log.d(TAG, "Value is: " + value2);

                            Chat chat = item.getValue(Chat.class);
                            if(chat.users.containsKey(friendUid)){
                                chatRoomUid = item.getKey(); //채팅룸 아이디키
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
});
    }
}
