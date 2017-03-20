
package plugin.domibus.eu.bris.submission.components._1;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the plugin.domibus.eu.bris.submission.components._1 package. 
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

    private final static QName _SubmissionMessageInfo_QNAME = new QName("http://eu.domibus.plugin/bris/submission/components/1.0", "SubmissionMessageInfo");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: plugin.domibus.eu.bris.submission.components._1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Acknowledgement }
     * 
     */
    public Acknowledgement createAcknowledgement() {
        return new Acknowledgement();
    }

    /**
     * Create an instance of {@link SubmissionMessageInfoType }
     * 
     */
    public SubmissionMessageInfoType createSubmissionMessageInfoType() {
        return new SubmissionMessageInfoType();
    }

    /**
     * Create an instance of {@link FaultDetail }
     * 
     */
    public FaultDetail createFaultDetail() {
        return new FaultDetail();
    }

    /**
     * Create an instance of {@link SubmissionBody }
     * 
     */
    public SubmissionBody createSubmissionBody() {
        return new SubmissionBody();
    }

    /**
     * Create an instance of {@link SubmissionHeader }
     * 
     */
    public SubmissionHeader createSubmissionHeader() {
        return new SubmissionHeader();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubmissionMessageInfoType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eu.domibus.plugin/bris/submission/components/1.0", name = "SubmissionMessageInfo")
    public JAXBElement<SubmissionMessageInfoType> createSubmissionMessageInfo(SubmissionMessageInfoType value) {
        return new JAXBElement<SubmissionMessageInfoType>(_SubmissionMessageInfo_QNAME, SubmissionMessageInfoType.class, null, value);
    }

}
