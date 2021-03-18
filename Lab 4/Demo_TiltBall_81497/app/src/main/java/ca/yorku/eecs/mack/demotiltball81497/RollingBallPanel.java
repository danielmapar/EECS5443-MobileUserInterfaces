package ca.yorku.eecs.mack.demotiltball81497;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RollingBallPanel extends View
{
    static MediaPlayer mp = null;

    final static float DEGREES_TO_RADIANS = 0.0174532925f;

    // the ball diameter will be min(width, height) / this_value
    final static float BALL_DIAMETER_ADJUST_FACTOR = 30;

    final static int DEFAULT_LABEL_TEXT_SIZE = 20; // tweak as necessary
    final static int DEFAULT_STATS_TEXT_SIZE = 10;
    final static int DEFAULT_GAP = 7; // between lines of text
    final static int DEFAULT_OFFSET = 10; // from bottom of display

    final static int MODE_NONE = 0;
    final static int PATH_TYPE_SQUARE = 1;
    final static int PATH_TYPE_CIRCLE = 2;

    final static float PATH_WIDTH_NARROW = 2f; // ... x ball diameter
    final static float PATH_WIDTH_MEDIUM = 4f; // ... x ball diameter
    final static float PATH_WIDTH_WIDE = 8f; // ... x ball diameter

    float radiusOuter, radiusInner;

    Bitmap ball, decodedBallBitmap;
    int ballDiameter;

    float dT; // time since last sensor event (seconds)

    float width, height, pixelDensity;
    int labelTextSize, statsTextSize, gap, offset;

    float arrowXStart, arrowYStart;

    RectF innerRectangle, outerRectangle, innerShadowRectangle, outerShadowRectangle, lapLineRectangle, ballNow;
    boolean touchFlag, touchLapLineFlag;
    Vibrator vib;
    int wallHits;

    float xBall, yBall; // top-left of the ball (for painting)
    float xBallCenter, yBallCenter; // center of the ball

    float pitch, roll;
    float tiltAngle, tiltMagnitude;

    // parameters from Setup dialog
    String orderOfControl;
    float gain, pathWidth;
    int pathType, currentLap, numberOfLaps;

    float velocity; // in pixels/second (velocity = tiltMagnitude * tiltVelocityGain
    float dBall; // the amount to move the ball (in pixels): dBall = dT * velocity
    float xCenter, yCenter; // the center of the screen
    long now, lastT;
    Paint statsPaint, labelPaint, linePaint, fillPaint, backgroundPaint;
    float[] updateY;

    long lapStartTime = 0;
    float[] lapTimes;
    boolean isBallInsidePath;
    double outsideOfPathStartTime = -1;
    double totalTimeOutsidePath = 0;
    double experimentStartTime = -1;


    Activity activity;

    public RollingBallPanel(Context contextArg)
    {
        super(contextArg);
        initialize(contextArg);
    }

    public RollingBallPanel(Context contextArg, AttributeSet attrs)
    {
        super(contextArg, attrs);
        initialize(contextArg);
    }

    public RollingBallPanel(Context contextArg, AttributeSet attrs, int defStyle)
    {
        super(contextArg, attrs, defStyle);
        initialize(contextArg);
    }

    // things that can be initialized from within this View
    private void initialize(Context c)
    {
        isBallInsidePath = isBallInPath();

        linePaint = new Paint();
        linePaint.setColor(Color.RED);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2);
        linePaint.setAntiAlias(true);

        fillPaint = new Paint();
        fillPaint.setColor(0xffccbbbb);
        fillPaint.setStyle(Paint.Style.FILL);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.FILL);

        labelPaint = new Paint();
        labelPaint.setColor(Color.BLACK);
        labelPaint.setTextSize(DEFAULT_LABEL_TEXT_SIZE);
        labelPaint.setAntiAlias(true);

        statsPaint = new Paint();
        statsPaint.setAntiAlias(true);
        statsPaint.setTextSize(DEFAULT_STATS_TEXT_SIZE);

        // NOTE: we'll create the actual bitmap in onWindowFocusChanged
        decodedBallBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ball);

        lastT = System.nanoTime();
        this.setBackgroundColor(Color.LTGRAY);
        touchFlag = false;
        touchLapLineFlag = false;
        outerRectangle = new RectF();
        innerRectangle = new RectF();
        innerShadowRectangle = new RectF();
        outerShadowRectangle = new RectF();
        lapLineRectangle = new RectF();
        ballNow = new RectF();
        wallHits = 0;
        currentLap = 0;

        vib = (Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Called when the window hosting this view gains or looses focus.  Here we initialize things that depend on the
     * view's width and height.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        if (!hasFocus)
            return;

        width = this.getWidth();
        height = this.getHeight();

        // the ball diameter is nominally 1/30th the smaller of the view's width or height
        ballDiameter = width < height ? (int)(width / BALL_DIAMETER_ADJUST_FACTOR)
                : (int)(height / BALL_DIAMETER_ADJUST_FACTOR);

        // now that we know the ball's diameter, get a bitmap for the ball
        ball = Bitmap.createScaledBitmap(decodedBallBitmap, ballDiameter, ballDiameter, true);

        // center of the view
        xCenter = width / 2f;
        yCenter = height / 2f;

        arrowXStart = xCenter;
        arrowYStart = yCenter;

        // top-left corner of the ball
        xBall = xCenter;
        yBall = yCenter;

        // center of the ball
        xBallCenter = xBall + ballDiameter / 2f;
        yBallCenter = yBall + ballDiameter / 2f;

        // configure outer rectangle of the path
        radiusOuter = width < height ? 0.40f * width : 0.40f * height;
        outerRectangle.left = xCenter - radiusOuter;
        outerRectangle.top = yCenter - radiusOuter;
        outerRectangle.right = xCenter + radiusOuter;
        outerRectangle.bottom = yCenter + radiusOuter;

        // configure inner rectangle of the path
        // NOTE: medium path width is 4 x ball diameter
        radiusInner = radiusOuter - pathWidth * ballDiameter;
        innerRectangle.left = xCenter - radiusInner;
        innerRectangle.top = yCenter - radiusInner;
        innerRectangle.right = xCenter + radiusInner;
        innerRectangle.bottom = yCenter + radiusInner;

        // configure outer shadow rectangle (needed to determine wall hits)
        // NOTE: line thickness (aka stroke width) is 2
        outerShadowRectangle.left = outerRectangle.left + ballDiameter - 2f;
        outerShadowRectangle.top = outerRectangle.top + ballDiameter - 2f;
        outerShadowRectangle.right = outerRectangle.right - ballDiameter + 2f;
        outerShadowRectangle.bottom = outerRectangle.bottom - ballDiameter + 2f;

        // configure inner shadow rectangle (needed to determine wall hits)
        innerShadowRectangle.left = innerRectangle.left + ballDiameter - 2f;
        innerShadowRectangle.top = innerRectangle.top + ballDiameter - 2f;
        innerShadowRectangle.right = innerRectangle.right - ballDiameter + 2f;
        innerShadowRectangle.bottom = innerRectangle.bottom - ballDiameter + 2f;

        // initialize a few things (e.g., paint and text size) that depend on the device's pixel density
        pixelDensity = this.getResources().getDisplayMetrics().density;
        labelTextSize = (int)(DEFAULT_LABEL_TEXT_SIZE * pixelDensity + 0.5f);
        labelPaint.setTextSize(labelTextSize);

        statsTextSize = (int)(DEFAULT_STATS_TEXT_SIZE * pixelDensity + 0.5f);
        statsPaint.setTextSize(statsTextSize);

        gap = (int)(DEFAULT_GAP * pixelDensity + 0.5f);
        offset = (int)(DEFAULT_OFFSET * pixelDensity + 0.5f);

        // compute y offsets for painting stats (bottom-left of display)
        updateY = new float[8]; // up to 8 lines of stats will appear
        for (int i = 0; i < updateY.length; ++i)
            updateY[i] = height - offset - i * (statsTextSize + gap);
    }

    /*
     * Do the heavy lifting here! Update the ball position based on the tilt angle, tilt
     * magnitude, order of control, etc.
     */
    public void updateBallPosition(float pitchArg, float rollArg, float tiltAngleArg, float tiltMagnitudeArg)
    {
        isBallInsidePath = isBallInPath();
        pitch = pitchArg; // for information only (see onDraw)
        roll = rollArg; // for information only (see onDraw)
        tiltAngle = tiltAngleArg;
        tiltMagnitude = tiltMagnitudeArg;

        // get current time and delta since last onDraw
        now = System.nanoTime();
        dT = (now - lastT) / 1000000000f; // seconds
        lastT = now;

        // don't allow tiltMagnitude to exceed 45 degrees
        final float MAX_MAGNITUDE = 45f;
        tiltMagnitude = tiltMagnitude > MAX_MAGNITUDE ? MAX_MAGNITUDE : tiltMagnitude;

        // This is the only code that distinguishes velocity-control from position-control
        if (orderOfControl.equals("Velocity")) // velocity control
        {
            // compute ball velocity (depends on the tilt of the device and the gain setting)
            velocity = tiltMagnitude * gain;

            // compute how far the ball should move (depends on the velocity and the elapsed time since last update)
            dBall = dT * velocity; // make the ball move this amount (pixels)

            // compute the ball's new coordinates (depends on the angle of the device and dBall, as just computed)
            float dx = (float)Math.sin(tiltAngle * DEGREES_TO_RADIANS) * dBall;
            float dy = -(float)Math.cos(tiltAngle * DEGREES_TO_RADIANS) * dBall;
            xBall += dx;
            yBall += dy;

        } else
        // position control
        {
            // compute how far the ball should move (depends on the tilt of the device and the gain setting)
            dBall = tiltMagnitude * gain;

            // compute the ball's new coordinates (depends on the angle of the device and dBall, as just computed)
            float dx = (float)Math.sin(tiltAngle * DEGREES_TO_RADIANS) * dBall;
            float dy = -(float)Math.cos(tiltAngle * DEGREES_TO_RADIANS) * dBall;
            xBall = xCenter + dx;
            yBall = yCenter + dy;
        }

        // make an adjustment, if necessary, to keep the ball visible (also, restore if NaN)
        if (Float.isNaN(xBall) || xBall < 0)
            xBall = 0;
        else if (xBall > width - ballDiameter)
            xBall = width - ballDiameter;
        if (Float.isNaN(yBall) || yBall < 0)
            yBall = 0;
        else if (yBall > height - ballDiameter)
            yBall = height - ballDiameter;

        // oh yea, don't forget to update the coordinate of the center of the ball (needed to determine wall  hits)
        xBallCenter = xBall + ballDiameter / 2f;
        yBallCenter = yBall + ballDiameter / 2f;

        if (isBallInsidePath) {
            if (outsideOfPathStartTime > 0) totalTimeOutsidePath += System.nanoTime() - outsideOfPathStartTime;
            outsideOfPathStartTime = -1;
            linePaint.setColor(Color.BLUE);
            fillPaint.setColor(0xFFADD8E6);
        } else {
            if (experimentStartTime > 0 && outsideOfPathStartTime < 0) outsideOfPathStartTime = System.nanoTime();
            linePaint.setColor(Color.RED);
            fillPaint.setColor(0xffccbbbb);
        }

        if (ballTouchingLapLine() && !touchLapLineFlag) {
            touchLapLineFlag = true;

            if (currentLap == 0) {
                experimentStartTime = System.nanoTime();
                lapStartTime = System.currentTimeMillis();
                currentLap++;
            }
            else {
                lapTimes[currentLap-1] =  System.currentTimeMillis() - lapStartTime;
                lapStartTime = System.currentTimeMillis();
                if (currentLap+1 > numberOfLaps) loadReportView();
                else currentLap++;
            }
            mp.start();
        } else if (!ballTouchingLapLine() && touchLapLineFlag) {
            touchLapLineFlag = false;
        }

        // if ball touches wall, vibrate and increment wallHits count
        // NOTE: We also use a boolean touchFlag so we only vibrate on the first touch
        if (ballTouchingLine() && !touchFlag)
        {
            touchFlag = true; // the ball has *just* touched the line: set the touchFlag

            if (isBallInsidePath) {
                vib.vibrate(50); // 50 ms vibrotactile pulse
                ++wallHits;
            }

        } else if (!ballTouchingLine() && touchFlag) {
            touchFlag = false; // the ball is no longer touching the line: clear the touchFlag
        }

        invalidate(); // force onDraw to redraw the screen with the ball in its new position
    }

    protected void onDraw(Canvas canvas)
    {
        float   lapLineX = innerRectangle.left,
                lapLineY = innerRectangle.top+(innerRectangle.bottom-innerRectangle.top)/2,
                lapLineX1 = outerRectangle.left,
                lapLineY1 = lapLineY;

        lapLineRectangle.left = lapLineX1;
        lapLineRectangle.top = lapLineY;
        lapLineRectangle.right = lapLineX;
        lapLineRectangle.bottom = lapLineY1;

        // draw the paths
        if (pathType == PATH_TYPE_SQUARE)
        {
            // draw fills
            canvas.drawRect(outerRectangle, fillPaint);
            canvas.drawRect(innerRectangle, backgroundPaint);

            // draw lines
            canvas.drawRect(outerRectangle, linePaint);
            canvas.drawRect(innerRectangle, linePaint);

            canvas.drawLine(lapLineX, lapLineY, lapLineX1, lapLineY1, linePaint);

        } else if (pathType == PATH_TYPE_CIRCLE) {
            // draw fills
            canvas.drawOval(outerRectangle, fillPaint);
            canvas.drawOval(innerRectangle, backgroundPaint);

            // draw lines
            canvas.drawOval(outerRectangle, linePaint);
            canvas.drawOval(innerRectangle, linePaint);

            canvas.drawLine(lapLineX, lapLineY, lapLineX1, lapLineY1, linePaint);
        }
        float toXArrow = (float)Math.sin(tiltAngle * DEGREES_TO_RADIANS);
        float toYArrow = -(float)Math.cos(tiltAngle * DEGREES_TO_RADIANS);
        float arrowLength = 100;

        this.drawArrow(linePaint, canvas,arrowXStart, arrowYStart, arrowXStart+toXArrow * arrowLength, arrowYStart+toYArrow * arrowLength);

        // draw label
        canvas.drawText("Demo_TiltBall", 6f, labelTextSize, labelPaint);

        // draw stats (pitch, roll, tilt angle, tilt magnitude)
        if (pathType == PATH_TYPE_SQUARE || pathType == PATH_TYPE_CIRCLE)
        {
            canvas.drawText("Wall hits = " + wallHits, 6f, updateY[7], statsPaint);
            canvas.drawText("Number of laps = " + String.valueOf(currentLap) + "/" + String.valueOf(numberOfLaps), 6f, updateY[6], statsPaint);
            canvas.drawText("Lap time (s) = " + String.valueOf((lapStartTime == 0 ? lapStartTime : System.currentTimeMillis()-lapStartTime)/1000), 6f, updateY[5], statsPaint);
            canvas.drawText("-----------------", 6f, updateY[4], statsPaint);
        }
        canvas.drawText(String.format(Locale.CANADA, "Tablet pitch (degrees) = %.2f", pitch), 6f, updateY[3],
                statsPaint);
        canvas.drawText(String.format(Locale.CANADA, "Tablet roll (degrees) = %.2f", roll), 6f, updateY[2], statsPaint);
        canvas.drawText(String.format(Locale.CANADA, "Ball x = %.2f", xBallCenter), 6f, updateY[1], statsPaint);
        canvas.drawText(String.format(Locale.CANADA, "Ball y = %.2f", yBallCenter), 6f, updateY[0], statsPaint);

        // draw the ball in its new location
        canvas.drawBitmap(ball, xBall, yBall, null);

    } // end onDraw

    private void drawArrow(Paint paint, Canvas canvas, float from_x, float from_y, float to_x, float to_y)
    {
        float angle,anglerad, radius, lineangle;

        //values to change for other appearance *CHANGE THESE FOR OTHER SIZE ARROWHEADS*
        radius=30f;
        angle=35f;

        //some angle calculations
        anglerad= (float) (Math.PI*angle/180.0f);
        lineangle= (float) (Math.atan2(to_y-from_y,to_x-from_x));

        //tha line
        canvas.drawLine(from_x,from_y,to_x,to_y,paint);

        //tha triangle
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(to_x, to_y);
        path.lineTo((float)(to_x-radius*Math.cos(lineangle - (anglerad / 2.0))),
                (float)(to_y-radius*Math.sin(lineangle - (anglerad / 2.0))));
        path.lineTo((float)(to_x-radius*Math.cos(lineangle + (anglerad / 2.0))),
                (float)(to_y-radius*Math.sin(lineangle + (anglerad / 2.0))));
        path.close();

        canvas.drawPath(path, paint);
    }

    /*
     * Configure the rolling ball panel according to setup parameters
     */
    public void configure(String pathMode, String pathWidthArg, int gainArg, String orderOfControlArg, int numberOfLapsArg, MediaPlayer mediaPlayer, Activity parent)
    {
        activity = parent;
        mp = mediaPlayer;

        // square vs. circle
        if (pathMode.equals("Square"))
            pathType = PATH_TYPE_SQUARE;
        else if (pathMode.equals("Circle"))
            pathType = PATH_TYPE_CIRCLE;
        else
            pathType = MODE_NONE;

        // narrow vs. medium vs. wide
        if (pathWidthArg.equals("Narrow"))
            pathWidth = PATH_WIDTH_NARROW;
        else if (pathWidthArg.equals("Wide"))
            pathWidth = PATH_WIDTH_WIDE;
        else
            pathWidth = PATH_WIDTH_MEDIUM;

        gain = gainArg;
        numberOfLaps = numberOfLapsArg;
        lapTimes = new float[numberOfLaps];
        currentLap = 0;
        orderOfControl = orderOfControlArg;
    }

    // returns true if the ball is touching (i.e., overlapping) the line of the inner or outer path border
    public boolean ballTouchingLine()
    {
        if (pathType == PATH_TYPE_SQUARE)
        {
            ballNow.left = xBall;
            ballNow.top = yBall;
            ballNow.right = xBall + ballDiameter;
            ballNow.bottom = yBall + ballDiameter;

            if (RectF.intersects(ballNow, outerRectangle) && !RectF.intersects(ballNow, outerShadowRectangle))
                return true; // touching outside rectangular border

            if (RectF.intersects(ballNow, innerRectangle) && !RectF.intersects(ballNow, innerShadowRectangle))
                return true; // touching inside rectangular border

        } else if (pathType == PATH_TYPE_CIRCLE)
        {
            final float ballDistance = (float)Math.sqrt((xBallCenter - xCenter) * (xBallCenter - xCenter)
                    + (yBallCenter - yCenter) * (yBallCenter - yCenter));

            if (Math.abs(ballDistance - radiusOuter) < (ballDiameter / 2f))
                return true; // touching outer circular border

            if (Math.abs(ballDistance - radiusInner) < (ballDiameter / 2f))
                return true; // touching inner circular border
        }
        return false;
    }

    public boolean ballTouchingLapLine()
    {
        ballNow.left = xBall;
        ballNow.top = yBall;
        ballNow.right = xBall + ballDiameter;
        ballNow.bottom = yBall + ballDiameter;

        float toYArrow = -(float)Math.cos(tiltAngle * DEGREES_TO_RADIANS);

        return toYArrow > 0 && RectF.intersects(ballNow, lapLineRectangle);
    }

    public boolean isBallInPath()
    {
        if (pathType == PATH_TYPE_SQUARE)
        {
            ballNow.left = xBall;
            ballNow.top = yBall;
            ballNow.right = xBall + ballDiameter;
            ballNow.bottom = yBall + ballDiameter;

            if( RectF.intersects(ballNow, innerRectangle))
                return false;
            if( RectF.intersects(ballNow, outerRectangle))
                return true;
            return false;

        } else if (pathType == PATH_TYPE_CIRCLE)
        {
            final float ballDistance = (float)Math.sqrt((xBallCenter - xCenter) * (xBallCenter - xCenter)
                    + (yBallCenter - yCenter) * (yBallCenter - yCenter));

            if (ballDistance > radiusInner && ballDistance<radiusOuter) return true;
            return false;
        }
        return false;
    }

    public void loadReportView() {

        float totalLapTimes = 0;
        for (int i = 0; i < lapTimes.length; i++) {
            totalLapTimes += lapTimes[i];
        }

        totalLapTimes = totalLapTimes / lapTimes.length / 1000;

        double now = System.nanoTime();
        double totalExperimentTime = now - experimentStartTime;

        double inPathTime = totalExperimentTime - totalTimeOutsidePath;

        double percentageInPathMovementTime = (inPathTime*100)/totalExperimentTime;

        // bundle up parameters to pass on to activity
        Bundle b = new Bundle();
        b.putInt("numberOfLaps", numberOfLaps);
        b.putFloat("lapTime", totalLapTimes);
        b.putInt("numberOfWallsHit", wallHits);
        b.putFloat("percentageInPathMovementTime", (float) percentageInPathMovementTime);

        // start experiment activity
        Intent i = new Intent(getContext(), ReportActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtras(b);
        getContext().startActivity(i);

        activity.finish();
    }
}
