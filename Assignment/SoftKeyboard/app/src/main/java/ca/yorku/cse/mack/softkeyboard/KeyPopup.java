package ca.yorku.cse.mack.softkeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * KeyPopup - popup key for soft keyboard
 * <p>
 *
 * @author (c) Scott MacKenzie, 2015
 */
public class KeyPopup extends View
{
    //final static String MYDEBUG = "MYDEBUG"; // for Log.i messages

    private final static int FILL_COLOR = Color.LTGRAY;
    private final static int BORDER_COLOR = Color.RED;
    private final static int LINE_WIDTH = 4;
    private final static float WIDTH_FACTOR = 1.0f;

    public int width;
    public int height;
    public boolean visible;
    Paint linePaint, fillPaint, textPaint;
    Rect popup;
    String keyText;
    int xText, yText;
    float heightFactor;

    public KeyPopup(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initialize();
    }

    public KeyPopup(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initialize();
    }

    public KeyPopup(Context context)
    {
        super(context);
        initialize();
    }

    private void initialize()
    {
        popup = new Rect();

        setFocusableInTouchMode(true);
        setFocusable(true);
        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(LINE_WIDTH);
        linePaint.setColor(BORDER_COLOR); // default is button-up

        fillPaint = new Paint();
        fillPaint.setColor(FILL_COLOR);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        visible = false;
    }

    /*
     * Since we are extending View, we must override onMeasure. See DemoCustomButton for further
     * details.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        setMeasuredDimension(width, height);
    }

    /*
     * Draw the KeyPopup object. Modify the code here to create the desired appearance.
     */
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (!visible)
            return;

        canvas.drawRect(popup, fillPaint);
        canvas.drawRect(popup, linePaint);
        canvas.drawText(keyText, xText, yText, textPaint);
    }

    /*
     * The width/height arguments supplied are the width and height of an alpha key on the soft
     * keyboard.
     */
    public void initializeKey(int widthArg, int heightArg, float heightFactorArg)
    {
        textPaint.setTextSize(0.7f * heightArg);
        heightFactor = heightFactorArg;
        width = (int)(WIDTH_FACTOR * widthArg + 0.5f);
        height = (int)(heightFactor * heightArg + 0.5f);

        popup.left = LINE_WIDTH / 2;
        popup.right = width - LINE_WIDTH / 2;
        popup.top = LINE_WIDTH / 2;
        popup.bottom = height - LINE_WIDTH / 2;

        xText = width / 2;
        yText = height / 2 - (int)(textPaint.ascent() / 3);
    }

    public void renderAbove(Key k)
    {
        if (k == null)
            visible = false;

        else
        {
            visible = true;
            this.setLeft(k.getLeft());
            this.setRight(k.getRight());
            this.setTop((int)(k.getTop() - height + 0.5f));
            this.setBottom(k.getTop());
            this.setText(k.getText());
        }
        this.invalidate();
    }

    public void setText(String textArg)
    {
        keyText = textArg;
    }
}