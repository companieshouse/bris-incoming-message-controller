package uk.gov.ch.bris.util;

import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;



public class DecodeBase64 {

    private final static Logger LOGGER = LoggerFactory.getLogger();
    
	public static String decodeInput(String encodedString) {
	    
	    // Get bytes from string
		byte[] byteArray = Base64.decodeBase64(encodedString.getBytes());
		
		// Print the decoded string
		String decodedString = new String(byteArray);
		
		// Print the decoded String
		LOGGER.debug(encodedString + " = " + encodedString, new HashMap<String, Object>());
		LOGGER.debug(decodedString + " = " + decodedString, new HashMap<String, Object>());
		
		return decodedString;
	}


}

