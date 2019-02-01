package com.example.blogapp;

/**
 * Created by hamza on 6/17/2018.
 */

public class BlogPost {
    private String post_user_name, post_user_image_url, post_desc, post_image_url, post_timestamp;

    public BlogPost() {
    }

    public BlogPost(String post_user_name, String post_user_image_url, String post_desc, String post_image_url, String post_timestamp) {
        this.post_user_name = post_user_name;
        this.post_user_image_url = post_user_image_url;
        this.post_desc = post_desc;
        this.post_image_url = post_image_url;
        this.post_timestamp = post_timestamp;
    }

    public String getPost_user_name() {
        return post_user_name;
    }

    public void setPost_user_name(String post_user_name) {
        this.post_user_name = post_user_name;
    }

    public String getPost_user_image_url() {
        return post_user_image_url;
    }

    public void setPost_user_image_url(String post_user_image_url) {
        this.post_user_image_url = post_user_image_url;
    }

    public String getPost_desc() {
        return post_desc;
    }

    public void setPost_desc(String post_desc) {
        this.post_desc = post_desc;
    }

    public String getPost_image_url() {
        return post_image_url;
    }

    public void setPost_image_url(String post_image_url) {
        this.post_image_url = post_image_url;
    }

    public String getPost_timestamp() {
        return post_timestamp;
    }

    public void setPost_timestamp(String post_timestamp) {
        this.post_timestamp = post_timestamp;
    }
}
