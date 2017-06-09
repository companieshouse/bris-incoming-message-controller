package uk.gov.ch.bris.domain;

import java.net.URL;

/**
 * Created by rkumar on 21/04/2017.
 */
public class BrisMessageType {

    private URL url;
    private String className;

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

    @Override
    public String toString() {
        return "BrisMessageType{" +
                "url=" + url +
                ", className='" + className + '\'' +
                '}';
    }
}
