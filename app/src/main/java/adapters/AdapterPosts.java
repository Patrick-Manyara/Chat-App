package adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chattry.AddPostActivity;
import com.example.chattry.PostDetailActivity;
import com.example.chattry.R;
import com.example.chattry.TheirProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import models.ModelPost;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {

    Context context;
    List<ModelPost> postList;

    String myUid;

    private DatabaseReference likesRef;
    private DatabaseReference postsRef;

    boolean mProcessLike=false;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        //get data
        final String uid=postList.get(i).getUid();
        String uEmail=postList.get(i).getuEmail();
        String uName=postList.get(i).getuName();
        String uDp=postList.get(i).getuDp();
        final String pId=postList.get(i).getpId();
        final String pTitle=postList.get(i).getpTitle();
        final String pDescription=postList.get(i).getpDescr();
        final String pImage=postList.get(i).getpImage();
        String pTimeStamp=postList.get(i).getpTime();
        String pLikes=postList.get(i).getpLikes();
        String pComments=postList.get(i).getpComments();

        Calendar calendar= Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        final String pTime= DateFormat.format("dd/MM/yyyy hh:mm:aa",calendar).toString();



        //set data
        myHolder.uNameTv.setText(uName);
        myHolder.pTimeTv.setText(pTime);
        myHolder.pTitleTv.setText(pTitle);
        myHolder.pDescriptionTv.setText(pDescription);
        myHolder.pLikesTv.setText(pLikes + " Likes");
        myHolder.pCommentsTv.setText(pComments + " Comments");

        setLikes(myHolder, pId);



        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_face_def).into(myHolder.uPictureIv);

        }
        catch (Exception e){

        }

        if (pImage.equals("noImage")){
            myHolder.pImageIv.setVisibility(View.GONE);

        }
        else {
            myHolder.pImageIv.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(pImage).into(myHolder.pImageIv);

            }
            catch (Exception e){

            }
        }



        myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                showMoreOptions(myHolder.moreBtn, uid, myUid, pId, pImage);
            }
        });
        myHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get total number of likes
                //if current user hasnt liked it before
                //increase value by 1, otherwise decrease by 1
                final int pLikes = Integer.parseInt(postList.get(i).getpLikes());
                mProcessLike = true;

                final String postIde=postList.get(i).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mProcessLike){
                            if (dataSnapshot.child(postIde).hasChild(myUid)){
                                postsRef.child(postIde).child("pLikes").setValue("" +(pLikes-1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike= false;
                            }
                            else {
                                postsRef.child(postIde).child("pLikes").setValue("" +(pLikes+1));
                                likesRef.child(postIde).child(myUid).setValue("Liked");
                                mProcessLike= false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        myHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);


            }
        });
        myHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable)myHolder.pImageIv.getDrawable();
                if (bitmapDrawable == null){
                    shareTextOnly(pTitle, pDescription);
                }
                else {
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle,pDescription, bitmap);
                }
            }
        });

        myHolder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TheirProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });


    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        //concatenate title and description to share
        String shareBody = pTitle + "\n" + pDescription;

        //first we will save the image in cache
        Uri uri = saveImageToShare(bitmap);

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));
    }


    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.example.chattry.fileprovider", file);


        }
        catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return  uri;

    }


    private void shareTextOnly(String pTitle, String pDescription) {
        //concatenate title and description to share
        String shareBody = pTitle + "\n" + pDescription;

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));
    }


    private void setLikes(final MyHolder holder, final String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postKey).hasChild(myUid)){
                    //user has liked this post
                    //to indicate that the post is liked by the signed in user, change the liked icon's color and text
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_blue, 0, 0, 0);
                    holder.likeBtn.setText("Liked");
                }
                else {
                    //post not liked
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option in only posts of currently signed in user
        if (uid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE, 0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE, 1,0,"Edit");
        }

        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Detail");


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if (id==0){
                    //delete is clicked
                    beginDelete(pId,pImage);
                }

                else if (id==1){
                    //edit is clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }

                else if (id==2){

                    //start post detail is clicked
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId);
                    context.startActivity(intent);
                }



                return false;
            }
        });

        popupMenu.show();
    }


    private void beginDelete(String pId, String pImage) {
        //posts can be wth or without image
        if (pImage.equals("noImage")){
            deleteWithoutImage(pId);
        }
        else {
            deleteWithImage(pId,pImage);
        }
    }


    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void deleteWithImage(final String pId, String pImage) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        //delete using url
        //delete from db using posts id

        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //image deleted, now delete database
                Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                fquery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //cant progress
                pd.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{

        ImageView uPictureIv, pImageIv;
        ImageButton moreBtn;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
        Button likeBtn, commentBtn, shareBtn;
        LinearLayout profileLayout;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            uPictureIv= itemView.findViewById(R.id.uPictureIv);
            pImageIv= itemView.findViewById(R.id.pImageIv);
            moreBtn= itemView.findViewById(R.id.moreBtn);
            uNameTv= itemView.findViewById(R.id.uNameTv);
            pTimeTv= itemView.findViewById(R.id.uTimeTv);
            pTitleTv= itemView.findViewById(R.id.pTitleTv);
            pCommentsTv= itemView.findViewById(R.id.pCommentsTv);
            pDescriptionTv= itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv= itemView.findViewById(R.id.pLikesTv);
            likeBtn= itemView.findViewById(R.id.likeBtn);
            commentBtn= itemView.findViewById(R.id.commentBtn);
            shareBtn= itemView.findViewById(R.id.shareBtn);
            profileLayout= itemView.findViewById(R.id.profileLayout);



        }
    }
}
