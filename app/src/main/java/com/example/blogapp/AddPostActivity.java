package com.example.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class AddPostActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar toolbar;

    private ImageView imageView_post_cover;
    private EditText editText_post_title;
    private Button btn_add_post;

    private final static int GALLERY_PICK = 1;
    Bitmap post_image_bitmap = null;
    byte[] image_bytes = new byte[0];
    private DatabaseReference getPostsDatabaseReference;
    private StorageReference getPostsImageStorageReference;

    private ProgressDialog progressDialog;
    private DatabaseReference usersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.addPost_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Post");

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        imageView_post_cover = (ImageView) findViewById(R.id.imageView_post_cover);
        editText_post_title = (EditText) findViewById(R.id.editText_post_title);
        btn_add_post = (Button) findViewById(R.id.btn_add_post);

        imageView_post_cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        progressDialog = new ProgressDialog(this);
        btn_add_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setTitle("New Post");
                progressDialog.setMessage("Please wait while post is being shared");
                addNewPost();
            }
        });

    }

   /* @Override
    protected void onPause() {
        super.onPause();
        usersRef.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user_name = dataSnapshot.child("user_name").getValue().toString();

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddPostActivity.this, "Error Retreiving User Name", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private void addNewPost() {
        try {
            if (!TextUtils.isEmpty(editText_post_title.getText().toString())){
                progressDialog.show();
                uploadPostImage();
            }else{
                Toast.makeText(this, "Add a Description", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void addPostToDatabase(String imageDownloadUri){
        try {
            DatabaseReference post_key = FirebaseDatabase.getInstance().getReference().child("Posts").push();
            DatabaseReference post_ref = post_key.getRef();

            post_ref.child("post_user_name").setValue(MainActivity.user_name);
            post_key.child("post_user_image_url").setValue(MainActivity.user_image_url);
            post_ref.child("post_desc").setValue(editText_post_title.getText().toString());
            post_ref.child("post_timestamp").setValue(getTimestamp());
            post_ref.child("post_image_url").setValue(imageDownloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        progressDialog.dismiss();
                        Intent mainIntent = new Intent(AddPostActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        Toast.makeText(AddPostActivity.this, "Post Added Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, "Error: Post Adding Post", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void uploadPostImage() {
        try {
            // saving compressed image
            getPostsImageStorageReference = FirebaseStorage.getInstance().getReference().child("Posts_Images");
            StorageReference filePath = getPostsImageStorageReference.child(System.currentTimeMillis() + ".jpg");
            UploadTask uploadTask = filePath.putBytes(image_bytes);

            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        final String imageDownloadUri = task.getResult().getDownloadUrl().toString();
                        addPostToDatabase(imageDownloadUri);
                    }else{
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, "Firebase: Error Uploading Picture.", Toast.LENGTH_SHORT)
                                .show();
                        //progressDialog.dismiss();
                    }
                }});
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            imageView_post_cover.setImageURI(imageUri);
            // start image cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {


               /* progressDialog.setTitle("Updating Profile Image");
                progressDialog.setMessage("Please wait while we update your profile image.");
                progressDialog.show();*/

                Uri resultUri = result.getUri();
                // get firebase stuff
                String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                try {
                    File image_filepath_uri = new File(resultUri.getPath());
                    // image bitmap conversion
                    post_image_bitmap = new Compressor(this)
                            .setMaxWidth(500)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(image_filepath_uri);
                }catch (Exception e){
                    Toast.makeText(AddPostActivity.this, "Error Converting Image to Bitmap", Toast.LENGTH_SHORT)
                            .show();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                post_image_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                image_bytes = byteArrayOutputStream.toByteArray();

            } else {
                Exception error = result.getError();
            }
        }
    }

    public String getTimestamp() {
        String timestamp = null;
        try {
            String dayOfTheWeek = (String) DateFormat.format("EEEE", new Date()); // Thursday
            String currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(Calendar.MINUTE);
            timestamp = dayOfTheWeek.substring(0, 3).toUpperCase() + ", " + currentTime;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return timestamp;
    }

   /* private void getUserName(String post_user_id) {

        Log.d("username", "getUserName: "+post_user_id);
        usersRef.child(post_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user_name = dataSnapshot.child("user_name").getValue().toString();
                Log.d("username", "getUserName1: "+user_name);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddPostActivity.this, "Error Retreiving User Name", Toast.LENGTH_SHORT).show();
            }
        });
        Log.d("username", "getUserName2: "+user_name);



    }*/
}
