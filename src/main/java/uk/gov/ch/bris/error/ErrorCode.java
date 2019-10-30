package uk.gov.ch.bris.error;

import uk.gov.ch.bris.constants.ErrorCodeMessages;

/**
 * Created by rkumar on 22/03/2017.
 */
public enum ErrorCode {


	/* ---- Constants ---- */

    /**
     * List of error codes as per
     * published on the BRIS collaboration space.
     */
    NO_ERROR ("NO Error"),
    ERR_BR_0100 (ErrorCodeMessages.INVALID_MESSAGE),
    ERR_BR_0101 (ErrorCodeMessages.INVALID_MESSAGE),
    ERR_BR_0102 (ErrorCodeMessages.INVALID_MESSAGE),
    ERR_BR_0103 (ErrorCodeMessages.INVALID_MESSAGE),
    ERR_BR_0104 (ErrorCodeMessages.INVALID_MESSAGE),

    ERR_BR_0300 ("Invalid Company Details message"),
    ERR_BR_0500 ("Invalid Cross Border Merger message"),
    ERR_BR_0501 (ErrorCodeMessages.INVALID_NOTIFICATION),
    ERR_BR_0502 (ErrorCodeMessages.INVALID_NOTIFICATION),
    ERR_BR_0503 (ErrorCodeMessages.INVALID_NOTIFICATION),
    ERR_BR_0504 (ErrorCodeMessages.INVALID_NOTIFICATION), 
    ERR_BR_0600 ("Invalid Branch Disclosure message"),
    ERR_BR_0601 (ErrorCodeMessages.INVALID_NOTIFICATION),
    ERR_BR_0602 (ErrorCodeMessages.INVALID_NOTIFICATION),
    ERR_BR_0603 (ErrorCodeMessages.INVALID_NOTIFICATION),
    ERR_BR_0700 ("Invalid Update LED message"),
    ERR_BR_0701 (ErrorCodeMessages.INVALID_MESSAGE),
    ERR_BR_0800 ("Invalid Manage Subscription message"),
    ERR_BR_0801 (ErrorCodeMessages.INVALID_MESSAGE),
    ERR_BR_3000 (ErrorCodeMessages.MESSAGE_PROCESSING_ERROR),
    ERR_BR_3001 (ErrorCodeMessages.MESSAGE_PROCESSING_ERROR),
    ERR_BR_3002 (ErrorCodeMessages.MESSAGE_PROCESSING_ERROR),

    ERR_BR_5100 ("Message validation error"),
    ERR_BR_5101 (ErrorCodeMessages.MESSAGE_VALIDATION_FAILED),
    ERR_BR_5102 (ErrorCodeMessages.MESSAGE_VALIDATION_FAILED),
    ERR_BR_5103 (ErrorCodeMessages.MESSAGE_VALIDATION_FAILED),
    ERR_BR_5104 (ErrorCodeMessages.MESSAGE_VALIDATION_FAILED),
    ERR_BR_5105 (ErrorCodeMessages.MESSAGE_VALIDATION_FAILED),
    ERR_BR_5106 (ErrorCodeMessages.MESSAGE_VALIDATION_FAILED),
    ERR_BR_5107 (ErrorCodeMessages.MESSAGE_VALIDATION_FAILED),
    ERR_BR_5108 (ErrorCodeMessages.MESSAGE_VALIDATION_FAILED),

    ERR_BR_5200 (ErrorCodeMessages.MESSAGE_PROCESSING_ERROR),
    ERR_BR_5201 ("Message processing failed"),

    ERR_BR_0901 (ErrorCodeMessages.INVALID_NOTIFICATION);



	/* ---- Instance Variables ---- */

    private String description = null;

	/* ---- Constructors ---- */

    private ErrorCode(String description) {
        this.description = description;
    }

	/* ---- Business Methods ---- */

	/* ---- Getters and Setters ---- */

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
