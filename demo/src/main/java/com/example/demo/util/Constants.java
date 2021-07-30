package com.example.demo.util;

public class Constants {

    // ====================== MESSAGES =============================
    public static final String MSG_SUCCESS = "Success";
    public static final String MSG_DELETED = "Deleted";
    public static final String MSG_UPDATED = "Updated";
    public static final String MSG_FAILED = "Failed";
    public static final String MSG_BADREQUEST = "Bad request";
    public static final String MSG_INVALID_ID = "Invalid id";
    public static final String MSG_INVALID_PATH = "Invalid Path";
    public static final String EXPORT_SUCCESS = "Successfully exported!";

    public static final String ROOT_PATH_TEMP="/tmp/";


    // ===================== FILE EXTENSION
    // ======================================
    public static final String FILE_EXT_CSV = "csv";
    public static final String FILE_EXT_EXCEL = "xlsx";
    public static final String FILE_EXT_JPG = "image/jpg";
    public static final String FILE_EXT_JPEG = "image/jpeg";
    public static final String FILE_EXT_PNG = "image/png";
    public static final String FILE_EXT_PDF = "pdf";


    public static final String ERR_EMPTY_COLUMN = "Empty Column found";
    public static final String ERR_EMPTY_COLUMN_HEADER = "Empty Column Headers found";
    public static final String ERR_INVALID_COLUMN = "Invalid columns name";


    public static final String REQUIRED_FIELD_MISSING = "Required field \"%s\" is missing to map";
    public static final String EMPTY_OR_NULL_DATA = "Empty or null at row %d in column \"%s\"";
    public static final String IMP_FILEMAP_NULL_OR_INVALID = "File key null or invalid";

    enum CELL_FORMAT {
        CURRENCY,
        NUMBER,
        DATE,
        CUSTOM,
        PERCENTAGE,
        DECIMAL_NUMBER
    }
}
