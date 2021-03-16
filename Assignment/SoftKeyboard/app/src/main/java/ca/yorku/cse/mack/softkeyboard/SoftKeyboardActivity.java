package ca.yorku.cse.mack.softkeyboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import ca.yorku.cse.mack.softkeyboard.KeyboardPanel.OnKeystrokeListener;

/**
 * <h1>SoftKeyboard</h1>
 *
 * <h3>Summary</h3>
 *
 * <ul>
 * <li>Android experiment software for text entry using a soft keyboard.
 * <p>
 * <li>The following soft keyboards are supported:
 * <p>
 *
 * <ul>
 * <li>Qwerty
 * <li>Opti
 * <li>Opti II
 * <li>Fitaly
 * <li>Lewis-Kennedy-LaLomia
 * <li>Metropolis
 * </ul>
 * </ul>
 * <p>
 *
 * <h3>Related References</h3>
 *
 * <ul>
 * <li>
 * <a href="http://www.yorku.ca/mack/CHI99a.html">The design and evaluation of a high performance
 * soft keyboard</a>, by MacKenzie and Zhang (<i>CHI '99</i>). This is the paper where the Opti
 * design was introduced. Opti was the first soft keyboard to be designed using a Fitts-digram
 * model.
 * <p>
 *
 * <li>
 * <a href="http://dl.acm.org/citation.cfm?id=633380">Physics-based graphical keyboard design</a>,
 * by Hunter, Zhai, and Smith (<i>CHI 2000</i>). This paper introduced the Metropolis soft keyboard,
 * offered as an alternative Opti. Metropolis was also designed using a Fitts-digram model; however,
 * the design process was automated using an algorithm.
 * <p>
 *
 * <li>
 * <a href="http://pro.sagepub.com/content/43/5/420.short">Evaluation of typing key layouts for stylus input</a>, by
 * Lewis, LaLomia, & Kennedy (<i>HFES '99</i>). This is the paper introducing
 * the LKL (Lewis-Kennedy-LaLomia) keyboard.
 * <p>
 *
 * <li>
 * <a href="http://www.fitaly.com/">Textware Solutions</a> web site. Textware Solutions is the
 * company that introduced the Fitaly soft keyboard.
 * <p>
 *
 * <li>
 * <a href="http://www.yorku.ca/mack/hci3.html">Text entry for mobile computing: Models and methods,
 * theory and practice</a>, by MacKenzie and Soukoreff (<i>Human-Computer Interaction</i>, 2002).
 * This paper includes an extensive section on soft keyboards, including a comparison of entry speed
 * predictions using a Fitts-digram model. The comparison includes Qwerty, Opti, Opti II,
 * Metropolis, LKL, Fitaly, and several other layouts (Figure 21).
 * <p>
 *
 * <li>
 * <a href="http://www.yorku.ca/mack/bit95.html">Theoretical upper and lower bounds on typing speed
 * using a stylus and soft keyboard</a>, by Soukoreff and MacKenzie (<i>Behaviour & Information
 * Technology</i>, 1995). This is the paper that introduced the Fitts-digram model for designing
 * soft keyboards.
 * <p>
 *
 * <li>
 * <a href="http://www.yorku.ca/mack/chi03b.html">Phrase sets for evaluating text entry
 * techniques</a>, by MacKenzie, and Soukoreff (<i>CHI 2003</i>). This is the paper that introduced
 * the phrase set ("phases2.txt") that is now widely used for evaluating text entry techniques.
 * <p>
 * </ul>
 *
 * <h3>SoftKeyboardSetup Parameters</h3>
 *
 * Upon launching, the program presents a setup dialog: (click to enlarge)
 * <p>
 *
 * <center> <a href="SoftKeyboard-1.jpg"><img src="SoftKeyboard-1.jpg"
 * width=200 alt="image"></a> </center>
 * <p>
 *
 * The setup parameters are embedded in the application. The default settings (shown) may be changed
 * via the setup dialog. Changes may be saved. Saved changes become the default settings when the
 * application is next launched.
 *
 * The setup parameters are as follows:
 *
 * <blockquote>
 * <table border="1" cellspacing="0" cellpadding="6" valign="top">
 * <tr bgcolor="#cccccc">
 * <th>Parameter
 * <th>Description
 *
 * <tr valign="top">
 * <td>Participant Code
 * <td>Identifies the current participant. This is used in forming the names for the output data
 * files. Also, the sd2 output data file includes a column with the participant code.
 * <p>
 *
 * <tr valign="top">
 * <td>Session Code
 * <td>Identifies the session. Useful if testing proceeds over multiple sessions to gauge the
 * progression of learning. The session code is used in forming the name for the output data file.
 * Also, the sd2 output data file contains a column with the session code.
 * <p>
 *
 * Note: The setup dialog does not include an entry for "Block code". The block code is generated
 * automatically by the software.
 * <p>
 *
 * <tr valign="top">
 * <td>Group Code
 * <td>Identifies the group to which the participant was assigned. The group code is used in forming
 * the name for the output data file. Also, the sd2 output data file contains a column with the
 * group code.
 * <p>
 *
 * A group code is useful if counterbalancing was used (i.e., participants were assigned to groups
 * to offset order effects). This is common practice for testing the levels of a within-subjects
 * independent variable.
 * <p>
 *
 * <tr valign="top">
 * <td>Condition Code
 * <td>An arbitrary code used to associate a test condition with this invocation. This parameter
 * might be useful if the software is used in an experiment that investigates conditions that are
 * not inherently part of the application (e.g., Gender &rarr; male, female; User position &rarr;
 * sitting, standing, walking).
 * <p>
 *
 * The condition code is used in forming the name for the output data
 * file. Also, the sd2 output data file contains a column with the condition code.
 * <p>
 *
 * <tr valign="top">
 * <td>Keyboard Layout
 * <td>Used to select the keyboard layout. Different keyboard layouts are defined in resource file
 * embedded in the application. The options currently supported are as follows: (click to enlarge)
 * <p>
 *
 * <center> <b>Qwerty<b><br>
 * <a href="SoftKeyboard-qwerty.jpg"><img src="SoftKeyboard-qwerty.jpg"
 * width=150 alt="image"></a>
 * <p>
 *
 * <b>Opti<b><br>
 * <a href="SoftKeyboard-opti.jpg"><img src="SoftKeyboard-opti.jpg"
 * width=150 alt="image"></a>
 * <p>
 *
 * <b>Opti II<b><br>
 * <a href="SoftKeyboard-opti2.jpg"><img src="SoftKeyboard-opti2.jpg"
 * width=150 alt="image"></a>
 * <p>
 *
 * <b>Fitaly<b><br>
 * <a href="SoftKeyboard-fitaly.jpg"><img src="SoftKeyboard-fitaly.jpg"
 * width=150 alt="image"></a>
 * <p>
 *
 * <b>Lewis-Kennedy-LaLomia<b><br>
 * <a href="SoftKeyboard-lewis.jpg"><img src="SoftKeyboard-lewis.jpg"
 * width=150 alt="image"></a>
 * <p>
 *
 * <b>Metropolis<b><br>
 * <a href="SoftKeyboard-metropolis.jpg"><img src="SoftKeyboard-metropolis.jpg"
 * width=150 alt="image"></a>
 * <p>
 * </center>
 *
 * <tr valign="top">
 * <td>Keyboard Scale
 * <td>Adjusts the size of the keyboard using the specified scaling factor. This parameter allows
 * the keyboard size to be adjusted without changing the keyboard definition in the resource file. A
 * scale setting of 1 leaves the keyboard size as specified in the corresponding resource file.
 * <p>
 *
 * The value entered will be parsed to a <code>float</code> before it is passed on to the main activity.  If the value
 * entered is not parsable to a <code>float</code>, a 20-ms vibrotactile pulse is emitted to signal that a correction
 * is required.
 * <p>
 *
 * The keyboard scale is used in forming the name for the output data
 * file. Also, the sd2 output data file contains a column with the keyboard scale.
 * <p>
 *
 * <tr valign="top">
 * <td>Offset From Bottom
 * <td>Specifies the distance from bottom of the display where the soft keyboard is positioned.
 * The value is in Android's "density independent pixels" (dip), where 1 inch =
 * 160 dip.
 * <p>
 *
 * The value entered will be parsed to a <code>float</code> before it is passed on to the main activity.  If the value
 * entered is not parsable to a <code>float</code>, a 20-ms vibrotactile pulse is emitted to signal that a correction
 * is required.
 * <p>
 *
 * <tr valign="top">
 * <td>Number of Phrases
 * <td>Specifies the number of phrases presented to the participant in the current block.
 * <p>
 *
 * <tr valign="top">
 * <td>Phrases File
 * <td>Specifies a file containing phrases of text to be presented to participants for entry.
 * Phrases are drawn from the file at random. Typically, <a href="phrases2.txt">phrases2.txt</a> is
 * used. This is the phrase set published by MacKenzie and Soukoreff in 2003 (see Related
 * References, above). Other file names may be specified. For example, the file <a
 * href="quickbrownfox.txt">quickbrownfox.txt</a> contains the phrase
 * <p>
 *
 * <pre>
 *     the quick brown fox jumps over the lazy dog
 * </pre>
 *
 * This phrase might be useful for demonstration or specialized testing.
 * <p>
 *
 * <tr valign="top">
 * <td>Show Popup Key
 * <td>A checkbox item that determines whether or not a popup key appears above an alpha key when
 * touched.
 * <p>
 *
 *
 * <tr valign="top">
 * <td>Lowercase Only
 * <td>A checkbox item that determines whether uppercase and lowercase letters are permitted in the
 * presented text phrases. If checked, any uppercase letters in the presented text phrases are
 * converted to lowercase.
 * <p>
 *
 * <tr valign="top">
 * <td>Show Presented Text During Input
 * <td>A checkbox item that determines whether or not the presented text is visible during entry of
 * a phrase. Either way, the text phrase appears at the beginning of a trial. If this option is
 * unchecked, the phrase will disappear when the first keystroke is entered.
 * <p>
 *
 * </table>
 * </blockquote>
 * <p>
 *
 * <h3>Program Operation</h3>
 *
 * The experiment launches with the selected soft keyboard displayed and with a text phrase
 * presented for input. The text phrase is selected at random from the phrase set specified in the
 * setup dialog.
 * <p>
 *
 * Text is entered by tapping with a finger on keys on the soft keyboard. The transcribed text
 * appears below the presented text in a separate text field. Timing and data collection begin with
 * the first tap on the soft keyboard.
 * <p>
 *
 * An example using a Google <i>Nexus 4</i> with the Qwerty keyboard is shown below on the left:
 * (click to enlarge)
 * <p>
 *
 * <center> <a href="SoftKeyboard-2.jpg"><img src="SoftKeyboard-2.jpg"
 * width=200 alt="image"></a> <a href="SoftKeyboard-3.jpg"><img src="SoftKeyboard-3.jpg"
 * width=200 alt="image"></a> <a href="SoftKeyboard-4.jpg"><img src="SoftKeyboard-4.jpg"
 * width=200 alt="image"></a> </center>
 * <p>
 *
 * Since the user's finger is likely to obscure the intended key, a popup key appears above the
 * touched key. This is shown in the middle image above (the user's finger is on the c-key). The
 * popup key only appears if the corresponding option in the setup dialog is selected. If the wrong
 * key appears, the user may slide her finger to the correct key. As the finger slides, the popup
 * key changes accordingly. Selection occurs on finger-lift. Input is aborted if the finger slides
 * off the keyboard before finger-lift.
 * <p>
 *
 * If a mistake is made, the "Bksp" may be tapped to erase the last character entered.
 * <p>
 *
 * At the end of a phrase, the user taps "Enter", whereupon timing and data collection end. A
 * results popup shows the presented and transcribed text phrases, the entry speed (in words per
 * minute), the error rate (%), and the keystrokes per character (KSPC). An example is shown in the
 * right image above.
 * <p>
 *
 * When the user taps "OK", the next phrase appears for input. After the specified number of phrases
 * are entered, the application terminates.
 * <p>
 *
 * Below are examples of the UI showing the effect of different settings from the setup dialog. The
 * effect of checking "Position keyboard at bottom of UI" is shown on the left. The effect of
 * changing "Keyboard scale" to 0.75 is shown on the right. (Click to enlarge.)
 * <p>
 *
 * <center> <a href="SoftKeyboard-6.jpg"><img src="SoftKeyboard-6.jpg"
 * width=200 alt="image"></a> <a href="SoftKeyboard-7.jpg"><img src="SoftKeyboard-7.jpg"
 * width=200 alt="image"></a> </center>
 * <p>
 *
 * <h3>Output Data Files</h3>
 *
 * As entry proceeds, user performance data are written to files. The files are stored into a
 * directory called "SoftKeyboardExperimentData" &ndash; a sub-directory within the device's public
 * external storage directory. This is typically a location in the device's SD memory card.
 * <p>
 *
 * There are two output data files, an "sd1" file and an "sd2" file. ("sd" is for "summary data".)
 * The sd1 file contains data on a keystroke by keystroke basis, including timestamps. The sd2 file
 * contains summary data for each phrase, one line per phrase. Examples follow:
 * <p>
 *
 * <ul>
 * <li><a href="SoftKeyboard-sd1-example.txt">sd1 example</a>
 * <li><a href="SoftKeyboard-sd2-example.txt">sd2 example</a>
 * </ul>
 * <p>
 *
 * Actual output files use "SoftKeyboardExperiment" as the base filename. This is followed by the
 * participant code, the session code, the block code, the group code, the condition code, and the
 * layout name. An example might be
 * <code>SoftKeyboardExperiment-P01-S01-B01-G01-C01-Qwerty-Scale_0.8.sd1</code>.
 * <p>
 *
 * In most cases, the sd2 data files are the primary files used for data analyses in an experimental
 * evaluation. The data in the sd2 files are full-precision, comma-delimited, to facilitate
 * importing into a spreadsheet or statistics application. Below is an example for the sd2 file
 * above, after importing into Microsoft Excel: (click to enlarge)
 * <p>
 *
 * <center> <a href="SoftKeyboard-5.jpg"><img src="SoftKeyboard-5.jpg"
 * width=800 alt="image"></a> </center>
 * <p>
 *
 * <h3>Future Enhancements</h3>
 *
 * The soft keyboards implemented in this application are bare-bones, and are limited to the entry
 * of lowercase alpha characters. The only non-alpha keys are SPACE, BACKSPACE, and ENTER. This is
 * sufficient for a basic evaluation of soft keyboard layouts; however, a comprehensive evaluation
 * should include the additional characters and interactions typically used for text entry. These
 * features are noted here as possible future enhancements, and include the following:
 * <p>
 *
 * <ul>
 * <li><b>Shift Mode</b> &ndash; a dedicated key labeled SHIFT maps the alpha characters to
 * uppercase. Shift mode could include shift lock whereby, for example, two taps on SHIFT
 * lock the keyboard in shift mode. A subsequent tap returns the keyboard to normal mode. Or,
 * perhaps shift could use swipes: touch a key, then swipe upward off the key to generate the
 * uppercase version of the alpha character.
 * <p>
 *
 * <li><b>Symbol Mode</b> &ndash; a mechanism to enter the myriad of non-alpha characters required
 * for text entry. Tapping a SYMBOL key remaps the characters appearing on keys. As with shift mode,
 * a symbol lock mode could be considered.
 * <p>
 *
 * <li><b>Word Completion</b> &ndash; a dedicated area above the keyboard presents suggested words
 * completing the current word stem. Tap the word to enter it.
 * <p>
 *
 * </ul>
 *
 * The issues of "basic evaluation" versus "comprehensive evaluation" (see above) bear on
 * <i>internal validity</i> and <i>external validity</i> in conducting a user study to evaluate soft
 * keyboards. While it might seem that a comprehensive evaluation is always preferred over a basic
 * evaluation, this is not necessarily the case. Further discussion is found in the Soukoreff and
 * MacKenzie paper, <a href="http://www.yorku.ca/mack/chi03b.html">Phrase sets for evaluating text
 * entry techniques</a> (<i>CHI 2003</i>).
 * <p>
 *
 *
 * <h3>Miscellaneous</h3>
 *
 * When using this program in an experiment, it is a good idea to terminate all other applications
 * and disable the system's network connection. This will maintain the integrity of the data
 * collected and ensure that the program runs without hesitations.
 * <p>
 *
 * @author (c) Scott MacKenzie, 2015-2017
 */
