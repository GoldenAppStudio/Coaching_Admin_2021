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
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class QuestionActivity extends AppCompatActivity {

    Dialog _myDialog;
    EditText question, option_a, option_b, option_c, option_d, answer, timer;
    Button _continue, upload_question_button;
    ProgressBar progressBar;
    ImageView cancel;
    LinearLayout upload_question_layout, question_uploaded_success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        getSupportActionBar().setTitle("Question List");
        FloatingActionButton fab = findViewById(R.id.add_question_floating_button);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        List<Question> questions = new ArrayList<>();
        RecyclerView question_recyclerview;
        final QuestionRecycler[] question_adapter = new QuestionRecycler[1];
        question_recyclerview = findViewById(R.id.question_recycler);
        question_recyclerview.setHasFixedSize(true);
        question_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        SkeletonScreen skeletonScreen = Skeleton.bind(question_recyclerview)
                .adapter(question_adapter[0])
                .load(R.layout.question_recycler)
                .show();

        firebaseFirestore.collection("QuizList")
                .document(getIntent().getStringExtra("id")).collection("Questions")
                .get().addOnSuccessListener(documentSnapshot -> {
            for (DocumentSnapshot ds : documentSnapshot.getDocuments()) {
                Question q = ds.toObject(Question.class);
                questions.add(q);
            }

            Collections.reverse(questions);
            question_adapter[0] = new QuestionRecycler(QuestionActivity.this, questions, getIntent().getStringExtra("id"));
            question_recyclerview.setAdapter(question_adapter[0]);
        });


        _myDialog = new Dialog(QuestionActivity.this);
        _myDialog.setContentView(R.layout.new_question_popup);

        question = _myDialog.findViewById(R.id.question_new);
        option_a = _myDialog.findViewById(R.id.option_a_new);
        option_b = _myDialog.findViewById(R.id.option_b_new);
        option_c = _myDialog.findViewById(R.id.option_c_new);
        option_d = _myDialog.findViewById(R.id.option_d_new);
        timer = _myDialog.findViewById(R.id.timer_new);
        answer = _myDialog.findViewById(R.id.answer_new);
        cancel = _myDialog.findViewById(R.id.cancel_new_question_dialog);
        upload_question_button = _myDialog.findViewById(R.id.upload_question_button);
        _continue = _myDialog.findViewById(R.id.continue_button_question_uploaded);
        progressBar = _myDialog.findViewById(R.id.progress_bar_view);

        upload_question_layout = _myDialog.findViewById(R.id.upload_question_layout);
        question_uploaded_success = _myDialog.findViewById(R.id.question_uploaded_success);

        _continue.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(),
                    QuestionActivity.class);
            intent.putExtra("id", getIntent().getStringExtra("id"));
            startActivity(intent);
            finish();
        });

        cancel.setOnClickListener(view -> _myDialog.dismiss());

        fab.setOnClickListener(view -> _myDialog.show());

        upload_question_button.setOnClickListener(view -> {
            if (question.getText().toString().isEmpty()) {
                question.setError("Question is required!");
            } else if (option_a.getText().toString().isEmpty()) {
                option_a.setError("Option A required!");
            } else if (option_b.getText().toString().isEmpty()) {
                option_b.setError("Option B required!");
            } else if (option_c.getText().toString().isEmpty()) {
                option_c.setError("Option C required!");
            } else if (option_d.getText().toString().isEmpty()) {
                option_d.setError("Option D required!");
            } else if (answer.getText().toString().isEmpty()) {
                answer.setError("Answer required!");
            } else if (timer.getText().toString().isEmpty()) {
                timer.setError("Timer required!");
            } else {

                if (!answer.getText().toString().equals(option_a.getText().toString()) &&
                        !answer.getText().toString().equals(option_b.getText().toString()) &&
                        !answer.getText().toString().equals(option_c.getText().toString()) &&
                        !answer.getText().toString().equals(option_d.getText().toString())) {
                    Toast.makeText(this, "Answer must match with one of the options", Toast.LENGTH_SHORT).show();
                } else {
                    upload_question_button.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    // Add a new document with a generated id.

                    Map<String, Object> data = new HashMap<>();
                    data.put("question", question.getText().toString());
                    data.put("option_a", option_a.getText().toString());
                    data.put("option_b", option_b.getText().toString());
                    data.put("option_c", option_c.getText().toString());
                    data.put("option_d", option_d.getText().toString());
                    data.put("timer", Integer.parseInt(timer.getText().toString()));
                    data.put("answer", answer.getText().toString());
                    data.put("id", "null");

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("QuizList").document(getIntent().getStringExtra("id"))
                            .collection("Questions")
                            .add(data)
                            .addOnSuccessListener((OnSuccessListener<DocumentReference>) documentReference -> {
                                documentReference.update("id", documentReference.getId());
                                upload_question_layout.setVisibility(View.GONE);
                                question_uploaded_success.setVisibility(View.VISIBLE);
                            });
                }
            }
        });
    }
}

