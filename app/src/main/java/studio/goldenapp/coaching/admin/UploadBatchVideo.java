package studio.goldenapp.coaching.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UploadBatchVideo extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_batch_video);

        _myDialog = new Dialog(UploadBatchVideo.this);
        _myDialog.setContentView(R.layout.upload_video_popup);

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

        initialise_video_upload();

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

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("batches/" + BatchActivity.this_batch_id + "/store/videos");
        String uniqueID = db.push().getKey();

        final UploadTask[] uploadTask = new UploadTask[1];
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("batches/" + BatchActivity.this_batch_id + "/store/videos/" + uniqueID + ".mp4");
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
                Toast.makeText(UploadBatchVideo.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
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