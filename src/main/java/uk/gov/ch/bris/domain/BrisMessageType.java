package uk.gov.ch.bris.domain;

import java.net.URL;

import eu.europa.ec.bris.v140.jaxb.br.aggregate.MessageObjectType;

/**
 * Created by rkumar on 21/04/2017.
 */
public class BrisMessageType {

    private URL url;
    private String className;
    private MessageObjectType messageObjectType;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public MessageObjectType getMessageObjectType() {
        return messageObjectType;
    }

    public void setMessageObjectType(MessageObjectType messageObjectType) {
        this.messageObjectType = messageObjectType;
    }


    @Override
    public String toString() {
        return "BrisMessageType{" +
                "url=" + url +
                ", className='" + className + '\'' +
                '}';
    }


}
