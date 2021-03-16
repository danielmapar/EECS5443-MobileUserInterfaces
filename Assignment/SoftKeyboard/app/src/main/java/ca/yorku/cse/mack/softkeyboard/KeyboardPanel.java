package ca.yorku.cse.mack.softkeyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

@SuppressLint("ClickableViewAccessibility")
public class KeyboardPanel extends RelativeLayout implements View.OnTouchListener
{
	//final static String MYDEBUG = "MYDEBUG"; // for Log.i messages

	final int DEFAULT_MARGIN = 10; // density-independent pixels
	final float POPUP_KEY_HEIGHT_FACTOR = 1.5f; // times height of alpha keys

	Vibrator vibrator;
	boolean showPopupKey;
	float offsetFromBottom;

	// define the callback listener
	public interface OnKeystrokeListener
	{
		void onKeystroke(KeyboardEvent ke);
	}

	// declare the callback listener (used by the activity implementing the keyboard)
	OnKeystrokeListener onKeystrokeListener;

	int baseWidth, baseHeight; // of each key
	int keyboardWidth, keyboardHeight;
	float topMargin; // top margin is special (because of the popup keys)
	float margin; // left, right, bottom
	float pixelDensityFactor; // =1 for 160 dpi (Android default)
	long timeStampFingerDown, timeStampFingerUp;

	Key[] key;
	Key currentKey, previousKey;
	KeyPopup popupKey;

	// Should provide three constructors to correspond to each of the three in View.
	public KeyboardPanel(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initialize(context);
	}

	public KeyboardPanel(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialize(context);
	}

	public KeyboardPanel(Context context)
	{
		super(context);
		initialize(context);
	}

	private void initialize(Context c)
	{
		this.setOnTouchListener(this);
		// this.setBackgroundColor(BACKGROUND_COLOR); // Pink (useful for layout debugging)

		pixelDensityFactor = c.getResources().getDisplayMetrics().density;
		margin = DEFAULT_MARGIN * pixelDensityFactor;
	}

	public void setVibrator(Vibrator vArg)
	{
		vibrator = vArg;
	}

	public void setShowPopupKey(boolean showPopupKeyArg)
	{
		showPopupKey = showPopupKeyArg;
	}

	public void setOffsetFromBottom(float offsetFromBottomArg)
	{
		offsetFromBottom = offsetFromBottomArg;
	}

	public void loadKeyboardFromResource(int resourceId, float scalingFactorArg)
	{
		// read the keyboard definition from a resource file into a String array
		String[] keyboardDef = getResources().getStringArray(resourceId);

		/*
		 * 1st entry contains baseline key width and height. The values are in device-independent
		 * pixels, as per the Android default of 160 pixels per inch. If, for example, the baseline
		 * key width is 40, the keys will be 1/4 inch wide -- on any Android device!
		 */
		String[] widthheight = keyboardDef[0].split(",");
		baseWidth = (int)(Integer.parseInt(widthheight[0].trim()) * pixelDensityFactor + 0.5f);
		baseHeight = (int)(Integer.parseInt(widthheight[1].trim()) * pixelDensityFactor + 0.5f);

		baseWidth *= scalingFactorArg;
		baseHeight *= scalingFactorArg;

		/*
		 * The remaining entries define the keys, so the number of keys is the size of the array
		 * minus 1.
		 */
		key = new Key[keyboardDef.length - 1];

		/*
		 * The first thing to do is determine the width and height of the alpha keys (using "A" as
		 * the archetype). We need the alpha key height, in particular, to ensure there is enough
		 * room above the top row of keys to show the popup key.
		 */
		int keyWidthAlpha = 0;
		int keyHeightAlpha = 0;
		for (int i = 1; i < keyboardDef.length; ++i)
		{
			String[] s = keyboardDef[i].split(",");
			String keyText = s[0].trim();
			int keyWidth = (int)(Float.parseFloat(s[3].trim()) * baseWidth + 0.5f);
			int keyHeight = (int)(Float.parseFloat(s[4].trim()) * baseHeight + 0.5f);

			if (keyText.equals("A"))
			{
				keyWidthAlpha = keyWidth;
				keyHeightAlpha = keyHeight;
				break;
			}
		}

		// Set the top margin to ensure there is enough from for the popup key
		if (POPUP_KEY_HEIGHT_FACTOR * keyHeightAlpha > margin)
			topMargin = POPUP_KEY_HEIGHT_FACTOR * keyHeightAlpha;
		else
			topMargin = margin;

		keyboardWidth = 0;
		keyboardHeight = 0;

		// now define the keys (note use of margin and topMargin)
		for (int i = 1; i < keyboardDef.length; ++i)
		{
			String[] s = keyboardDef[i].split(",");
			String keyText = s[0].trim();
			int keyX = (int)(margin + Float.parseFloat(s[1].trim()) * baseWidth + 0.5f);
			int keyY = (int)(topMargin + Float.parseFloat(s[2].trim()) * baseHeight + 0.5f);
			int keyWidth = (int)(Float.parseFloat(s[3].trim()) * baseWidth + 0.5f);
			int keyHeight = (int)(Float.parseFloat(s[4].trim()) * baseHeight + 0.5f);

			// update the keyboard width and height, as keys are added
			if (keyX + keyWidth > keyboardWidth)
				keyboardWidth = keyX + keyWidth;
			if (keyY + keyHeight > keyboardHeight)
				keyboardHeight = keyY + keyHeight;

			key[i - 1] = new Key(this.getContext());
			key[i - 1].initializeKey(keyText, keyWidth, keyHeight);
			key[i - 1].setLeft(keyX);
			key[i - 1].setRight(keyX + keyWidth);
			key[i - 1].setTop(keyY);
			key[i - 1].setBottom(keyY + keyHeight);
			this.addView(key[i - 1]);
		}
		//keyboardWidth += margin; // add right margin
		//keyboardHeight += margin; // add bottom margin
		keyboardHeight += (int)(offsetFromBottom * pixelDensityFactor);

		if (showPopupKey)
		{
			// add the popup key last (if enabled), so it gets rendered above the other keys
			popupKey = new KeyPopup(this.getContext());
			popupKey.initializeKey(keyWidthAlpha, keyHeightAlpha, POPUP_KEY_HEIGHT_FACTOR);
			this.addView(popupKey);
		}
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		// super.onLayout(changed, left, top, right, bottom);
	}

