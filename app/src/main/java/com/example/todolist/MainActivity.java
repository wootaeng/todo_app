package com.example.todolist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.byappsoft.huvleadlib.ANClickThroughAction;
import com.byappsoft.huvleadlib.AdListener;
import com.byappsoft.huvleadlib.AdView;
import com.byappsoft.huvleadlib.BannerAdView;
import com.byappsoft.huvleadlib.NativeAdResponse;
import com.byappsoft.huvleadlib.ResultCode;
import com.byappsoft.huvleadlib.SDKSettings;
import com.byappsoft.sap.launcher.Sap_act_main_launcher;
import com.byappsoft.sap.utils.Sap_Func;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    Fragment mainFragment;
    EditText inputToDo;
    Context context;

    public static NoteDatabase noteDatabase = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainFragment = new MainFragment();

        //getSupportFragmentManager 을 이용하여 이전에 만들었던 **FrameLayout**에 `fragment_main.xml`이 추가
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mainFragment).commit();

        Button saveButton = findViewById(R.id.saveBtn);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                saveToDo();

                Toast.makeText(getApplicationContext(),"추가되었습니다.",Toast.LENGTH_SHORT).show();

            }
        });
        openDatabase();

        setHuvleAD();
        if(!checkPermission()){
            requestSapPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkPermission()){
            Sap_Func.setNotiBarLockScreen(this, false);
            Sap_act_main_launcher.initsapStart(this, "bynetwork", true, true);
        }
    }

    private boolean checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestSapPermissions() {
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }catch (Exception ignored){
        }
    }

    //huvle
    private void setHuvleAD() {
        SDKSettings.useHttps(true);
        final BannerAdView staticBav = findViewById(R.id.banner_view);
        // 아래 "test" 값은 http://ssp.huvle.com/ 에서 가입 > 매체생성 > zoneid 입력후 테스트 하시고, release시점에 허블에 문의주시면 인증됩니다. 배너사이즈는 변경하지 마세요.
        initBannerView(staticBav, "test",320,50);
    }
    private void initBannerView(final BannerAdView bav, String id, int w , int h) {
        bav.setPlacementID(id);
        bav.setAdSize(w, h);
        bav.setShouldServePSAs(false);
        bav.setClickThroughAction(ANClickThroughAction.OPEN_DEVICE_BROWSER); // 광고 클릭시 브라우저를 기본브라우저로 Open
        bav.setResizeAdToFitContainer(true);
        AdListener adListener = new AdListener() {
            @Override public void onAdRequestFailed(AdView bav, ResultCode errorCode) {/*광고가 없을때 처리*/}
            @Override public void onAdLoaded(AdView bav) {
                Log.v("Huvle_Banner", "The Ad Loaded!");}
            @Override public void onAdLoaded(NativeAdResponse nativeAdResponse) {Log.v("Huvle_Banner", "Ad onAdLoaded NativeAdResponse");}
            @Override public void onAdExpanded(AdView bav) {Log.v("Huvle_Banner", "Ad expanded");}
            @Override public void onAdCollapsed(AdView bav) {Log.v("Huvle_Banner", "Ad collapsed");}
            @Override public void onAdClicked(AdView bav) {Log.v("Huvle_Banner", "Ad clicked; opening browser");}
            @Override public void onAdClicked(AdView adView, String clickUrl) {Log.v("Huvle_Banner", "onAdClicked with click URL");}
            @Override public void onLazyAdLoaded(AdView adView) {}
        };
        bav.setAdListener(adListener);
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                bav.loadAd();
            }
        }, 0);
    }

    private void saveToDo(){
        inputToDo = findViewById(R.id.inputToDo);

        //EditText에 적힌 글을 가져오기
        String todo = inputToDo.getText().toString();

        //테이블에 값을 추가하는 sql구문 insert...
        String sqlSave = "insert into " + NoteDatabase.TABLE_NOTE + " (TODO) values (" +
                "'" + todo + "')";

        //sql문 실행
        NoteDatabase database = NoteDatabase.getInstance(context);
        database.execSQL(sqlSave);

        //저장과 동시에 EditText 안의 글 초기화
        inputToDo.setText("");
    }


    public void openDatabase() {
        // open database
        if (noteDatabase != null) {
            noteDatabase.close();
            noteDatabase = null;
        }

        noteDatabase = NoteDatabase.getInstance(this);
        boolean isOpen = noteDatabase.open();
        if (isOpen) {
            Log.d(TAG, "Note database is open.");
        } else {
            Log.d(TAG, "Note database is not open.");
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (noteDatabase != null) {
            noteDatabase.close();
            noteDatabase = null;
        }
    }


}