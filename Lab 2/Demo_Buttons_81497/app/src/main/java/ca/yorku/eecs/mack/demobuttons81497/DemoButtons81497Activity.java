package ca.yorku.eecs.mack.demobuttons81497;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Demo_Buttons - with modifications by...
 *
 * Login ID - dmapar
 * Student ID - 216181497
 * Last name - Parreira
 * First name(s) - Daniel
 */
public class DemoButtons81497Activity extends Activity {
    private final static String MYDEBUG = "MYDEBUG"; // for Log.i messages

    // Daniels comment
    // Constants created to keep track of our map of UI components data
    private final static String SIMPLE_BUTTON__KEY = "simple_button"; // "key" for simple button
    private final static String CHECK_BOX_KEY = "check_box"; // "key" for check box
    private final static String RADIO_BUTTON_1_KEY = "radion_button_1"; // "key" for radio button 1
    private final static String RADIO_BUTTON_2_KEY = "radion_button_2"; // "key" for radio button 2
    private final static String RADIO_BUTTON_3_KEY = "radion_button_3"; // "key" for radio button 3
    private final static String ON_OFF_BUTTON_KEY = "on_off_button"; // "key" for on/off button
    private final static String BACKSPACE_BUTTON_KEY = "backspace_button"; // "key" for backspace button

    // Daniels comment
    // My new original feature is a custom dropdown
    private final static String DROPDOWN_KEY = "dropdown"; // "key" for dropdown

    Button b;
    CheckBox cb;
    RadioButton rb1, rb2, rb3;
    ToggleButton tb;
    ImageButton backspaceButton;
    TextView buttonClickStatus, checkBoxClickStatus, radioButtonClickStatus, toggleButtonClickStatus,
            backspaceButtonClickStatus;
    Spinner dropdown;
    String[] dropdownItems;

    String buttonClickString, backspaceString;

    // called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String s = " (savedInstanceState bundle is ";
        if (savedInstanceState == null)
            s += "null)";
        else
            s += "NOT null)";

        Log.i(MYDEBUG, "onCreate!" + s);

