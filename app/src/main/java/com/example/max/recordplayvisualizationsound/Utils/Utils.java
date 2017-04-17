package com.example.max.recordplayvisualizationsound.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import com.example.max.recordplayvisualizationsound.Objects.Complex;
import com.example.max.recordplayvisualizationsound.Objects.FFT;
import com.example.max.recordplayvisualizationsound.Objects.FrequencyGraphPoints;
import com.example.max.recordplayvisualizationsound.Objects.FrequencyGraphPoint;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by Max on 15.04.2017.
 */

public class Utils {

    public static final String RECORDS_DIR = "/RecordPlayVisualizationSound/Records/";
    public static final String POINTS_DIR = "/RecordPlayVisualizationSound/Points/";
    public static final int TIME_FRAME_FOR_FREQUENCY = 250;

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


    public static byte[] convert3gpToByteArray(String AudioSavePathInDevice ){

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
            temp = (double)((tempSignal[2*i] & 0xFF) | (tempSignal[2*i+1] << 8)) / 32768.0F;
            complexSignal[i] = new Complex(temp,0.0);
        }

        y = FFT.fft(complexSignal);

        mMaxFFTSample = 0.0;
        for(int i = 0; i < (mNumberOfFFTPoints/2); i++)
        {
            absSignal[i] = Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2));
            if(absSignal[i] > mMaxFFTSample)
            {
                mMaxFFTSample = absSignal[i];
            }
        }
        return absSignal;
    }
    public static List<FrequencyGraphPoint> prepareDataForGraph(double[] frequency, int trackDuration){
        int timeForGraph = 0;
        List<FrequencyGraphPoint> dataForGraph = new ArrayList<FrequencyGraphPoint>();
        int frequencyDataLength = frequency.length;
        int timeCoefficient = trackDuration/TIME_FRAME_FOR_FREQUENCY;
        FrequencyGraphPoint tempFrequencyGraphPoint;
        int compressionCoefficient = frequencyDataLength/timeCoefficient;
        int frequencyCompressedSum;
        for (int i =0; i<timeCoefficient; i++){
            frequencyCompressedSum = 0;
            tempFrequencyGraphPoint = new FrequencyGraphPoint();
            tempFrequencyGraphPoint.setPointX(timeForGraph);
            timeForGraph+=TIME_FRAME_FOR_FREQUENCY;
            for(int j = 0; j<compressionCoefficient;j++){
                frequencyCompressedSum+=frequency[i*compressionCoefficient+j];
            }
            if(frequencyCompressedSum == 0){
                tempFrequencyGraphPoint.setPointY(0);
            }
            else{
            tempFrequencyGraphPoint.setPointY(frequencyCompressedSum/compressionCoefficient);
            }
            dataForGraph.add(tempFrequencyGraphPoint);
        }
        EventBus.getDefault().post(new MessageEvent(Messages.DATA_FOR_GRAPH_READY, dataForGraph));
        return  dataForGraph;
    }
   public static void objectToJSON(List<FrequencyGraphPoint> frequencyGraphPointList, Context context){
       Gson gson = new Gson();
       String tempString = gson.toJson(frequencyGraphPointList);
       FileOutputStream outputStream;
       String path =Utils.checkCreateFolder(Utils.POINTS_DIR) +
               Utils.CreateRandomAudioFileName(5) + "Points.json";

       try {
           outputStream = new FileOutputStream(new File(path));
           outputStream.write(tempString.getBytes());
           outputStream.close();
           EventBus.getDefault().post(new MessageEvent(Messages.CONVERT_FREQUENCY_TO_JSON_HAS_ENDED,null));
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

  public static void objectToXML(List<FrequencyGraphPoint> frequencyGraphPointList)  {
      FrequencyGraphPoints frequencyGrapPoints  = new FrequencyGraphPoints();
      frequencyGrapPoints.setFrequencyGraphPointList(frequencyGraphPointList);
      String path =Utils.checkCreateFolder(Utils.POINTS_DIR) +
              Utils.CreateRandomAudioFileName(5) + "Points.xml";
      File xmlFile = new File(path);
      try
      {
          Serializer serializer = new Persister();
          serializer.write(frequencyGrapPoints, xmlFile);
          EventBus.getDefault().post(new MessageEvent(Messages.CONVERT_FREQUENCY_TO_XML_HAS_ENDED,null));
      }
      catch (Exception e)
      {
          e.printStackTrace();
      }
  }
}
