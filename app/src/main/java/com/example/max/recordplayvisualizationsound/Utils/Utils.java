package com.example.max.recordplayvisualizationsound.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import com.example.max.recordplayvisualizationsound.Objects.Complex;
import com.example.max.recordplayvisualizationsound.Objects.FFT;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by Max on 15.04.2017.
 */

public class Utils {

    public static final String RECORDS_DIR = "/RecordPlayVisualizationSound/Records/";
    public static final String POINTS_DIR = "/RecordPlayVisualizationSound/Points/";

    public static boolean checkPermission(Context gottenApplicationContext) {
        int result = ContextCompat.checkSelfPermission(gottenApplicationContext,
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(gottenApplicationContext,
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    public static String checkCreateFolder(String directory_path){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+directory_path;
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        return path;
    }
    public static String CreateRandomAudioFileName(int string){
        Random random = new Random();
        String RandomAudioFileName = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder stringBuilder = new StringBuilder( string );
        int i = 0 ;
        while(i < string ) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));

            i++ ;
        }
        return stringBuilder.toString();
    }

   /* public static void MediaRecorderReady(MediaRecorder mediaRecorder, String AudioSavePathInDevice){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }*/

    public static byte[] convert3gpToByteArray(String AudioSavePathInDevice ){

      /*  MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(AudioSavePathInDevice);// the adresss location of the sound on sdcard.
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        MediaFormat mf = mex.getTrackFormat(0);
        int duration = mf.getInteger(MediaFormat.KEY_DURATION);
        int sampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);*/
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(AudioSavePathInDevice);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            while (fileInputStream.available() > 0) {
                byteArrayOutputStream.write(fileInputStream.read());
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static double[] calculateFFT(byte[] signal)
    {
       final int mNumberOfFFTPoints =1024;
        double compressC = signal.length/mNumberOfFFTPoints;
        byte[] tempSignal = new byte[2048];
        int j = 0;
        for(int i=0; i<signal.length;i+=compressC){

            tempSignal[j] = signal[i];
            j++;
        }
        double mMaxFFTSample;

        double temp;
        Complex[] y;
        Complex[] complexSignal = new Complex[mNumberOfFFTPoints];
        double[] absSignal = new double[mNumberOfFFTPoints/2];

        for(int i = 0; i < mNumberOfFFTPoints; i++){
           // temp = (double)((signal[2*i] & 0xFF) | (signal[2*i+1] << 8)) / 32768.0F;
            temp = (double)((tempSignal[2*i] & 0xFF) | (tempSignal[2*i+1] << 8)) / 32768.0F;
            complexSignal[i] = new Complex(temp,0.0);
        }

        y = FFT.fft(complexSignal);

        mMaxFFTSample = 0.0;
       // mPeakPos = 0;
        for(int i = 0; i < (mNumberOfFFTPoints/2); i++)
        {
            absSignal[i] = Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2));
            if(absSignal[i] > mMaxFFTSample)
            {
                mMaxFFTSample = absSignal[i];
              //  mPeakPos = i;
            }
        }

        return absSignal;

    }

}
