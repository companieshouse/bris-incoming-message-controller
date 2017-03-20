
package plugin.domibus.eu.bris.delivery.components._1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import plugin.domibus.eu.bris.common.aggregate.components._1.AddressInfoType;


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
 *         &lt;element ref="{http://eu.domibus.plugin/bris/common/aggregate/components/1.0}AddressInfo"/&gt;
 *         &lt;element ref="{http://eu.domibus.plugin/bris/delivery/components/1.0}DeliveryMessageInfo"/&gt;
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
    "addressInfo",
    "deliveryMessageInfo"
})
@XmlRootElement(name = "DeliveryHeader")
public class DeliveryHeader {

    @XmlElement(name = "AddressInfo", namespace = "http://eu.domibus.plugin/bris/common/aggregate/components/1.0", required = true)
    protected AddressInfoType addressInfo;
    @XmlElement(name = "DeliveryMessageInfo", required = true)
    protected DeliveryMessageInfoType deliveryMessageInfo;

    /**
     * Gets the value of the addressInfo property.
     * 
     * @return
     *     possible object is
     *     {@link AddressInfoType }
     *     
     */
    public AddressInfoType getAddressInfo() {
        return addressInfo;
    }

    /**
     * Sets the value of the addressInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressInfoType }
     *     
     */
    public void setAddressInfo(AddressInfoType value) {
        this.addressInfo = value;
    }

    /**
     * Gets the value of the deliveryMessageInfo property.
     * 
     * @return
     *     possible object is
     *     {@link DeliveryMessageInfoType }
     *     
     */
    public DeliveryMessageInfoType getDeliveryMessageInfo() {
        return deliveryMessageInfo;
    }

    /**
     * Sets the value of the deliveryMessageInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeliveryMessageInfoType }
     *     
     */
    public void setDeliveryMessageInfo(DeliveryMessageInfoType value) {
        this.deliveryMessageInfo = value;
    }

}
