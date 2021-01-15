package studio.goldenapp.coaching.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.android.gms.common.api.Batch;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class BatchActivity extends AppCompatActivity {

    Dialog _myDialog;
    EditText fsm_title, fsm_content, fsm_teacher;
    Button _continue, upload_fsm_button;
    ProgressBar progressBar;
    LinearLayout upload_fsm_layout, fsm_uploaded_success;

    public static String this_batch_id;
    public static String this_batch_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch);
        getSupportActionBar().setTitle("" + getIntent().getStringExtra("batch_name") + " (#ID: " + getIntent().getStringExtra("batch_id") + ")");
        this_batch_id = getIntent().getStringExtra("batch_id");
        this_batch_name = getIntent().getStringExtra("batch_name");

        Button video_button = findViewById(R.id.store_video_button);
        Button pdf_button = findViewById(R.id.store_pdf_button);
        Button fsm_button = findViewById(R.id.store_fsm_button);
        RelativeLayout video_layout = findViewById(R.id.store_video_layout);
        RelativeLayout pdf_layout = findViewById(R.id.store_pdf_layout);
        RelativeLayout fsm_layout = findViewById(R.id.store_fsm_layout);

        FloatingActionButton fab1 = findViewById(R.id.upload_video_floating_button_batch);
        FloatingActionButton fab2 = findViewById(R.id.upload_pdf_floating_button_batch);
        FloatingActionButton fab3 = findViewById(R.id.upload_fsm_floating_button_batch);

        _myDialog = new Dialog(BatchActivity.this);
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
                    BatchActivity.class);
            startActivity(intent);
            finish();
        });

        video_button.setOnClickListener(view1 -> {
            video_button.setBackgroundColor(Color.parseColor("#7579e7"));
            pdf_button.setBackgroundColor(Color.parseColor("#bbbbbb"));
            fsm_button.setBackgroundColor(Color.parseColor("#bbbbbb"));

            video_button.setTextColor(Color.parseColor("#ffffff"));
            pdf_button.setTextColor(Color.parseColor("#000000"));
            fsm_button.setTextColor(Color.parseColor("#000000"));

            video_layout.setVisibility(View.VISIBLE);
            pdf_layout.setVisibility(View.GONE);
            fsm_layout.setVisibility(View.GONE);
        });
        pdf_button.setOnClickListener(view1 -> {
            video_button.setBackgroundColor(Color.parseColor("#bbbbbb"));
            pdf_button.setBackgroundColor(Color.parseColor("#7579e7"));
            fsm_button.setBackgroundColor(Color.parseColor("#bbbbbb"));

            video_button.setTextColor(Color.parseColor("#000000"));
            pdf_button.setTextColor(Color.parseColor("#ffffff"));
            fsm_button.setTextColor(Color.parseColor("#000000"));

            video_layout.setVisibility(View.GONE);
            pdf_layout.setVisibility(View.VISIBLE);
            fsm_layout.setVisibility(View.GONE);
        });
        fsm_button.setOnClickListener(view1 -> {
            video_button.setBackgroundColor(Color.parseColor("#bbbbbb"));
            pdf_button.setBackgroundColor(Color.parseColor("#bbbbbb"));
            fsm_button.setBackgroundColor(Color.parseColor("#7579e7"));

            video_button.setTextColor(Color.parseColor("#000000"));
            pdf_button.setTextColor(Color.parseColor("#000000"));
            fsm_button.setTextColor(Color.parseColor("#ffffff"));

            video_layout.setVisibility(View.GONE);
            pdf_layout.setVisibility(View.GONE);
            fsm_layout.setVisibility(View.VISIBLE);
        });

        DatabaseReference reference;
        List<_MoreVideo> list1 = new ArrayList<>();
        RecyclerView recyclerView1;
        final _MoreVideoRecycler[] adapter1 = new _MoreVideoRecycler[1];

        DatabaseReference pdf_reference;
        List<_Pdfs> pdf_list = new ArrayList<>();
        RecyclerView pdf_recyclerview;
        final _PdfRecycler[] pdf_adapter = new _PdfRecycler[1];

        DatabaseReference fsm_reference;
        List<_Fsm> fsm_list = new ArrayList<>();
        RecyclerView fsm_recyclerview;
        final _FsmRecycler[] fsm_adapter = new _FsmRecycler[1];

        recyclerView1 = findViewById(R.id.more_video_thumb_recycle);
        recyclerView1.setHasFixedSize(true);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        pdf_recyclerview = findViewById(R.id.store_pdf_recyclerview);
        pdf_recyclerview.setHasFixedSize(true);
        pdf_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        fsm_recyclerview = findViewById(R.id.store_fsm_recyclerview);
        fsm_recyclerview.setHasFixedSize(true);
        fsm_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        SkeletonScreen skeletonScreen1 = Skeleton.bind(recyclerView1)
                .adapter(adapter1[0])
                .load(R.layout.more_video_recycler)
                .show();

        SkeletonScreen skeletonScreen2 = Skeleton.bind(pdf_recyclerview)
                .adapter(pdf_adapter[0])
                .load(R.layout.store_pdf_recycler)
                .show();

        SkeletonScreen skeletonScreen3 = Skeleton.bind(fsm_recyclerview)
                .adapter(fsm_adapter[0])
                .load(R.layout.store_fsm_recycler)
                .show();

        reference = FirebaseDatabase.getInstance().getReference("batches/" + getIntent().getStringExtra("batch_id") + "/store/videos/");
        pdf_reference = FirebaseDatabase.getInstance().getReference("batches/" + getIntent().getStringExtra("batch_id") + "/store/pdfs/");
        fsm_reference = FirebaseDatabase.getInstance().getReference("batches/" + getIntent().getStringExtra("batch_id") + "/store/fsm/");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    _MoreVideo moreVideo = dataSnapshot.getValue(_MoreVideo.class);
                    list1.add(moreVideo);
                }

                Collections.reverse(list1);
                adapter1[0] = new _MoreVideoRecycler(BatchActivity.this, list1);
                recyclerView1.setAdapter(adapter1[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        pdf_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    _Pdfs pdfs = dataSnapshot.getValue(_Pdfs.class);
                    pdf_list.add(pdfs);
                }

                Collections.reverse(pdf_list);
                pdf_adapter[0] = new _PdfRecycler(BatchActivity.this, pdf_list, getIntent().getStringExtra("batch_id"));
                pdf_recyclerview.setAdapter(pdf_adapter[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        fsm_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    _Fsm fsm = dataSnapshot.getValue(_Fsm.class);
                    fsm_list.add(fsm);
                }

                Collections.reverse(fsm_list);
                fsm_adapter[0] = new _FsmRecycler(BatchActivity.this, fsm_list, getIntent().getStringExtra("batch_id"));
                fsm_recyclerview.setAdapter(fsm_adapter[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        fab1.setOnClickListener(view -> {
            Intent intent = new Intent(BatchActivity.this, UploadBatchVideo.class);
            startActivity(intent);
        });

        fab2.setOnClickListener(view -> {
            Intent intent = new Intent(BatchActivity.this, UploadBatchPdf.class);
            startActivity(intent);
        });

        fab3.setOnClickListener(view -> {
            _myDialog.show();
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
                DatabaseReference db = FirebaseDatabase.getInstance().getReference("batches/" + getIntent().getStringExtra("batch_id") + "/store/fsm");
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
    }
}

class _MoreVideoRecycler extends RecyclerView.Adapter<_MoreVideoRecycler.ViewHolder> {

    View view;
    Context context;
    List<_MoreVideo> MainImageUploadInfoList;
    public static String SUB_SERVICE_UID;

    public _MoreVideoRecycler(Context context, List<_MoreVideo> TempList) {
        this.MainImageUploadInfoList = TempList;
        this.context = context;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.more_video_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final _MoreVideo moreVideo = MainImageUploadInfoList.get(position);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference gsReference = storage.getReferenceFromUrl("gs://coaching-institute-project.appspot.com/store/videos/" + moreVideo.getVideo_id() + ".mp4");

        gsReference.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context).load(uri.toString()).into(holder.image)).addOnFailureListener(exception -> {
            // Handle any errors
        });
    }

    @Override
    public int getItemCount() {
        return MainImageUploadInfoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, price, price_;
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.more_video_title);
            price = itemView.findViewById(R.id.more_video_price);
            price_ = itemView.findViewById(R.id.more_video_price_);
            image = itemView.findViewById(R.id.more_video_thumb_recycle);
        }
    }
}