class QuestionRecycler extends RecyclerView.Adapter<QuestionRecycler.ViewHolder> {

    View view;
    Context context;
    List<Question> MainImageUploadInfoList;
    String quiz_id;

    public QuestionRecycler(Context context, List<Question> TempList, String quiz_id) {
        this.MainImageUploadInfoList = TempList;
        this.context = context;
        this.quiz_id = quiz_id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Question question = MainImageUploadInfoList.get(position);
        holder.title.setText(question.getQuestion());
        holder.details.setText(String.format("Timer: %s ", String.valueOf(question.getTimer())));

        holder.delete_question.setOnClickListener(view -> new AlertDialog.Builder(context)
                .setTitle("Are you sure?")
                .setMessage("You are about to delete this question which cannot be undone. Continue?")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes",
                        (dialog, which) -> {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("QuizList")
                                    .document(quiz_id)
                                    .collection("Questions")
                                    .document(question.getId()).delete();
                            Intent intent = new Intent(context, QuestionActivity.class);
                            intent.putExtra("id", quiz_id);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                            Toast.makeText(context, "Question deleted from database", Toast.LENGTH_SHORT).show();
                        }).create().show());

        holder.edit_question.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, QuestionEdit.class);
            intent.putExtra("question", question.getQuestion());
            intent.putExtra("option_a", question.getOption_a());
            intent.putExtra("option_b", question.getOption_b());
            intent.putExtra("option_c", question.getOption_c());
            intent.putExtra("id", question.getId());
            intent.putExtra("quiz_id", quiz_id);
            intent.putExtra("timer", String.valueOf(question.getTimer()));
            intent.putExtra("option_d", question.getOption_d());
            intent.putExtra("answer", question.getAnswer());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return MainImageUploadInfoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, details;
        ImageView delete_question, edit_question;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.question_title_recycler);
            details = itemView.findViewById(R.id.question_details_recycler);
            delete_question = itemView.findViewById(R.id.delete_question);
            edit_question = itemView.findViewById(R.id.edit_question);
        }
    }
}

class Question {

    String id;
    String question, option_a, option_b, option_c, option_d, answer;
    int timer;

    public Question() {
        //empty constructor needed
    }

    public Question(String id, String question, String option_a, String option_b, String option_c, String option_d, String answer, int timer) {
        this.id = id;
        this.question = question;
        this.option_a = option_a;
        this.option_b = option_b;
        this.option_c = option_c;
        this.option_d = option_d;
        this.answer = answer;
        this.timer = timer;
    }

    public String getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getOption_a() {
        return option_a;
    }

    public String getOption_b() {
        return option_b;
    }

    public String getOption_c() {
        return option_c;
    }

    public String getOption_d() {
        return option_d;
    }

    public String getAnswer() {
        return answer;
    }

    public int getTimer() {
        return timer;
    }
}