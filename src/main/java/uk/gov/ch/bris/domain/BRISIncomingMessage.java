package uk.gov.ch.bris.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.Size;

import org.bson.types.Binary;
import org.joda.time.DateTime;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@ComponentScan
@Document(collection = "incoming_messages")
public class BRISIncomingMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** No-args constructor */
    public BRISIncomingMessage() {
    }

    /** Constructor */
    public BRISIncomingMessage(String messageId, String correlationId, String message) {
        this();
        this.messageId = messageId;
        this.correlationId = correlationId;
        this.message = message;
    }

    public BRISIncomingMessage(String messageId, String correlationId, String message, String status) {
        this(messageId, correlationId, message);
        this.status = status;
    }

    public BRISIncomingMessage(String messageId, String correlationId, String message, String status, DateTime timestamp) {
        this(messageId, correlationId, message, status);
        this.createdOn = timestamp;
    }

    @Id
    private String id;

    @Size(min = 5, max = 100)
    @Field("message_id")
    private String messageId;

    @Field("correlation_id")
    private String correlationId;
    
    @Field("sender")
    private String sender;
    
    @Field("receiver")
    private String receiver;

    @Field("message_type")
    private String messageType;

    @Field("message")
    private String message;

    @Field("invalid_message")
    private String invalidMessage;

    @Field("status")
    private String status;

    @Field("created_on")
    private DateTime createdOn;

    @Field("binary_attachment")
    private Binary data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getInvalidMessage() {
        return invalidMessage;
    }

    public void setInvalidMessage(String invalidMessage) {
        this.invalidMessage = invalidMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Binary getData() {
        return data;
    }

    public void setData(Binary data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BRISIncomingMessage bRISIncomingMessage = (BRISIncomingMessage) o;

        if ( ! Objects.equals(id, bRISIncomingMessage.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "BRISIncomingMessage{" +
                "_id=" + id +
                ", messageId='" + messageId + "'" +
                ", correlationId='" + correlationId + "'" +
                ", sender='" + sender + "'" +
                ", receiver='" + receiver + "'" +
                ", messageType='" + messageType + "'" +
                ", message='" + message + "'" +
                ", status='" + status + "'" +
                ", data='" + data + "'" +
                ", createdOn='" + createdOn + "'" +
                '}';
    }
}
