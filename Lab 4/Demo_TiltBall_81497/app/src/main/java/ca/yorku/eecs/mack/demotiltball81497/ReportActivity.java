package ca.yorku.eecs.mack.demotiltball81497;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

public class ReportActivity extends Activity {

    private int report;

    // called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.report);

        // get parameters selected by user from setup dialog
        Bundle b = getIntent().getExtras();
        int numberOfLaps = b.getInt("numberOfLaps");
        float lapTime = b.getFloat("lapTime");
        int numberOfWallsHit = b.getInt("numberOfWallsHit");
        float percentageInPathMovementTime = b.getFloat("percentageInPathMovementTime");

        TextView numberOfLapsView = (TextView) findViewById(R.id.paramNumberOfLaps);
        numberOfLapsView.setText(String.valueOf(numberOfLaps));


        TextView lapTimeView = findViewById(R.id.paramLapTime);
        lapTimeView.setText(String.format("%.2f s (mean/lap)", lapTime));

        TextView numberOfWallsHitView = findViewById(R.id.paramNumberOfWallsHit);
        numberOfWallsHitView.setText(String.valueOf(numberOfWallsHit));

        TextView percentageInPathMovementTimeView = findViewById(R.id.paramPercentageInPathMovementTime);
        percentageInPathMovementTimeView.setText(String.format("%.2f %%", percentageInPathMovementTime));

    }

    // called when the "Setup" button is tapped
    public void clickSetup(View view)
    {
        Intent i = new Intent(getApplicationContext(), DemoTiltBallSetup.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    /** Called when the "Exit" button is pressed. */
    public void clickExit(View view)
    {
        super.onDestroy(); // cleanup
        this.finish(); // terminate
    }

}
