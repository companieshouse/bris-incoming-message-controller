package uk.gov.ch.bris.util;

import org.apache.commons.codec.binary.Base64;

public class DecodeBase64 {

    public static String decodeInput(String encodedString) {
        
        // Get bytes from string
        byte[] byteArray = Base64.decodeBase64(encodedString.getBytes());
        
        // Print the decoded string
        String decodedString = new String(byteArray);
        
        // Print the decoded String
        System.out.println(encodedString + " = ");
        
        return decodedString;
    }

    /*
    <payload payloadId="cid:message">
        <?xml version="1.0" encoding="UTF-8"?>
        <hello>world</hello>
    </payload> 
     */
}