public class SoftKeyboardActivity extends Activity implements OnKeystrokeListener
{
    private final static String MYDEBUG = "MYDEBUG"; // for Log.i messages

    private final static String APP = "SoftKeyboard";
    private final static String DATA_DIRECTORY = "/SoftKeyboardData/";
    private final static String SD2_HEADER = "App,Participant,Session,Block,Group,Condition,Layout,Scale,"
            + "Keystrokes,Characters,Time(s),Speed(wpm),ErrorRate(%),KSPC\n";

    private int numberOfPhrases;
    private boolean lowercaseOnly, showPresentedTextDuringEntry;

    private int keystrokeCount; // number of strokes in a phrase
    private int phraseCount;
    private boolean done = false;
    private boolean endOfPhrase, firstKeystrokeInPhrase;
    private StringBuilder transcribedBuffer;
    private String presentedBuffer;
    private TextView presentedText;
    private EditText transcribedText;
    private Random r = new Random();
    private ArrayList<Sample> samples;
    private long elapsedTimeForPhrase;
    private long timeStartOfPhrase;
    private String[] phrases;
    private BufferedWriter sd1, sd2;
    private File f1, f2;
    private String sd2Leader; // sd2Leader to identify conditions for data written to sd2 files.

    // compute typing speed in wpm, given text entered and time in ms
    public static float wpm(String text, long msTime)
    {
        float speed = text.length();
        speed = speed / (msTime / 1000.0f) * (60 / 5);
        return speed;
    }

