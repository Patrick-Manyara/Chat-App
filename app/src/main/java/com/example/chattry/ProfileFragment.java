package com.example.chattry;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    Uri image_uri;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String profileOrCoverPhoto;
    //storage
    StorageReference storageReference;

    //path where imags of user profile and covr will be stored
    String storagPath="Users_Profile_Cover_Imgs/";



    ImageView avatarTv, coverIv;
    TextView nameTv, emailTv, phoneTv;

    FloatingActionButton fab;
    ProgressDialog pd;

    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_GALLERY_CODE=300;
    private static final int IMAGE_PICK_CAMERA_CODE=400;

    String cameraPermissions[];
    String storagePermissions[];

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference= firebaseDatabase.getReference("Users");
        storageReference=getInstance().getReference();

        //initializing arrays of permissions
        cameraPermissions=new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        //uri of picked image



        avatarTv=view.findViewById(R.id.avatarIv);
        nameTv=view.findViewById(R.id.nameTv);
        emailTv=view.findViewById(R.id.emailTv);
        phoneTv=view.findViewById(R.id.phoneTv);
        coverIv=view.findViewById(R.id.coverIv);
        fab=view.findViewById(R.id.fab);

        pd = new ProgressDialog(getActivity());

        /* Getting info on currently signed in user using email. by using orderByChild Query
        * we can show the detail from a node whos key named email has value equal to the current user
        * it will search eevry node and match details when it finds the correct one */

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until the required data is found
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    String name= ""+ds.child("name").getValue();
                    String email= ""+ds.child("email").getValue();
                    String phone= ""+ds.child("phone").getValue();
                    String image= ""+ds.child("image").getValue();
                    String cover= ""+ds.child("cover").getValue();

                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        Picasso.get().load(image).into(avatarTv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_add_photo).into(avatarTv);
                    }

                    try {
                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e){

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });


        return view;
    }

    private boolean checkStoragePermission(){
        //CHECK IF STORAGE PERMISSION IS ENABLED

        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);

    }


    private boolean checkCameraPermission(){
        //CHECK IF STORAGE PERMISSION IS ENABLED

        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);

        boolean result1= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);

    }





    private void showEditProfileDialog() {
        //Options shown in Dialog
        String options []={"Edit Profile Picture","Edit Cover Photo","Edit Name","Edit Phone"};

        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    //Edit Profile Pic
                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto="image";
                    showImagePicDialog();

                }
                else if (which==1){
                    //Edit Cover
                    pd.setMessage("Updating Cover Photo");
                    profileOrCoverPhoto="cover";
                    showImagePicDialog();

                }
                else if (which==2){
                    //Edit Name
                    pd.setMessage("Updating Name");
                    showNamePhoneUpdateDialog("name");
                }
                else if (which==3){
                    //Edit Phone
                    pd.setMessage("Updating Phone Number");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });

        builder.create().show();

    }

    private void showNamePhoneUpdateDialog(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key);
        LinearLayout linearLayout=new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        final EditText editText=new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add buttons
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

                }
                else {
                    Toast.makeText(getActivity(), "Please Enter "+key, Toast.LENGTH_SHORT).show();

                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        //create and show dialog
        builder.create().show();
    }

    private void showImagePicDialog() {
        //show dialog containing options camera and gallery
        String options []={"Camera","Gallery"};

        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    //Camera
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }


                }
                else if (which==1){
                    //Gallery
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }

                }


            }
        });

        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                        //enabled
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(getActivity(),"Enable Permissions",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            break;



            case STORAGE_REQUEST_CODE:{

                if (grantResults.length>0){
                    boolean writeStorageAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){
                        //enabled
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(getActivity(),"Enable Permissions",Toast.LENGTH_SHORT).show();
                    }
                }

            }

            break;
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        
        if(requestCode==IMAGE_PICK_GALLERY_CODE){
            image_uri=data.getData();
            uploadProfileCoverPhoto(image_uri);
        }
        
        if(requestCode==IMAGE_PICK_CAMERA_CODE){
            uploadProfileCoverPhoto(image_uri);
        }
        
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void uploadProfileCoverPhoto(final Uri uri) {
        pd.show();

        String filePathAndName=storagPath+ ""+profileOrCoverPhoto +"_"+ user.getUid();

        StorageReference storageReference2nd=storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();

                if (uriTask.isSuccessful()){
                    HashMap<String, Object> results = new HashMap<>();

                    results.put(profileOrCoverPhoto, downloadUri.toString());

                    databaseReference.child(user.getUid()).updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Error...", Toast.LENGTH_SHORT).show();


                        }
                    });
                }

                else {
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Some error occurred", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });




    }

    private void pickFromGallery() {
        //PICK FROM GALLERY
        Intent galleryIntent=new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);


    }

    private void pickFromCamera() {
        //intent of picking image from camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        //put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user != null){


        }

        else {
            Intent myintent=new Intent(getActivity(), MainActivity.class);
            startActivity(myintent);
            getActivity().finish();
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }



        return super.onOptionsItemSelected(item);
    }
}
