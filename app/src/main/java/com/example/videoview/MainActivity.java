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
import android.content.Context;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private VideoView videoView;
    private Button btn_start;
    private Button btn_pause;
    private Button btn_stop;
    private LinearLayout visLayout;
    private VisualizerView visView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        visView = new VisualizerView(getApplicationContext(),null);
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

        dispatcher.addAudioProcessor(new PitchProcessor(PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, new PitchDetectionHandler() {

            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                    AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                runOnUiThread( new Runnable(){
                    @Override
                    public void run() {
                        TextView text = (TextView) findViewById(R.id.textView);
                        text.setText("" + pitchInHz);
                        visView.addAmplitude(pitchInHz*70); // update the VisualizeView
                        visView.invalidate(); // refresh the VisualizerView
                    }
                });

            }
        }));
        new Thread(dispatcher, "Audio Dispatcher").start();

        visLayout.removeAllViews();
        visLayout.addView(visView);
    }


    private void bindViews() {
        videoView = (VideoView) findViewById(R.id.videoView);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_pause = (Button) findViewById(R.id.btn_pause);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        visLayout = (LinearLayout) findViewById(R.id.visualizer);


        btn_start.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_stop.setOnClickListener(this);

        //根据文件路径播放
        //if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        //    videoView.setVideoPath(Environment.getExternalStorageDirectory() + "/lesson.mp4");
        //}

        //读取放在 raw 目录下的文件
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.heart));
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
            case R.id.btn_stop:
                videoView.stopPlayback();
                break;
        }
    }
}
