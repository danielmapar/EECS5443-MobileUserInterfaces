package ca.yorku.eecs.mack.demoandroid81497;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

/**
 * Demo_Android - with modifications by...
 *
 * Login ID - dmapar
 * Student ID - 216181497
 * Last name - Parreira
 * First name(s) - Daniel
 */
public class DemoAndroid81497Activity extends Activity implements OnClickListener {
    private final static String MYDEBUG = "MYDEBUG"; // for Log.i messages

    private Button incrementButton, decrementButton, exitButton, resetButton;
    private TextView textview;
    private int clickCount;

    // called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initialize();
        Log.i(MYDEBUG, "Initialization done. Application running.");
    }

    private void initialize() {
        // get references to buttons and text view from the layout manager (rather than instantiate them)
        incrementButton = (Button) findViewById(R.id.incbutton);
        decrementButton = (Button) findViewById(R.id.decbutton);
        resetButton = (Button) findViewById(R.id.resetbutton);
        exitButton = (Button) findViewById(R.id.exitbutton);
        textview = (TextView) findViewById(R.id.textview);

        // some code is missing here

        // We need to set a listener class for our buttons (a class that implements OnClickListener).
        // Our activity implements OnClickListener, so we will use "this" and attach to our buttons.
        // That will then trigger onClick.
        incrementButton.setOnClickListener(this);
        decrementButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);

        // initialize the click count
        clickCount = 0;

        // initialize the text field with the click count
        textview.setText(String.format(Locale.CANADA, "Count: %d", clickCount));
    }

    // this code executes when a button is clicked (i.e., tapped with user's finger)
    @Override
    public void onClick(View v) {
        if (v == incrementButton) {
            Log.i(MYDEBUG, "Increment button clicked!");
            ++clickCount;

        } else if (v == decrementButton) {
            Log.i(MYDEBUG, "Decrement button clicked!");
            --clickCount;

        } else if (v == resetButton) {
            Log.i(MYDEBUG, "Reset button clicked!");
            clickCount = 0;
        } else if (v == exitButton) {
            Log.i(MYDEBUG, "Good bye!");
            this.finish();

        } else {
            Log.i(MYDEBUG, "Oops: Invalid Click Event!");
        }
        // update click count
        textview.setText(String.format(Locale.CANADA, "Count: %d", clickCount));
    }
}