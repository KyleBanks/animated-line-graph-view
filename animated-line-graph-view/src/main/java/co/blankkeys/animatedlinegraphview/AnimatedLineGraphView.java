package co.blankkeys.animatedlinegraphview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Displays and animates a line graph painted on the background of the view.
 *
 * Created by kylewbanks on 2017-10-07.
 */
public class AnimatedLineGraphView extends LinearLayout {

    private static final int DEFAULT_ANIMATION_DURATION_MS = 900;
    private static final int DEFAULT_LINE_THICKNESS = 15;
    private static final int DEFAULT_CIRCLE_RADIUS = 20;
    private static final float DEFAULT_GRAPH_PADDING_PCT = 0.02f;
    private static final int DEFAULT_LINE_COLOR = android.R.color.black;
    private static final int DEFAULT_CIRCLE_COLOR = android.R.color.black;

    private int animationDuration;
    private int circleRadius;
    private float paddingPercent;

    private float[] data;

    private Paint linePaint;
    private Paint circlePaint;
    private Path path;
    private float[][] pathPoints;

    private float width;
    private float height;

    private long animationStartTimeMs;
    private int tickCount;

    public AnimatedLineGraphView(Context context) {
        super(context);
        init(null);
    }

    public AnimatedLineGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public AnimatedLineGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimatedLineGraphView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
        calculatePath();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean invalidate = tick();
        canvas.drawPath(path, linePaint);

        if (invalidate) {
            float[] coords = getCurrentCoordinates();
            if (coords != null)
                canvas.drawCircle(coords[0], coords[1], circleRadius, circlePaint);

            postInvalidate();
        }
    }

    /**
     * Initializes the view using the provided AttributeSet to override defaults.
     *
     * @param attrs
     */
    private void init(AttributeSet attrs) {
        // Ensures onDraw is called for this view.
        setWillNotDraw(false);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        path = new Path();

        if (attrs == null) {
            setDefaults();
            return;
        }

        TypedArray attributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.AnimatedLineGraphView, 0, 0);
        try {
            setAnimationDuration(attributes.getInt(R.styleable.AnimatedLineGraphView_duration, DEFAULT_ANIMATION_DURATION_MS));
            setPaddingPercent(attributes.getFloat(R.styleable.AnimatedLineGraphView_paddingPercent, DEFAULT_GRAPH_PADDING_PCT));
            setLineColor(attributes.getResourceId(R.styleable.AnimatedLineGraphView_lineColor, DEFAULT_LINE_COLOR));
            setLineThickness(attributes.getInt(R.styleable.AnimatedLineGraphView_lineThickness, DEFAULT_LINE_THICKNESS));
            setCircleColor(attributes.getResourceId(R.styleable.AnimatedLineGraphView_circleColor, DEFAULT_CIRCLE_COLOR));
            setCircleRadius(attributes.getInt(R.styleable.AnimatedLineGraphView_circleRadius, DEFAULT_CIRCLE_RADIUS));
        } finally {
            attributes.recycle();
        }
    }

    private void setDefaults() {
        setAnimationDuration(DEFAULT_ANIMATION_DURATION_MS);
        setPaddingPercent(DEFAULT_GRAPH_PADDING_PCT);
        setLineColor(DEFAULT_LINE_COLOR);
        setLineThickness(DEFAULT_LINE_THICKNESS);
        setCircleColor(DEFAULT_CIRCLE_COLOR);
        setCircleRadius(DEFAULT_CIRCLE_RADIUS);
    }

    private void reset() {
        if (path != null)
            path.reset();
        pathPoints = null;
        animationStartTimeMs = System.currentTimeMillis();
        tickCount = 0;
        postInvalidate();
    }

    /**
     * Sets the data set to display and recalculates the line path.
     *
     * @param data
     */
    public void setData(float[] data) {
        this.data = data;
        calculatePath();
    }

    /**
     * Calculates and caches the full path that will be used to draw the data set.
     */
    private void calculatePath() {
        reset();
        if (data == null || data.length <= 1) {
            return;
        }

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        int count = data.length;
        for (float point : data) {
            if (point < min) {
                min = point;
            }
            if (point > max) {
                max = point;
            }
        }

        min *= (1 - paddingPercent);
        max *= (1 + paddingPercent);
        float spread = max - min;
        if (spread == 0.0) {
            return;
        }

        pathPoints = new float[count][2];
        for (int i = 0; i < count; i++) {
            float point = data[i];

            float x = width * ((float) i / (count - 1));
            float y = height * ((point - min) / spread);

            pathPoints[i] = new float[]{x, y};
        }
    }

    /**
     * Returns the x/y coordinates to be used based on the current animation progress.
     *
     * @return
     */
    private float[] getCurrentCoordinates() {
        if (pathPoints == null) {
            return null;
        }

        long elapsed = System.currentTimeMillis() - animationStartTimeMs;
        if (elapsed > animationDuration) {
            return null;
        }

        int frame = (int) (pathPoints.length * Math.min(1, elapsed / (float) animationDuration));
        if (frame >= pathPoints.length) {
            return null;
        }

        return pathPoints[frame];
    }

    /**
     * Adds the next animation coordinates to the line path.
     *
     * @return
     */
    private boolean tick() {
        float[] coords = getCurrentCoordinates();
        if (coords == null) {
            return false;
        }

        if (tickCount == 0) {
            path.moveTo(pathPoints[0][0], pathPoints[0][1]);
        } else {
            path.lineTo(coords[0], coords[1]);
        }

        tickCount ++;
        return true;
    }

    /**
     * Returns the animation duration, in milliseconds.
     *
     * @return
     */
    public int getAnimationDuration() {
        return animationDuration;
    }

    /**
     * Sets the animation duration, in milliseconds.
     *
     * @param animationDuration
     */
    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    /**
     * Sets the thickness of the drawn line.
     *
     * @param lineThickness
     */
    public void setLineThickness(int lineThickness) {
        this.linePaint.setStrokeWidth(lineThickness);
    }

    /**
     * Returns the radius of the circle.
     *
     * @return
     */
    public int getCircleRadius() {
        return circleRadius;
    }

    /**
     * Sets the radius of the circle to draw.
     *
     * @param circleRadius
     */
    public void setCircleRadius(int circleRadius) {
        this.circleRadius = circleRadius;
    }

    /**
     * Returns the inner-padding as a percentage of the view size.
     *
     * @return
     */
    public float getPaddingPercent() {
        return paddingPercent;
    }

    /**
     * Sets the inner-padding as a percentage of the view size.
     *
     * @param paddingPercent
     */
    public void setPaddingPercent(float paddingPercent) {
        this.paddingPercent = paddingPercent;
    }

    /**
     * Sets the color resource ID to use for the line.
     *
     * @param lineColor
     */
    public void setLineColor(int lineColor) {
        this.linePaint.setColor(getResources().getColor(lineColor));
    }

    /**
     * Sets the color resource ID to use for the circle.
     *
     * @param circleColor
     */
    public void setCircleColor(int circleColor) {
        this.circlePaint.setColor(getResources().getColor(circleColor));
    }


}
