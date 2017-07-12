package uk.gov.ch.bris.transformer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IncomingMessage {

    @JsonProperty("incoming_id")
    private String incomingMessageId;

    public String getIncomingMessageId() {
        return this.incomingMessageId;
    }
    
    public void setIncomingMessageId(String outgoingMessageId) {
        this.incomingMessageId = outgoingMessageId;
    }

}
