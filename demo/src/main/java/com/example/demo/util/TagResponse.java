package com.example.demo.util;

public class TagResponse {

    public String message="";
    public Object errorList;
    //public int code;


    public TagResponse() {
    }

    public TagResponse(String message)
    {
        this.message = message;

    }

    @Override
    public String toString() {
        return "TagResponse [message=" + message + ", errorList=" + errorList + "]";
    }
}