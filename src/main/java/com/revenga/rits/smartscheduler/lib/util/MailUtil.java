package com.revenga.rits.smartscheduler.lib.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.springframework.util.CollectionUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class MailUtil {
	
	public static final String MAIL_SMTP_USER = "mail.smtp.user";
	public static final String MAIL_SMTP_PASSWORD = "mail.smtp.password";
	public static final String MAIL_SMTP_FROM = "mail.smtp.from";
	public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
	public static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
	public static final String MAIL_SMTP_HOST = "mail.smtp.host";
	public static final String MAIL_SMTP_PORT = "mail.smtp.port";
	public static final String MAIL_SMTP_SSL_TRUST = "mail.smtp.ssl.trust";
	public static final String MAIL_SMTP_SSL_PROTOCOLS = "mail.smtp.ssl.protocols";
	
	private MailUtil() {

		throw new IllegalStateException(this.getClass().getSimpleName());
	}

	public static boolean send(Properties props, String addressTO, String addressCC, String addressBCC, String subject,
			String body, String[] filePaths) throws AddressException, MessagingException, IOException {
		
		boolean result = false;
		List<String> attachments = new ArrayList<>();
		
		if (props != null && !StringUtils.isEmpty(addressTO)) {
			
			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {

					return new PasswordAuthentication(props.getProperty(MAIL_SMTP_USER),
							props.getProperty(MAIL_SMTP_PASSWORD));
				}
			});
			
		    MimeMessage message = new MimeMessage(session);
		    
		    if (!StringUtils.isEmpty(props.getProperty(MAIL_SMTP_FROM))) {
		    	
		    	message.setFrom(new InternetAddress(props.getProperty(MAIL_SMTP_FROM)));
		    }
		    
	        message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(addressTO));
	        
	        if (!StringUtils.isEmpty(addressCC)) {
	        
	        	message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(addressCC));
	        }
	        
	        if (!StringUtils.isEmpty(addressBCC)) {
	        	
	        	message.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(addressBCC));
	        }
	        
	        message.setSubject(subject != null ? subject : "");
	        
			if (!ArrayUtils.isEmpty(filePaths)) {
				
				BodyPart messageBodyPart = new MimeBodyPart(); 
		        messageBodyPart.setText(body);
		        
		        Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);
				
				for (String filePath : Arrays.asList(filePaths)) {
					
					MimeBodyPart attachmentPart = new MimeBodyPart();
					attachmentPart.attachFile(new File(filePath));
					multipart.addBodyPart(attachmentPart);
					
					attachments.add(attachmentPart.getFileName());
				}
				
				message.setContent(multipart);
			}
			else {
			
				message.setContent(body != null ? body : "", "text/html; charset=utf-8");
			}
			
	        Transport transport = session.getTransport("smtp");
	        
	        transport.connect(props.getProperty(MAIL_SMTP_HOST), props.getProperty(MAIL_SMTP_USER), props.getProperty(MAIL_SMTP_PASSWORD));
	        transport.sendMessage(message, message.getAllRecipients());
	        
	        traceSend(Level.DEBUG, props, addressTO, addressCC, addressBCC, subject, body, attachments);

	        transport.close();
	        
	        result = true;
		}
		
		return result;

	}
	
	private static void traceSend(Level level, Properties props, String addressTo, String addressCC, String addressBCC,
			String subject, String body, List<String> attachments) {
		
		log.log(level, "------------- SEND MAIL -------------");
		log.log(level, "");
	    log.log(level, "<<< MESSAGE >>>");
	    log.log(level, "    from: " + props.getProperty(MAIL_SMTP_FROM));
	    log.log(level, "    addressTo: " + addressTo);
	    log.log(level, "    addressCC: " + addressCC);
	    log.log(level, "    addressBCC: " + addressBCC);
	    log.log(level, "    subject: " + subject);
	    log.log(level, "    body: " + body);
	    
	    if (!CollectionUtils.isEmpty(attachments)) {
	    
	    	log.log(level, "    attachments: " + StringUtils.join(attachments));
	    }
		
	    log.log(level, "");
	    log.log(level, "-------------------------------------");
	}
}
