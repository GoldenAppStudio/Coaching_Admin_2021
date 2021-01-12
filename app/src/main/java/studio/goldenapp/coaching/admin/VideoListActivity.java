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

public class VideoListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        DatabaseReference video_reference;
        List<Video> video_list = new ArrayList<>();
        RecyclerView video_recyclerview;
        final VideoRecycler[] video_adapter = new VideoRecycler[1];
        video_recyclerview = findViewById(R.id.video_recycler);
        video_recyclerview.setHasFixedSize(true);
        video_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        SkeletonScreen skeletonScreen = Skeleton.bind(video_recyclerview)
                .adapter(video_adapter[0])
                .load(R.layout.video_recycler)
                .show();

        video_reference = FirebaseDatabase.getInstance().getReference("store/videos/");

        video_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Video video = dataSnapshot.getValue(Video.class);
                    video_list.add(video);
                }

                Collections.reverse(video_list);
                video_adapter[0] = new VideoRecycler(VideoListActivity.this, video_list);
                video_recyclerview.setAdapter(video_adapter[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
}


class VideoRecycler extends RecyclerView.Adapter<VideoRecycler.ViewHolder> {

    View view;
    Context context;
    List<Video> MainImageUploadInfoList;

    public VideoRecycler(Context context, List<Video> TempList) {
        this.MainImageUploadInfoList = TempList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Video video = MainImageUploadInfoList.get(position);
        holder.title.setText(video.getVideo_title());
        holder.details.setText(String.format("by %s (%s)", video.getVideo_teacher(), video.getVideo_upload_time()));

        holder.itemView.setOnClickListener(v -> {
            Dialog myDialog = new Dialog(context);
            myDialog.setContentView(R.layout.show_popup);
            TextView title = myDialog.findViewById(R.id.notification_title_popup);
            TextView content = myDialog.findViewById(R.id.notification_content_popup);

            title.setText(video.getVideo_title());
            content.setText(video.getVideo_description());

            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            myDialog.show();
        });

        holder.delete_video.setOnClickListener(view -> new AlertDialog.Builder(context)
                .setTitle("Are you sure?")
                .setMessage("You are about to delete this video which cannot be undone. Continue?")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes",
                        (dialog, which) -> {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("store/videos/");
                            databaseReference.child(video.getVideo_id()).removeValue();
                            StorageReference reference = FirebaseStorage.getInstance().getReference("store/videos");
                            reference.child(video.getVideo_id() + ".mp4").delete();
                            Intent intent = new Intent(context, VideoListActivity.class);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                            Toast.makeText(context, "Video deleted from database", Toast.LENGTH_SHORT).show();
                        }).create().show());
    }

    @Override
    public int getItemCount() {
        return MainImageUploadInfoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, details;
        ImageView delete_video;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.video_title_recycler);
            details = itemView.findViewById(R.id.video_details_recycler);
            delete_video = itemView.findViewById(R.id.delete_video);
        }
    }

}

class Video {
    String video_title, video_teacher, video_upload_time;
    String video_strike_price, video_price, video_id;
    String video_description;

    public Video() {
    }

    public Video(String video_title, String video_teacher, String video_upload_time, String video_strike_price, String video_price, String video_id, String video_description) {
        this.video_title = video_title;
        this.video_teacher = video_teacher;
        this.video_upload_time = video_upload_time;
        this.video_strike_price = video_strike_price;
        this.video_price = video_price;
        this.video_id = video_id;
        this.video_description = video_description;
    }

    public String getVideo_title() {
        return video_title;
    }

    public String getVideo_description() {
        return video_description;
    }

    public String getVideo_teacher() {
        return video_teacher;
    }

    public String getVideo_upload_time() {
        return video_upload_time;
    }

    public String getVideo_strike_price() {
        return video_strike_price;
    }

    public String getVideo_price() {
        return video_price;
    }

    public String getVideo_id() {
        return video_id;
    }
}