    // Called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // init study parameters from shared preferences passed from setup dialog
        Bundle b = getIntent().getExtras();
        String participantCode = b.getString("participantCode");
        String sessionCode = b.getString("sessionCode");
        String groupCode = b.getString("groupCode");
        String conditionCode = b.getString("conditionCode");
        String keyboardLayout = b.getString("keyboardLayout");
        float scalingFactor = b.getFloat("keyboardScale");
        float offsetFromBottom = b.getFloat("offsetFromBottom");
        numberOfPhrases = b.getInt("numberOfPhrases");
        String phrasesFile = b.getString("phrasesFile");
        boolean showPopupKey = b.getBoolean("showPopupKey");
        lowercaseOnly = b.getBoolean("lowercaseOnly");
        showPresentedTextDuringEntry = b.getBoolean("showPresented");

        String scaleString = "Scale_" + scalingFactor;

        presentedText = (TextView)findViewById(R.id.presented);
        transcribedText = (EditText)findViewById(R.id.transcribed);

        KeyboardPanel keyboard = (KeyboardPanel)findViewById(R.id.keyboard);
        keyboard.setOnKeystrokeListener(this);
        keyboard.setVibrator((Vibrator)getSystemService(Context.VIBRATOR_SERVICE));
        keyboard.setShowPopupKey(showPopupKey);
        keyboard.setOffsetFromBottom(offsetFromBottom);
        keyboard.invalidate();

