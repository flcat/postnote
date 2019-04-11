package com.flcat.postnote;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    List<Chat> mChat;
    String stEmail;
    String stchatTime;

    final int right = 1;
    final int left = 2;
    final int left_again = 3;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public TextView nickname;
        public TextView chatTime;
        public ImageView userphoto;
        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.mTextView);
            nickname = (TextView)itemView.findViewById(R.id.chat_usernick_text);
            userphoto = (ImageView) itemView.findViewById(R.id.chat_user_image);
            chatTime = (TextView)itemView.findViewById(R.id.time_text);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatAdapter(List<Chat> mChat,String email,String chatTime) {
        this.mChat = mChat;
        this.stEmail = email;
        this.stchatTime = chatTime;
    }

    @Override
    public int getItemViewType(int position) {

        if(mChat.get(position).getEmail().equals(stEmail)) {
            return right;
        /*
        } else if(mChat.get(position).getEmail().equals(mChat.get(position+1).getEmail())){
            return left_again;
        */

        } else {
            return left;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v;
        if (viewType == right) {
            // create a new view
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_text_item_right, parent, false);
        } else if(viewType == left_again) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_onlytext_item, parent, false);
        } else {
            // create a new view
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_text_item, parent, false);
        }

        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        // && getItemViewType(position)!=left_again
        if (!mChat.get(position).getEmail().equals(stEmail)) {
            holder.nickname.setText(mChat.get(position).getEmail().toString());
        }
        Log.e("이메일",mChat.get(position).getEmail().toString());
        holder.mTextView.setText(mChat.get(position).getText());
        //if (mChat.get(position).getChatTime() != mChat.get(position+1).getChatTime()) {
        holder.chatTime.setText(mChat.get(position).getChatTime());
        //holder.chatTime.setText(stchatTime);
        //}
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mChat.size();
    }
}

