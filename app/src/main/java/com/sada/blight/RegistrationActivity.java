package com.sada.blight;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
//import com.theartofdev.edmodo.cropper.CropImage;
//import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "RegistrationActivity";
    private static final int GALLERY_REQUEST = 1;
    private static final String DEFAULT_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/blight-c675c.appspot.com/o/ProfilePics%2Fic_default.png?alt=media&token=252154f2-ee45-4d8d-abd1-05480dbabf77";

    private EditText etName, etEmail, etBloodgroup, etContact, etEmergencyContact, etPassword;
    private ImageButton bAddImage;
    private Button bRegister;
    private static ProgressDialog progressDialog;
    private Uri uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        findViews();
        bAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickUserImage();
            }
        });

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = null;
                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                try {
                    user = new User(etName.getText().toString().trim(),
                            etEmail.getText().toString().trim(),
                            etBloodgroup.getText().toString().trim(),
                            etContact.getText().toString().trim(),
                            etEmergencyContact.getText().toString().trim(),
                            deviceToken,
                            DEFAULT_IMAGE_URL,
                            etPassword.getText().toString().trim());
                } catch (Exception e) {
                    Log.e(TAG, "onClick: ", e);
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                if (isDataValid(user)) {
                    FirebaseHelper firebaseHelper = new FirebaseHelper();
                    firebaseHelper.registerUser(user);
                }
            }
        });
    }

    private boolean isDataValid(User user) {
        boolean dataValid = true;

        etName.setError(null);
        etPassword.setError(null);
        etContact.setError(null);
        etEmergencyContact.setError(null);
        etBloodgroup.setError(null);
        etEmail.setError(null);

        if (TextUtils.isEmpty(user.getName())) {
            etName.setError("Field cannot be empty!!!");
            dataValid = false;
        }
        if (TextUtils.isEmpty(user.getEmail())) {
            etEmail.setError("Field cannot be empty!!!");
            dataValid = false;
        } else if (!(user.getEmail().contains("@") && !user.getEmail().contains(" "))) {
            etEmail.setError("Please enter a valid Email-ID");
            dataValid = false;
        }
        if (TextUtils.isEmpty(user.getBloodgroup())) {
            etBloodgroup.setError("Field cannot be empty!!!");
            dataValid = false;
        }
        if (TextUtils.isEmpty(user.getContact())) {
            etContact.setError("Field cannot be empty!!!");
            dataValid = false;
        } else if (!(user.getContact().matches("[0-9]+") &&
                user.getContact().length() == 10)) {
            etContact.setError("Please enter a valid Phone No");
            dataValid = false;
        }
        if (TextUtils.isEmpty(user.getEmergency_contact())) {
            etEmergencyContact.setError("Field cannot be empty!!!");
            dataValid = false;
        } else if (!(user.getEmergency_contact().matches("[0-9]+") &&
                user.getEmergency_contact().length() == 10)) {
            etEmergencyContact.setError("Please enter a valid Phone No");
            dataValid = false;
        }
        if (TextUtils.isEmpty(user.getPassword())) {
            etPassword.setError("Field cannot be empty!!!");
            dataValid = false;
        }

        return dataValid;
    }

    private void pickUserImage() {
        Log.e(TAG, "pickUserImage: FUNCTION STARTED");
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            uri = data.getData();
            bAddImage.setImageURI(uri);
            // start picker to get image for cropping and then use the image in cropping activity
//            CropImage.activity(uri)
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setAspectRatio(1, 1)
//                    .start(this);

        }

//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK) {
//                Uri resultUri = result.getUri();
//                uri = resultUri;
//                bAddImage.setImageURI(resultUri);
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                Exception error = result.getError();
//                Toast.makeText(this, "CROPPING UNSUCCESSFULL : " + error.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        }
    }

    public class FirebaseHelper {

        private static final String TAG = "FirebaseHelper";

        private FirebaseAuth auth;
        private DatabaseReference databaseUser;
        private StorageReference storageProfilePics;

        public FirebaseHelper() {
            auth = FirebaseAuth.getInstance();
            databaseUser = FirebaseDatabase.getInstance().getReference().child("users");
            storageProfilePics = FirebaseStorage.getInstance().getReference();
        }

        public void registerUser(User user) {
            startRegister(user);
        }

        private void startRegister(final User user) {
            Log.e(TAG, "startRegister: REGISTERING USER...");
            progressDialog.setMessage("Registering User...");
            progressDialog.show();
            auth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.e(TAG, "onComplete: REGISTERING USER SUCCESSFULL");
                                String userId = auth.getCurrentUser().getUid();

                                uploadImage(user.getProfile_pic());
                                DatabaseReference currentUser = databaseUser.child(userId);

                                HashMap<String, String> userMap = new HashMap<String, String>();
                                userMap.put("name", user.getName());
                                userMap.put("email", user.getEmail());
                                userMap.put("bloodgroup", user.getBloodgroup());
                                userMap.put("contact", user.getContact());
                                userMap.put("emergency_contact", user.getEmergency_contact());
                                userMap.put("device_token", user.getDevice_token());
                                userMap.put("profile_pic", user.getProfile_pic());

                                currentUser.setValue(userMap);
                                progressDialog.dismiss();

                                Log.e(TAG, "onComplete: Redirecting to HomeActivity");
                                Toast.makeText(getApplicationContext(), "Registration Successfull !!!",
                                        Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), HomeActivity
                                        .class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: REGISTRATION FAILED due to : " + e.getMessage());
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "REGISTRATION FAILED because : " +
                            e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        private void uploadImage(String profilePicURL) {
            Log.e(TAG, "uploadImage: FUNCTION STARTED");
            progressDialog.show();
            if (uri != null) {
                Log.e(TAG, "uploadImage: URI NOT NULL");
                StorageReference filePath = storageProfilePics.child("ProfilePics").child(uri
                        .getLastPathSegment());
                filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        DatabaseReference newUser = databaseUser.child(auth.getCurrentUser().getUid());
                        newUser.child("profilepic").setValue(downloadUrl.toString());
                        Log.e(TAG, "onSuccess: Image Added ...");
                        progressDialog.dismiss();
//                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to finish setup due to : " +
                                e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onFailure: FAILED TO FINISH SETUP");
                        progressDialog.dismiss();
                    }
                });
            } else {
                Log.e(TAG, "uploadImage: URI NULL");
                DatabaseReference newUser = databaseUser.child(auth.getCurrentUser().getUid());
                newUser.child("profilepic").setValue(profilePicURL);
                Log.e(TAG, "onSuccess: SETUP DONE ...");
                progressDialog.dismiss();
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        }

    }

    private void findViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etBloodgroup = findViewById(R.id.etBloodgroup);
        etContact = findViewById(R.id.etContact);
        etEmergencyContact = findViewById(R.id.etEmergencyContact);
        etPassword = findViewById(R.id.etPassword);
        bAddImage = findViewById(R.id.bAddImage);
        bRegister = findViewById(R.id.bRegister);
        progressDialog = new ProgressDialog(this);
    }
}
