package uk.gov.ch.bris.client;


import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.GregorianCalendar;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import eu.europa.ec.bris.jaxb.br.generic.acknowledgement.template.br.addition.v2_0.AddBusinessRegisterAcknowledgementTemplateType;
import eu.europa.ec.bris.jaxb.br.generic.acknowledgement.template.br.removal.v2_0.RemoveBusinessRegisterAcknowledgementTemplateType;
import eu.europa.ec.bris.jaxb.br.generic.acknowledgement.v2_0.AcknowledgementTemplateType;
import eu.europa.ec.bris.jaxb.br.generic.acknowledgement.v2_0.BRAcknowledgement;
import eu.europa.ec.bris.jaxb.br.generic.notification.template.br.addition.v2_0.AddBusinessRegisterNotificationTemplateType;
import eu.europa.ec.bris.jaxb.br.generic.notification.template.br.removal.v2_0.RemoveBusinessRegisterNotificationTemplateType;
import eu.europa.ec.bris.jaxb.br.generic.notification.template.company.euid.change.v2_0.ChangeCompanyEUIDNotificationTemplateType;
import eu.europa.ec.bris.jaxb.br.generic.notification.v2_0.BRNotification;
import eu.europa.ec.bris.jaxb.br.generic.notification.v2_0.NotificationTemplateType;
import eu.europa.ec.bris.jaxb.components.aggregate.v1_5.BusinessRegisterReference;
import eu.europa.ec.bris.jaxb.components.aggregate.v1_5.BusinessRegisterType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.BusinessRegisterCodeType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CompanyEUIDType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.CountryType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.DateTimeType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.LanguageCodeType;
import eu.europa.ec.bris.jaxb.components.basic.v1_4.LocalisedBusinessRegisterNameType;
import eu.europa.ec.digit.message.container.jaxb.v1_0.ContainerBody;
import eu.europa.ec.digit.message.container.jaxb.v1_0.ContainerHeader;
import eu.europa.ec.digit.message.container.jaxb.v1_0.ContainerHeader.AddressInfo;
import eu.europa.ec.digit.message.container.jaxb.v1_0.MessageContainer;
import eu.europa.ec.digit.message.container.jaxb.v1_0.MessageInfo;
import eu.europa.ec.digit.message.container.jaxb.v1_0.PartyType;


public class MessageContainerHelper {

    public static MessageContainer newAddBRNotification(
            String correlationId,
            String messageId,
            String businessRegisterId,
            String businessRegisterName,
            String countryCode) throws Exception {

        XMLGregorianCalendar now = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());

        eu.europa.ec.bris.jaxb.br.generic.notification.template.br.addition.v2_0.ObjectFactory factory = new eu.europa.ec.bris.jaxb.br.generic.notification.template.br.addition.v2_0.ObjectFactory();
        AddBusinessRegisterNotificationTemplateType template = factory.createAddBusinessRegisterNotificationTemplateType();

        DateTimeType notificationDate = new DateTimeType();
        notificationDate.setValue(now);
        template.setNotificationDateTime(notificationDate);
        
        BusinessRegisterType br = new BusinessRegisterType();
        BusinessRegisterCodeType brCode = new BusinessRegisterCodeType();
        brCode.setValue(businessRegisterId);
        br.setBusinessRegisterCode(brCode);
        CountryType brCountry = new CountryType();
        brCountry.setValue(countryCode);
        br.setBusinessRegisterCountry(brCountry);
        LocalisedBusinessRegisterNameType brName = new LocalisedBusinessRegisterNameType();
        brName.setLanguageID(LanguageCodeType.EN.value());
        brName.setValue(businessRegisterName);
        br.getLocalisedBusinessRegisterName().add(brName);
        template.setBusinessRegister(br);

