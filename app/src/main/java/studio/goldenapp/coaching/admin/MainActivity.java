
package studio.goldenapp.coaching.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Storage permissions ...
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int SELECT_VIDEO = 1;
    private String selectedVideoPath;

    Dialog _myDialog;
    TextView upload_video_name;
    EditText video_title, video_description, video_teacher, video_price, strike_price;
    String videoTitle, videoDescription, videoTeacher, videoPrice, strikePrice;
    Button file_picker, _continue, upload_video_button;
    RelativeLayout _progressBar_layout;
    ProgressBar progressBar;
    LinearLayout upload_video_layout, video_uploaded_success;

    Dialog dialog;
    EditText name, address, phone, email, description;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _myDialog = new Dialog(MainActivity.this);
        _myDialog.setContentView(R.layout.upload_video_popup);

        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.general_popup);

        name = dialog.findViewById(R.id.name_general);
        description = dialog.findViewById(R.id.description_general);
        email = dialog.findViewById(R.id.email_general);
        phone = dialog.findViewById(R.id.phone_new);
        address = dialog.findViewById(R.id.address_new);
        submit = dialog.findViewById(R.id.upload_general_button);

        upload_video_name = _myDialog.findViewById(R.id.upload_video_name);
        video_title = _myDialog.findViewById(R.id.video_title_new);
        video_price = _myDialog.findViewById(R.id.video_price_new);
        strike_price = _myDialog.findViewById(R.id.video_strike_price_new);
        video_teacher = _myDialog.findViewById(R.id.video_teacher_new);
        video_description = _myDialog.findViewById(R.id.video_content_new);
        file_picker = _myDialog.findViewById(R.id.file_picker);
        upload_video_button = _myDialog.findViewById(R.id.upload_video_button);
        _continue = _myDialog.findViewById(R.id.continue_button_video_uploaded);
        _progressBar_layout = _myDialog.findViewById(R.id.progress_bar_view);
        progressBar = _myDialog.findViewById(R.id.RAND_3);

        upload_video_layout = _myDialog.findViewById(R.id.upload_video_layout);
        video_uploaded_success = _myDialog.findViewById(R.id.video_uploaded_success);

        if (getIntent().getExtras() != null) {
            _myDialog.show();
            video_title.setText(getIntent().getExtras().getString("video_title"));
            video_price.setText(getIntent().getExtras().getString("video_price"));
            strike_price.setText(getIntent().getExtras().getString("strike_price"));
            video_description.setText(getIntent().getExtras().getString("video_description"));
            video_teacher.setText(getIntent().getExtras().getString("video_teacher"));
            file_picker.setVisibility(View.GONE);
            upload_video_name.setVisibility(View.VISIBLE);
            upload_video_button.setVisibility(View.VISIBLE);

            File video_file = new File(getIntent().getExtras().getString("base_video"));
            upload_video_name.setText(video_file.getName());
        }

        submit.setOnClickListener(view -> {
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("contacts");
            db.child("name").setValue(name.getText().toString());
            db.child("description").setValue(description.getText().toString());
            db.child("email").setValue(email.getText().toString());
            db.child("phone").setValue(phone.getText().toString());
            db.child("address").setValue(address.getText().toString());

            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        final CardView create_batch_card = findViewById(R.id.create_batch_card);
        final CardView batch_list_card = findViewById(R.id.batch_list_card);
        final CardView notification_card = findViewById(R.id.notification_card);
        final CardView upload_video_card = findViewById(R.id.upload_video_card);
        final CardView pdf_card = findViewById(R.id.pdf_card);
        final CardView fsm_card = findViewById(R.id.fsm_card);
        final CardView student_list_card = findViewById(R.id.student_list_card);
        final CardView quiz_card = findViewById(R.id.quiz_card);
        final CardView video_list_card = findViewById(R.id.video_list_card);
        final CardView general_card = findViewById(R.id.general_card);

        general_card.setOnClickListener(view -> {
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("contacts");
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        if(snapshot.child("name").exists()) {
                            name.setText(snapshot.child("name").getValue().toString());
                        }
                        if(snapshot.child("description").exists()) {
                            description.setText(snapshot.child("description").getValue().toString());
                        }
                        if(snapshot.child("email").exists()) {
                            email.setText(snapshot.child("email").getValue().toString());
                        }
                        if(snapshot.child("phone").exists()) {
                            phone.setText(snapshot.child("phone").getValue().toString());
                        }
                        if(snapshot.child("address").exists()) {
                            address.setText(snapshot.child("address").getValue().toString());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }

            });
            dialog.show();
        });

        create_batch_card.setOnClickListener(view -> initialise_batch_creation());

        batch_list_card.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, BatchList.class);
            startActivity(intent);
        });

        quiz_card.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, QuizActivity.class);
            startActivity(intent);
        });

        video_list_card.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, VideoListActivity.class);
            startActivity(intent);
        });

        notification_card.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        upload_video_card.setOnClickListener(view -> initialise_video_upload());

        pdf_card.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PdfActivity.class);
            startActivity(intent);
        });

        fsm_card.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FsmActivity.class);
            startActivity(intent);
        });

        student_list_card.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, StudentListActivity.class);
            startActivity(intent);
        });

        upload_video_button.setOnClickListener(view -> upload_video());

        _continue.setOnClickListener(view -> {
            getIntent().getExtras().clear();
            Intent intent = new Intent(getApplicationContext(),
                    MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    public void initialise_batch_creation() {

        final Dialog myDialog = new Dialog(MainActivity.this);
        myDialog.setContentView(R.layout.create_batch_popup);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();

        final EditText batch_name = myDialog.findViewById(R.id.batch_name_new);
        final EditText batch_id = myDialog.findViewById(R.id.batch_id_new);
        final EditText batch_password = myDialog.findViewById(R.id.batch_password_new);
        final EditText batch_creator = myDialog.findViewById(R.id.batch_creator_new);
        final Button create_batch = myDialog.findViewById(R.id.create_batch_button);
        final Button _continue = myDialog.findViewById(R.id.continue_button_batch_created);
        final RelativeLayout progressBar_layout = myDialog.findViewById(R.id.progress_bar_view);

        final LinearLayout batch_create_layout = myDialog.findViewById(R.id.create_new_batch_layout);
        final LinearLayout batch_created_success = myDialog.findViewById(R.id.batch_created_success);

        _continue.setOnClickListener(view -> myDialog.dismiss());

        create_batch.setOnClickListener(view -> {
            if (batch_name.getText().toString().isEmpty()) {
                batch_name.setError("Batch Name is required!");
            } else if (batch_id.getText().toString().isEmpty()) {
                batch_id.setError("Batch ID is required!");
            } else if (batch_password.getText().toString().isEmpty()) {
                batch_password.setError("Batch password is required!");
            } else if (batch_creator.getText().toString().isEmpty()) {
                batch_creator.setError("Batch creator is required!");
            } else {
                create_batch.setVisibility(View.GONE);
                progressBar_layout.setVisibility(View.VISIBLE);

                final List<String> batch_id_list = new ArrayList<>();

                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("batches");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            batch_id_list.add(dataSnapshot.child("batch_id").getValue().toString());
                        }

                        if (batch_id_list.contains(batch_id.getText().toString())) {
                            batch_id.setError("Batch ID is taken, please try another!");
                            progressBar_layout.setVisibility(View.GONE);
                            create_batch.setVisibility(View.VISIBLE);
                        } else {
                            if (create_batch(
                                    batch_name.getText().toString(),
                                    batch_id.getText().toString(),
                                    batch_password.getText().toString(),
                                    batch_creator.getText().toString()
                            ) == 1
                            ) {
                                batch_create_layout.setVisibility(View.GONE);
                                batch_created_success.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }

    public int create_batch(String batch_name, String batch_id, String batch_password, String batch_creator) {

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("batches");

        reference.child(batch_id).child("batch_name").setValue(batch_name);
        reference.child(batch_id).child("batch_id").setValue(batch_id);
        reference.child(batch_id).child("batch_password").setValue(batch_password);
        reference.child(batch_id).child("created_by").setValue(batch_creator);
        reference.child(batch_id).child("batch_created_time").setValue(formatter.format(date));
        return 1;
    }

    public void initialise_video_upload() {

        _myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        _myDialog.show();

        file_picker.setOnClickListener(view -> {
            videoTitle = video_title.getText().toString();
            videoPrice = video_price.getText().toString();
            strikePrice = strike_price.getText().toString();
            videoDescription = video_description.getText().toString();
            videoTeacher = video_teacher.getText().toString();
            _myDialog.dismiss();
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, SELECT_VIDEO);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_VIDEO) {
                selectedVideoPath = getPath(data.getData());
                if (selectedVideoPath == null) {
                    Log.e("FILE_PATH_ERROR", "selected video path = null!");
                    finish();
                } else {
                    if (video_title.getText().toString().isEmpty()) {
                        video_title.setError("Video Title is required!");
                    } else if (video_description.getText().toString().isEmpty()) {
                        video_description.setError("Video Description is required!");
                    } else if (video_teacher.getText().toString().isEmpty()) {
                        video_teacher.setError("Video Teacher/s is required!");
                    } else {
                        Intent intent = new Intent(getApplicationContext(),
                                MainActivity.class);
                        intent.putExtra("base_video", selectedVideoPath);
                        intent.putExtra("video_title", videoTitle);
                        intent.putExtra("video_price", videoPrice);
                        intent.putExtra("strike_price", strikePrice);
                        intent.putExtra("video_description", videoDescription);
                        intent.putExtra("video_teacher", videoTeacher);

                        startActivity(intent);
                        Toast.makeText(this, "" + selectedVideoPath, Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
        finish();
    }

    // Get absolute path of selected video ...
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else return null;
    }

    public void upload_video() {
        if (video_title.getText().toString().isEmpty()) {
            video_title.setError("Video Title is required!");
        } else if (video_description.getText().toString().isEmpty()) {
            video_description.setError("Video Description is required!");
        } else if (video_teacher.getText().toString().isEmpty()) {
            video_teacher.setError("Video Teacher/s is required!");
        } else {
            upload_video_button.setVisibility(View.GONE);
            _progressBar_layout.setVisibility(View.VISIBLE);
            start_video_upload();
        }
    }

    // Storage Permission is granted. Now video uploading can be started ...
    public void start_video_upload() {

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("store/videos");
        String uniqueID = db.push().getKey();

        final UploadTask[] uploadTask = new UploadTask[1];
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("store/videos/" + uniqueID + ".mp4");
        Uri file = Uri.fromFile(new File(getIntent().getExtras().getString("base_video")));
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                uploadTask[0] =  storageRef.putFile(file);
                // Register observers to listen for when the download is done or if it fails
                uploadTask[0].addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                }).addOnSuccessListener(taskSnapshot -> {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    upload_video_layout.setVisibility(View.GONE);
                    video_uploaded_success.setVisibility(View.VISIBLE);
                    upload_video_data(db, uniqueID);
                });

                uploadTask[0].addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d("PROGRESS", "Upload is " + progress + "% done");
                    int currentprogress = (int) progress;
                    progressBar.setProgress(currentprogress);
                }).addOnPausedListener(taskSnapshot -> System.out.println("Upload is paused"));

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }

        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission, you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    // Upload video data to Firebase Database ...
    public void upload_video_data(DatabaseReference db, String uniqueID) {

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();

        assert uniqueID != null;
        db.child(uniqueID).child("video_title").setValue(getIntent().getExtras().getString("video_title"));
        db.child(uniqueID).child("video_id").setValue(uniqueID);
        db.child(uniqueID).child("video_teacher").setValue(getIntent().getExtras().getString("video_teacher"));
        db.child(uniqueID).child("video_description").setValue(getIntent().getExtras().getString("video_description"));
        db.child(uniqueID).child("video_upload_time").setValue(formatter.format(date));
        db.child(uniqueID).child("video_price").setValue(getIntent().getExtras().getString("video_price"));
        db.child(uniqueID).child("strike_price").setValue(getIntent().getExtras().getString("strike_price"));
    }
}
