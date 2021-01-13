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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudentListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        getSupportActionBar().setTitle("Student List");
        DatabaseReference student_reference;
        List<Student> student_list = new ArrayList<>();
        RecyclerView student_recyclerview;
        final StudentListRecycler[] student_adapter = new StudentListRecycler[1];
        student_recyclerview = findViewById(R.id.student_list_recycler);
        student_recyclerview.setHasFixedSize(true);
        student_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        SkeletonScreen skeletonScreen = Skeleton.bind(student_recyclerview)
                .adapter(student_adapter[0])
                .load(R.layout.student_list_recycler)
                .show();

        student_reference = FirebaseDatabase.getInstance().getReference("users/");

        student_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Student student = dataSnapshot.getValue(Student.class);
                    student_list.add(student);
                }

                Collections.reverse(student_list);
                student_adapter[0] = new StudentListRecycler(StudentListActivity.this, student_list);
                student_recyclerview.setAdapter(student_adapter[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}


class StudentListRecycler extends RecyclerView.Adapter<StudentListRecycler.ViewHolder> {

    View view;
    Context context;
    List<Student> MainImageUploadInfoList;

    public StudentListRecycler(Context context, List<Student> TempList) {
        this.MainImageUploadInfoList = TempList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_list_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Student student = MainImageUploadInfoList.get(position);
        holder.title.setText(student.getName());
        holder.details.setText(String.format("%s, %s (%s)",
                student.getVillage(), student.getDistrict(), student.getState()));

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, StudentProfileActivity.class);
            intent.putExtra("student_id", student.getUID());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return MainImageUploadInfoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, details;
        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.student_name_recycler);
            details = itemView.findViewById(R.id.student_details_recycler);
        }
    }

}

class Student {
    private String name, village, district, state, UID, phone;

    public Student() {
        //empty constructor needed
    }

    public Student(String name, String UID, String village,
                String district, String state, String phone) {
        this.name = name;
        this.village = village;
        this.district = district;
        this.UID = UID;
        this.state = state;
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public String getVillage() {
        return village;
    }

    public String getDistrict() {
        return district;
    }

    public String getState() {
        return state;
    }

    public String getUID() {
        return UID;
    }
}