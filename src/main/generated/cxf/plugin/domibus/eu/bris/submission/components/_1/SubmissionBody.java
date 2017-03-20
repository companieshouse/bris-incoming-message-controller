
package plugin.domibus.eu.bris.submission.components._1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import plugin.domibus.eu.bris.common.aggregate.components._1.AttachmentType;
import plugin.domibus.eu.bris.common.aggregate.components._1.MessageContentType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://eu.domibus.plugin/bris/common/aggregate/components/1.0}MessageContent"/&gt;
 *         &lt;element ref="{http://eu.domibus.plugin/bris/common/aggregate/components/1.0}Attachment" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "messageContent",
    "attachment"
})
@XmlRootElement(name = "SubmissionBody")
public class SubmissionBody {

    @XmlElement(name = "MessageContent", namespace = "http://eu.domibus.plugin/bris/common/aggregate/components/1.0", required = true)
    protected MessageContentType messageContent;
    @XmlElement(name = "Attachment", namespace = "http://eu.domibus.plugin/bris/common/aggregate/components/1.0")
    protected AttachmentType attachment;

    /**
     * Gets the value of the messageContent property.
     * 
     * @return
     *     possible object is
     *     {@link MessageContentType }
     *     
     */
    public MessageContentType getMessageContent() {
        return messageContent;
    }

    /**
     * Sets the value of the messageContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageContentType }
     *     
     */
    public void setMessageContent(MessageContentType value) {
        this.messageContent = value;
    }

    /**
     * Gets the value of the attachment property.
     * 
     * @return
     *     possible object is
     *     {@link AttachmentType }
     *     
     */
    public AttachmentType getAttachment() {
        return attachment;
    }

    /**
     * Sets the value of the attachment property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttachmentType }
     *     
     */
    public void setAttachment(AttachmentType value) {
        this.attachment = value;
    }

}
