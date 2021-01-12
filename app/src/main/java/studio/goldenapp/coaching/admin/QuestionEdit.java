package studio.goldenapp.coaching.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class QuestionEdit extends AppCompatActivity {

    Dialog _myDialog;
    EditText question, option_a, option_b, option_c, option_d, answer, timer;
    Button _continue, upload_question_button;
    ProgressBar progressBar;
    ImageView cancel;
    LinearLayout upload_question_layout, question_uploaded_success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_edit);

        _myDialog = new Dialog(QuestionEdit.this);
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


        question.setText(getIntent().getStringExtra("question"));
        option_a.setText(getIntent().getStringExtra("option_a"));
        option_b.setText(getIntent().getStringExtra("option_b"));
        option_c.setText(getIntent().getStringExtra("option_c"));
        option_d.setText(getIntent().getStringExtra("option_d"));
        answer.setText(getIntent().getStringExtra("answer"));
        timer.setText(getIntent().getStringExtra("timer"));

        _continue.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(),
                    QuestionActivity.class);
            intent.putExtra("id", getIntent().getStringExtra("quiz_id"));
            startActivity(intent);
            finish();
        });

        cancel.setOnClickListener(view -> {
            _myDialog.dismiss();
            Intent intent = new Intent(getApplicationContext(),
                    QuestionActivity.class);
            intent.putExtra("id", getIntent().getStringExtra("quiz_id"));
            startActivity(intent);
            finish();
        });

        _myDialog.show();

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

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("QuizList").document(getIntent().getStringExtra("quiz_id"))
                            .collection("Questions")
                            .document(getIntent().getStringExtra("id"))
                            .update(data);

                    upload_question_layout.setVisibility(View.GONE);
                    question_uploaded_success.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
