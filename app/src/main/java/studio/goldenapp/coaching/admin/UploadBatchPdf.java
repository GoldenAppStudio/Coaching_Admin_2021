package studio.goldenapp.coaching.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class UploadBatchPdf extends AppCompatActivity {

    // Storage permissions ...
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private final int PICK_PDF_CODE = 2342;

    Dialog _myDialog;
    TextView upload_pdf_name;
    EditText pdf_title, pdf_description, pdf_teacher;
    String pdfTitle, pdfDescription, pdfTeacher;
    Button file_picker, _continue, upload_pdf_button;
    RelativeLayout _progressBar_layout;
    ProgressBar progressBar;
    LinearLayout upload_pdf_layout, pdf_uploaded_success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_batch_pdf);
        getSupportActionBar().setTitle("Upload PDF to " + BatchActivity.this_batch_name);

        // All the code below (in this activity) is for adding/uploading pdfs ...
        // Floating Action Button for Upload PDFs ...
        _myDialog = new Dialog(UploadBatchPdf.this);
        _myDialog.setContentView(R.layout.upload_pdf_popup);

        upload_pdf_name = _myDialog.findViewById(R.id.upload_pdf_name);
        pdf_title = _myDialog.findViewById(R.id.pdf_title_new);
        pdf_teacher = _myDialog.findViewById(R.id.pdf_teacher_new);
        pdf_description = _myDialog.findViewById(R.id.pdf_content_new);
        file_picker = _myDialog.findViewById(R.id.pdf_picker);
        upload_pdf_button = _myDialog.findViewById(R.id.upload_pdf_button);
        _continue = _myDialog.findViewById(R.id.continue_button_pdf_uploaded);
        _progressBar_layout = _myDialog.findViewById(R.id.progress_bar_view);
        progressBar = _myDialog.findViewById(R.id.RAND_3);

        upload_pdf_layout = _myDialog.findViewById(R.id.upload_pdf_layout);
        pdf_uploaded_success = _myDialog.findViewById(R.id.pdf_uploaded_success);

        if (getIntent().getExtras() != null) {
            _myDialog.show();
            pdf_title.setText(getIntent().getExtras().getString("pdf_title"));
            pdf_description.setText(getIntent().getExtras().getString("pdf_description"));
            pdf_teacher.setText(getIntent().getExtras().getString("pdf_teacher"));
            file_picker.setVisibility(View.GONE);
            upload_pdf_name.setVisibility(View.VISIBLE);
            upload_pdf_button.setVisibility(View.VISIBLE);

            File pdf_file = new File(getIntent().getExtras().getString("base_pdf"));
            upload_pdf_name.setText(pdf_file.getName());
        }

        initialise_pdf_upload();

        upload_pdf_button.setOnClickListener(view -> upload_pdf());

        _continue.setOnClickListener(view -> {
            getIntent().getExtras().clear();
            Intent intent = new Intent(getApplicationContext(),
                    BatchActivity.class);
            intent.putExtra("batch_id", BatchActivity.this_batch_id);
            intent.putExtra("batch_name", BatchActivity.this_batch_name);
            startActivity(intent);
            finish();
        });
    }

    public void initialise_pdf_upload() {
        _myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        _myDialog.show();

        file_picker.setOnClickListener(view -> {
            pdfTitle = pdf_title.getText().toString();
            pdfDescription = pdf_description.getText().toString();
            pdfTeacher = pdf_teacher.getText().toString();
            _myDialog.dismiss();
            Intent intentPDF = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intentPDF.setType("application/pdf");
            intentPDF.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentPDF.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intentPDF , "Select PDF"), PICK_PDF_CODE);
           /* Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, SELECT_PDF);*/
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_PDF_CODE) {
                String selectedPdfPath = data.getData().toString();
                Intent intent = new Intent(getApplicationContext(),
                        UploadBatchPdf.class);
                intent.putExtra("base_pdf", selectedPdfPath);
                intent.putExtra("pdf_title", pdfTitle);
                intent.putExtra("pdf_description", pdfDescription);
                intent.putExtra("pdf_teacher", pdfTeacher);

                startActivity(intent);
                Toast.makeText(this, "" + selectedPdfPath, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    // Get absolute path of selected pdf ...
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else return null;
    }

    public void upload_pdf() {
        if (pdf_title.getText().toString().isEmpty()) {
            pdf_title.setError("PDF Title is required!");
        } else if (pdf_description.getText().toString().isEmpty()) {
            pdf_description.setError("PDF Description is required!");
        } else if (pdf_teacher.getText().toString().isEmpty()) {
            pdf_teacher.setError("PDF Teacher/s is required!");
        } else {
            upload_pdf_button.setVisibility(View.GONE);
            _progressBar_layout.setVisibility(View.VISIBLE);
            start_pdf_upload();
        }
    }

    // Storage Permission is granted. Now pdf uploading can be started ...
    public void start_pdf_upload() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("batches/" + BatchActivity.this_batch_id + "/store/pdfs");
        String uniqueID = db.push().getKey();

        final UploadTask[] uploadTask = new UploadTask[1];
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("batches/" + BatchActivity.this_batch_id + "/store/pdfs/" + uniqueID + ".pdf");
        // Uri file = Uri.fromFile(new File(getIntent().getExtras().getString("base_pdf")));
        Uri file = Uri.parse(getIntent().getExtras().getString("base_pdf"));
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                uploadTask[0] = storageRef.putFile(file);
                // Register observers to listen for when the download is done or if it fails
                uploadTask[0].addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                }).addOnSuccessListener(taskSnapshot -> {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    upload_pdf_layout.setVisibility(View.GONE);
                    pdf_uploaded_success.setVisibility(View.VISIBLE);
                    upload_pdf_data(db, uniqueID);
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
                Toast.makeText(UploadBatchPdf.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }

        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    // Upload pdf data to Firebase Database ...
    public void upload_pdf_data(DatabaseReference db, String uniqueID) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();

        assert uniqueID != null;
        db.child(uniqueID).child("pdf_title").setValue(getIntent().getExtras().getString("pdf_title"));
        db.child(uniqueID).child("pdf_id").setValue(uniqueID);
        db.child(uniqueID).child("pdf_teacher").setValue(getIntent().getExtras().getString("pdf_teacher"));
        db.child(uniqueID).child("pdf_description").setValue(getIntent().getExtras().getString("pdf_description"));
        db.child(uniqueID).child("pdf_upload_time").setValue(formatter.format(date));
    }
}