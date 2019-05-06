package com.flcat.postnote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;

public class UserInfoFragment extends Fragment {

    String TAG = getClass().getSimpleName();
    ImageView iv;
    Bitmap bitmap;
    String stUid;
    String stEmail;
    String stPhoto;
    ProgressBar pbpic;


    private StorageReference mStorageRef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_userinfo, container, false);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("email", Context.MODE_PRIVATE);
        stUid = sharedPreferences.getString("uid","");
        stEmail = sharedPreferences.getString("email","");
        iv = (ImageView)v.findViewById(R.id.user_pic);
        stPhoto = sharedPreferences.getString("userphoto","");
        //Log.e("뭐야",stPhoto);

        if(TextUtils.isEmpty(stPhoto) && TodoActivity.bitmap != null){
            iv.setImageBitmap(TodoActivity.bitmap);
            //iv.setImageURI(Uri.parse(stPhoto));
            uploadImage();
        }
        pbpic = (ProgressBar)v.findViewById(R.id.pbpic);
        // Read from the database
        if(myRef.child("users").child(stUid).toString() != null) {
            pbpic.setVisibility(View.VISIBLE);
            myRef.child("users").child(stUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    //String value = dataSnapshot.getValue().toString();
                    stPhoto = dataSnapshot.child("userphoto").getValue().toString();
                    //Log.e("유저인포이미지",stPhoto);
                    if(TextUtils.isEmpty(stPhoto)) {
                        pbpic.setVisibility(View.GONE);
                    } else {
                        pbpic.setVisibility(View.VISIBLE);
                        Picasso.get()
                                .load(stPhoto)
                                .fit()
                                .centerInside()
                                .into(iv, new Callback.EmptyCallback() {
                                    @Override public void onSuccess() {
                                        // Index 0 is the image view.
                                        Log.d(TAG, "Picasso SUCCESS");
                                        pbpic.setVisibility(View.GONE);
                                    }
                                });
                    }
                    //Log.d(TAG, "Value is: " + value);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        } else {
            Toast.makeText(getActivity(),"프로필 사진을 등록해주세요.",Toast.LENGTH_SHORT).show();
        }

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent,4002);
            }
        });

        if (ContextCompat.checkSelfPermission(getActivity(),android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        return v;

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == 4002) {
                Uri image = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image);
                    //iv.setImageURI(image);
                    iv.setImageBitmap(bitmap);
                    uploadImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    public void uploadImage(){
        StorageReference riversRef = mStorageRef.child("users").child(stUid+".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if(TextUtils.isEmpty(stPhoto) && TodoActivity.bitmap != null) {
            TodoActivity.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        } else {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = riversRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("users");
                Log.e("유저사진주소",String.valueOf(downloadUrl));
                Hashtable<String, String> profile = new Hashtable<String, String>();
                //profile.put("uid",stUid);
                profile.put("email",stEmail);
                profile.put("userphoto",String.valueOf(downloadUrl));
                profile.put("key",stUid);
                myRef.child(stUid).setValue(profile);
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue().toString();
                        Log.d("Profile",value);
                        if(dataSnapshot != null){
                            Toast.makeText(getActivity(),"프로필 사진 업로드 완료.",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}