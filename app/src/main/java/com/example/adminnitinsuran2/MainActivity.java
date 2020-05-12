package com.example.adminnitinsuran2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
private static final int Pickimagerequest=1;
private ImageView mimage;
private Button choosefilebtn,uploadimgbtn,uploadmsgbtn,uploadnewsbtn;
private EditText titleedittext,desedittext,msgedittext,newsedittext;
private Uri imageuri;
//private ProgressBar progressBar;
private StorageReference mstorageReference;
private DatabaseReference mdatabaseReference;
private DatabaseReference databaseReference2;
    @Override
   protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mimage=findViewById(R.id.mainactimage);
        choosefilebtn=findViewById(R.id.choosefilebtn);
        uploadimgbtn=findViewById(R.id.uploadimgbtn);
               uploadmsgbtn =findViewById(R.id.uploadmsgbtn);
               uploadnewsbtn=findViewById(R.id.uploadnewsbtn);
               titleedittext =findViewById(R.id.titledittext);
                desedittext=findViewById(R.id.desedittext);
                msgedittext=findViewById(R.id.messageedittext);
                newsedittext=findViewById(R.id.newsedittext);
                if(!checkInternet())
                {
                    Toast.makeText(MainActivity.this,"Please turn on your internet and start app",Toast.LENGTH_LONG).show();

                }
                databaseReference2=FirebaseDatabase.getInstance().getReference();
                mstorageReference= FirebaseStorage.getInstance().getReference("uploads");
                mdatabaseReference= FirebaseDatabase.getInstance().getReference("uploads");
                uploadimgbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadImage();
                    }
                });
                choosefilebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openFilechooser();
                    }
                });
                uploadmsgbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(msgedittext.getText().toString().equals(""))
                            Toast.makeText(MainActivity.this,"Please fill message before uploading",Toast.LENGTH_SHORT).show();
                        else
                            databaseReference2.child("message").setValue(msgedittext.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MainActivity.this,"Upload succesfull",Toast.LENGTH_SHORT).show();
                                }
                            });

                    }
                });
                uploadnewsbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(newsedittext.getText().toString().equals(""))
                            Toast.makeText(MainActivity.this,"Please fill news before uploading",Toast.LENGTH_SHORT).show();
                        else
                            databaseReference2.child("news").setValue(newsedittext.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MainActivity.this,"Upload succesfull",Toast.LENGTH_SHORT).show();
                                }

                            });

                    }
                });

    }

    private boolean checkInternet() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;
        return connected;

    }

    private String getFileExtension(Uri uri)
{
    ContentResolver cr=getContentResolver();
    MimeTypeMap mime=MimeTypeMap.getSingleton();
    return mime.getExtensionFromMimeType(cr.getType(uri));
}
    private void uploadImage() {
        if(imageuri!=null) {
                  final StorageReference filerefrence=mstorageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageuri));
                  filerefrence.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                      @Override
                      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                         filerefrence.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                             @Override
                             public void onSuccess(Uri uri) {
                                 Upload upload=new Upload("Title :"+titleedittext.getText().toString(),"Description :"+desedittext.getText().toString(),uri.toString());
                                 String uploadid=mdatabaseReference.push().getKey();
                                 mdatabaseReference.child(uploadid).setValue(upload);
                             }
                         });
                          Toast.makeText(MainActivity.this,"UploadSuccesful",Toast.LENGTH_SHORT).show();



                      }
                  })
                          .addOnFailureListener(new OnFailureListener() {
                              @Override
                              public void onFailure(@NonNull Exception e) {
                                  Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                              }
                          });
                          //.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                      @Override
//                      public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
//                          double progress=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
//                          progressBar.setProgress((int) progress);
//                      }
//                  });
        }
        else
        {
            Toast.makeText(MainActivity.this,"Pick a file",Toast.LENGTH_SHORT).show();
        }
    }

    private void openFilechooser() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,Pickimagerequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Pickimagerequest&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null)
        {
            imageuri=data.getData();
            Picasso.get().load(imageuri).into(mimage);
        }
    }
}
