package com.yorku.swipeanalyses;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText nameInputField;
    private Spinner userGroupIdSpinner;
    private Button startExperimentBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameInputField = (EditText) findViewById(R.id.name);
        userGroupIdSpinner= (Spinner) findViewById(R.id.userGroupId);
        startExperimentBtn = (Button) findViewById(R.id.startExperiment);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userGroupIdSpinner.setAdapter(adapter);

        nameInputField.addTextChangedListener(new TextValidator(nameInputField) {
            @Override public void validate(TextView textView, String text) {
                if( nameInputField.getText().toString().length() == 0 )
                    nameInputField.setError( "Name is required!" );
            }
        });

        startExperimentBtn.setOnClickListener(v -> {
            if( nameInputField.getText().toString().length() == 0) {
                nameInputField.setError( "Name is required!" );
                return;
            }
            Intent myIntent = new Intent(MainActivity.this, CardStackActivity.class);
            myIntent.putExtra("username", nameInputField.getText().toString());
            myIntent.putExtra("userGroupId", userGroupIdSpinner.getSelectedItem().toString());
            startActivity(myIntent);
            finish();
        });
    }
}