	// attach the keystroke listener to this KeyboardPanel
	public void setOnKeystrokeListener(OnKeystrokeListener onKeystrokeListenerArg)
	{
		onKeystrokeListener = onKeystrokeListenerArg;
	}

	// process touch events on this KeyboardPanel
	@Override
	public boolean onTouch(View v, MotionEvent me)
	{
		float x = me.getX();
		float y = me.getY();

		currentKey = findKey(Math.round(x), Math.round(y)); // null if the touch event is in the
															// margin

		switch (me.getAction() & MotionEvent.ACTION_MASK)
		{
			case MotionEvent.ACTION_DOWN:

				if (currentKey != null)
				{
					vibrator.vibrate(10);
					currentKey.setKeyPressed(true);
					previousKey = currentKey;

					// adjust location and text for popup key accordingly
					if (showPopupKey)
					{
						if (currentKey.getType() == KeyboardEvent.TYPE_ALPHA)
							popupKey.renderAbove(currentKey);
						else
							popupKey.renderAbove(null); // no popup key
					}

					timeStampFingerDown = System.currentTimeMillis();
				}
				break;

			/*
			 * The finger is moving on the soft keyboard surface. Several scenarios must be tested.
			 * See below.
			 */
			case MotionEvent.ACTION_MOVE:

				// test: finger is moving in a non-key area of keyboard
				if (currentKey == null && previousKey == null)
					break;

				// test: finger has moved from a key to a non-key area of keyboard
				if (currentKey == null)
				{
					previousKey.setKeyPressed(false);
					previousKey = null;
					popupKey.renderAbove(null);
				}

				// test: finger has moved from one key to another
				else if (previousKey != null && currentKey != previousKey)
				{
					currentKey.setKeyPressed(true);
					previousKey.setKeyPressed(false);
					previousKey = currentKey;

					if (showPopupKey)
					{
						// adjust location and text for popup key accordingly
						if (currentKey.getType() == KeyboardEvent.TYPE_ALPHA)
							popupKey.renderAbove(currentKey);
						else
							popupKey.renderAbove(null); // make previous popup disappear
					}
				}

				break;

			/*
			 * The critical work is done here (finish with a callback to the Activity using
			 * onKeystroke)
			 */
			case MotionEvent.ACTION_UP:

				if (currentKey != null && previousKey != null)
				{
					String keyText = currentKey.getText();
					int charCode = currentKey.getCharCode(); // default
					int type = currentKey.getType(); // default
					currentKey.setKeyPressed(false);
					previousKey.setKeyPressed(false);
					if (showPopupKey)
						popupKey.renderAbove(null);
					timeStampFingerUp = System.currentTimeMillis();

					// callback to the activity implementing OnKeystrokeListener
					onKeystrokeListener.onKeystroke(new KeyboardEvent(keyText, charCode, type, timeStampFingerDown,
							timeStampFingerUp));

					currentKey = null;
					previousKey = null;
				}
				break;
		}

		invalidate(); // refresh the keyboard L&F
		return true;
	}

	/*
	 * Return the key on which the touch event occurred. Return null if the touch event was on the
	 * space/margins around the keys.
	 */
	private Key findKey(int x, int y)
	{
		Key foundKey = null;
		for (Key k : key)
		{
			if (x >= k.getLeft() && x <= k.getRight() && y >= k.getTop() && y <= k.getBottom())
			{
				foundKey = k;
				break;
			}
		}
		return foundKey;
	}

	/*
	 * onMeasure - Since this is a custom View, we must override onMeasure.
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(keyboardWidth, keyboardHeight);
	}
}
