package com.clinton.adrreport.utils;

public class User {
    public String username;
    public String token;
    public String fullName;
    public String role;
    public String _id;

    @Override
    public String toString() {
        return "ID: "+ this._id + " Token: " + this.token + " FullName: " + this.fullName + " Email: " + this.username;
    }
}