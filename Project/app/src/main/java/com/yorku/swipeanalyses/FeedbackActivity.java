package com.yorku.swipeanalyses;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import androidx.appcompat.app.AppCompatActivity;

public class FeedbackActivity extends AppCompatActivity {

    private String question1Answer = "Easy";
    private String question2Answer = "Horizontal";
    private String question3Answer = "Horizontal";
    private String username;
    private String userGroupId;
    private String swipeTimes;
    private String experimentDuration;
    private static final String TAG = "FeedbackActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        userGroupId = intent.getStringExtra("userGroupId");
        swipeTimes = intent.getStringExtra("swipeTimes");
        experimentDuration = intent.getStringExtra("experimentDuration");
    }

    @SuppressLint("NonConstantResourceId")
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.q1_easy:
                if (checked)
                    question1Answer = "Easy";
                    break;

            case R.id.q1_neutral:
                if (checked)
                    question1Answer = "Neutral";
                    break;

            case R.id.q1_hard:
                if (checked)
                    question1Answer = "Hard";
                    break;

            case R.id.q2_horizontal_swipe:
                if (checked)
                    question2Answer = "Horizontal";
                break;

            case R.id.q2_vertical_swipe:
                if (checked)
                    question2Answer = "Vertical";
                break;

            case R.id.q2_no_swipe:
                if (checked)
                    question2Answer = "Buttons";
                break;

            case R.id.q3_horizontal_swipe:
                if (checked)
                    question3Answer = "Horizontal";
                break;

            case R.id.q3_vertical_swipe:
                if (checked)
                    question3Answer = "Vertical";
                break;

            case R.id.q3_no_swipe:
                if (checked)
                    question3Answer = "Buttons";
                break;
            default:
                break;
        }
    }

    public void onFinishExperiment(View view) {

        String fileName = "report.sd";

        try {
            File directory = new File(Environment.getExternalStorageDirectory() +"/SwipeExperiment/");
            if (!directory.exists() && !directory.mkdirs())
            {
                Log.e(TAG, "Failed to create directory: " + directory.getAbsolutePath());
                Intent myIntent = new Intent(FeedbackActivity.this, MainActivity.class);
                startActivity(myIntent);
                finish(); // terminate
            }

            File reportFile = new File(directory, fileName);
            if (reportFile.createNewFile()) {
                Log.i(TAG,"File created: " + reportFile.getName());

                FileOutputStream fileOutputStream = new FileOutputStream(reportFile, true);
                OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
                writer.append("username,userGroupId,swipeTimes,experimentDuration,q1,q2,q3\n");
                writer.close();
                fileOutputStream.close();

            } else {
                Log.i(TAG,"File already exists.");
            }

            FileOutputStream fileOutputStream = new FileOutputStream(reportFile, true);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            writer.append(username + "," + userGroupId + "," + swipeTimes + "," + experimentDuration + "," + question1Answer + "," + question2Answer + "," + question3Answer + "\n");
            writer.close();
            fileOutputStream.close();

        } catch (IOException e) {
            Log.e(TAG, "Unable to save file.");
            e.printStackTrace();
        } finally {
            Intent myIntent = new Intent(FeedbackActivity.this, MainActivity.class);
            startActivity(myIntent);
            finish();// terminate
        }

    }
}
