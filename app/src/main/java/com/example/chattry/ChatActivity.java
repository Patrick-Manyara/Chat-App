package com.example.chattry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import adapters.AdapterChat;
import models.ModelChat;


public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileTv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn;

    //for checking is the usr has seen
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;
    List<ModelChat> chatList;
    AdapterChat adapterChat;

    String hisUid;
    String myUid;
    String hisImage;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");


        recyclerView=findViewById(R.id.chat_recyclerView);
        profileTv=findViewById(R.id.profileTv);
        nameTv=findViewById(R.id.nameTv);
        userStatusTv=findViewById(R.id.userStatusTv);
        messageEt=findViewById(R.id.messageEt);
        sendBtn=findViewById(R.id.sendBtn);

        Intent intent=getIntent();
        hisUid = intent.getStringExtra("hisUid");

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbRef=firebaseDatabase.getReference("Users");

        //LayOut for Recycler
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //search user to get his info
        Query userQuery=userDbRef.orderByChild("uid").equalTo(hisUid);
        //get his pic and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                    hisImage=""+ds.child("image").getValue();

                    String onlineStatus=""+ds.child("onlineStatus").getValue();
                    if (onlineStatus.equals("online")){
                        userStatusTv.setText(onlineStatus);
                    }
                    else {
                        Calendar cal=Calendar.getInstance(Locale.ENGLISH);
                        cal.setTimeInMillis(Long.parseLong(onlineStatus));
                        String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                        userStatusTv.setText("Last seen at: " +dateTime);
                        

                    }



                    nameTv.setText(name);
                    try{
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_white_face).into(profileTv);

                    } catch (Exception e){
                        Picasso.get().load(R.drawable.ic_white_face).into(profileTv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEt.getText().toString().trim();
                //check if text is empty
                if(TextUtils.isEmpty(message)){

                    //is empty
                    Toast.makeText(ChatActivity.this, "Cannot send empty message",Toast.LENGTH_SHORT).show();

                }
                else {
                    //text not empty
                    sendMessage(message);
                }


            }
        });

        readMessages();
        
        seenMessage();
        


        //onclick user from users list, pass uid using intent
        //so get that uid here to get the profile pic,name and start chat








    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelChat chat=ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)){
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)  ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }

                    adapterChat=new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterChat);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp=String.valueOf(System.currentTimeMillis());

        HashMap<String,Object> hashMap =new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        databaseReference.child("Chats").push().setValue(hashMap);

        messageEt.setText("");
    }

    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user != null){
            myUid = user.getUid();

        }

        else {
            Intent myintent=new Intent(this, MainActivity.class);
            startActivity(myintent);
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //hide search view
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
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

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference().child(myUid);
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("onlineStatus", status);
        dbRef.updateChildren(hashMap);
    }



    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        String timestamp=String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);

        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }
}
