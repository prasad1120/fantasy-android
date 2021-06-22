package com.example.fantasyscore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.canhub.cropper.CropImage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

import static com.example.fantasyscore.Secrets.SHEET_URL;

public class MainActivity extends AppCompatActivity implements OnItemClickListener  {

    final static int WINNER_PHOTO_TIME_MS =3000;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    ImageView imageView;
    Button skipBtn;
    CountDownTimer timer;
    RecyclerAdapter recyclerAdapter;
    Match[] matches;
    int numberOfMatchesScoresUploaded = 0;
    int positionClicked = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.loading);
        imageView = findViewById(R.id.image_view);
        skipBtn = findViewById(R.id.skip_btn);

        skipBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hidePhoto();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (timer != null) {
            timer.cancel();
        }
    }

    void getSchedule() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseObj = new JSONObject(response);
                    numberOfMatchesScoresUploaded = responseObj.getInt("noOfMatchesScoresUploaded");
                    matches = Match.parseMatchesJson(new JSONObject(responseObj.getString("matches")));
                    progressBar.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    populateTable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        HashMap<String, String> params = new HashMap<>();
        params.put("version", "IPL 2021");
        params.put("action", "getSchedule");
        NetworkHandler.postData(getApplicationContext(), params, responseListener);
    }

    void showPhoto() {
        imageView.setVisibility(View.VISIBLE);
        skipBtn.setVisibility(View.VISIBLE);
        skipBtn.setEnabled(false);

        timer = new CountDownTimer(WINNER_PHOTO_TIME_MS, 1000) {

            public void onTick(long millisUntilFinished) {
                skipBtn.setText(millisUntilFinished / 1000 + "");
            }

            public void onFinish() {
                skipBtn.setText("skip");
                skipBtn.setEnabled(true);
            }
        }.start();
    }

    void hidePhoto() {
        imageView.setVisibility(View.INVISIBLE);
        skipBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hidePhoto();
        getImageList();
        getSchedule();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        Drawable drawable = menu.getItem(0).getIcon();
        if (drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primaryTextColor), PorterDuff.Mode.SRC_ATOP);
        }
        return true;
    }

    public boolean openSheets(MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(SHEET_URL));
        startActivity(intent);
        return true;
    }

    void populateTable() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter = new RecyclerAdapter(matches, numberOfMatchesScoresUploaded, this);
        recyclerView.setAdapter(recyclerAdapter);

        if (numberOfMatchesScoresUploaded < 1) {
            return;
        }

        recyclerView.getLayoutManager().scrollToPosition(numberOfMatchesScoresUploaded - 1);
    }

    boolean isNumber(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (!((c >= '0' && c <= '9') || c == '.')) {
                return false;
            }
        }
        return true;
    }

    public void openScoreActivity(String matchId, String info, ArrayList<String> teamNames, ArrayList<String> scores) {
        Intent intent = new Intent(this, ScoresActivity.class);
        Bundle args = new Bundle();
        args.putString("matchId", matchId);
        args.putString("info", info);
        args.putSerializable("teamNames", teamNames);
        args.putSerializable("scores", scores);
        intent.putExtra("BUNDLE",args);
        startActivity(intent);
    }

    public String getTeam(String team) {

        for (int i = 0; i < NetworkHandler.teams.size(); i++) {
            if (team.contains(NetworkHandler.teams.get(i))) {
                return NetworkHandler.teams.get(i);
            }
        }
        return null;
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Inside OnactivityResult", "abc");
        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE:
                    CropImage.INSTANCE.activity(data.getData()).start(MainActivity.this);
                    break;

                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.INSTANCE.getActivityResult(data);

                    if (resultCode == RESULT_OK) {
                        scanScoreboardImg(requestCode, result.getUri());
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Exception error = result.getError();
                        Log.e("Crop error", error.getLocalizedMessage());
                    }
                    break;
            }

        }
    }

    void scanScoreboardImg(final int requestCode, Uri selectedImage) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();

            detector.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {

                            ArrayList<String> teamNames = new ArrayList<>();
                            ArrayList<String> scores = new ArrayList<>();
                            Scanner scanner = new Scanner(firebaseVisionText.getText());

                            while (scanner.hasNextLine()) {
                                String word = scanner.nextLine();
                                String team = getTeam(word);

                                if (team != null) {
                                    teamNames.add(team);
                                } else if (isNumber(word) && (Double.parseDouble(word) > 7)){
                                    scores.add(word);
                                }
                            }
                            scanner.close();

                            for (String team: NetworkHandler.teams) {
                                if (!teamNames.contains(team)) {
                                    teamNames.add(team);
                                    scores.add("0.0");
                                }
                            }

                            SimpleDateFormat format = new SimpleDateFormat("dd/MM  HH:mm");
                            String info = "#" + matches[positionClicked - 1].id + "\n" + matches[positionClicked - 1].t1 + " vs " + matches[positionClicked - 1].t2 + "\n" + format.format(matches[positionClicked - 1].time);

                            openScoreActivity(positionClicked + "", info, teamNames, scores);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("MainActivity", e.getLocalizedMessage());
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void getImageList() {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference listRef = storage.getReference().child("winner_images");

        listRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        Log.d("MainActivity", "Image list returned");
                        StorageReference randomImgRef = listResult.getItems().get((int) Math.ceil(Math.random() * listResult.getItems().size()) - 1);

                        downloadImg(randomImgRef);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                    }
                });
    }

    void downloadImg(StorageReference ref) {
        final long ONE_MEGABYTE = 1024 * 1024;
        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(Bitmap.createBitmap(bmp));
                showPhoto();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        positionClicked = position + 1;
        showUpdateScoresChooser();
    }

    void showUpdateScoresChooser() {
        final String[] options = {
                "Manually", "Using Screenshot"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Update Scores");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ("Manually".equals(options[which])) {
                    ArrayList<String> scores= new ArrayList<>(Arrays.asList(new String[40]));
                    Collections.fill(scores, "0.0");

                    SimpleDateFormat format = new SimpleDateFormat("dd/MM  HH:mm");
                    String info = "#" + matches[positionClicked - 1].id + "\n" + matches[positionClicked - 1].t1 + " vs " + matches[positionClicked - 1].t2 + "\n" + format.format(matches[positionClicked - 1].time);

                    openScoreActivity(positionClicked + "", info, NetworkHandler.teams, scores);
                } else if ("Using Screenshot".equals(options[which])) {
                    CropImage.INSTANCE.startPickImageActivity(MainActivity.this);
                }
            }
        });
        builder.show();
    }
}
