package org.js.tt5b;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView tvPhase;
    ConstraintLayout clDis;
    ConstraintLayout clDur;
    TextView tvSecDis;
    TextView tvSecDur;
    Button bAbort;
    Button bAbase;
    Button bBbase;
    Button bMotor;
    TextView tvNbase;
    TextView tvNmotor;

    Context context;
    Integer mainCD=800;
    TextToSpeech t2s=null;
    Boolean t2sInit=false;
    public enum Phase {PREP, DIST, DURA, ENDED};
    Phase curPhase=Phase.PREP;
    Drawable dBlue,dOrange;
    Integer nbMotRun=0;
    Integer motAlrt=null;
    Integer Nbase=0;
    Long msStart;
    Long nxtMs;
    int reqStart=10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=getApplicationContext();
        tvPhase=(TextView) findViewById(R.id.phase);
        bAbort=(Button) findViewById(R.id.abort);
        clDis=(ConstraintLayout) findViewById(R.id.dis);
        dOrange=clDis.getBackground();
        clDur=(ConstraintLayout) findViewById(R.id.dur);
        dBlue=clDur.getBackground();
        tvSecDis=(TextView) findViewById(R.id.secDis);
        tvSecDur=(TextView) findViewById(R.id.secDur);
        bAbase=(Button) findViewById(R.id.baseA);
        bBbase=(Button) findViewById(R.id.baseB);
        bMotor=(Button) findViewById(R.id.motor);
        tvNbase=(TextView) findViewById(R.id.nbBase);
        tvNmotor=(TextView) findViewById(R.id.motNb);
        bAbort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        t2s=new TextToSpeech(context,onInit);
        setPhase(Phase.PREP);
        bAbase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.playSoundEffect(SoundEffectConstants.CLICK);
                pushA();
            }
        });
        bBbase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.playSoundEffect(SoundEffectConstants.CLICK);
                pushB();
            }
        });
        bMotor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.playSoundEffect(SoundEffectConstants.CLICK);
                pushM();
            }
        });
        Intent strtAct=new Intent(this,Start.class);
        startActivityForResult(strtAct,reqStart);
    }

    @Override
    public void onDestroy(){
        if (t2s!=null) t2s.shutdown();
        super.onDestroy();
    }

    private TextToSpeech.OnInitListener onInit=new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status==TextToSpeech.SUCCESS){
                t2s.setLanguage(Locale.ENGLISH);
                t2sInit=true;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode==reqStart && curPhase==Phase.PREP){
            msStart= SystemClock.uptimeMillis();
            nxtMs=msStart+1000;
            nbMotRun++;
            setPhase(Phase.DIST);
            tvNmotor.setText(nbMotRun.toString());
            mHandler.postAtTime(timerTask,nxtMs);
        }
    }

    void setPhase(Phase nwPhase){
        switch (nwPhase){
            case PREP:
                clDis.setBackground(dOrange);
                clDur.setBackground(dBlue);
                tvPhase.setText("Preparation");
                bAbase.setText("Start");
                bAbase.setEnabled(true);
                bBbase.setEnabled(false);
                bMotor.setEnabled(false);
                break;
            case DIST:
                clDis.setBackground(dOrange);
                clDur.setBackground(dBlue);
                tvPhase.setText("Distance");
                bAbase.setText("Base A");
                bAbase.setEnabled(false);
                bBbase.setEnabled(true);
                bMotor.setEnabled(true);
                break;
            case DURA:
                clDis.setBackground(dBlue);
                clDur.setBackground(dOrange);
                tvPhase.setText("Duration");
                bAbase.setEnabled(false);
                bBbase.setEnabled(false);
                bMotor.setEnabled(false);
                break;
            case ENDED:
                clDis.setBackground(dBlue);
                clDur.setBackground(dBlue);
                tvPhase.setText("Terminated");
                break;
        }
        curPhase=nwPhase;
    }

    void pushM(){
        if (curPhase!=Phase.DIST) return;
        nbMotRun++;
        tvNmotor.setText((nbMotRun.toString()));
        if (nbMotRun>9) tvNmotor.setBackgroundColor(Color.RED);
        else if (nbMotRun>8) tvNmotor.setBackgroundColor(Color.YELLOW);
        if (nbMotRun>7){
            Integer distCD=mainCD-600;
            motAlrt=distCD-3;
        }
    }

    void pushA(){
        if (curPhase==Phase.PREP){
            msStart= SystemClock.uptimeMillis();
            nxtMs=msStart+1000;
            nbMotRun++;
            setPhase(Phase.DIST);
            tvNmotor.setText(nbMotRun.toString());
            mHandler.postAtTime(timerTask,nxtMs);
        } else if (curPhase==Phase.DIST){
            Nbase++;
            bAbase.setEnabled(false);
            bBbase.setEnabled(true);
            tvNbase.setText(Nbase.toString());
        }
    }

    void pushB(){
        if (curPhase==Phase.DIST){
            Nbase++;
            bAbase.setEnabled(true);
            bBbase.setEnabled(false);
            tvNbase.setText(Nbase.toString());
        }
    }

    private Handler mHandler=new Handler();
    private Runnable timerTask=new Runnable() {
        @Override
        public void run() {
            nxtCount();
        }
    };

    void nxtCount(){
        mainCD--;
        nxtMs+=1000;
        if (curPhase==Phase.DIST){
            mHandler.postAtTime(timerTask,nxtMs);
            Integer distCD=mainCD-600;
            tvSecDis.setText(distCD.toString());
            if (t2sInit) {
                if (distCD < 11) t2s.speak(distCD.toString(), TextToSpeech.QUEUE_FLUSH, null);
                else {
                    if (motAlrt!=null){
                        if (distCD<=motAlrt){
                            t2s.speak("Motor "+nbMotRun.toString(),TextToSpeech.QUEUE_FLUSH,
                                    null);
                            motAlrt=null;
                        }
                    }
                    if (distCD < 51) {
                        int rest = distCD % 10;
                        if (rest == 0) t2s.speak(distCD.toString(), TextToSpeech.QUEUE_FLUSH,
                                null);
                    } else if (distCD == 100) t2s.speak(distCD.toString(), TextToSpeech.QUEUE_FLUSH,
                            null);
                    else if (distCD == 195) t2s.speak("Distance", TextToSpeech.QUEUE_FLUSH,
                            null);
                }
            }
            if (distCD==0) setPhase(Phase.DURA);
        } else if (curPhase==Phase.DURA){
            tvSecDur.setText(mainCD.toString());
            if (t2sInit){
                if (mainCD<11) t2s.speak(mainCD.toString(),TextToSpeech.QUEUE_FLUSH,null);
                else if (mainCD<61){
                    int rest=mainCD%10;
                    if (rest==0) t2s.speak(mainCD.toString(),TextToSpeech.QUEUE_FLUSH,null);
                }
                else if (mainCD<301){
                    int rest=mainCD%60;
                    if (rest==0){
                        Integer min=mainCD/60;
                        t2s.speak(min.toString()+" minutes",TextToSpeech.QUEUE_FLUSH,
                                null);
                    }
                } else if (mainCD==590) t2s.speak("Duration",TextToSpeech.QUEUE_FLUSH,
                        null);
            }
            if(mainCD>0){
                mHandler.postAtTime(timerTask,nxtMs);
            } else setPhase(Phase.ENDED);
        }

    }
}