        setContentView(R.layout.main);
        init();
    }

    private void init() {
        b = (Button) findViewById(R.id.button);
        cb = (CheckBox) findViewById(R.id.checkbox);
        rb1 = (RadioButton) findViewById(R.id.radiobutton1);
        rb2 = (RadioButton) findViewById(R.id.radiobutton2);
        rb3 = (RadioButton) findViewById(R.id.radiobutton3);
        rb1.toggle();
        tb = (ToggleButton) findViewById(R.id.togglebutton);
        backspaceButton = (ImageButton) findViewById(R.id.backspacebutton);

        dropdown = findViewById(R.id.spinner1);
        // Create a list of items for the spinner.
        dropdownItems = new String[]{"Apples", "Bananas", "Mellon"};

        // Create an adapter to describe how the items are displayed, adapters are used in several places in android.
        // There are multiple variations of this, but this is the basic variant.
        dropdown.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, dropdownItems));

        buttonClickStatus = (TextView) findViewById(R.id.buttonclickstatus);
        checkBoxClickStatus = (TextView) findViewById(R.id.checkboxclickstatus);
        radioButtonClickStatus = (TextView) findViewById(R.id.radiobuttonclickstatus);
        toggleButtonClickStatus = (TextView) findViewById(R.id.togglebuttonclickstatus);
        backspaceButtonClickStatus = (TextView) findViewById(R.id.backspacebuttonclickstatus);

        buttonClickString = "";
        backspaceString = "";

        buttonClickStatus.setText(buttonClickString);
        checkBoxClickStatus.setText(R.string.unchecked);
        radioButtonClickStatus.setText(R.string.red);
        radioButtonClickStatus.setTextColor(Color.RED);
        toggleButtonClickStatus.setText(R.string.off);
    }

    // Daniels comments
    // Overloaded some activity methods to track app state changes
    @Override
    public void onStart() {
        super.onStart();
        Log.i(MYDEBUG, "onStart!");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(MYDEBUG, "onResume!");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(MYDEBUG, "onPause!");
    }

    @Override
    public void onRestart() {
        Log.i(MYDEBUG, "onRestart!");
        super.onRestart();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(MYDEBUG, "onStop!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(MYDEBUG, "onDestroy!");
    }

    // handle button clicks
    public void buttonClick(View v) {
        // plain button
        if (v == b) {
            buttonClickString += ".";
            buttonClickStatus.setText(buttonClickString);
        }

        // checkbox
        else if (v == cb) {
            if (cb.isChecked()) {
                checkBoxClickStatus.setText(R.string.checked);
            } else {
                checkBoxClickStatus.setText(R.string.unchecked);
            }
        }

        // Daniels comment
        // Removed the "setCheck" since it is already done by the components themselves

        // radio button #1 (RED)
        else if (v == rb1) {
            if (rb1.isChecked()) {
                radioButtonClickStatus.setText(R.string.red);
                radioButtonClickStatus.setTextColor(Color.RED);
            }
        }

        // radio button #2 (GREEN)
        else if (v == rb2) {
            if (rb2.isChecked()) {
                radioButtonClickStatus.setText(R.string.green);
                radioButtonClickStatus.setTextColor(Color.GREEN);
            }
        }

        // radio button #3 (BLUE)
        else if (v == rb3) {
            if (rb3.isChecked()) {
                radioButtonClickStatus.setText(R.string.blue);
                radioButtonClickStatus.setTextColor(Color.BLUE);
            }
        }

        // toggle button
        else if (v == tb) {
            tb.setActivated(tb.isChecked());
            if (tb.isChecked())
                toggleButtonClickStatus.setText(R.string.on);
            else
                toggleButtonClickStatus.setText(R.string.off);
        }

        // backspace button
        else if (v == backspaceButton) {
            backspaceString += "BK ";
            backspaceButtonClickStatus.setText(backspaceString);
        }

    }

    // Daniels comment
    // The following method "onSaveInstanceState" is responsible for saving the state of our UI
    // components inside the "savedInstanceState" object when the orientation changes.

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(MYDEBUG, "onSaveInstanceState!");
        savedInstanceState.putString(SIMPLE_BUTTON__KEY, buttonClickString);
        savedInstanceState.putBoolean(CHECK_BOX_KEY, cb.isChecked());
        savedInstanceState.putBoolean(RADIO_BUTTON_1_KEY, rb1.isChecked());
        savedInstanceState.putBoolean(RADIO_BUTTON_2_KEY, rb2.isChecked());
        savedInstanceState.putBoolean(RADIO_BUTTON_3_KEY, rb3.isChecked());
        savedInstanceState.putBoolean(ON_OFF_BUTTON_KEY, tb.isChecked());
        savedInstanceState.putString(BACKSPACE_BUTTON_KEY, backspaceString);
    }

    // Daniels comment
    // The following method is responsible for retrieving the UI state when the orientation
    // changes. It will get the information inside the "savedInstanceState" object and
    // re-populate the fields

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(MYDEBUG, "onRestoreInstanceState!");

        buttonClickString = savedInstanceState.getString(SIMPLE_BUTTON__KEY);
        this.buttonClick(b);

        cb.setChecked(savedInstanceState.getBoolean(CHECK_BOX_KEY));
        this.buttonClick(cb);

        rb1.setChecked(savedInstanceState.getBoolean(RADIO_BUTTON_1_KEY));
        this.buttonClick(rb1);

        rb2.setChecked(savedInstanceState.getBoolean(RADIO_BUTTON_2_KEY));
        this.buttonClick(rb2);

        rb3.setChecked(savedInstanceState.getBoolean(RADIO_BUTTON_3_KEY));
        this.buttonClick(rb3);

        tb.setChecked(savedInstanceState.getBoolean(ON_OFF_BUTTON_KEY));
        this.buttonClick(tb);

        backspaceString = savedInstanceState.getString(BACKSPACE_BUTTON_KEY);
        this.buttonClick(backspaceButton);
    }
}