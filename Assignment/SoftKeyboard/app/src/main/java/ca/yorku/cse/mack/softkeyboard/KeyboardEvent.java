package ca.yorku.cse.mack.softkeyboard;

import android.view.KeyEvent;

/**
 * KeyboardEvent - This class represents a Keyboard event and holds information about the event. A
 * Keyboard event occurs on finger lift, after the key data are processed. The information available
 * in a <code>KeyboardEvent</code> object includes the the character code, key type, etc.
 * 
 * @author (c) Scott MacKenzie, 2015-2017
 * 
 */
class KeyboardEvent
{
	static final int TYPE_UNDEFINED = -2;
	//static final int TYPE_NONE = -1;
	static final int TYPE_ALPHA = 0;
	//static final int TYPE_NUMERIC = 1;
	static final int TYPE_SPACE = 2;
	static final int TYPE_ENTER = 3;
	static final int TYPE_BACKSPACE = 4;
	//static final int TYPE_SYMBOL = 5;

	static final int CHAR_NULL = 0;
	static final int CHAR_ENTER = KeyEvent.KEYCODE_ENTER;
	static final int CHAR_BACKSPACE = KeyEvent.KEYCODE_DEL;
	static final int CHAR_SPACE = ' ';

	String raw; // raw string
	int charCode; // character code for sending to a text field
	int type; // type of gesture
	long timeStampFingerDown; // time stamp of finger down (beginning of gesture)
	long timeStampFingerUp; // time stamp of finger up (end of gesture)
	int duration; // duration of gesture

	KeyboardEvent(String rawArg, int charCodeArg, int typeArg, long timeStampFingerDownArg,
			long timeStampFingerUpArg)
	{
		raw = rawArg;
		charCode = charCodeArg;
		type = typeArg;
		timeStampFingerDown = timeStampFingerDownArg;
		timeStampFingerUp = timeStampFingerUpArg;
		duration = (int)(timeStampFingerUp - timeStampFingerDown);
	}
}
