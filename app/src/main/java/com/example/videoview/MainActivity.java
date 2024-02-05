package com.example.videoview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.net.Uri;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import android.widget.TextView;
import com.example.videoview.VisualizerView;
import com.opencsv.CSVReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private VideoView videoView;
    private Button btn_start;
    private Button btn_pause;
    private LinearLayout visLayout;
    private VisualizerView visView ;
    private TextView text2;
    private List<List<Float>> barChart = new ArrayList<>();
    private List<List<Float>> barChart2 = new ArrayList<>();
    private List<List<List<Float>>> segChart = new ArrayList<>();
    private int nframe = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        text2 = (TextView) findViewById(R.id.textView2);
        visView = new VisualizerView(getApplicationContext(),null);
        visLayout.removeAllViews();
        visLayout.addView(visView);
        readCSVAndChop();
        for (List<Float> rebar : barChart) {
            if ( Math.floor(rebar.get(1)/10)-Math.floor(rebar.get(0)/10) == 1){
                float tens = (float)Math.floor(rebar.get(1)/10)*10;
                barChart2.add(Arrays.asList(rebar.get(0),tens,rebar.get(2)));
                barChart2.add(Arrays.asList(tens,rebar.get(1),rebar.get(2)));
            }
            else{
                barChart2.add(rebar);
            }
        }
        for (int i = 0; i*10 < barChart2.get(barChart2.size()-1).get(0); i++) {
            List<List<Float>> temp= new ArrayList<>();
            for (List<Float> bar : barChart2) {
                if(bar.get(0)/10 < i+1 && bar.get(0)/10 >= i) {
                    float aa = bar.get(0)-i*10;
                    float ab = bar.get(1)-i*10;
                    temp.add(Arrays.asList(aa,ab,bar.get(2)));
                }
            }
            segChart.add(temp);
        }
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 2048, 1536);
        dispatcher.addAudioProcessor(new PitchProcessor(PitchEstimationAlgorithm.FFT_YIN, 44100, 2048, new PitchDetectionHandler() {

            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                    AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                runOnUiThread( new Runnable(){
                    @Override
                    public void run() {
                        double semitone;
                        if(pitchInHz < 0.000001f){
                            semitone = 0;
                        }
                        else{
                            //semitone = 138 + 24 * Math.log(pitchInHz / 440f) / Math.log(2f);
                            semitone = 69 + 12 * Math.log(pitchInHz / 440f) / Math.log(2f);
                        }
                        //float semitoner = Math.round(semitone)/2f;
                        TextView text = (TextView) findViewById(R.id.textView);
                        text.setText("" + semitone);
                        float fsemi = (float)semitone;
                        visView.addAmplitude(fsemi); // update the VisualizeView
                        visView.invalidate(); // refresh the VisualizerView
                        int ntime = videoView.getCurrentPosition();
                        if(ntime/10000 >= segChart.size()){
                            List<List<Float>> newChart = new ArrayList<>();
                            visView.addback( newChart);}
                        else{visView.addback(segChart.get(ntime/10000));}
                        text2.setText("" + ntime);
                        if (ntime - nframe*10000 > 0) {
                            nframe ++;
                            visView.clear(); // remove oldest power value
                        }
                    }
                });
            }
        }));
        new Thread(dispatcher, "Audio Dispatcher").start();

    }


    private void bindViews() {
        videoView = (VideoView) findViewById(R.id.videoView);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_pause = (Button) findViewById(R.id.btn_pause);
        visLayout = (LinearLayout) findViewById(R.id.visualizer);


        btn_start.setOnClickListener(this);
        btn_pause.setOnClickListener(this);

        //根据文件路径播放
        //if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        //    videoView.setVideoPath(Environment.getExternalStorageDirectory() + "/lesson.mp4");
        //}

        //读取放在 raw 目录下的文件
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.dontleave));
        videoView.setMediaController(new MediaController(this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                videoView.start();
                break;
            case R.id.btn_pause:
                videoView.pause();
                break;
        }
    }
    public void readCSVAndChop() {
        try{
            CSVReader reader = new CSVReader(new InputStreamReader(getResources().openRawResource(R.raw.dontleave_ly2)));//Specify asset file name
            String [] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                barChart.add(Arrays.asList(Float.valueOf(nextLine[0]),Float.valueOf(nextLine[1]),Float.valueOf(nextLine[2])));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        }

}
