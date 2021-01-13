package studio.goldenapp.coaching.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FsmActivity extends AppCompatActivity {

    Dialog _myDialog;
    EditText fsm_title, fsm_content, fsm_teacher;
    Button _continue, upload_fsm_button;
    ProgressBar progressBar;
    LinearLayout upload_fsm_layout, fsm_uploaded_success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fsm);

        getSupportActionBar().setTitle("Free Study Material");
        FloatingActionButton fab = findViewById(R.id.upload_fsm_floating_button);
        DatabaseReference fsm_reference;
        List<Fsm> fsm_list = new ArrayList<>();
        RecyclerView fsm_recyclerview;
        final FsmRecycler[] fsm_adapter = new FsmRecycler[1];
        fsm_recyclerview = findViewById(R.id.fsm_recycler);
        fsm_recyclerview.setHasFixedSize(true);
        fsm_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        SkeletonScreen skeletonScreen = Skeleton.bind(fsm_recyclerview)
                .adapter(fsm_adapter[0])
                .load(R.layout.fsm_recycler)
                .show();

        fsm_reference = FirebaseDatabase.getInstance().getReference("store/fsm/");

        fsm_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Fsm fsm = dataSnapshot.getValue(Fsm.class);
                    fsm_list.add(fsm);
                }

                Collections.reverse(fsm_list);
                fsm_adapter[0] = new FsmRecycler(FsmActivity.this, fsm_list);
                fsm_recyclerview.setAdapter(fsm_adapter[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        _myDialog = new Dialog(FsmActivity.this);
        _myDialog.setContentView(R.layout.upload_fsm_popup);

        fsm_title = _myDialog.findViewById(R.id.fsm_title_new);
        fsm_teacher = _myDialog.findViewById(R.id.fsm_teacher_new);
        fsm_content = _myDialog.findViewById(R.id.fsm_content_new);
        upload_fsm_button = _myDialog.findViewById(R.id.upload_fsm_button);
        _continue = _myDialog.findViewById(R.id.continue_button_fsm_uploaded);
        progressBar = _myDialog.findViewById(R.id.progress_bar_view);

        upload_fsm_layout = _myDialog.findViewById(R.id.upload_fsm_layout);
        fsm_uploaded_success = _myDialog.findViewById(R.id.fsm_uploaded_success);

        _continue.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(),
                    FsmActivity.class);
            startActivity(intent);
            finish();
        });

        upload_fsm_button.setOnClickListener(view -> {
            if (fsm_title.getText().toString().isEmpty()) {
                fsm_title.setError("FSM Title is required!");
            } else if (fsm_content.getText().toString().isEmpty()) {
                fsm_content.setError("FSM Description is required!");
            } else if (fsm_teacher.getText().toString().isEmpty()) {
                fsm_teacher.setError("FSM Teacher/s is required!");
            } else {
                upload_fsm_button.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                // Upload pdf data to Firebase Database ...
                DatabaseReference db = FirebaseDatabase.getInstance().getReference("store/fsm");
                String uniqueID = db.push().getKey();

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();

                assert uniqueID != null;
                db.child(uniqueID).child("fsm_title").setValue(fsm_title.getText().toString());
                db.child(uniqueID).child("fsm_id").setValue(uniqueID);
                db.child(uniqueID).child("fsm_teacher").setValue(fsm_teacher.getText().toString());
                db.child(uniqueID).child("fsm_content").setValue(fsm_content.getText().toString());
                db.child(uniqueID).child("fsm_upload_time").setValue(formatter.format(date));

                upload_fsm_layout.setVisibility(View.GONE);
                fsm_uploaded_success.setVisibility(View.VISIBLE);
            }
        });

        fab.setOnClickListener(view -> {
            _myDialog.show();
        });
    }
}

class FsmRecycler extends RecyclerView.Adapter<FsmRecycler.ViewHolder> {

    View view;
    Context context;
    List<Fsm> MainImageUploadInfoList;

    public FsmRecycler(Context context, List<Fsm> TempList) {
        this.MainImageUploadInfoList = TempList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fsm_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Fsm fsm = MainImageUploadInfoList.get(position);
        holder.title.setText(fsm.getFsm_title());
        holder.details.setText(String.format("by %s (%s)", fsm.getFsm_teacher(), fsm.getFsm_upload_time()));

        holder.itemView.setOnClickListener(v -> {
            Dialog myDialog = new Dialog(context);
            myDialog.setContentView(R.layout.show_popup);
            TextView title = myDialog.findViewById(R.id.notification_title_popup);
            TextView content = myDialog.findViewById(R.id.notification_content_popup);

            title.setText(fsm.getFsm_title());
            content.setText(fsm.getFsm_content());

            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            myDialog.show();
        });

        holder.delete_fsm.setOnClickListener(view -> new AlertDialog.Builder(context)
                .setTitle("Are you sure?")
                .setMessage("You are about to delete this notes which cannot be undone. Continue?")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes",
                        (dialog, which) -> {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("store/fsm/");
                            databaseReference.child(fsm.getFsm_id()).removeValue();
                            Intent intent = new Intent(context, FsmActivity.class);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                            Toast.makeText(context, "FSM deleted from database", Toast.LENGTH_SHORT).show();
                        }).create().show());
    }

    @Override
    public int getItemCount() {
        return MainImageUploadInfoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, details;
        ImageView delete_fsm;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.fsm_title_recycler);
            details = itemView.findViewById(R.id.fsm_details_recycler);
            delete_fsm = itemView.findViewById(R.id.delete_fsm);
        }
    }

}


class Fsm {
    private String fsm_title, fsm_content, fsm_teacher;
    private String fsm_id, fsm_upload_time;

    public Fsm() {
        //empty constructor needed
    }

    public Fsm(String fsm_title, String fsm_content, String fsm_teacher,
               String fsm_id, String fsm_upload_time) {
        this.fsm_title = fsm_title;
        this.fsm_content = fsm_content;
        this.fsm_teacher = fsm_teacher;
        this.fsm_id = fsm_id;
        this.fsm_upload_time = fsm_upload_time;
    }

    public String getFsm_upload_time() {
        return fsm_upload_time;
    }

    public String getFsm_title() {
        return fsm_title;
    }

    public String getFsm_teacher() {
        return fsm_teacher;
    }

    public String getFsm_id() {
        return fsm_id;
    }

    public String getFsm_content() {
        return fsm_content;
    }
}
