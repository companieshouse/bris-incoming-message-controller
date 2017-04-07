package uk.gov.ch.bris.error;

/**
 * Created by rkumar on 22/03/2017.
 */
public enum ErrorCode {


	/* ---- Constants ---- */

    /**
     * List of error codes as per
     * published on the BRIS collaboration space.
     */
    ERR_BR_0100 ("Business rule violation, message not accepted"),
    ERR_BR_0101 ("Invalid EUID,Business rule violation, message not accepted"),
    ERR_BR_0102 ("Invalid address value, message not accepted"),
    ERR_BR_0103 ("Invalid pair ([code, country]) , message not accepted"),
    ERR_BR_0104 ("Invalid payment reference (reserved for validation of the payment reference), message not accepted"),

    ERR_BR_0300 ("Business rule violation, company details message not accepted"),
    ERR_BR_0500 ("Business rule violation, CBM notification not accepted"),
    ERR_BR_0600 ("Business rule violation, BD notification not accepted"),
    ERR_BR_0700 ("Business rule violation, update LED message not accepted"),
    ERR_BR_0800 ("Business rule violation, subscription request not accepted"),
    ERR_BR_3000 ("Business rule violation, company details not accepted"),

    ERR_BR_5100 ("The validation of the message header coming from the ECP failed"),
    ERR_BR_5101 ("The BR is not authorised to receive this type of messages "),
    ERR_BR_5102 ("Message schema validation error ,The validation of the message failed"),
    ERR_BR_5103 ("Message uniqueness error. Message ID or Correlation ID not unique (or message ID NOT equal to correlation ID for request messages),The validation of the message failed"),
    ERR_BR_5104 ("Response message correlation error. Response message not correlated to any request.,Response message correlation error. Response message not correlated to any request.,The validation of the message failed"),
    ERR_BR_5105 ("Response/Request compliance error,The validation of the message failed"),
    ERR_BR_5106 ("BR message can not be deserialised to correct message type. ,The validation of the message failed"),

    ERR_BR_5200 ("The processing of the message failed"),
    ERR_BR_5201 ("BR unexpected internal (technical) error,The processing of the message failed (unexpected error)");


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
