package com.example.max.recordplayvisualizationsound.Activiites;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.max.recordplayvisualizationsound.Objects.FrequencyGraphPoint;
import com.example.max.recordplayvisualizationsound.R;
import com.example.max.recordplayvisualizationsound.Utils.MessageEvent;
import com.example.max.recordplayvisualizationsound.Utils.Messages;
import com.example.max.recordplayvisualizationsound.Utils.Utils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.button_record)
    Button buttonRecord;

    @BindView(R.id.button_stop)
    Button buttonStop;

    @BindView(R.id.button_play)
    Button buttonPlay;

    @BindView(R.id.button_stop_playing_recording)
    Button buttonStopPlayingRecording;

    @BindView(R.id.button_to_json_xml)
    Button buttonToJSONXML;

    @BindView(R.id.chartMain)
    BarChart frequencyBarChart;

    private static String AudioSavePathInDevice = null;//todo refactor later!!!
    private static MediaRecorder mediaRecorder ;
   // Random random ;
  //  String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer ;
    double [] frequency1;
    ArrayList<FrequencyGraphPoint> frequencyGraphPointArrayList;
    int recordDuration;
    public static int barCounter = 1;
     public static BarData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {//todo refactor later!!!
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initOnClickListeners();
       // random = new Random();
      //  FirebaseCrash.report(new Exception("My first Android non-fatal error"));//crash reporting
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-5405208829283027~3111027393");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Subscribe
    public void onMessageEvent(MessageEvent event) {
        switch (event.message){
            case PLAY_SOUND_HAS_ENDED:
                buttonPlay.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);
                buttonRecord.setEnabled(true);
                frequencyBarChart.setVisibility(View.GONE);
                break;
            case BLOCK_STOP_HAS_ENDED:
                buttonStop.setEnabled(true);
                break;
            case DATA_FOR_GRAPH_READY:
                frequencyGraphPointArrayList = (ArrayList<FrequencyGraphPoint>) event.link;

            //    String xmlTest = Utils.objectToXML(frequencyGraphPointArrayList);


                buttonPlay.setEnabled(true);
                buttonToJSONXML.setEnabled(true);
                break;
            case CONVERT_FREQUENCY_TO_XML_HAS_ENDED:
                Toast.makeText(this, "Saving xml file was successfully",Toast.LENGTH_SHORT).show();
                Utils.objectToJSON(frequencyGraphPointArrayList,this);
                break;
            case CONVERT_FREQUENCY_TO_JSON_HAS_ENDED:
                Toast.makeText(this, "Saving json file was successfully",Toast.LENGTH_SHORT).show();
                break;
            case TIME_FREEZE_HAS_ENDED:
                /*FrequencyGraphPoint tempFrequencyGraphPoint = frequencyGraphPointArrayList.get(barCounter);
                data.addEntry(new BarEntry((float) tempFrequencyGraphPoint.getPointX(),(float) tempFrequencyGraphPoint.getPointY()),barCounter);
                data.notifyDataChanged();*/
                if(barCounter==0){
                frequencyBarChart.invalidate();
                    barCounter++;
                }
                else if(barCounter<frequencyGraphPointArrayList.size()){
                    data = new BarData(getDataSet(frequencyGraphPointArrayList,barCounter));
                    frequencyBarChart.setData(data);
                    frequencyBarChart.invalidate();
                    barCounter++;

                    Handler handler = new Handler();
                    Runnable r = new Runnable(){
                        public void run() {
                            try {

                                Thread.sleep(Utils.TIME_FRAME_FOR_FREQUENCY);
                                EventBus.getDefault().post(new MessageEvent(Messages.TIME_FREEZE_HAS_ENDED,null));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    };
                    handler.post(r);
                }
                else if(barCounter == frequencyGraphPointArrayList.size()){
                    barCounter = 1;
                }

                break;

        }
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.button_record:
                buttonToJSONXML.setEnabled(false);
                buttonPlay.setEnabled(false);
                frequencyBarChart.setVisibility(View.GONE);
                if(Utils.checkPermission(getApplicationContext())) {

                    AudioSavePathInDevice =
                            Utils.checkCreateFolder(Utils.RECORDS_DIR) +
                                    Utils.CreateRandomAudioFileName(5) + "AudioRecording.3gp";

                    MediaRecorderReady();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    buttonRecord.setEnabled(false);
                    Handler handler = new Handler();
                    Runnable r = new Runnable(){
                        public void run() {
                            try {
                                Thread.sleep(1500);
                                EventBus.getDefault().post(new MessageEvent(Messages.BLOCK_STOP_HAS_ENDED,null));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    };
                    handler.post(r);
                   // buttonStop.setEnabled(true);

                    Toast.makeText(MainActivity.this, "Recording started",
                            Toast.LENGTH_LONG).show();
                } else {
                    requestPermission();
                }
                break;
            case R.id.button_stop:
                mediaRecorder.stop();
                buttonStop.setEnabled(false);
               // buttonPlay.setEnabled(true);
                buttonRecord.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);
                byte [] bytes = Utils.convert3gpToByteArray(AudioSavePathInDevice);
                frequency1 = Utils.calculateFFT(bytes);
              //  recordDuration = Utils.getDuration(AudioSavePathInDevice);
             //   Utils.prepareDataForGraph(frequency1,recordDuration);
                Toast.makeText(MainActivity.this, "Recording Completed",
                        Toast.LENGTH_LONG).show();
              //  buttonPlay.setEnabled(true);
                recordDuration = prepareMediaPlayerGetDuration(mediaPlayer,AudioSavePathInDevice);
                Utils.prepareDataForGraph(frequency1,recordDuration);
                break;
            case R.id.button_play:

                buttonStop.setEnabled(false);
                buttonRecord.setEnabled(false);
                buttonPlay.setEnabled(false);
                buttonStopPlayingRecording.setEnabled(true);
                frequencyBarChart.setVisibility(View.VISIBLE);
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        EventBus.getDefault().post(new MessageEvent(Messages.PLAY_SOUND_HAS_ENDED, null));
                    }
                });
                Toast.makeText(MainActivity.this, "Recording Playing",
                        Toast.LENGTH_LONG).show();
                data = new BarData(getDataSet(frequencyGraphPointArrayList,barCounter));
                frequencyBarChart.setData(data);
                frequencyBarChart.invalidate();
                Handler handler = new Handler();
                Runnable r = new Runnable(){
                    public void run() {
                        try {

                            Thread.sleep(Utils.TIME_FRAME_FOR_FREQUENCY);
                            EventBus.getDefault().post(new MessageEvent(Messages.TIME_FREEZE_HAS_ENDED,null));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                };
                handler.post(r);
               // frequencyBarChart.setDescription("My Chart");
               // frequencyBarChart.animateXY(2000, 2000);

                break;
            case R.id.button_stop_playing_recording:
                buttonStop.setEnabled(false);
                buttonRecord.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);
                buttonPlay.setEnabled(true);

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    MediaRecorderReady();
                }
                break;
            case R.id.button_to_json_xml:
                Toast.makeText(this, "Save me ", Toast.LENGTH_SHORT).show();
                Utils.objectToXML(frequencyGraphPointArrayList);
                break;

        }
    }

    public void initOnClickListeners(){
        buttonRecord.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
        buttonStop.setEnabled(false);
        buttonPlay.setOnClickListener(this);
        buttonPlay.setEnabled(false);
        buttonStopPlayingRecording.setOnClickListener(this);
        buttonStopPlayingRecording.setEnabled(false);
        buttonToJSONXML.setOnClickListener(this);
        frequencyBarChart.setVisibility(View.GONE);
    }
    public  void MediaRecorderReady(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    private int prepareMediaPlayerGetDuration(MediaPlayer mediaPlayer, String AudioSavePathInDevice){
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(AudioSavePathInDevice);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mediaPlayer.getDuration();
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(MainActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private ArrayList<IBarDataSet> getDataSet(List<FrequencyGraphPoint> frequencyGraphPointList, int amount) {
        ArrayList<IBarDataSet> dataSets = null;

        ArrayList<BarEntry> valueSet1 = new ArrayList<>();
        FrequencyGraphPoint fGP;
        for(int i = 0; i<amount;i++){
            fGP = frequencyGraphPointList.get(i);
            BarEntry barEntry = new BarEntry((float) fGP.getPointX(), (float) fGP.getPointY()); // Jan
            valueSet1.add(barEntry);

        }
      /*  for(FrequencyGraphPoint fGP:frequencyGraphPointList){
            BarEntry barEntry = new BarEntry((float) fGP.getPointX(), (float) fGP.getPointY()); // Jan
            valueSet1.add(barEntry);
        }*/

        BarDataSet barDataSet1 = new BarDataSet(valueSet1, "Frequency");
        barDataSet1.setBarBorderWidth(3);
        barDataSet1.setBarBorderColor(Color.rgb(0, 155, 0));
        barDataSet1.setColor(Color.rgb(0, 155, 0));
        dataSets = new ArrayList<>();
        dataSets.add(barDataSet1);
        return dataSets;
    }

    private ArrayList<String> getXAxisValues(List<FrequencyGraphPoint> frequencyGraphPointList) {
        ArrayList<String> xAxis = new ArrayList<>();
        for(FrequencyGraphPoint fGP:frequencyGraphPointList){
           xAxis.add(""+fGP.getPointX());
        }
        return xAxis;
    }
}
