package com.uniware.integrations.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "asyncExecute")
public class AsyncExecute {
    @JacksonXmlProperty(isAttribute = true)
    private String name;
    @JacksonXmlProperty(isAttribute = true)
    private String condition;
    @JacksonXmlProperty(isAttribute = true)
    private String delayInSeconds;
    @JacksonXmlProperty(isAttribute = true)
    private String retryCount;
    @JacksonXmlProperty
    private Param param;
    @JacksonXmlProperty
    @JacksonXmlElementWrapper(useWrapping = true)
    private Param instructions;

    @JacksonXmlRootElement(localName = "param")
    public static class Param{
        @JacksonXmlProperty(isAttribute = true)
        private String name;
        @JacksonXmlProperty(isAttribute = true)
        private String value;
    }
}
