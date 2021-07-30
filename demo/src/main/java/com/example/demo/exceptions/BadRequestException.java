package com.example.demo.exceptions;

import java.util.Map;

public class BadRequestException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    private String message = null;

    public BadRequestException() {
        super();
    }

    private  Map<String,String>  errorList;

    public BadRequestException(String message,  Map<String,String>  list) {
        super(message);
        this.message=message;
        this.errorList=list;
    }
    public BadRequestException(String message){
        super(message);
        this.message = message;
    }

    public BadRequestException(Throwable cause) {
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