        return createMessageContainer(correlationId, messageId, now, createNotification(factory.createAddBusinessRegisterNotificationTemplate(template)));
    }
    
    public static MessageContainer newUpdateEUIDNotification(
            String correlationId,
            String messageId,
            String formerCompanyEUIDString,
            String newCompanyEUIDString) throws Exception {

        XMLGregorianCalendar now = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());

        eu.europa.ec.bris.jaxb.br.generic.notification.template.company.euid.change.v2_0.ObjectFactory factory = new eu.europa.ec.bris.jaxb.br.generic.notification.template.company.euid.change.v2_0.ObjectFactory();
        ChangeCompanyEUIDNotificationTemplateType template = factory.createChangeCompanyEUIDNotificationTemplateType();

        DateTimeType notificationDate = new DateTimeType();
        notificationDate.setValue(now);
        template.setNotificationDateTime(notificationDate);
        
        CompanyEUIDType formerCompanyEUID = new CompanyEUIDType();
        formerCompanyEUID.setValue(formerCompanyEUIDString);

        CompanyEUIDType newCompanyEUID = new CompanyEUIDType();
        newCompanyEUID.setValue(newCompanyEUIDString);

        template.setFormerCompanyEUID(formerCompanyEUID);
        template.setCompanyEUID(newCompanyEUID);

        return createMessageContainer(correlationId, messageId, now, createNotification(factory.createChangeCompanyEUIDNotificationTemplate(template)));
    }
    
    public static MessageContainer newRemoveBRNotification(
            String correlationId,
            String messageId,
            String businessRegisterId,
            String countryCode) throws Exception {

        XMLGregorianCalendar now = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());

        eu.europa.ec.bris.jaxb.br.generic.notification.template.br.removal.v2_0.ObjectFactory factory = new eu.europa.ec.bris.jaxb.br.generic.notification.template.br.removal.v2_0.ObjectFactory();
        RemoveBusinessRegisterNotificationTemplateType template = factory.createRemoveBusinessRegisterNotificationTemplateType();

        DateTimeType notificationDate = new DateTimeType();
        notificationDate.setValue(now);
        template.setNotificationDateTime(notificationDate);
        
        BusinessRegisterReference br = new BusinessRegisterReference();
        BusinessRegisterCodeType brCode = new BusinessRegisterCodeType();
        brCode.setValue(businessRegisterId);
        br.setBusinessRegisterCode(brCode );
        CountryType brCountry = new CountryType();
        brCountry.setValue(countryCode);
        br.setBusinessRegisterCountry(brCountry);
        template.setBusinessRegisterReference(br);

        return createMessageContainer(correlationId, messageId, now, createNotification(factory.createRemoveBusinessRegisterNotificationTemplate(template)));
    }

    public static MessageContainer newAddBRAcknowledgment(String correlationId, String messageId) throws Exception {
        XMLGregorianCalendar now = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());

        eu.europa.ec.bris.jaxb.br.generic.acknowledgement.template.br.addition.v2_0.ObjectFactory factory = new eu.europa.ec.bris.jaxb.br.generic.acknowledgement.template.br.addition.v2_0.ObjectFactory();
        AddBusinessRegisterAcknowledgementTemplateType template = factory.createAddBusinessRegisterAcknowledgementTemplateType();

        DateTimeType notificationDate = new DateTimeType();
        notificationDate.setValue(now);
        template.setSendingDateTime(notificationDate);

        return createMessageContainer(correlationId, messageId, now, createAcknowledgment(factory.createAddBusinessRegisterAcknowledgementTemplate(template)));
    }

    public static MessageContainer newRemoveBRAcknowledgment(String correlationId, String messageId) throws Exception {
        XMLGregorianCalendar now = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());

        eu.europa.ec.bris.jaxb.br.generic.acknowledgement.template.br.removal.v2_0.ObjectFactory factory = new eu.europa.ec.bris.jaxb.br.generic.acknowledgement.template.br.removal.v2_0.ObjectFactory();
        RemoveBusinessRegisterAcknowledgementTemplateType template = factory.createRemoveBusinessRegisterAcknowledgementTemplateType();

        DateTimeType notificationDate = new DateTimeType();
        notificationDate.setValue(now);
        template.setSendingDateTime(notificationDate);

        return createMessageContainer(correlationId, messageId, now, createAcknowledgment(factory.createRemoveBusinessRegisterAcknowledgementTemplate(template)));
    }
    
    private static BRNotification createNotification(JAXBElement<? extends NotificationTemplateType> template) {
        BRNotification notification = new BRNotification();
        notification.setNotificationTemplate(template);
        return notification;
    }

    private static BRAcknowledgement createAcknowledgment(JAXBElement<? extends AcknowledgementTemplateType> template) {
        BRAcknowledgement ack = new BRAcknowledgement();
        ack.setAcknowledgementTemplate(template);
        return ack;
    }

    private static MessageContainer createMessageContainer(String correlationId, String messageId,
            XMLGregorianCalendar timestamp, Object content) throws Exception {
        MessageContainer container = new MessageContainer();
        container.setContainerHeader(getMessageHeader(correlationId, messageId, timestamp));
        container.setContainerBody(createContainerBody(content));
        return container;
    }

    private static ContainerHeader getMessageHeader(String correlationId, String messageId, XMLGregorianCalendar timestamp) throws DatatypeConfigurationException {
        ContainerHeader header = new ContainerHeader();
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setCorrelationID(correlationId);
        messageInfo.setMessageID(messageId);
        messageInfo.setTimestamp(timestamp);
        header.setMessageInfo(messageInfo);
        
        PartyType receiver = new PartyType();
        receiver.setCountryCode("UK");
        receiver.setCode("EW");
        PartyType sender = new PartyType();
        sender.setCountryCode("EU");
        sender.setCode("BRIS");
        
        AddressInfo addressInfo = new AddressInfo();
        addressInfo.setReceiver(receiver);
        addressInfo.setSender(sender);
        header.setAddressInfo(addressInfo);
        return header;
    }

    private static ContainerBody createContainerBody(Object response) throws JAXBException {

        ContainerBody containerBody = new ContainerBody();
        containerBody.setMessageContent(createMessageContent(response));

        return containerBody;
    }

    private static ContainerBody.MessageContent createMessageContent(Object response) throws JAXBException {

        ContainerBody.MessageContent messageContent = new ContainerBody.MessageContent();
        messageContent.setValue(createDataHandler(response));

        return messageContent;
    }

    private static DataHandler createDataHandler(Object response) throws JAXBException {

        String data = marshalToString(response);

        return new DataHandler(new ByteArrayDataSource(data.getBytes(StandardCharsets.UTF_8), "text/plain; charset=UTF-8"));
    }

    private static String marshalToString(Object object) throws JAXBException {
        StringWriter writer = new StringWriter();

        JAXBContext jaxbContext = JAXBContext.newInstance(MessageContainer.class, BRNotification.class, BRAcknowledgement.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(object, writer);

        return writer.toString();
    }
}
