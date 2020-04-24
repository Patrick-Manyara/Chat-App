package adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chattry.ChatActivity;
import com.example.chattry.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import models.ModelUser;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;
    List<ModelUser> userList;

    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        //inflate layout(row user.xml

        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);


        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        String userImage=userList.get(i).getImage();
        String userName=userList.get(i).getName();

        final String hisUid=userList.get(i).getUid();

        final String userEmail=userList.get(i).getEmail();

        myHolder.mNameTv.setText(userName);
        myHolder.mEmailTv.setText(userEmail);
        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_face_def).into(myHolder.mAvatarTv);
        }
        catch (Exception e){}

        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //use user id to identify intnded chat
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mAvatarTv;
        TextView mNameTv, mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);


            mAvatarTv=itemView.findViewById(R.id.avatarIv);
            mNameTv=itemView.findViewById(R.id.nameTv);
            mEmailTv=itemView.findViewById(R.id.emailTv);



        }
    }
}
