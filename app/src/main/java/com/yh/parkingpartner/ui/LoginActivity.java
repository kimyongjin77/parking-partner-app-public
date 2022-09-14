package com.yh.parkingpartner.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yh.parkingpartner.R;
import com.yh.parkingpartner.api.ApiLoginActivity;
import com.yh.parkingpartner.api.NetworkClient;
import com.yh.parkingpartner.config.Config;
import com.yh.parkingpartner.model.User;
import com.yh.parkingpartner.model.UserRes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    EditText etxtEmail;
    EditText etxtPassword;
    Button btnLogin;
    Button btnRegister;
    TextView txtPwdChange;

    //네트워크 처리 보여주는 프로그래스 다이얼로그
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.getSupportActionBar().hide();

        etxtEmail=findViewById(R.id.etxtEmail);
        etxtPassword=findViewById(R.id.etxtPassword);
        btnLogin=findViewById(R.id.btnLogin);
        btnRegister=findViewById(R.id.btnRegister);
        txtPwdChange=findViewById(R.id.txtPwdChange);

        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        etxtEmail.setText(sp.getString(Config.SP_KEY_EMAIL, ""));

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //로그인 처리 코딩
                String  email=etxtEmail.getText().toString().trim();
                //문자열 이메일 패턴
                Pattern patten= Patterns.EMAIL_ADDRESS;
                if(!patten.matcher(email).matches()){
                    Toast.makeText(getApplicationContext(), "이메일을 정확하게 입력하세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                String pwd=etxtPassword.getText().toString().trim();
                if(pwd.length() < 4 || pwd.length() > 8){
                    Toast.makeText(getApplicationContext(), "암호는 4자이상 8자이하로 입력하세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                //retrofit을 사용하여 api 호출
                //네트워크데이터를 보내고 있다는 프로그래스 다이얼로그를 먼저 띄운다..
                showProgress("처리 중입니다...");

                Retrofit retrofit= NetworkClient.getRetrofitClient(LoginActivity.this, Config.PP_BASE_URL);
                ApiLoginActivity api=retrofit.create(ApiLoginActivity.class);

                //body(raw) email=aaa@naver.com, password=1234
                User user=new User(email, pwd);
                Call<UserRes> call=api.login(user);

                call.enqueue(new Callback<UserRes>() {
                    @Override
                    public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                        dismissProgress();
                        //http상태코드 확인
                        if(response.isSuccessful()) {
                            UserRes registerRes = response.body();
                            //엑세스토큰은 이제 앱에서 api호출할때마다 해더에 넣어서 보내야 한다.
                            //따라서 액세스토큰은 쉐어드프리퍼런스에 저장해 놓는다.
                            //SharedPreferences 를 이용해서, 앱 내의 저장소에 영구저장하는 방법
                            //엡을 삭제하기 전까지는 영구적으로 저장되며 앱을 삭제하면 같이 삭제 된다.
                            //저장소를 만든다.
                            if (registerRes.getResult().equals("success")) {
                                SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                                //편집기를 만든다.
                                SharedPreferences.Editor editor = sp.edit();
                                //작성한다.
                                editor.putString(Config.SP_KEY_ACCESS_TOKEN, registerRes.getAccessToken());
                                editor.putString(Config.SP_KEY_NAME, registerRes.getName());
                                editor.putString(Config.SP_KEY_EMAIL, user.getEmail());
                                editor.putString(Config.SP_KEY_IMG_PROFILE, registerRes.getImg_profile());

                                editor.putString(Config.SP_KEY_PRK_CENTER_ID, registerRes.getPrk_center_id());
                                editor.putString(Config.SP_KEY_PRK_PLCE_NM, registerRes.getPrk_plce_nm());
                                editor.putString(Config.SP_KEY_PRK_PLCE_ADRES, registerRes.getPrk_plce_adres());
                                editor.putInt(Config.SP_KEY_PARKING_CHRGE_BS_TIME, registerRes.getParking_chrge_bs_time());
                                editor.putInt(Config.SP_KEY_PARKING_CHRGE_BS_CHRG, registerRes.getParking_chrge_bs_chrg());
                                editor.putInt(Config.SP_KEY_PARKING_CHRGE_ADIT_UNIT_TIME, registerRes.getParking_chrge_adit_unit_time());
                                editor.putInt(Config.SP_KEY_PARKING_CHRGE_ADIT_UNIT_CHRGE, registerRes.getParking_chrge_adit_unit_chrge());
                                editor.putInt(Config.SP_KEY_PARKING_CHRGE_ONE_DAY_CHRGE, registerRes.getParking_chrge_one_day_chrge());
                                editor.putInt(Config.SP_KEY_PRK_ID,registerRes.getPrk_id());
                                editor.putString(Config.SP_KEY_START_PRK_AT,registerRes.getStart_prk_at().replace("T", " "));
                                editor.putString(Config.SP_KEY_IMG_PAK,registerRes.getImg_prk());
                                editor.putString(Config.SP_KEY_PRK_AREA,registerRes.getPrk_area());

                                editor.putInt(Config.SP_KEY_PUSH_PRK_ID,registerRes.getPrk_id());

                                //저장한다.
                                editor.apply();

                                //알러트 다이얼로그(팝업)
                                AlertDialog.Builder alert=new AlertDialog.Builder(LoginActivity.this);
                                alert.setTitle("로그인 성공");
                                alert.setMessage(registerRes.getName()+" 님 환영합니다.\n홈화면으로 이동합니다.");
                                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });

                                //알러트 다이얼로그의 버튼을 안누르면, 화면이 넘어가지 않게..
                                alert.setCancelable(false);
                                //다이얼로그 화면에 보이기
                                alert.show();
                            }

                        } else {
                            try{
                                JSONObject errorBody= new JSONObject(response.errorBody().string());
                                Toast.makeText(getApplicationContext(),
//                                        "에러발생\n"+
//                                        "코드 : "+response.code()+"\n" +
                                        "에러 : "+errorBody.getString("error")
                                        , Toast.LENGTH_LONG).show();
                                Log.i("로그", "에러발생 : "+response.code()+", "+errorBody.getString("error"));
                            }catch (IOException | JSONException e){
                                Toast.makeText(getApplicationContext(),
//                                        "에러발생\n"+
//                                                "코드 : "+response.code()+"\n" +
                                                "에러 : "+e.getMessage()
                                        , Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserRes> call, Throwable t) {
                        //통신실패 네트워크 자체 문제로 실패되는 경우
                        dismissProgress();
                        Toast.makeText(getApplicationContext(), "시스템에러발생 : "+t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.i("로그", t.getMessage());
                    }
                });
            }
        });

        txtPwdChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=etxtEmail.getText().toString().trim();
                //문자열 이메일 패턴
                Pattern patten= Patterns.EMAIL_ADDRESS;
                if(!patten.matcher(email).matches()){
                    Toast.makeText(getApplicationContext(), "이메일을 정확하게 입력하세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                showProgress("회원정보 확인 중입니다...");

                Retrofit retrofit= NetworkClient.getRetrofitClient(LoginActivity.this, Config.PP_BASE_URL);
                ApiLoginActivity api=retrofit.create(ApiLoginActivity.class);

                Call<UserRes> call=api.user_find(email);
                call.enqueue(new Callback<UserRes>() {
                    @Override
                    public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                        dismissProgress();
                        //http상태코드 확인
                        if(response.isSuccessful()) {
                            UserRes registerRes = response.body();
                            if (registerRes.getResult().equals("success")) {
                                //알러트 다이얼로그(팝업)
                                AlertDialog.Builder alert=new AlertDialog.Builder(LoginActivity.this);
                                alert.setTitle("회원정보 확인");
                                alert.setMessage("이메일 : "+registerRes.getEmail()+"\n"+
                                                            "닉네임 : "+registerRes.getName()+"\n\n"+
                                                            "타인의 계정을 도용하는 것은 불법입니다."+"\n\n"+
                                                            "진행하시겠습니까?"
                                );
                                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
                                        intent.putExtra("setpwd", true);
                                        intent.putExtra("userinfo", registerRes);
                                        startActivity(intent);
                                    }
                                });
                                alert.setNegativeButton("No", null);
                                //알러트 다이얼로그의 버튼을 안누르면, 화면이 넘어가지 않게..
                                alert.setCancelable(false);
                                //다이얼로그 화면에 보이기
                                alert.show();
                            }

                        } else {
                            try{
                                JSONObject errorBody= new JSONObject(response.errorBody().string());
                                Toast.makeText(getApplicationContext(),
                                        "에러 : "+errorBody.getString("error")
                                        , Toast.LENGTH_LONG).show();
                                Log.i("로그", "에러발생 : "+response.code()+", "+errorBody.getString("error"));
                            }catch (IOException | JSONException e){
                                Toast.makeText(getApplicationContext(),
                                        "에러 : "+e.getMessage()
                                        , Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<UserRes> call, Throwable t) {
                        //통신실패 네트워크 자체 문제로 실패되는 경우
                        dismissProgress();
                        Toast.makeText(getApplicationContext(), "시스템에러발생 : "+t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.i("로그", t.getMessage());
                    }
                });

            }
        });

    }

    //프로그래스다이얼로그 표시
    void showProgress(String msg){
        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }
    //프로그래스다이얼로그 숨기기
    void dismissProgress(){
        progressDialog.dismiss();
    }
}