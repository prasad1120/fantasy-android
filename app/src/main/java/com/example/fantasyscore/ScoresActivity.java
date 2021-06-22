package com.example.fantasyscore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.fantasyscore.NetworkHandler.teams;

public class ScoresActivity extends AppCompatActivity {
    String matchId;
    String info;
    ArrayList<String> teamNames;
    ArrayList<String> scores;

    TableLayout table;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        Bundle args = getIntent().getBundleExtra("BUNDLE");
        matchId = args.getString("matchId");
        info = args.getString("info");
        teamNames = args.getStringArrayList("teamNames");
        scores = args.getStringArrayList("scores");

        table = findViewById(R.id.tableLayoutScores);
        progressBar = findViewById(R.id.loading);

        ((TextView) findViewById(R.id.infoTv)).setText(info);
        populateTable();

        final Button saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                findViewById(R.id.backgroundView).setVisibility(View.VISIBLE);
                saveBtn.setEnabled(false);

                postScores();
            }
        });
    }

    void postScores() {
        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            ConstraintLayout layout = (ConstraintLayout) row.getChildAt(0);
            EditText scoreEt = layout.findViewById(R.id.scoreEt);
            scores.add(i, scoreEt.getText().toString());
        }

        HashMap<String, String> dataToPost = new HashMap<>();
        dataToPost.put("version", "IPL 2021");
        dataToPost.put("action", "addEntry");
        dataToPost.put("matchId", matchId + "");

        dataToPost.put("info", info);

        for (int i=0; i<teams.size(); i++) {

            int index = teamNames.indexOf(teams.get(i));
            String score = index != -1 ? scores.get(index) : "0";

            dataToPost.put("t" + (i + 1), score);
        }

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MainActivity", "postEntryResponse: " + response);
                finish();
            }
        };

        NetworkHandler.postData(getApplicationContext(), dataToPost, responseListener);
    }

    void populateTable() {

        for (int i = 0; i < teamNames.size(); i++) {
            TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.score_row, null);

            ConstraintLayout layout = (ConstraintLayout) row.getChildAt(0);
            TextView teamNameTv = layout.findViewById(R.id.team1);
            teamNameTv.setText(teamNames.get(i));

            EditText scoreEt = layout.findViewById(R.id.scoreEt);
            scoreEt.setText(scores.get(i));

            table.addView(row);
        }
    }
}
