package uk.gov.ch.bris.api.error;

/**
 * Created by rkumar on 29/03/2017.
 */

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import uk.gov.ch.bris.error.ErrorCode;

public class BusinessError implements Serializable {

    /* ---- Constants ---- */
    public static final long serialVersionUID = 201508141449L;

  /* ---- Instance Variables ---- */

    private ErrorCode errorCode = null;

  /* ---- Constructors ---- */

    public BusinessError(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

  /* ---- Business Methods ---- */

    public String toString() {
        return new ToStringBuilder(this).append("errorCode", errorCode.toString()).toString();
    }

  /* ---- Getters and Setters ---- */

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
