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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        getSupportActionBar().setTitle("Notification");
        DatabaseReference databaseReference;
        final List<Notification> list = new ArrayList<>();
        final RecyclerView recyclerView;
        final NotificationRecycler[] adapter = new NotificationRecycler[1];
        FloatingActionButton floatingActionButton = findViewById(R.id.add_notification);

        floatingActionButton.setOnClickListener(view -> add_new_notification());

        recyclerView = findViewById(R.id.notification_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SkeletonScreen skeletonScreen = Skeleton.bind(recyclerView)
                .adapter(adapter[0])
                .load(R.layout.notification_recycler)
                .show();

        databaseReference = FirebaseDatabase.getInstance().getReference("notifications/");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Notification notification = dataSnapshot.getValue(Notification.class);
                    list.add(notification);
                }

                Collections.reverse(list);
                adapter[0] = new NotificationRecycler(NotificationActivity.this, list);
                recyclerView.setAdapter(adapter[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void add_new_notification() {

        final Dialog myDialog = new Dialog(NotificationActivity.this);
        myDialog.setContentView(R.layout.add_notification_popup);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();

        final EditText notification_title = myDialog.findViewById(R.id.notification_title_new);
        final EditText notification_content = myDialog.findViewById(R.id.content_new);
        final EditText notification_creator = myDialog.findViewById(R.id.teacher_new);
        final Button add_notification = myDialog.findViewById(R.id.new_notification_button);
        final Button _continue = myDialog.findViewById(R.id.continue_button_notification_added);
        final RelativeLayout progressBar_layout = myDialog.findViewById(R.id.progress_bar_view);

        final LinearLayout create_new_notification_layout = myDialog.findViewById(R.id.create_new_notification_layout);
        final LinearLayout notification_added_success = myDialog.findViewById(R.id.notification_added_success);

        _continue.setOnClickListener(view -> {
            myDialog.dismiss();
            startActivity(getIntent());
            finish();
        });

        add_notification.setOnClickListener(view -> {
            if (notification_title.getText().toString().isEmpty()) {
                notification_title.setError("Notification title is required!");
            } else if (notification_content.getText().toString().isEmpty()) {
                notification_content.setError("Notification content is required!");
            } else if (notification_creator.getText().toString().isEmpty()) {
                notification_creator.setError("Notification creator is required!");
            } else {
                add_notification.setVisibility(View.GONE);
                progressBar_layout.setVisibility(View.VISIBLE);

                add_notification_to_database(
                        notification_title.getText().toString(),
                        notification_content.getText().toString(),
                        notification_creator.getText().toString()
                );

                create_new_notification_layout.setVisibility(View.GONE);
                notification_added_success.setVisibility(View.VISIBLE);
            }
        });
    }

    public void add_notification_to_database(String title, String content, String creator) {

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("notifications");
        String uniqueID = reference.push().getKey();

        assert uniqueID != null;
        reference.child(uniqueID).child("title").setValue(title);
        reference.child(uniqueID).child("UID").setValue(uniqueID);
        reference.child(uniqueID).child("content").setValue(content);
        reference.child(uniqueID).child("teacher").setValue(creator);
        reference.child(uniqueID).child("time").setValue(formatter.format(date));
    }
}

class NotificationRecycler extends RecyclerView.Adapter<NotificationRecycler.ViewHolder> {

    View view;
    Context context;
    List<Notification> MainImageUploadInfoList;

    public NotificationRecycler(Context context, List<Notification> TempList) {
        this.MainImageUploadInfoList = TempList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Notification notification = MainImageUploadInfoList.get(position);
        holder.title.setText(notification.getTitle());
        holder.details.setText(String.format("by %s (%s)", notification.getTeacher(), notification.getTime()));

        holder.delete_notification.setOnClickListener(view -> new AlertDialog.Builder(context)
                .setTitle("Are you sure?")
                .setMessage("You are about to delete this notification which cannot be undone. Continue?")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes",
                        (dialog, which) -> {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notifications/");
                            databaseReference.child(notification.getUID()).removeValue();
                            Intent intent = new Intent(context, NotificationActivity.class);
                            context.startActivity(intent);
                            ((Activity)context).finish();
                            Toast.makeText(context, "Notification deleted from database", Toast.LENGTH_SHORT).show();
                        }).create().show());

        holder.itemView.setOnClickListener(v -> {
            Dialog myDialog = new Dialog(context);
            myDialog.setContentView(R.layout.add_notification_popup);
            TextView title = myDialog.findViewById(R.id.edit_notification_title_bar);
            TextView success_title = myDialog.findViewById(R.id.RAND_1);

            EditText notification_title = myDialog.findViewById(R.id.notification_title_new);
            EditText notification_content = myDialog.findViewById(R.id.content_new);
            EditText notification_creator = myDialog.findViewById(R.id.teacher_new);
            Button update_notification = myDialog.findViewById(R.id.new_notification_button);
            Button _continue = myDialog.findViewById(R.id.continue_button_notification_added);
            RelativeLayout progressBar_layout = myDialog.findViewById(R.id.progress_bar_view);

            final LinearLayout create_new_notification_layout = myDialog.findViewById(R.id.create_new_notification_layout);
            final LinearLayout notification_added_success = myDialog.findViewById(R.id.notification_added_success);

            title.setText("Edit Notification");
            success_title.setText("Notification Updated!");
            update_notification.setText("Update Notification");
            notification_title.setText(notification.getTitle());
            notification_content.setText(notification.getContent());
            notification_creator.setText(notification.getTeacher());

            _continue.setOnClickListener(view -> {
                myDialog.dismiss();
                Intent intent = new Intent(context, NotificationActivity.class);
                context.startActivity(intent);
                ((Activity)context).finish();
            });

            update_notification.setOnClickListener(view -> {
                if (notification_title.getText().toString().isEmpty()) {
                    notification_title.setError("Notification title is required!");
                } else if (notification_content.getText().toString().isEmpty()) {
                    notification_content.setError("Notification content is required!");
                } else if (notification_creator.getText().toString().isEmpty()) {
                    notification_creator.setError("Notification creator is required!");
                } else {
                    update_notification.setVisibility(View.GONE);
                    progressBar_layout.setVisibility(View.VISIBLE);

                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("notifications");
                    String uniqueID = notification.getUID();

                    reference.child(uniqueID).child("title").setValue(notification_title.getText().toString());
                    reference.child(uniqueID).child("content").setValue(notification_content.getText().toString());
                    reference.child(uniqueID).child("teacher").setValue(notification_creator.getText().toString());
                    reference.child(uniqueID).child("time").setValue(formatter.format(date));

                    create_new_notification_layout.setVisibility(View.GONE);
                    notification_added_success.setVisibility(View.VISIBLE);
                }
            });

            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            myDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return MainImageUploadInfoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, details;
        ImageView delete_notification;
        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notification_title_recycler);
            details = itemView.findViewById(R.id.notification_details_recycler);
            delete_notification = itemView.findViewById(R.id.delete_notification);
        }
    }
}

class Notification {
    private String title;
    private String time;
    private String teacher;
    private String UID;
    private String content;

    public Notification() {
        //empty constructor needed
    }

    public Notification(String title, String time,
                        String teacher, String UID, String content) {
        this.title = title;
        this.time = time;
        this.teacher = teacher;
        this.UID = UID;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getTime() {
        return time;
    }

    public String getUID() {
        return UID;
    }

    public String getContent() {
        return content;
    }
}
