package com.example.videoview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;




public class VisualizerView extends View {

    private static final int LINE_WIDTH = 3; // width of visualizer lines
    private static final int LINE_SCALE = 75; // scales visualizer lines
    private final Paint linePaint; // specifies line drawing characteristics
    private List<Float> amplitudes = new ArrayList<>(); // amplitudes for line lengths
    private int width; // width of this View
    private int height; // height of this View
    private final Paint linePaint2; // specifies line drawing characteristic
    private List<List<Float>> segment = new ArrayList<>();

    // constructor
    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs); // call superclass constructor
        linePaint = new Paint(); // create Paint for lines
        linePaint2 = new Paint(); // create Paint for lines
        linePaint.setColor(Color.GREEN); // set color to green
        linePaint.setStrokeWidth(LINE_WIDTH); // set stroke width
        linePaint2.setStrokeWidth(3f);
        linePaint2.setColor(Color.RED);
        linePaint2.setStyle(Paint.Style.STROKE);
        setContentDescription(null);
        segment.add(Arrays.asList(100f,100f,100f));
    }

    // called when the dimensions of the View change
    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        width = w; // new width of this View`
        height = h; // new height of this View
        amplitudes = new ArrayList<>(width / LINE_WIDTH);
    }


     //Clears all amplitudes to prepare for a new visualization

    public void clear() {
        amplitudes.clear();
        invalidate();
    }
    public void addback(List<List<Float>> seg) {
        segment = seg;
    }

     //@param amplitude Adds the given amplitude value to the amplitudes ArrayList

    public void addAmplitude(float amplitude) {
        amplitudes.add(amplitude); // add newest to the amplitudes ArrayList

        // if the power lines completely fill the VisualizerView
        if (amplitudes.size() * LINE_WIDTH >= width) {
            amplitudes.remove(0); // remove oldest power value
        }
    }
    // draw the visualizer with scaled lines representing the amplitudes
    @Override
    public void onDraw(Canvas canvas) {
        if (amplitudes.isEmpty()) canvas.drawColor(Color.TRANSPARENT);
       // if (amplitudes.size() * LINE_WIDTH >= width) {
        //    amplitudes.remove(0); // remove oldest power value
       // }
        int middle = height / 2; // get the middle of the View
        float height2 = height-50;
        float heightre = height2/18f;
        float widthre = (width-100)/10;
        float curX = 0; // start curX at zero
        if(!(segment.isEmpty())) {
            for (List<Float> segline : segment) {
                float a = segline.get(0) * widthre;
                float b = segline.get(1) * widthre;
                float c = (segline.get(2) +0.5f)* heightre;
                canvas.drawRect(a, height2*70.5f/18f-c, b, height2*70.5f/18f-c+height2/18f , linePaint2);
            }
        }
        // for each item in the amplitudes ArrayList
        for (float power : amplitudes) {
            float scaledHeight = power*heightre; // scale the power
            curX += 5.53f; // increase X by LINE_WIDTH

            // draw a line representing this item in the amplitudes ArrayLis
            canvas.drawLine(curX, height2*70.5f/18f - scaledHeight , curX+5.53f, height2*70.5f/18f - scaledHeight, linePaint);
        }
    }

}
