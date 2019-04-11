package com.flcat.postnote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

        private AlertDialog dialog;
        public static FirebaseUser user;
        public static FirebaseAuth mAuth; //파이어베이스
        private FirebaseDatabase database;
        private DatabaseReference myRef;
        private FirebaseAuth.AuthStateListener mAuthListener;

        //구글 로그인
        String userPhoto;

        //Firebase 로그인
        String userEmail = null;
        String userPassword = null;

        GoogleApiClient mGoogleApiClient;
        int RC_SIGN_IN = 1000;
        ProgressBar pgbar;

        BackgroundTask backgroundTask = new BackgroundTask();



        String TAG = LoginActivity.class.getName();


        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);
            //backgroundTask= new BackgroundTask();

            mAuth = FirebaseAuth.getInstance();
            pgbar = (ProgressBar)findViewById(R.id.progress);
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    user = firebaseAuth.getCurrentUser();
                    if(user != null){
                        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("email", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("uid",user.getUid());
                        editor.putString("email",user.getEmail());
                        editor.apply();
                    } else {
                        Log.e("firebase login","로그인 실패");
                    }
                }
            };
            final EditText email = (EditText)findViewById(R.id.login_email_edit);
            email.setInputType ( InputType. TYPE_TEXT_FLAG_NO_SUGGESTIONS );
            final EditText password = (EditText)findViewById(R.id.login_password_edit);
            final Button loginBtn = (Button)findViewById(R.id.login_btn);


        // google login
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // 구글 로그인 버튼
        SignInButton signInButton = (SignInButton) findViewById(R.id.google_login_btn);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        findViewById(R.id.google_login_btn).setOnClickListener(this);

            //firebase 로그인 버튼
            loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userEmail = email.getText().toString();
                userPassword = password.getText().toString();

                    if (email.length() == 0 || password.length() == 0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        dialog = builder.setMessage("아이디 혹은 비밀번호를 입력해주세요. firebase login")
                                .setNegativeButton("다시 시도",null)
                                .create();
                        dialog.show();
                    }

                    if(email.length() != 0 && password.length() != 0){
                    pgbar.setVisibility(View.VISIBLE);
                    firebaseuserlogin(userEmail, userPassword);
                    //mysqluserlogin(login_edit_email,login_edit_password);
                }

            }
        });

        final Button signupBtn = (Button)findViewById(R.id.signup_btn);
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.google_login_btn:
                signIn();
                break;
        }
    }

    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if(result.isSuccess()) {
                    GoogleSignInAccount acct = result.getSignInAccount();
                    firebaseAuthWithGoogle(acct);
                }

            }
        }
    }
    private void updateUI(boolean signedIn) {
        if (signedIn) {
            //서버 다운시 주석처리
            backgroundTask.execute();
            //서버 다운시 아래 코드 사용
            /*
            Intent intent = new Intent(getApplicationContext(), TodoActivity.class);
            intent.putExtra("email", userEmail);
            intent.putExtra("userPhoto", userPhoto);
            //intent.putExtra("noteList",result);
            LoginActivity.this.startActivity(intent);
            finish();
            */
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            dialog = builder.setMessage("로그인에 실패하였습니다. updateUI")
                    .setNegativeButton("다시 시도",null)
                    .create();
                    dialog.show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    class BackgroundTask extends AsyncTask<Void, Void, String>
    {
        String target;

        @Override
        protected void onPreExecute() {
            try {
                target = "http://flcat.vps.phps.kr/Notelist.php?email="+ URLEncoder.encode(userEmail,"UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                }
            }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                //서버 고장났을경우 주석처리

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
            Intent intent = new Intent(getApplicationContext(), TodoActivity.class);
            intent.putExtra("email", userEmail);
            intent.putExtra("userPhoto", userPhoto);
            intent.putExtra("noteList",result);
            LoginActivity.this.startActivity(intent);
            finish();
        }
    }
private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        // Signed in successfully, show authenticated UI.
        String token = acct.getIdToken();
        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                            pgbar.setVisibility(View.GONE);
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            userEmail = acct.getEmail();
                            userPhoto = acct.getPhotoUrl() + "";
                            updateUI(true);
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.

                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                            String formattedDate = df.format(calendar.getTime());
                            //firebase db
                            database = FirebaseDatabase.getInstance();
                            myRef = database.getReference("users").child(user.getUid());
                            if(user != null){
                                /*
                                //최초 실행 여부 판단하는 구문
                                SharedPreferences pref = getSharedPreferences("isFirst", LoginActivity.MODE_PRIVATE);
                                boolean first = pref.getBoolean("isFirst", false);
                                if(first==false) {
                                    Log.d("Is first Time?", "first");
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putBoolean("isFirst", true);
                                    editor.commit();
                                */
                                    Hashtable<String, String> profile = new Hashtable<>();
                                    profile.put("email", user.getEmail());
                                    profile.put("userphoto", userPhoto); //user.getPhotoUrl().toString()
                                    profile.put("uid", user.getUid());
                                    myRef.setValue(profile);
                                    //myRef.child("chats").child(formattedDate).setValue("hello world");
                                }
                                /*
                                else{
                                    Log.d("Is first Time?", "not first");
                                }
                                */
                            } else {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "GoogleSignin failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

public void firebaseuserlogin(final String email, final String password){
    mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        pgbar.setVisibility(View.GONE);
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        //FirebaseUser user = mAuth.getCurrentUser();
                        //서버다운시 주석처리
                        //mysqluserlogin(email,password);
                        //서버다운시 주석해제
                        updateUI(true);

                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                        String formattedDate = df.format(calendar.getTime());
                        //firebase db
                        database = FirebaseDatabase.getInstance();
                        myRef = database.getReference("users").child(user.getUid());
                        if(user != null){
                            /*
                            //최초 실행 여부 판단하는 구문
                            SharedPreferences pref = getSharedPreferences("isFirst", LoginActivity.MODE_PRIVATE);
                            boolean first = pref.getBoolean("isFirst", false);
                            if(first==false) {
                                Log.d("Is first Time?", "first");
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putBoolean("isFirst", true);
                                editor.commit();
                            */
                                    Hashtable<String, String> profile = new Hashtable<>();
                                    profile.put("email", user.getEmail());
                                    profile.put("userphoto", ""); //user.getPhotoUrl().toString()
                                    profile.put("uid", user.getUid());
                                    myRef.setValue(profile);
                                    myRef.child("chats").child(formattedDate).setValue("hello world");
                            }
                            /*
                            else{
                                Log.d("Is first Time?", "not first");
                            }
                        }
                        */
                    } else {
                        pgbar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(false);
                    }

                    // ...
                }
            });
}
public void mysqluserlogin(final String email, final String password){
    Response.Listener<String> responseListener = new Response.Listener<String>(){
        @Override
        public void onResponse(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                boolean success = jsonResponse.getBoolean("success");
                if(success){
                    Log.d("mysqluserlogin","login success");
                    updateUI(true);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    dialog = builder.setMessage("로그인에 실패하였습니다. mysqluserlogin")
                            .setNegativeButton("다시 시도",null)
                            .create();
                            dialog.show();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    LoginRequest loginRequest = new LoginRequest(userEmail, userPassword, responseListener);
    RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
    queue.add(loginRequest);
}
//로그아웃 관련 세션관리
@Override
public void onStart() {
    super.onStart();
    mAuth.addAuthStateListener(mAuthListener);
}

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }
}