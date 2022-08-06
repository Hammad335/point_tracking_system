package com.codewithhamad.muetbustracker.models;

public class User {
    private String userName, email, password, androidId;
    boolean permissionGranted;

    public User() {
    }

//    public User(String userName, String email, String password) {
//        this.userName = userName;
//        this.email = email;
//        this.password = password;
//    }

//    public User(String userName, String email, String password, String androidId) {
//        this.userName = userName;
//        this.email = email;
//        this.password = password;
//        this.androidId = androidId;
//        permissionGranted= false;
//    }

    public User(String userName, String email, String password, String androidId, boolean permissionGranted) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.androidId = androidId;
        this.permissionGranted= permissionGranted;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public boolean isPermissionGranted() {
        return permissionGranted;
    }

    public void setPermissionGranted(boolean permissionGranted) {
        this.permissionGranted = permissionGranted;
    }
}
