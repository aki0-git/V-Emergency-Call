package jp.enigma.v_emergency_call;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class MainActivity extends AppCompatActivity {

    private boolean check_permission_text;
    long pattern[] = {1000, 1000};
    private Vibrator vibrator;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckBox check_permission = findViewById(R.id.check_permission);
        TextView save_button = findViewById(R.id.save_button);
        ImageView hero = findViewById(R.id.hero);

        SharedPreferences pref = getSharedPreferences("v_emergency_call", MODE_PRIVATE);
        check_permission_text = pref.getBoolean("check_permission", false);

        vibrator = ((Vibrator) getSystemService(VIBRATOR_SERVICE));

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) { }
        });

        if(check_permission_text) {
            startRinging();
        }

        check_permission.setChecked(check_permission_text);

        hero.setOnClickListener(v -> {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(this);
            } else {
                Log.d("TAG", "The interstitial ad wasn't ready yet.");
            }
            Intent intent = new Intent(v.getContext(), ManualActivity.class);
            startActivity(intent);
        });

        save_button.setOnClickListener(v -> {
            // import android.content.SharedPreferences;
            SharedPreferences pref1 = getSharedPreferences("v_emergency_call", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref1.edit();
            editor.putBoolean("check_permission", check_permission.isChecked());
            editor.apply();
            finish();
        });

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i("onAdLoaded", "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("onAdFailedToLoad", loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });

    }

    private void startRinging() {
        Log.e("startRinging", "start");
        int ringerMode = ((AudioManager) getSystemService(AUDIO_SERVICE)).getRingerMode();

        if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            VibrationEffect vibe = VibrationEffect.createWaveform(pattern, 0);  //第二引数にはpattern配列の初期インデックスが入る。
            vibrator.vibrate(vibe);
        } else {
            vibrator.vibrate(pattern, 0);
        }

        if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            return;
        }

        Ringtone ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE));
        ringtone.play();
    }

    @Override
    public void onPause() {
        super.onPause();
        vibrator.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(check_permission_text) {
            startRinging();
        }
    }

}