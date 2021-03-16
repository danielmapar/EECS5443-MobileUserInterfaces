package ca.yorku.cse.mack.softkeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

/**
 * Key - a class for keys for the soft keyboard
 * <p>
 *
 * @author (c) Scott MacKenzie, 2015-2017
 */
public class Key extends View
{
    final static String MYDEBUG = "MYDEBUG"; // for Log.i messages
    private final int UP_FILL_COLOR = Color.GRAY;
    private final int DOWN_FILL_COLOR = 0xff8080ff;
    private final int BORDER_COLOR = Color.BLACK;

    // requirements for key "type"
    private final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijlkmnopqrstuvwxyz";
    private final String SPACE = "Space";
    private final String ENTER = "Enter";
    private final String BACKSPACE = "Bksp";

    public int width;
    public int height;
    Paint linePaint, fillPaint, textPaint;
    Rect keyShape;
    String keyText;
    int keyType;
    int charCode;
    int xText, yText;

    /**
     * Construct a Key object.
     */
    public Key(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initialize();
    }

    /**
     * Construct a Key object.
     */
    public Key(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initialize();
    }

    /**
     * Construct a Key object.
     */
    public Key(Context context)
    {
        super(context);
        initialize();
    }

    private void initialize()
    {
        setFocusableInTouchMode(true);
        setFocusable(true);

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(5);
        linePaint.setColor(BORDER_COLOR);

        fillPaint = new Paint();
        fillPaint.setColor(UP_FILL_COLOR);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);

        keyShape = new Rect();
    }

    /*
     * Since we are extending View, we must override onMeasure. See DemoCustomButton for further
     * discussion.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        setMeasuredDimension(width, height);
    }

    /*
     * Draw the key. Modify the code here to create the desired appearance.
     */
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        canvas.drawRect(keyShape, fillPaint);
        canvas.drawRect(keyShape, linePaint);
        canvas.drawText(keyText, xText, yText, textPaint);
    }

    public void initializeKey(String textArg, int widthArg, int heightArg)
    {
        keyText = textArg;
        width = widthArg;
        height = heightArg;

        // set the key's "type" and character code based on the key text
        if (ALPHA.contains(keyText))
        {
            keyType = KeyboardEvent.TYPE_ALPHA;
            keyText = keyText.toLowerCase(Locale.CANADA);
            charCode = keyText.toLowerCase(Locale.CANADA).charAt(0);
            setTextSize(0.7f);

        } else if (keyText.equals(SPACE))
        {
            keyType = KeyboardEvent.TYPE_SPACE;
            charCode = KeyboardEvent.CHAR_SPACE;
            setTextSize(0.5f);

        } else if (keyText.equals(ENTER))
        {
            keyType = KeyboardEvent.TYPE_ENTER;
            charCode = KeyboardEvent.CHAR_ENTER;
            setTextSize(0.5f);

        } else if (keyText.equals(BACKSPACE))
        {
            keyType = KeyboardEvent.TYPE_BACKSPACE;
            charCode = KeyboardEvent.CHAR_BACKSPACE;
            setTextSize(0.5f);

        } else
        {
            keyType = KeyboardEvent.TYPE_UNDEFINED;
            charCode = KeyboardEvent.CHAR_NULL;
        }

        xText = width / 2;
        yText = height / 2 - (int)(textPaint.ascent() / 3);

        keyShape.left = 0;
        keyShape.right = width;
        keyShape.top = 0;
        keyShape.bottom = height;
    }

    /*
     * Set the text size based on the text to display in the key and the size of the key.
     *
     * The argument passed in is a nominal factor that sets the text size as a ratio of key height
     * (e.g., 0.7 x height for alpha keys). A further adjustment is necessary if the text is too
     * wide to fit in the key. This might happen for keys such as SPACE, BACKSPACE, or ENTER
     * (depending on the key width). The adjustment ensures the width of the text does not exceed 85%
     * of the width of the key.
     */
    private void setTextSize(float nominalArg)
    {
        // nominal text size (works fine for alpha keys and wide SPACE keys)
        textPaint.setTextSize(nominalArg * height);

        // adjust the text size for narrow keys
        float textWidth = textPaint.measureText(keyText);
        float textSize = textPaint.getTextSize();
        float newTextSize = textSize * ((0.85f * width) / textWidth);
        if (newTextSize < textSize)
            textPaint.setTextSize(newTextSize);
    }

    public String getText()
    {
        return keyText;
    }

    public void setText(String textArg)
    {
        keyText = textArg;
    }

    public int getType()
    {
        return keyType;
    }

    public int getCharCode()
    {
        return charCode;
    }

    public void setKeyPressed(boolean pressed)
    {
        if (pressed)
            fillPaint.setColor(DOWN_FILL_COLOR);
        else
            fillPaint.setColor(UP_FILL_COLOR);
        invalidate();
    }

}