package uk.gov.ch.bris.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecodeBase64 {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecodeBase64.class);
    
	public static String decodeInput(String encodedString) {
		
		// Get bytes from string
		byte[] byteArray = Base64.decodeBase64(encodedString.getBytes());
		
		// Print the decoded string
		String decodedString = new String(byteArray);
		
		// Print the decoded String
		LOGGER.info(encodedString + " = ");
		
		return decodedString;
	}


}

