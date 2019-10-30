package uk.gov.ch.bris.domain;

import java.net.URL;

/**
 * Created by rkumar on 21/04/2017.
 */
public class BrisMessageType {

    private URL url;
    private String className;
    private BrisMessageHeaderType messageHeader;
    private String validationXML;
    // XML string of the message content - used when content in MessageContainer
    private String contentString;

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

    public BrisMessageHeaderType getMessageHeader() {
        return messageHeader;
    }
    
    public void setMessageHeader(BrisMessageHeaderType messageHeader) {
        this.messageHeader = messageHeader;
    }

    public String getValidationXML() {
        return validationXML;
    }

    public void setValidationXML(String validationXML) {
        this.validationXML = validationXML;
    }
    
    public String getContentString() {
        return contentString;
    }
    
    public void setContentString(String contentString) {
        this.contentString = contentString;
    }

    @Override
    public String toString() {
        return "BrisMessageType{" +
                "url=" + url +
                ", className='" + className + '\'' +
                ", messageHeader=" + messageHeader +
                ", validationXML='" + validationXML + '\'' +
                ", contentString='" + contentString + '\'' +
                '}';
    }
}
