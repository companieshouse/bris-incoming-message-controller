package uk.gov.ch.bris.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.Size;

import org.joda.time.DateTime;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@ComponentScan
@Document(collection = "brisIncomingMessageCollection")
public class BRISIncomingMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** No-args constructor */
    public BRISIncomingMessage() {
    }

    /** Constructor */
    public BRISIncomingMessage(String messageId, String message) {
        this.messageId = messageId;
        this.message = message;
    }
    
    public BRISIncomingMessage(String messageId, String message, String status) {
        this.messageId = messageId;
        this.message = message;
        this.status = status;
    }
    
    @Id
    private String id;

    @Size(min = 5, max = 100)
    @Field("messageId")
    private String messageId;

    @Field("message")
    private String message;
    
    @Field("status")
    private String status;
    
    @Field("timestamp")
    private DateTime timestamp;
    
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }    
    
    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
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
                ", message='" + message + "'" +
                ", status='" + status + "'" +
                ", timestamp='" + timestamp + "'" +
                '}';
    }
}
