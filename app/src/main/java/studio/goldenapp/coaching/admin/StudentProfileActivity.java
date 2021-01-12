package studio.goldenapp.coaching.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

public class StudentProfileActivity extends AppCompatActivity {

    String[] text = {};
    int[] images = {
            R.drawable.hash,
            R.drawable.call,
            R.drawable.mail,
            R.drawable.birthday,
            R.drawable.location,
            R.drawable.parents
            // R.drawable.description
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        final ListView[] mListView = new ListView[1];
        DatabaseReference databaseReference;
        ProgressDialog progressDialog;
        ImageView circleImageView;
        TextView textView, textView1;

        getSupportActionBar().setTitle("Student Profile");

        progressDialog = new ProgressDialog(StudentProfileActivity.this);
        progressDialog.setMessage("Loading Data from Database");
        progressDialog.show();

        textView = findViewById(R.id.student_name);
        textView1 = findViewById(R.id.student_details);
        mListView[0] = findViewById(R.id.list_view_student_profile);

        databaseReference = FirebaseDatabase.getInstance().getReference("users/" + getIntent().getExtras().getString("student_id"));
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.child("name").exists()) {
                    textView.setText(snapshot.child("name").getValue().toString());
                } else textView.setText("N/A");

                if(snapshot.child("aadhar").exists()) {
                    textView1.setText(String.format("Aadhar No : %s", snapshot.child("aadhar").getValue().toString()));
                } else textView1.setText("N/A");

                text = new String[]{
                        snapshot.child("UID").getValue().toString(),
                        snapshot.child("phone").getValue().toString(),
                        snapshot.child("email").getValue().toString(),
                        snapshot.child("dob").getValue().toString(),
                        snapshot.child("village").getValue().toString() + ", " + snapshot.child("district").getValue().toString() + " (" + snapshot.child("state").getValue().toString() + ")",
                        snapshot.child("fathers_name").getValue().toString() + " / " + snapshot.child("mothers_name").getValue().toString()
                };

                CustomList adapter = new
                        CustomList(StudentProfileActivity.this, text, images);
                mListView[0] = findViewById(R.id.list_view_student_profile);
                mListView[0].setAdapter(adapter);
                mListView[0].setOnItemClickListener((parent, view, position, id) -> {});
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

    }
}