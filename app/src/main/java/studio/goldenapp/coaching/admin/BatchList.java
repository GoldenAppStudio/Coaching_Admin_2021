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
import android.widget.Button;
import android.widget.EditText;
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
import java.util.List;

public class BatchList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_list);

        DatabaseReference databaseReference;
        final List<BatchClass> list = new ArrayList<>();
        final RecyclerView recyclerView;
        final BatchListRecycler[] adapter = new BatchListRecycler[1];

        recyclerView = findViewById(R.id.batch_list_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SkeletonScreen skeletonScreen = Skeleton.bind(recyclerView)
                .adapter(adapter[0])
                .load(R.layout.batch_list_recycler)
                .show();

        databaseReference = FirebaseDatabase.getInstance().getReference("batches/");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    BatchClass batch = dataSnapshot.getValue(BatchClass.class);
                    list.add(batch);
                }

                adapter[0] = new BatchListRecycler(BatchList.this, list);
                recyclerView.setAdapter(adapter[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}

class BatchListRecycler extends RecyclerView.Adapter<BatchListRecycler.ViewHolder> {

    View view;
    Context context;
    List<BatchClass> MainImageUploadInfoList;

    public BatchListRecycler(Context context, List<BatchClass> TempList) {
        this.MainImageUploadInfoList = TempList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.batch_list_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final BatchClass batch = MainImageUploadInfoList.get(position);
        holder.title.setText(batch.getBatch_name());
        holder.details.setText(String.format("by %s (%s)", batch.getCreated_by(), batch.getBatch_created_time()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BatchActivity.class);
            intent.putExtra("batch_id", batch.getBatch_id());
            intent.putExtra("batch_name", batch.getBatch_name());
            context.startActivity(intent);
        });

        holder.delete_batch.setOnClickListener(view -> {
            Dialog myDialog = new Dialog(context);
            myDialog.setContentView(R.layout.detele_batch_popup);
            TextView batch_id = myDialog.findViewById(R.id.batch_id_delete);
            EditText batch_password = myDialog.findViewById(R.id.batch_password_delete);
            Button delete_batch = myDialog.findViewById(R.id.delete_batch_button);
            Button _continue = myDialog.findViewById(R.id.continue_button_batch_deleted);

            batch_id.setText(batch.getBatch_id());
            delete_batch.setOnClickListener(view1 -> {
                if (batch_password.getText().toString().isEmpty()) {
                    batch_password.setError("Password can't be empty");
                } else {
                    if (batch_password.getText().toString().equals(batch.getBatch_password())) {
                        new AlertDialog.Builder(context)
                                .setTitle("Are you sure?")
                                .setMessage("You are about to delete this batch which cannot be undone. Continue?")
                                .setNegativeButton("No", (dialog, which) -> {
                                    dialog.dismiss();
                                    myDialog.dismiss();
                                })
                                .setPositiveButton("Yes",
                                        (dialog, which) -> {
                                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("batches/");
                                            databaseReference.child(batch.getBatch_id()).removeValue();
                                            Intent intent = new Intent(context, BatchList.class);
                                            context.startActivity(intent);
                                            ((Activity) context).finish();
                                            Toast.makeText(context, "Batch deleted from database", Toast.LENGTH_SHORT).show();
                                        }).create().show();
                    } else {
                        batch_password.setError("Password is wrong");
                    }
                }
            });
            myDialog.show();
        });

        /*holder.itemView.setOnClickListener(v -> {
            Dialog myDialog = new Dialog(context);
            myDialog.setContentView(R.layout.show_popup);


            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            myDialog.show();
        });*/
    }

    @Override
    public int getItemCount() {
        return MainImageUploadInfoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, details;
        ImageView delete_batch;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.batch_name_recycler);
            details = itemView.findViewById(R.id.batch_details_recycler);
            delete_batch = itemView.findViewById(R.id.delete_batch);
        }
    }
}

class BatchClass {
    private String batch_name;
    private String created_by;
    private String batch_created_time;
    private String batch_id;
    private String batch_password;

    public BatchClass() {
        //empty constructor needed
    }

    public BatchClass(String batch_name, String created_by,
                      String batch_created_time, String batch_id, String batch_password) {
        this.batch_name = batch_name;
        this.created_by = created_by;
        this.batch_created_time = batch_created_time;
        this.batch_id = batch_id;
        this.batch_password = batch_password;
    }

    public String getBatch_name() {
        return batch_name;
    }

    public String getBatch_id() {
        return batch_id;
    }

    public String getCreated_by() {
        return created_by;
    }

    public String getBatch_password() {
        return batch_password;
    }

    public String getBatch_created_time() {
        return batch_created_time;
    }
}