class _MoreVideo {
    private String video_title, video_description, video_price, video_teacher, video_duration;
    private String video_id, video_upload_time, strike_price;

    public _MoreVideo() {
        //empty constructor needed
    }

    public _MoreVideo(String video_title, String video_description, String video_upload_time,
                      String video_teacher, String video_price, String video_id, String video_duration, String strike_price) {
        this.video_title = video_title;
        this.video_teacher = video_teacher;
        this.video_price = video_price;
        this.video_id = video_id;
        this.video_description = video_description;
        this.video_upload_time = video_upload_time;
        this.video_duration = video_duration;
        this.strike_price = strike_price;

    }

    public String getVideo_title() {
        return video_title;
    }

    public String getVideo_description() {
        return video_description;
    }

    public String getVideo_id() {
        return video_id;
    }

    public String getStrike_price() {
        return strike_price;
    }

    public String getVideo_duration() {
        return video_duration;
    }

    public String getVideo_price() {
        return video_price;
    }

    public String getVideo_teacher() {
        return video_teacher;
    }

    public String getVideo_upload_time() {
        return video_upload_time;
    }
}

class _PdfRecycler extends RecyclerView.Adapter<_PdfRecycler.ViewHolder> {

    View view;
    Context context;
    List<_Pdfs> MainImageUploadInfoList;
    String batch_id;