        // load keyboard from resource file, based on setup choice for "Layout"
        // NOTE: String names must be the same as in the setup dialog (see setupparameters.xml)
        if (keyboardLayout.equals("Qwerty"))
            keyboard.loadKeyboardFromResource(R.array.qwerty, scalingFactor);
        else if (keyboardLayout.equals("Opti"))
            keyboard.loadKeyboardFromResource(R.array.opti, scalingFactor);
        else if (keyboardLayout.equals("Opti II"))
            keyboard.loadKeyboardFromResource(R.array.opti2, scalingFactor);
        else if (keyboardLayout.equals("Fitaly"))
            keyboard.loadKeyboardFromResource(R.array.fitaly, scalingFactor);
        else if (keyboardLayout.equals("Lewis"))
            keyboard.loadKeyboardFromResource(R.array.lewis, scalingFactor);
        else if (keyboardLayout.equals("Metropolis"))
            keyboard.loadKeyboardFromResource(R.array.metropolis, scalingFactor);

        LinearLayout keyboardContainer = (LinearLayout)findViewById(R.id.keyboardcontainer);
        keyboardContainer.setGravity(Gravity.BOTTOM);

        // load phrases from resource file, based on setup choice for "Phrases file"
        if (phrasesFile.equals("phrases2"))
            phrases = getResources().getStringArray(R.array.phrases2);
        else if (phrasesFile.equals("phrases100"))
            phrases = getResources().getStringArray(R.array.phrases100);
        else if (phrasesFile.equals("quickbrownfox"))
            phrases = getResources().getStringArray(R.array.quickbrownfox);
        else if (phrasesFile.equals("alphabet"))
            phrases = getResources().getStringArray(R.array.alphabet);

