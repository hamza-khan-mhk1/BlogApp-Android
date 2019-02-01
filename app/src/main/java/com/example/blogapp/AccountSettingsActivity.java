package com.example.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSettingsActivity extends AppCompatActivity {
    private CircleImageView account_image;
    private EditText account_name;
    private Button btn_save_name;

    private final static int GALLERY_PICK = 1;

    private DatabaseReference getUserDataReference;
    private StorageReference accountImageStorageReference;

    private ProgressDialog progressDialog;

    Bitmap account_image_bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        account_image = (CircleImageView) findViewById(R.id.account_image);
        account_name = (EditText) findViewById(R.id.account_name);
        btn_save_name = (Button) findViewById(R.id.btn_save_name);

        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(
                        FirebaseAuth.getInstance().getCurrentUser().getUid()
                );
        getUserDataReference.keepSynced(true);
        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String user_name = dataSnapshot.child("user_name").getValue().toString();
                final String user_image = dataSnapshot.child("user_image").getValue().toString();

                account_name.setText(user_name);
                if (!user_image.equals("default_image")){

                    Picasso.with(AccountSettingsActivity.this).load(user_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image)
                            .into(account_image, new Callback() {
                                @Override
                                public void onSuccess() {}

                                @Override
                                public void onError() {
                                    Picasso.with(AccountSettingsActivity.this).load(user_image).placeholder(R.drawable.default_image)
                                            .into(account_image);
                                }
                            });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        accountImageStorageReference = FirebaseStorage.getInstance().getReference().child("Profile_Images");
        account_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        progressDialog = new ProgressDialog(this);
        btn_save_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_name = account_name.getText().toString();
                ChangeAccountName(new_name);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
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
                    account_image_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(image_filepath_uri);
                }catch (Exception e){
                    Toast.makeText(AccountSettingsActivity.this, "Error Converting Image to Bitmap", Toast.LENGTH_SHORT)
                            .show();
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                account_image_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                final byte[] image_bytes = byteArrayOutputStream.toByteArray();

                // saving compressed image
                StorageReference filePath = accountImageStorageReference.child(user_id + ".jpg");
                UploadTask uploadTask = filePath.putBytes(image_bytes);

                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            final String imageDownloadUri = task.getResult().getDownloadUrl().toString();

                            Map update_user_data = new HashMap();
                            update_user_data.put("user_image", imageDownloadUri);

                            getUserDataReference.updateChildren(update_user_data)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(AccountSettingsActivity.this, "Firebase: Profile Picture Reference Updated",
                                                    Toast.LENGTH_SHORT).show();
                                            //progressDialog.dismiss();
                                        }
                                    });
                        }else{
                            Toast.makeText(AccountSettingsActivity.this, "Firebase: Error Uploading Picture.", Toast.LENGTH_SHORT)
                                    .show();
                            //progressDialog.dismiss();
                        }
                    }});
            } else {
                Exception error = result.getError();
            }
        }
    }

    private void ChangeAccountName(String new_name) {
        if (TextUtils.isEmpty(new_name)){
            Toast.makeText(AccountSettingsActivity.this, "Provide a Name", Toast.LENGTH_SHORT).show();
        }else{
            progressDialog.setTitle("Changing Name");
            progressDialog.setMessage("Please wait while we update your name");
            progressDialog.show();

            getUserDataReference.child("user_name").setValue(new_name)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                progressDialog.dismiss();
                                Intent intent = new Intent(AccountSettingsActivity.this, MainActivity.class);
                                startActivity(intent);
                                Toast.makeText(AccountSettingsActivity.this, "Firebase: Name Updated", Toast.LENGTH_SHORT).show();
                                finish();
                            }else{
                                Toast.makeText(AccountSettingsActivity.this, "Firebase: Error Updating Name", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
