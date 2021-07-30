package com.example.demo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.log4j.Logger;

public class TagObjectMapperWrapper extends ObjectMapper{


    private static final long serialVersionUID = 1L;

    private final static Logger LOG = Logger.getLogger(TagObjectMapperWrapper.class);

    private static TagObjectMapperWrapper _tagObjectMapperWrapper;

    private TagObjectMapperWrapper() {}

    public static TagObjectMapperWrapper getInstance()
    {
        if(_tagObjectMapperWrapper == null)
            _tagObjectMapperWrapper = new TagObjectMapperWrapper();
        return _tagObjectMapperWrapper;
    }

    public String toString(Object object)
    {
        try {
            return writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOG.error(e);
            return null;
        }
    }

}