package com.yh.parkingpartner.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.yh.parkingpartner.R;
import com.yh.parkingpartner.api.ApiRegisterActivity;
import com.yh.parkingpartner.api.NetworkClient;
import com.yh.parkingpartner.config.Config;
import com.yh.parkingpartner.model.UserRes;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterActivity extends AppCompatActivity {

    TextView txtTitle;
    EditText etxtEmail;
    EditText etxtPassword;
    EditText etxtPasswordConfirm;
    EditText etxtName;
    ImageView imgProfile;
    Button btnLogin;
    Button btnRegister;

    //네트워크 처리 보여주는 프로그래스 다이얼로그
    ProgressDialog progressDialog;

    //사진관련된 변수들
    private File photoFile;

    boolean update;
    boolean setpwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.getSupportActionBar().hide();

        update=getIntent().getBooleanExtra("update", false);
        Log.i("로그", "RegisterActivity.onCreate update="+update);

        setpwd=getIntent().getBooleanExtra("setpwd", false);
        Log.i("로그", "RegisterActivity.onCreate setpwd="+setpwd);

        txtTitle=findViewById(R.id.txtTitle);
        etxtEmail=findViewById(R.id.etxtEmail);
        etxtPassword=findViewById(R.id.etxtPassword);
        etxtPasswordConfirm=findViewById(R.id.etxtPasswordConfirm);
        etxtName=findViewById(R.id.etxtName);
        imgProfile=findViewById(R.id.imgProfile);
        btnRegister=findViewById(R.id.btnRegister);
        btnLogin=findViewById(R.id.btnLogin);

        if(update) {
            txtTitle.setText("회원정보수정");
            //SharedPreferences 를 이용해서, 앱 내의 저장소에 영구저장된 데이터를 읽어오는 방법
            SharedPreferences sp = getApplication().getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
            etxtEmail.setText(sp.getString(Config.SP_KEY_EMAIL, ""));
            etxtEmail.setInputType(InputType.TYPE_NULL);
            etxtEmail.setFocusable(false);
            etxtName.setText(sp.getString(Config.SP_KEY_NAME, ""));
            if (!sp.getString(Config.SP_KEY_IMG_PROFILE, "").equals("")) {
                GlideUrl url = new GlideUrl(sp.getString(Config.SP_KEY_IMG_PROFILE, ""), new LazyHeaders.Builder().addHeader("User_Agent", "Android").build());
                Glide.with(getApplicationContext()).load(url).placeholder(R.drawable.ic_baseline_person_24).into(imgProfile);
            }
            btnRegister.setText("저장");
            btnLogin.setVisibility(View.GONE);
        }else if(setpwd){
            txtTitle.setText("회원정보수정");
            UserRes userinfo= (UserRes) getIntent().getSerializableExtra("userinfo");
            etxtEmail.setText(userinfo.getEmail());
            etxtEmail.setInputType(InputType.TYPE_NULL);
            etxtEmail.setFocusable(false);
            etxtName.setText(userinfo.getName());
            if (!userinfo.getImg_profile().equals("")) {
                GlideUrl url = new GlideUrl(userinfo.getImg_profile(), new LazyHeaders.Builder().addHeader("User_Agent", "Android").build());
                Glide.with(getApplicationContext()).load(url).placeholder(R.drawable.ic_baseline_person_24).into(imgProfile);
            }
            btnRegister.setText("저장");
            btnLogin.setVisibility(View.GONE);
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //카메라로 사진을 찍을 것인지, 앨범에서 사진을 가져올 것인지 선택할 수 있게 알러트 다이얼로그를 띄운다.
                showImageChoiceMethod();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //회원가입 저장 처리 코딩
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

                String pwdConfirm=etxtPasswordConfirm.getText().toString().trim();
                if(!pwd.equals(pwdConfirm)){
                    Toast.makeText(getApplicationContext(), "암호가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                String name=etxtName.getText().toString().trim();
                if(name.isEmpty()){
                    Toast.makeText(getApplicationContext(), "닉네임을 입력하세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                //retrofit을 사용하여 api 호출
                //네트워크데이터를 보내고 있다는 프로그래스 다이얼로그를 먼저 띄운다..
                showProgress("처리 중입니다...");

                Retrofit retrofit= NetworkClient.getRetrofitClient(RegisterActivity.this, Config.PP_BASE_URL);
                ApiRegisterActivity api=retrofit.create(ApiRegisterActivity.class);

                //body(form-data) email(text)=aaa@naver.com, password(text)=1234, name(text), img_profile(file)
                //@Multipart @Part MultipartBody.Part 파일 변수 만들기
                MultipartBody.Part photoBody=null;
                if(photoFile!=null){
                    RequestBody fileBody=RequestBody.create(photoFile, MediaType.parse("image/*"));
                    photoBody=MultipartBody.Part.createFormData("img_profile", photoFile.getName(), fileBody);
                }
                //@Multipart @Part RequestBody 텍스트 변수 만들기
                RequestBody emailBody=RequestBody.create(email, MediaType.parse("text/plain"));
                RequestBody pwdBody=RequestBody.create(pwd, MediaType.parse("text/plain"));
                RequestBody nameBody=RequestBody.create(name, MediaType.parse("text/plain"));
                Map<String, RequestBody> params = new HashMap<>();
                params.put("email", emailBody);
                params.put("password", pwdBody);
                params.put("name", nameBody);

//                Call<UserRes> call=api.register(photoBody, emailBody, pwdBody, nameBody);
                Call<UserRes> call=null;
                if(update || setpwd) {
                    call = api.userinfo_update(photoBody, params);
                }else{
                    call = api.register(photoBody, params);
                }
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
                                if(!setpwd) {
                                    SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                                    //편집기를 만든다.
                                    SharedPreferences.Editor editor = sp.edit();
                                    if(!update) {
                                        //작성한다.
                                        editor.putString(Config.SP_KEY_ACCESS_TOKEN, registerRes.getAccessToken());
                                        editor.putString(Config.SP_KEY_EMAIL, email);
                                        editor.putString(Config.SP_KEY_IMG_PROFILE, registerRes.getImg_profile());

                                        editor.putString(Config.SP_KEY_PRK_CENTER_ID, "");
                                        editor.putString(Config.SP_KEY_PRK_PLCE_NM, "");
                                        editor.putString(Config.SP_KEY_PRK_PLCE_ADRES, "");
                                        editor.putInt(Config.SP_KEY_PARKING_CHRGE_BS_TIME, 0);
                                        editor.putInt(Config.SP_KEY_PARKING_CHRGE_BS_CHRG, 0);
                                        editor.putInt(Config.SP_KEY_PARKING_CHRGE_ADIT_UNIT_TIME, 0);
                                        editor.putInt(Config.SP_KEY_PARKING_CHRGE_ADIT_UNIT_CHRGE, 0);
                                        editor.putInt(Config.SP_KEY_PARKING_CHRGE_ONE_DAY_CHRGE, 0);
                                        editor.putInt(Config.SP_KEY_PRK_ID, 0);
                                        editor.putString(Config.SP_KEY_START_PRK_AT, "");
                                        editor.putString(Config.SP_KEY_IMG_PAK, "");
                                        editor.putString(Config.SP_KEY_PRK_AREA, "");
                                        editor.putInt(Config.SP_KEY_PUSH_PRK_ID, 0);
                                    }else{
                                        if(photoFile!=null) {
                                            editor.putString(Config.SP_KEY_IMG_PROFILE, registerRes.getImg_profile());
                                        }
                                    }
                                    editor.putString(Config.SP_KEY_NAME, name);
                                    //저장한다.
                                    editor.apply();
                                }

                                if(!update && !setpwd) {
                                    //알러트 다이얼로그(팝업)
                                    AlertDialog.Builder alert = new AlertDialog.Builder(RegisterActivity.this);
                                    alert.setTitle("회원가입 성공");
                                    alert.setMessage(name + " 님 환영합니다.\n홈화면으로 이동합니다.");
                                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });

                                    //알러트 다이얼로그의 버튼을 안누르면, 화면이 넘어가지 않게..
                                    alert.setCancelable(false);
                                    //다이얼로그 화면에 보이기
                                    alert.show();
                                }else{
                                    Toast.makeText(getApplicationContext(), "수정완료", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }

                        } else {
                            try{
                                JSONObject errorBody= new JSONObject(response.errorBody().string());
                                Toast.makeText(getApplicationContext(),
//                                        "에러발생\n"+
//                                                "코드 : "+response.code()+"\n" +
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

    void showImageChoiceMethod(){
        androidx.appcompat.app.AlertDialog.Builder builder= new androidx.appcompat.app.AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle("프로필이미지");
        builder.setItems(R.array.alert_photo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0){
                    //카메라을 선택하면 카메라앱 실행 사진 찍기
                    //사진을 찍어 저장된 이미지를 이미지뷰에 보여준다.
                    camera();
                } else if(i==1){
                    //앨범을 선택하면 앨범앱 실행 사진 선택
                    //선택한 이미지를 이미지뷰에 보여준다.
                    album();
                }
            }
        });
        androidx.appcompat.app.AlertDialog alert=builder.create();
        alert.show();
    }

    private void camera(){
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA} , 1000);
            Toast.makeText(this, "카메라 권한 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(i.resolveActivity(this.getPackageManager())  != null  ){
                // 사진의 파일명을 만들기
                String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                photoFile = getPhotoFile(fileName);
                Uri fileProvider = FileProvider.getUriForFile(this, "com.yh.parkingpartner.fileprovider", photoFile);
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
                startActivityForResult(i, 100);
            } else{
                Toast.makeText(this, "이폰에는 카메라 앱이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File getPhotoFile(String fileName) {
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try{
            return File.createTempFile(fileName, ".jpg", storageDirectory);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    private void album(){
        if(checkPermission()){
            displayFileChoose();
        }else{
            requestPermission();
        }
    }

    private void requestPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(this, "권한 수락이 필요합니다.",
                    Toast.LENGTH_SHORT).show();
        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 500);
        }
    }

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(result == PackageManager.PERMISSION_DENIED){
            return false;
        }else{
            return true;
        }
    }

    private void displayFileChoose() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "사진 선택"), 300);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "권한 허가 되었음",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "아직 승인하지 않았음",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case 500: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "권한 허가 되었음",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "아직 승인하지 않았음",
                            Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 100 && resultCode == RESULT_OK){

            Bitmap photo = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

            ExifInterface exif = null;
            try {
                exif = new ExifInterface(photoFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            photo = rotateBitmap(photo, orientation);

            // 압축시킨다. 해상도 낮춰서
            OutputStream os;
            try {
                os = new FileOutputStream(photoFile);
                photo.compress(Bitmap.CompressFormat.JPEG, 50, os);
                os.flush();
                os.close();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }

            photo = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

            imgProfile.setImageBitmap(photo);
            //imgProfile.setScaleType(ImageView.ScaleType.FIT_XY);

        }else if(requestCode == 300 && resultCode == RESULT_OK && data != null && data.getData() != null){

            Uri albumUri = data.getData( );
            String fileName = getFileName( albumUri );
            try {

                ParcelFileDescriptor parcelFileDescriptor = getContentResolver( ).openFileDescriptor( albumUri, "r" );
                if ( parcelFileDescriptor == null ) return;
                FileInputStream inputStream = new FileInputStream( parcelFileDescriptor.getFileDescriptor( ) );
                photoFile = new File( this.getCacheDir( ), fileName );
                FileOutputStream outputStream = new FileOutputStream( photoFile );
                IOUtils.copy( inputStream, outputStream );

                // 압축시킨다. 해상도 낮춰서
                Bitmap photo = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                OutputStream os;
                try {
                    os = new FileOutputStream(photoFile);
                    photo.compress(Bitmap.CompressFormat.JPEG, 60, os);
                    os.flush();
                    os.close();
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
                }

                imgProfile.setImageBitmap( getBitmapAlbum( imgProfile, albumUri ) );
                //imgProfile.setScaleType(ImageView.ScaleType.FIT_XY);

            } catch ( Exception e ) {
                e.printStackTrace( );
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    //앨범에서 선택한 사진이름 가져오기
    public String getFileName( Uri uri ) {
        Cursor cursor = getContentResolver( ).query( uri, null, null, null, null );
        try {
            if ( cursor == null ) return null;
            cursor.moveToFirst( );
            @SuppressLint("Range") String fileName = cursor.getString( cursor.getColumnIndex( OpenableColumns.DISPLAY_NAME ) );
            cursor.close( );
            return fileName;

        } catch ( Exception e ) {
            e.printStackTrace( );
            cursor.close( );
            return null;
        }
    }

    //이미지뷰에 뿌려질 앨범 비트맵 반환
    public Bitmap getBitmapAlbum( View targetView, Uri uri ) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver( ).openFileDescriptor( uri, "r" );
            if ( parcelFileDescriptor == null ) return null;
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor( );
            if ( fileDescriptor == null ) return null;

            int targetW = targetView.getWidth( );
            int targetH = targetView.getHeight( );

            BitmapFactory.Options options = new BitmapFactory.Options( );
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeFileDescriptor( fileDescriptor, null, options );

            int photoW = options.outWidth;
            int photoH = options.outHeight;

            int scaleFactor = Math.min( photoW / targetW, photoH / targetH );
            if ( scaleFactor >= 8 ) {
                options.inSampleSize = 8;
            } else if ( scaleFactor >= 4 ) {
                options.inSampleSize = 4;
            } else {
                options.inSampleSize = 2;
            }
            options.inJustDecodeBounds = false;

            Bitmap reSizeBit = BitmapFactory.decodeFileDescriptor( fileDescriptor, null, options );

            ExifInterface exifInterface = null;
            try {
                if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                    exifInterface = new ExifInterface( fileDescriptor );
                }
            } catch ( IOException e ) {
                e.printStackTrace( );
            }

            int exifOrientation;
            int exifDegree = 0;

            //사진 회전값 구하기
            if ( exifInterface != null ) {
                exifOrientation = exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL );

                if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_90 ) {
                    exifDegree = 90;
                } else if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_180 ) {
                    exifDegree = 180;
                } else if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_270 ) {
                    exifDegree = 270;
                }
            }

            parcelFileDescriptor.close( );
            Matrix matrix = new Matrix( );
            matrix.postRotate( exifDegree );

            Bitmap reSizeExifBitmap = Bitmap.createBitmap( reSizeBit, 0, 0, reSizeBit.getWidth( ), reSizeBit.getHeight( ), matrix, true );
            return reSizeExifBitmap;

        } catch ( Exception e ) {
            e.printStackTrace( );
            return null;
        }
    }
}