        // get this device's default orientation
        int defaultOrientation = getDefaultDeviceOrientation();

        // force the keyboard to appear in the device's default orientation (and stay that way)
        if (defaultOrientation == Configuration.ORIENTATION_LANDSCAPE)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // use an ArrayList to store timestamp+keystroke samples
        samples = new ArrayList<Sample>();

        // ===================
        // File initialization
        // ===================

        // make a working directory (if necessary) to store data files
        File dataDirectory = new File(Environment.getExternalStorageDirectory() +
                DATA_DIRECTORY);
        if (!dataDirectory.exists() && !dataDirectory.mkdirs())
        {
            Log.e(MYDEBUG, "ERROR --> FAILED TO CREATE DIRECTORY: " + DATA_DIRECTORY);
            super.onDestroy(); // cleanup
            this.finish(); // terminate
        }

        /**
         * The following do-loop creates data files for output and a string sd2Leader to write to the sd2
         * output files.  Both the filenames and the sd2Leader are constructed by combining the setup parameters
         * so that the filenames and sd2Leader are unique and also reveal the conditions used for the block of input.
         *
         * The block code begins "B01" and is incremented on each loop iteration until an available
         * filename is found.  The goal, of course, is to ensure data files are not inadvertently overwritten.
         */
        int blockNumber = 0;
        do
        {
            ++blockNumber;
            String blockCode = String.format(Locale.CANADA, "B%02d", blockNumber);
            String baseFilename = String.format("%s-%s-%s-%s-%s-%s-%s-%s", APP, participantCode,
                    sessionCode, blockCode, groupCode, conditionCode, keyboardLayout, scaleString);
            f1 = new File(dataDirectory, baseFilename + ".sd1");
            f2 = new File(dataDirectory, baseFilename + ".sd2");

            // also make a comma-delimited leader that will begin each data line written to the sd2 file
            sd2Leader = String.format("%s,%s,%s,%s,%s,%s,%s,%s", APP, participantCode, sessionCode,
                    blockCode, groupCode, conditionCode, keyboardLayout, scaleString);
        } while (f1.exists() || f2.exists());

