package studio.goldenapp.coaching.admin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomList extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] web;
    private final int[] imageId;
    public CustomList(Activity context,
                      String[] web, int[] imageId) {
        super(context, R.layout.customlayout_student_profile, web);
        this.context = context;
        this.web = web;
        this.imageId = imageId;

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        @SuppressLint("ViewHolder")
        View rowView= inflater.inflate(R.layout.customlayout_student_profile, null, true);
        TextView txtTitle = rowView.findViewById(R.id.title);

        ImageView imageView = rowView.findViewById(R.id.list_icon);
        txtTitle.setText(web[position]);

        imageView.setImageResource(imageId[position]);
        return rowView;
    }
}
