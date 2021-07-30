package com.example.demo.exceptions;

import java.util.Map;


public class RecordNotFoundException extends Exception{

    private static final long serialVersionUID = 1L;

    private String message = null;

    private Map<String,String> errorList;

    public RecordNotFoundException(String message,  Map<String,String>  list) {
        super(message);
        this.message=message;
        this.errorList=list;
    }
    public RecordNotFoundException() {
        super();
        this.message = "Record not found";
    }

    public RecordNotFoundException(String message){
        super(message);
        this.message = message;
    }

    public RecordNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Map<String,String>  getErrorList() {
        return errorList;
    }

    public void setErrorList(Map<String,String>  errorList) {
        this.errorList = errorList;
    }
}