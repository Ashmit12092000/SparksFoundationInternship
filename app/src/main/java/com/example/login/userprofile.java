package com.example.login;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class userprofile extends AppCompatActivity {
    FirebaseAuth mAuth;
    ImageView image;
    TextView name;
    TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);
        mAuth=FirebaseAuth.getInstance();
        image=findViewById(R.id.image);
        name=findViewById(R.id.gname);
        email=findViewById(R.id.gemail);
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null) {
            String names = Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName();
            String emails = mAuth.getCurrentUser().getEmail();
            String images = Objects.requireNonNull(mAuth.getCurrentUser().getPhotoUrl()).toString();
            name.setText(names);
            email.setText(emails);
            Glide.with(this).load(images).placeholder(R.drawable.com_facebook_profile_picture_blank_square).into(image);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(userprofile.this,MainActivity.class));
                finish();

        }
        return super.onOptionsItemSelected(item);
    }
}