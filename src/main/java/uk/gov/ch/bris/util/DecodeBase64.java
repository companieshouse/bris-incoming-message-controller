package uk.gov.ch.bris.util;

import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.StructuredLogger;



public class DecodeBase64 {

    private final static Logger log = LoggerFactory.getLogger();
    
	public static String decodeInput(String encodedString) {
	    ((StructuredLogger) log).setNamespace("bris.incoming.controller");
	    
		// Get bytes from string
		byte[] byteArray = Base64.decodeBase64(encodedString.getBytes());
		
		// Print the decoded string
		String decodedString = new String(byteArray);
		
		// Print the decoded String
		log.debug(encodedString + " = " + encodedString, new HashMap<String, Object>());
		log.debug(decodedString + " = " + decodedString, new HashMap<String, Object>());
		
		return decodedString;
	}


}

