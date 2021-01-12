package studio.goldenapp.coaching.admin;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class QuizEdit extends AppCompatActivity {

    Dialog _myDialog;
    EditText quiz_title, quiz_desc, quiz_level, quiz_visibility, questions;
    Button _continue, upload_quiz_button;
    ProgressBar progressBar;
    LinearLayout upload_quiz_layout, quiz_uploaded_success;
    ImageView cancel, quiz_image;
    public static final int PICK_IMAGE = 1;
    Uri uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_edit);

        _myDialog = new Dialog(QuizEdit.this);
        _myDialog.setContentView(R.layout.new_quiz_popup);

        quiz_title = _myDialog.findViewById(R.id.quiz_title_new);
        quiz_desc = _myDialog.findViewById(R.id.quiz_desc_new);
        quiz_level = _myDialog.findViewById(R.id.quiz_level_new);
        questions = _myDialog.findViewById(R.id.quiz_questions_);
        quiz_visibility = _myDialog.findViewById(R.id.quiz_visibility_new);
        quiz_image = _myDialog.findViewById(R.id.quiz_image_new);
        showImage(Uri.parse(getIntent().getStringExtra("image")));
        cancel = _myDialog.findViewById(R.id.cancel_new_quiz_dialog);
        upload_quiz_button = _myDialog.findViewById(R.id.upload_quiz_button);
        _continue = _myDialog.findViewById(R.id.continue_button_quiz_uploaded);
        progressBar = _myDialog.findViewById(R.id.progress_bar_view);

        upload_quiz_layout = _myDialog.findViewById(R.id.upload_quiz_layout);
        quiz_uploaded_success = _myDialog.findViewById(R.id.quiz_uploaded_success);

        quiz_title.setText(getIntent().getStringExtra("name"));
        questions.setText(getIntent().getStringExtra("questions"));
        quiz_level.setText(getIntent().getStringExtra("level"));
        quiz_visibility.setText(getIntent().getStringExtra("visibility"));
        quiz_desc.setText(getIntent().getStringExtra("desc"));

        _continue.setOnClickListener(view -> {
            _myDialog.dismiss();
            Intent intent = new Intent(getApplicationContext(),
                    QuizActivity.class);
            startActivity(intent);
            finish();
        });

        cancel.setOnClickListener(view -> {
            _myDialog.dismiss();
            Intent intent = new Intent(getApplicationContext(),
                    QuizActivity.class);
            startActivity(intent);
            finish();
        });

        quiz_image.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image for Quiz"), PICK_IMAGE);
        });

        _myDialog.show();

        upload_quiz_button.setOnClickListener(view -> {
            if (quiz_title.getText().toString().isEmpty()) {
                quiz_title.setError("Quiz Title is required!");
            } else if (quiz_desc.getText().toString().isEmpty()) {
                quiz_desc.setError("Quiz Description required!");
            } else if (quiz_level.getText().toString().isEmpty()) {
                quiz_level.setError("Quiz Level is required!");
            } else if (questions.getText().toString().isEmpty()) {
                questions.setError("Total no. of questions are required!");
            } else {
                upload_quiz_button.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                // Add a new document with a generated id.

                Map<String, Object> data = new HashMap<>();
                data.put("name", quiz_title.getText().toString());
                data.put("desc", quiz_desc.getText().toString());
                data.put("level", quiz_level.getText().toString());
                data.put("questions", Integer.parseInt(questions.getText().toString()));
                if (quiz_visibility.getText().toString().isEmpty()) {
                    data.put("visibility", "public");
                } else data.put("visibility", quiz_visibility.getText().toString());

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("QuizList")
                        .document(getIntent().getStringExtra("id"))
                        .update(data);

                if(uri == null) {
                    upload_quiz_layout.setVisibility(View.GONE);
                    quiz_uploaded_success.setVisibility(View.VISIBLE);
                } else{
                    StorageReference reference = FirebaseStorage.getInstance().getReference("QuizImages");
                    UploadTask uploadTask = reference.child(getIntent().getStringExtra("id") + ".jpg").putFile(uri);
                    uploadTask.addOnSuccessListener(taskSnapshot -> reference.child(getIntent().getStringExtra("id") + ".jpg").getDownloadUrl().addOnSuccessListener(uri1 -> {
                        db.collection("QuizList")
                                .document(getIntent().getStringExtra("id"))
                                .update("image", uri1.toString());
                        upload_quiz_layout.setVisibility(View.GONE);
                        quiz_uploaded_success.setVisibility(View.VISIBLE);
                    }));
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            //TODO: action
            uri = data.getData();
            quiz_image.setImageURI(uri);
        }
    }

    void showImage(Uri uri) {
        Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.image)
                .into(quiz_image);
    }
}