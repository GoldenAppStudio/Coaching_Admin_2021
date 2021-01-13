package studio.goldenapp.coaching.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class QuizActivity extends AppCompatActivity {

    Dialog _myDialog;
    EditText quiz_title, quiz_desc, quiz_level, quiz_visibility, quiz_questions;
    Button _continue, upload_quiz_button;
    ProgressBar progressBar;
    LinearLayout upload_quiz_layout, quiz_uploaded_success;
    ImageView cancel, quiz_image;
    public static final int PICK_IMAGE = 1;
    Uri uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        getSupportActionBar().setTitle("Quiz");

        FloatingActionButton fab = findViewById(R.id.add_quiz_floating_button);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        List<Quiz> quizzes = new ArrayList<>();
        RecyclerView quiz_recyclerview;
        final QuizRecycler[] quiz_adapter = new QuizRecycler[1];
        quiz_recyclerview = findViewById(R.id.quiz_recycler);
        quiz_recyclerview.setHasFixedSize(true);
        quiz_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        SkeletonScreen skeletonScreen = Skeleton.bind(quiz_recyclerview)
                .adapter(quiz_adapter[0])
                .load(R.layout.quiz_recycler)
                .show();

        firebaseFirestore.collection("QuizList")
                .whereEqualTo("visibility", "public")
                .get().addOnSuccessListener(documentSnapshot -> {
            for (DocumentSnapshot ds : documentSnapshot.getDocuments()) {
                Quiz quiz = ds.toObject(Quiz.class);
                quizzes.add(quiz);
            }

            Collections.reverse(quizzes);
            quiz_adapter[0] = new QuizRecycler(QuizActivity.this, quizzes, QuizActivity.this);
            quiz_recyclerview.setAdapter(quiz_adapter[0]);
        });


        _myDialog = new Dialog(QuizActivity.this);
        _myDialog.setContentView(R.layout.new_quiz_popup);

        quiz_title = _myDialog.findViewById(R.id.quiz_title_new);
        quiz_desc = _myDialog.findViewById(R.id.quiz_desc_new);
        quiz_level = _myDialog.findViewById(R.id.quiz_level_new);
        quiz_questions = _myDialog.findViewById(R.id.quiz_questions_);
        quiz_visibility = _myDialog.findViewById(R.id.quiz_visibility_new);
        quiz_image = _myDialog.findViewById(R.id.quiz_image_new);
        cancel = _myDialog.findViewById(R.id.cancel_new_quiz_dialog);
        upload_quiz_button = _myDialog.findViewById(R.id.upload_quiz_button);
        _continue = _myDialog.findViewById(R.id.continue_button_quiz_uploaded);
        progressBar = _myDialog.findViewById(R.id.progress_bar_view);

        upload_quiz_layout = _myDialog.findViewById(R.id.upload_quiz_layout);
        quiz_uploaded_success = _myDialog.findViewById(R.id.quiz_uploaded_success);

        _continue.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(),
                    QuizActivity.class);
            startActivity(intent);
            finish();
        });

        cancel.setOnClickListener(view -> _myDialog.dismiss());

        fab.setOnClickListener(view -> _myDialog.show());

        quiz_image.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image for Quiz"), PICK_IMAGE);
        });

        upload_quiz_button.setOnClickListener(view -> {
            if (quiz_title.getText().toString().isEmpty()) {
                quiz_title.setError("Quiz Title is required!");
            } else if (quiz_desc.getText().toString().isEmpty()) {
                quiz_desc.setError("Quiz Description required!");
            } else if (quiz_level.getText().toString().isEmpty()) {
                quiz_level.setError("Quiz Level is required!");
            } else if (quiz_questions.getText().toString().isEmpty()) {
                quiz_questions.setError("Total no. of questions are required!");
            } else if (uri == null) {
                Toast.makeText(this, "Please provide a quiz image", Toast.LENGTH_SHORT).show();
            } else {
                upload_quiz_button.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                // Add a new document with a generated id.

                Map<String, Object> data = new HashMap<>();
                data.put("name", quiz_title.getText().toString());
                data.put("desc", quiz_desc.getText().toString());
                data.put("level", quiz_level.getText().toString());
                data.put("questions", Integer.parseInt(quiz_questions.getText().toString()));
                data.put("image", "null");
                data.put("id", "null");
                if (quiz_visibility.getText().toString().isEmpty()) {
                    data.put("visibility", "public");
                } else data.put("visibility", quiz_visibility.getText().toString());

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("QuizList")
                        .add(data)
                        .addOnSuccessListener((OnSuccessListener<DocumentReference>) documentReference -> {
                            StorageReference reference = FirebaseStorage.getInstance().getReference("QuizImages");
                            UploadTask uploadTask = reference.child(documentReference.getId() + ".jpg").putFile(uri);
                            uploadTask.addOnSuccessListener(taskSnapshot -> reference.child(documentReference.getId() + ".jpg").getDownloadUrl().addOnSuccessListener(uri1 -> {
                                documentReference.update("image", uri1.toString());
                                documentReference.update("id", documentReference.getId());
                                upload_quiz_layout.setVisibility(View.GONE);
                                quiz_uploaded_success.setVisibility(View.VISIBLE);
                            }));
                        })
                        .addOnFailureListener((OnFailureListener) e -> {
                        });
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
}

class QuizRecycler extends RecyclerView.Adapter<QuizRecycler.ViewHolder> {

    View view;
    Context context;
    List<Quiz> MainImageUploadInfoList;
    Activity activity;

    public QuizRecycler(Context context, List<Quiz> TempList, Activity activity) {
        this.MainImageUploadInfoList = TempList;
        this.context = context;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quiz_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Quiz quiz = MainImageUploadInfoList.get(position);
        holder.title.setText(quiz.getName());
        holder.details.setText(String.format("level: %s | questions: %s", quiz.getLevel(), quiz.getQuestions()));
        Glide.with(context)
                .load(Uri.parse(quiz.getImage()))
                .placeholder(R.drawable.image)
                .into(holder.quiz_image);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, QuestionActivity.class);
            intent.putExtra("id", quiz.getId());
            context.startActivity(intent);
        });

        holder.delete_quiz.setOnClickListener(view -> new AlertDialog.Builder(context)
                .setTitle("Are you sure?")
                .setMessage("You are about to delete this quiz which cannot be undone. Continue?")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes",
                        (dialog, which) -> {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("QuizList")
                                    .document(quiz.getId()).delete();
                            Intent intent = new Intent(context, QuizActivity.class);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                            Toast.makeText(context, "Quiz deleted from database", Toast.LENGTH_SHORT).show();
                        }).create().show());

        holder.edit_quiz.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, QuizEdit.class);
            intent.putExtra("name", quiz.getName());
            intent.putExtra("visibility", quiz.getVisibility());
            intent.putExtra("level", quiz.getLevel());
            intent.putExtra("image", quiz.getImage());
            intent.putExtra("id", quiz.getId());
            intent.putExtra("questions", String.valueOf(quiz.getQuestions()));
            intent.putExtra("desc", quiz.getDesc());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return MainImageUploadInfoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, details;
        ImageView delete_quiz, edit_quiz;
        ImageView quiz_image;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.quiz_title_recycler);
            details = itemView.findViewById(R.id.quiz_details_recycler);
            delete_quiz = itemView.findViewById(R.id.delete_quiz);
            edit_quiz = itemView.findViewById(R.id.edit_quiz);
            quiz_image = itemView.findViewById(R.id.RAND_5);
        }
    }

}

class Quiz {

    String id;
    String name, visibility, level, desc, image;
    int questions;

    public Quiz() {
        //empty constructor needed
    }

    public Quiz(String name, String visibility, String level,
                String desc, String image, int questions, String id) {
        this.name = name;
        this.visibility = visibility;
        this.desc = desc;
        this.level = level;
        this.image = image;
        this.questions = questions;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getLevel() {
        return level;
    }

    public String getDesc() {
        return desc;
    }

    public String getImage() {
        return image;
    }

    public int getQuestions() {
        return questions;
    }

    public String getId() {
        return id;
    }
}