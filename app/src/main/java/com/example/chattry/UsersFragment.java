package com.example.chattry;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import adapters.AdapterUsers;
import models.ModelUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUser> userList;
    FirebaseAuth firebaseAuth;

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_users, container, false);

        firebaseAuth=FirebaseAuth.getInstance();


        recyclerView = view.findViewById(R.id.users_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        userList=new ArrayList<>();

        //getAllusers
        getAllUsers();

        return view;
    }

    private void getAllUsers() {
        //get Currnt User
        final FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();

        //get path of DB named Users
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");

        //get all data
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();

                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelUser modelUser=ds.getValue(ModelUser.class);

                    //get all users except for signed in
                    if (!modelUser.getUid().equals(fUser.getUid())){
                        userList.add(modelUser);
                    }

                    adapterUsers=new AdapterUsers(getActivity(),userList);
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUsers(final String query) {
        final FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();

        //get path of DB named Users
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");

        //get all data
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();

                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelUser modelUser=ds.getValue(ModelUser.class);

                    //conditions to fulfil search
                    //1)user not current user
                    //2)usr name/email contains text entered in SearchView(not case sensitive)

                    //get all searched users except for signed in
                    if (!modelUser.getUid().equals(fUser.getUid())){

                        if(modelUser.getName().toLowerCase().contains(query.toLowerCase()) || modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            userList.add(modelUser);
                        }

                    }

                    adapterUsers=new AdapterUsers(getActivity(),userList);

                    //refresh adapter
                    adapterUsers.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                //called when user presses search
                //if search query is not empty
                if(!TextUtils.isEmpty(s.trim())){
                    //search text contains tex, search it
                    searchUsers(s);
                }
                else {
                    //seatrch text empty, get all users
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //if search query is not empty
                if(!TextUtils.isEmpty(s.trim())){
                    //search text contains tex, search it
                    searchUsers(s);
                }
                else {
                    //seatrch text empty, get all users
                    getAllUsers();
                }
                return false;
            }
        });

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

        else if (id==R.id.action_settings){
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
