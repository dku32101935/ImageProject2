package com.example.imageproject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "ImageProject";


    private Button mButtonHome;
    private Button mButtonDate;
    private Button mButtonFaceCheck;
    private ImageView mImageviewInHome;
    private Button mButtonTakeImage;
    private Button mButtonUpload;

    private Uri mImageUri;

    private StorageReference mStorageRef;
    private StorageTask mUploadTask;

    private FirebaseFirestore mDb;

    private FirebaseVisionFaceDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonHome = findViewById(R.id.button_home);
        mButtonDate = findViewById(R.id.button_date);
        mButtonFaceCheck = findViewById(R.id.button_face_check);
        mImageviewInHome = findViewById(R.id.imageview_in_main);
        mButtonTakeImage = findViewById(R.id.button_take_image);
        mButtonUpload = findViewById(R.id.button_upload);

        mStorageRef = FirebaseStorage.getInstance().getReference("Images");
        mDb = FirebaseFirestore.getInstance();

        mButtonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageUri = null;
                Picasso.with(MainActivity.this).load(mImageUri).into(mImageviewInHome);
            }
        });

        mButtonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openclass(DateView.class);
            }
        });

        mButtonFaceCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mButtonTakeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(MainActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    filequery();
                    fileupload();
                }
            }
        });
    }

    private String facecheck(){

        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();


        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(this, mImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        // Task completed successfully
                                        // ...
                                        for (FirebaseVisionFace face : faces) {

                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
    return "true";
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();

            Picasso.with(this).load(mImageUri).into(mImageviewInHome);
        }
    }

    private String getFileExtension(Uri uri) { //이미지 확장자 가져오기
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private String getTimeFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(System.currentTimeMillis());
    }

    private int getFaceCheck(Uri uri_facecheck) {
        int temp = 0;
        if (mImageUri != null) {

            Context mContext;
            mContext = this;

            FirebaseVisionImage image = null;

            try {
                image = FirebaseVisionImage.fromFilePath(mContext, mImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FirebaseVisionFaceDetectorOptions options =
                    new FirebaseVisionFaceDetectorOptions.Builder()
                            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                            .build();

            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);

            Task<List<FirebaseVisionFace>> result = detector.detectInImage(image);
            result.addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                @Override
                public void onSuccess(List<FirebaseVisionFace> faces) {
                    // Task completed successfully
                    // ...
                    for (FirebaseVisionFace face : faces) {
                        Toast.makeText(MainActivity.this, "얼굴", Toast.LENGTH_LONG).show();
                    }
                }
            });
            result.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Task failed with an exception
                    // ...
                }
            });
            Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }

        return temp;
    }

    private void filequery() {
        Map<String, Object> Inform = new HashMap<>();
        Inform.put("ImageUri", mImageUri.toString());
        Inform.put("Date", getTimeFormat());
        Inform.put("Face", facecheck());
        // Inform.put("FaceCheck", getFaceCheck(mImageUri));

        mDb.collection("Images")
                .add(Inform)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }

    private void fileupload() {
        if (mImageUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() //현재 시간을 이름으로 저장
                    + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_LONG).show();


                            //성공한 사진 삭제
                            mImageUri = null;
                            Picasso.with(MainActivity.this).load(mImageUri).into(mImageviewInHome);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void openclass(Class T) {
        Intent intent = new Intent(this, T);
        startActivity(intent);
    }
}