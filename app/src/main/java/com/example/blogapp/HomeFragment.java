package com.example.blogapp;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private RecyclerView blog_list_view;
    private FloatingActionButton btn_floatingAction;
    private DatabaseReference postsRef, usersRef;
    private FirebaseAuth mAuth;
    String user_id;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_home, container, false);
        blog_list_view = (RecyclerView) mView.findViewById(R.id.blog_list_view);

        //blog_list_view.setLayoutManager(new LinearLayoutManager(getContext()));
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        // Set the layout manager to your recyclerview
        blog_list_view.setLayoutManager(mLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid().toString();
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        postsRef.keepSynced(true);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        btn_floatingAction = (FloatingActionButton) mView.findViewById(R.id.btn_floatingAction);
        btn_floatingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("btn", "onClick: floating clicked");
                newPost();
            }
        });

        // Inflate the layout for this fragment
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("post_id", "on start ran");

        FirebaseRecyclerAdapter<BlogPost, PostsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<BlogPost, PostsViewHolder>
                (
                        BlogPost.class,
                        R.layout.blog_list_item,
                        PostsViewHolder.class,
                        postsRef
                ) {

            @Override
            protected void populateViewHolder(final PostsViewHolder viewHolder, BlogPost model, int position) {
                Log.d("post_id", "populate view holder ran");
                final String post_id = getRef(position).getKey();
                Log.d("post_id", post_id);
                postsRef.child(post_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        //post_user_id, post_desc, post_image_url, post_timestamp;
                        String post_user_name = dataSnapshot.child("post_user_name").getValue().toString();
                        String post_user_image_url = dataSnapshot.child("post_user_image_url").getValue().toString();
                        String post_desc = dataSnapshot.child("post_desc").getValue().toString();
                        String post_image_url = dataSnapshot.child("post_image_url").getValue().toString();
                        String post_timestamp = dataSnapshot.child("post_timestamp").getValue().toString();

                        viewHolder.setName(post_user_name);
                        viewHolder.setUserImage(getContext(), post_user_image_url);
                        viewHolder.setPostDesc(post_desc);
                        viewHolder.setImage(getContext(), post_image_url);
                        viewHolder.setDate(post_timestamp);


                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Error Retreiving Post from Firebase", Toast.LENGTH_SHORT).show();
                    }
                });


                postsRef.child(post_id).child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)){
                            viewHolder.blog_like.setBackgroundResource(R.mipmap.ic_like);
                            // avoiding another listener for this lil work
                            long count  = dataSnapshot.getChildrenCount();
                            viewHolder.blog_Likes.setText(count+" Likes");
                        }else{
                            viewHolder.blog_like.setBackgroundResource(R.mipmap.ic_unlike);
                            // avoiding another listener for this lil work
                            long count  = dataSnapshot.getChildrenCount();
                            viewHolder.blog_Likes.setText(count+" Likes");

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onBindViewHolder(final PostsViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                final String post_id = getRef(position).getKey();
                viewHolder.blog_like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {

                        postsRef.child(post_id).child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.hasChild(user_id)){

                                    postsRef.child(post_id).child("Likes").child(user_id).child("timestamp").setValue(getTimestamp())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){


                                                        Toast.makeText(getContext(), "Post Liked", Toast.LENGTH_SHORT).show();

                                                    }else{
                                                        Toast.makeText(getContext(), "Error liking", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }else{
                                    postsRef.child(post_id).child("Likes").child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {


                                            Toast.makeText(getContext(), "Post Unliked", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getContext(), "Unable to Like", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        };
        blog_list_view.setAdapter(firebaseRecyclerAdapter);

    }

   /* private String getUserName(String post_user_id) {
        usersRef.child(post_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user_name = dataSnapshot.child("user_name").getValue().toString();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error Retreiving User Name", Toast.LENGTH_SHORT).show();
            }
        });
        return user_name;
    } */

    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageView blog_like;
        TextView blog_Likes;

        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            blog_like  = (ImageView) mView.findViewById(R.id.blog_like);
            blog_Likes = (TextView) mView.findViewById(R.id.blog_likes);

        }



        public void setName(String userName) {
            TextView textView = (TextView) mView.findViewById(R.id.blog_username);
            textView.setText(userName);
        }

        public void setPostDesc(String postDesc) {
            TextView textView = (TextView) mView.findViewById(R.id.blog_desc);
            textView.setText(postDesc);
        }

        public void setImage(final Context context, final String post_image_url) {
            final ImageView imageView = (ImageView) mView.findViewById(R.id.blog_image);

            Picasso.with(context).load(post_image_url).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.blog_image_placeholder).into(imageView, new Callback() {
                @Override
                public void onSuccess() { }
                @Override
                public void onError() {
                    Picasso.with(context).load(post_image_url).placeholder(R.drawable.blog_image_placeholder).into(imageView);
                }
            });
        }

        public void setDate(String date) {
            TextView textView = (TextView) mView.findViewById(R.id.blog_date);
            textView.setText(date);
        }

        public void setUserImage(final Context context, final String post_user_image_url) {
            final ImageView imageView = (ImageView) mView.findViewById(R.id.blog_user_image);

            Picasso.with(context).load(post_user_image_url).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.circular).into(imageView, new Callback() {
                @Override
                public void onSuccess() { }
                @Override
                public void onError() {
                    Picasso.with(context).load(post_user_image_url).placeholder(R.drawable.circular).into(imageView);
                }
            });
        }
    }


    private void newPost() {
        Intent addPostIntent = new Intent(getActivity(), AddPostActivity.class);
        startActivity(addPostIntent);
    }

    public Object getTimestamp() {
        String timestamp = null;
        try {
            String dayOfTheWeek = (String) DateFormat.format("EEEE", new Date()); // Thursday
            String currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(Calendar.MINUTE);
            timestamp = dayOfTheWeek.substring(0, 3).toUpperCase() + ", " + currentTime;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return (Object) timestamp;
    }
}
