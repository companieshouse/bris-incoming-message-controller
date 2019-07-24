package uk.gov.ch.bris.domain;

import javax.xml.bind.annotation.XmlRootElement;

import uk.gov.ch.bris.error.ErrorCode;

@XmlRootElement
public class ValidationError {

    private BrisMessageHeaderType header;
    private ErrorCode errorCode;
    
	public BrisMessageHeaderType getHeader() {
        return header;
    }

    public void setHeader(BrisMessageHeaderType header) {
        this.header = header;
    }

    public ErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

}