    public _PdfRecycler(Context context, List<_Pdfs> TempList, String batch_id) {
        this.MainImageUploadInfoList = TempList;
        this.context = context;
        this.batch_id = batch_id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_pdf_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final _Pdfs pdfs = MainImageUploadInfoList.get(position);
        holder.title.setText(pdfs.getPdf_title());
        holder.details.setText(String.format("by %s (%s)", pdfs.getPdf_teacher(), pdfs.getPdf_upload_time()));

        holder.itemView.setOnClickListener(v -> {
            Dialog myDialog = new Dialog(context);
            myDialog.setContentView(R.layout.show_popup);
            TextView title = myDialog.findViewById(R.id.notification_title_popup);
            TextView content = myDialog.findViewById(R.id.notification_content_popup);

            title.setText(pdfs.getPdf_title());
            content.setText(pdfs.getPdf_description());

            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            myDialog.show();
        });

        holder.delete_pdf.setOnClickListener(view -> new AlertDialog.Builder(context)
                .setTitle("Are you sure?")
                .setMessage("You are about to delete this pdf which cannot be undone. Continue?")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes",
                        (dialog, which) -> {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("batches/" + batch_id + "/store/pdfs/");
                            databaseReference.child(pdfs.getPdf_id()).removeValue();
                            StorageReference reference = FirebaseStorage.getInstance().getReference("batches/" + batch_id + "store/pdfs/");
                            reference.child(pdfs.getPdf_id() + ".pdf").delete();
                            Intent intent = new Intent(context, BatchActivity.class);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                            Toast.makeText(context, "PDF deleted from database", Toast.LENGTH_SHORT).show();
                        }).create().show());
    }

    @Override
    public int getItemCount() {
        return MainImageUploadInfoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, details;
        ImageView delete_pdf;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.pdf_title);
            details = itemView.findViewById(R.id.pdf_details);
            delete_pdf = itemView.findViewById(R.id.delete_batch_pdf);
        }
    }
}

class _Pdfs {
    private String pdf_title, pdf_description, pdf_teacher;
    private String pdf_id, pdf_upload_time;

    public _Pdfs() {
        //empty constructor needed
    }

    public _Pdfs(String pdf_title, String pdf_description, String pdf_teacher,
                 String pdf_id, String pdf_upload_time) {
        this.pdf_title = pdf_title;
        this.pdf_description = pdf_description;
        this.pdf_teacher = pdf_teacher;
        this.pdf_id = pdf_id;
        this.pdf_upload_time = pdf_upload_time;
    }

    public String getPdf_description() {
        return pdf_description;
    }

    public String getPdf_id() {
        return pdf_id;
    }

    public String getPdf_teacher() {
        return pdf_teacher;
    }

    public String getPdf_title() {
        return pdf_title;
    }

    public String getPdf_upload_time() {
        return pdf_upload_time;
    }
}

class _FsmRecycler extends RecyclerView.Adapter<_FsmRecycler.ViewHolder> {

    View view;
    Context context;
    List<_Fsm> MainImageUploadInfoList;
    String batch_id;

    public _FsmRecycler(Context context, List<_Fsm> TempList, String batch_id) {
        this.MainImageUploadInfoList = TempList;
        this.context = context;
        this.batch_id = batch_id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_fsm_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final _Fsm fsm = MainImageUploadInfoList.get(position);
        holder.title.setText(fsm.getFsm_title());
        holder.details.setText(String.format("by %s (%s)", fsm.getFsm_teacher(), fsm.getFsm_upload_time()));

        holder.delete_fsm.setOnClickListener(view -> new AlertDialog.Builder(context)
                .setTitle("Are you sure?")
                .setMessage("You are about to delete this pdf which cannot be undone. Continue?")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes",
                        (dialog, which) -> {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("batches/" + batch_id + "/store/fsm/");
                            databaseReference.child(fsm.getFsm_id()).removeValue();
                            Intent intent = new Intent(context, BatchActivity.class);
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
            title = itemView.findViewById(R.id.fsm_title);
            details = itemView.findViewById(R.id.fsm_details);
            delete_fsm = itemView.findViewById(R.id.delete_batch_fsm);
        }
    }
}

class _Fsm {
    private String fsm_title, fsm_content, fsm_teacher;
    private String fsm_id, fsm_upload_time;

    public _Fsm() {
        //empty constructor needed
    }

    public _Fsm(String fsm_title, String fsm_content, String fsm_teacher,
                String fsm_id, String fsm_upload_time) {
        this.fsm_title = fsm_title;
        this.fsm_content = fsm_content;
        this.fsm_teacher = fsm_teacher;
        this.fsm_id = fsm_id;
        this.fsm_upload_time = fsm_upload_time;
    }

    public String getFsm_content() {
        return fsm_content;
    }

    public String getFsm_id() {
        return fsm_id;
    }

    public String getFsm_teacher() {
        return fsm_teacher;
    }

    public String getFsm_title() {
        return fsm_title;
    }

    public String getFsm_upload_time() {
        return fsm_upload_time;
    }
}