        try
        {
            sd1 = new BufferedWriter(new FileWriter(f1));
            sd2 = new BufferedWriter(new FileWriter(f2));

            // output header in sd2 file
            sd2.write(SD2_HEADER, 0, SD2_HEADER.length());
            sd2.flush();

        } catch (IOException e)
        {
            Log.e(MYDEBUG, "ERROR OPENING DATA FILES! e=" + e.toString());
            super.onDestroy();
            this.finish();

        } // end file initialization

        // initialized a buffer to hold the user's input
        transcribedBuffer = new StringBuilder();

        // give focus transcribed text field so flashing I-beam appears
        transcribedText.requestFocus();

        // prevent soft keyboard from popping up
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        phraseCount = 0;
        doNewPhrase();

    } // end onCreate

    // get the default orientation of the device (affects how the tilt meter is rendered)
    public int getDefaultDeviceOrientation()
    {
        WindowManager windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        Configuration config = getResources().getConfiguration();
        int rotation = windowManager.getDefaultDisplay().getRotation();

        if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation ==
                Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation ==
                Configuration.ORIENTATION_PORTRAIT))
            return Configuration.ORIENTATION_LANDSCAPE;
        else
            return Configuration.ORIENTATION_PORTRAIT;
    }

    // callback from keyboard when the user taps a key (process it)
    public void onKeystroke(KeyboardEvent ke)
    {
        // the follow test is now done the onClick method defined in showResultsDialog
        /*if (done)
        {
            doDone();
            return;
        }*/

        switch (ke.type)
        {
            case KeyboardEvent.TYPE_ENTER:
                // ignore ENTER if no text has been entered
                if (transcribedBuffer.length() > 0)
                    endOfPhrase = true;
                break;

            case KeyboardEvent.TYPE_BACKSPACE:
                if (transcribedBuffer.length() > 0)
                    transcribedBuffer.delete(transcribedBuffer.length() - 1, transcribedBuffer.length
                            ());
                break;

            default: // just a character
                transcribedBuffer.append((char)ke.charCode);
        }

        ++keystrokeCount;

        if (endOfPhrase)
        {
            elapsedTimeForPhrase = ke.timeStampFingerUp - timeStartOfPhrase;
            --keystrokeCount; // don't count the final (ENTER) keystroke
            doEndOfPhrase();

        } else
        {
            transcribedText.setText(transcribedBuffer);

            // move cursor to end
            transcribedText.setSelection(transcribedText.getText().length());

            if (firstKeystrokeInPhrase)
            {
                if (!showPresentedTextDuringEntry)
                    presentedText.setText("");

                timeStartOfPhrase = ke.timeStampFingerUp;
                firstKeystrokeInPhrase = false;
            }
            elapsedTimeForPhrase = ke.timeStampFingerUp - timeStartOfPhrase;
            samples.add(new Sample(elapsedTimeForPhrase, ke.raw));
        }
    }

    public void doNewPhrase()
    {
        String phrase = phrases[r.nextInt(phrases.length)];
        presentedBuffer = lowercaseOnly ? phrase.toLowerCase(Locale.US) : phrase;
        presentedText.setText(presentedBuffer);
        transcribedBuffer.setLength(0);
        transcribedText.setText(transcribedBuffer);

        keystrokeCount = 0;
        samples.clear();
        endOfPhrase = false;
        firstKeystrokeInPhrase = true;
    }

    public void doEndOfPhrase()
    {
        if (presentedBuffer == null)
            return; // "Enter" button pressed before anything entered

        String presentedPhrase = presentedBuffer.toLowerCase(Locale.getDefault()).trim();
        String transcribedPhrase = transcribedBuffer.toString().toLowerCase(Locale.getDefault())
                .trim();

        String resultsString = "Thank you!\n\n";
        resultsString += "Presented...\n   " + presentedPhrase + "\n";
        resultsString += "Transcribed...\n   " + transcribedPhrase + "\n";

        StringBuilder sd2Data = new StringBuilder(100);
        sd2Data.append(String.format("%s,", sd2Leader));

        // output number of strokes (aka keystrokes)
        sd2Data.append(String.format(Locale.CANADA, "%d,", keystrokeCount));

        // output number of characters entered
        sd2Data.append(String.format(Locale.CANADA, "%d,", transcribedPhrase.length()));

        // output time in seconds
        float d = elapsedTimeForPhrase / 1000;
        sd2Data.append(String.format(Locale.CANADA, "%.2f,", d));

        // append output time in minutes
        //d = elapsedTimeForPhrase / 1000.0f / 60.0f;

        // output speed in words per minute
        d = wpm(transcribedPhrase, elapsedTimeForPhrase);
        resultsString += String.format(Locale.CANADA, "Entry speed: %.2f wpm\n", d);
        sd2Data.append(String.format(Locale.CANADA, "%f,", d));

        // output error rate for transcribed text
        // MSD2 s1s2 = new MSD2(presentedPhrase, transcribedPhraseUncorrected);
        MSD s1s2 = new MSD(presentedPhrase, transcribedPhrase);
        d = (float)s1s2.getErrorRateNew();
        resultsString += String.format(Locale.CANADA, "Error rate: %.2f%%\n", d);
        sd2Data.append(String.format(Locale.CANADA, "%f,", d));

        // output KSPC (keystrokes per character)
        d = (float)keystrokeCount / transcribedPhrase.length();
        resultsString += String.format(Locale.CANADA, "KSPC: %.4f\n\n", d);
        resultsString += "Click OK to continue";
        sd2Data.append(String.format(Locale.CANADA, "%f\n", d)); // end of line too!

        // dump data
        StringBuilder sd1Stuff = new StringBuilder(100);
        sd1Stuff.append(String.format("%s\n", presentedPhrase));
        sd1Stuff.append(String.format("%s\n", transcribedPhrase));

        //Iterator<Sample> it = samples.iterator();
        //while (it.hasNext())
        //    sd1Stuff.append(String.format("%s\n", it.next()));

        for (Sample value : samples)
            sd1Stuff.append(String.format("%s\n", value));

        sd1Stuff.append("-----\n");

        // write to data files
        try
        {
            sd1.write(sd1Stuff.toString(), 0, sd1Stuff.length());
            sd1.flush();
            sd2.write(sd2Data.toString(), 0, sd2Data.length());
            sd2.flush();
        } catch (IOException e)
        {
            Log.e("MYDEBUG", "ERROR WRITING TO DATA FILE!\n" + e);
            super.onDestroy();
            this.finish();
        }

        // present results to user
        showResultsDialog(resultsString);

        // check if last phrase in block
        ++phraseCount;
        if (phraseCount < numberOfPhrases)
            doNewPhrase();
        else
            done = true; // will terminate on next tap (allows the user to see results from last
        // phrase)
    }

    private void showResultsDialog(String text)
    {
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.results_dialog, (ViewGroup)findViewById(R.id
                .results_layout));

        // Set text
        TextView results = (TextView)layout.findViewById(R.id.resultsArea);
        results.setText(text);

        // Initialize the dialog
        AlertDialog.Builder parameters = new AlertDialog.Builder(this);
        parameters.setView(layout).setCancelable(false).setNeutralButton("OK", new
                DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel(); // close this dialog
                        if (done)
                            doDone();
                    }
                }).show();
    }

    private void doDone()
    {
        try
        {
            sd1.close();
            sd2.close();

			/*
             * Make the saved data files visible in Windows Explorer. There seems to be bug doing
			 * this with Android 4.4. I'm using the following code, instead of sendBroadcast.
			 * See...
			 * 
			 * http://code.google.com/p/android/issues/detail?id=38282
			 */
            MediaScannerConnection.scanFile(this, new String[] {f1.getAbsolutePath(), f2
                            .getAbsolutePath()}, null,
                    null);

        } catch (IOException e)
        {
            Log.e(MYDEBUG, "ERROR CLOSING DATA FILES! e=" + e);
        }

        // 24/03/2017: finish by returning to the setup dialog
        startActivity(new Intent(getApplicationContext(), SoftKeyboardSetup.class));
        //super.onDestroy();
        this.finish();
    }

    // -------------------------------------------------------
    // Sample - simple class to hold a timestamp and keystroke
    // -------------------------------------------------------
    private class Sample
    {
        private long time;
        private String key;

        Sample(long timeArg, String keyArg)
        {
            time = timeArg;
            key = keyArg;
        }

        public String toString()
        {
            return time + ", " + key;
        }
    }
}