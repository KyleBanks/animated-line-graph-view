package co.blankkeys.algvdemo;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import co.blankkeys.animatedlinegraphview.AnimatedLineGraphView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int[] GRAPHS = new int[] {R.id.graph_default, R.id.graph_colored, R.id.graph_fast, R.id.graph_slow, R.id.graph_thin, R.id.graph_thick, R.id.graph_padded};

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetGraphs();
                handler.postDelayed(this, 2000);
            }
        }, 0);
    }

    private void resetGraphs() {
        for (int graphId : GRAPHS) {
            AnimatedLineGraphView graph = ((AnimatedLineGraphView) findViewById(graphId));
            graph.setData(generateData());
        }
    }

    private float[] generateData() {
        float[] data = new float[30];
        Random random = new Random();
        for (int i = 0;i < data.length; i++) {
            data[i] = random.nextFloat();
        }

        return data;
    }
}
