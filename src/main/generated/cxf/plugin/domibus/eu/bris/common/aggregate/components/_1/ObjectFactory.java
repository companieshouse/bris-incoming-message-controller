
package plugin.domibus.eu.bris.common.aggregate.components._1;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the plugin.domibus.eu.bris.common.aggregate.components._1 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _MessageContent_QNAME = new QName("http://eu.domibus.plugin/bris/common/aggregate/components/1.0", "MessageContent");
    private final static QName _Attachment_QNAME = new QName("http://eu.domibus.plugin/bris/common/aggregate/components/1.0", "Attachment");
    private final static QName _AddressInfo_QNAME = new QName("http://eu.domibus.plugin/bris/common/aggregate/components/1.0", "AddressInfo");
    private final static QName _Receiver_QNAME = new QName("http://eu.domibus.plugin/bris/common/aggregate/components/1.0", "Receiver");
    private final static QName _Sender_QNAME = new QName("http://eu.domibus.plugin/bris/common/aggregate/components/1.0", "Sender");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: plugin.domibus.eu.bris.common.aggregate.components._1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MessageContentType }
     * 
     */
    public MessageContentType createMessageContentType() {
        return new MessageContentType();
    }

    /**
     * Create an instance of {@link AttachmentType }
     * 
     */
    public AttachmentType createAttachmentType() {
        return new AttachmentType();
    }

    /**
     * Create an instance of {@link AddressInfoType }
     * 
     */
    public AddressInfoType createAddressInfoType() {
        return new AddressInfoType();
    }

    /**
     * Create an instance of {@link ReceiverType }
     * 
     */
    public ReceiverType createReceiverType() {
        return new ReceiverType();
    }

    /**
     * Create an instance of {@link SenderType }
     * 
     */
    public SenderType createSenderType() {
        return new SenderType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MessageContentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eu.domibus.plugin/bris/common/aggregate/components/1.0", name = "MessageContent")
    public JAXBElement<MessageContentType> createMessageContent(MessageContentType value) {
        return new JAXBElement<MessageContentType>(_MessageContent_QNAME, MessageContentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AttachmentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eu.domibus.plugin/bris/common/aggregate/components/1.0", name = "Attachment")
    public JAXBElement<AttachmentType> createAttachment(AttachmentType value) {
        return new JAXBElement<AttachmentType>(_Attachment_QNAME, AttachmentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddressInfoType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eu.domibus.plugin/bris/common/aggregate/components/1.0", name = "AddressInfo")
    public JAXBElement<AddressInfoType> createAddressInfo(AddressInfoType value) {
        return new JAXBElement<AddressInfoType>(_AddressInfo_QNAME, AddressInfoType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReceiverType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eu.domibus.plugin/bris/common/aggregate/components/1.0", name = "Receiver")
    public JAXBElement<ReceiverType> createReceiver(ReceiverType value) {
        return new JAXBElement<ReceiverType>(_Receiver_QNAME, ReceiverType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SenderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eu.domibus.plugin/bris/common/aggregate/components/1.0", name = "Sender")
    public JAXBElement<SenderType> createSender(SenderType value) {
        return new JAXBElement<SenderType>(_Sender_QNAME, SenderType.class, null, value);
    }

}
