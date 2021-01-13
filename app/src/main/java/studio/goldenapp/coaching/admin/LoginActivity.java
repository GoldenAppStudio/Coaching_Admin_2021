package studio.goldenapp.coaching.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

//        if(!isNetworkAvailable(this)) {
//            Toast.makeText(this,"Connect to internet first", Toast.LENGTH_LONG).show();
//            finish(); //Calling this method to close this activity when internet is not available.
//            System.exit(0);
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle("Login Activity");

        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        CircularProgressButton button = findViewById(R.id.submit_login);

        button.setOnClickListener(view -> {
            button.startAnimation();
            if(username.getText().toString().equals("") || password.getText().toString().equals("")) {
                Toast.makeText(this, "Username and Password required!", Toast.LENGTH_SHORT).show();
                button.revertAnimation();
            } else {
                DatabaseReference db = FirebaseDatabase.getInstance().getReference("admin");
                db.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(
                                username.getText().toString().equals(snapshot.child("username").getValue().toString()) &&
                                password.getText().toString().equals(snapshot.child("password").getValue().toString())        
                        ) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Username or Password is wrong", Toast.LENGTH_SHORT).show();
                            button.revertAnimation();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMan.getActiveNetworkInfo() != null && conMan.getActiveNetworkInfo().isConnected())
            return true;
        else
            return false;
    }
}