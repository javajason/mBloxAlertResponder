package smsresponder;

import java.io.*;
import java.net.URLDecoder;
import java.sql.Date;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.*;
import javax.xml.parsers.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.util.logging.*;

public class SMSutilities {
	// constants
	public static final Integer MT = 1;
	public static final Integer RESULT = 2;
	public static final Integer DR = 3;
	public static final Integer MO = 4;
	
	private static final String MT_STRING = "NotificationRequest";
	private static final String RESULT_STRING = "NotificationRequestResult";
	private static final String DR_STRING = "NotificationService";
	private static final String MO_STRING = "ResponseService";

	public static final String VALIDKEY = "valid";
	public static final String PASSWORDKEY = "password";
	// public static final String SHORTCODEKEY = "shortcode";
	public static final String PROFILEIDKEY = "profileID";
	public static final String SERVICEIDTMOKEY = "serviceIDTMO";
	public static final String SERVICEIDVZWKEY = "serviceIDVZW";
	public static final String SERVICEIDOTHERKEY = "serviceIDOTHER";
	public static final String SYSTEMTYPEKEY = "systemtype";
	
	public static final String USERNAMEMTKEY = "usernameMT";
	public static final String PASSWORDMTKEY = "passwordMT";
	public static final String SERVICEIDMTKEY = "serviceIDMT";
	public static final String PROFILEIDMTKEY = "profileIDMT";
	public static final String SYSTEMTYPEMTKEY = "systemtypeMT";
	public static final String DESTURLKEY = "destURL";
	
	// local variables
	private static Integer messageType;
	private static String resultCode;
	private static String resultText;
	private static String textBody;
	private static String destinationAddr;
	private static String shortCode;
	private static String msisdn;
	private static String username;
	private static String password;
	private static String batchID;
	private static String operatorID;
	private static String status;
	private static String timeStamp;
	private static String msgReference;
	private static String reason;
	
	private static final String [] whitelistedClientIPs = {"63.236.51.7",
														   "63.236.51.142",
														   "63.236.51.143",
														   "12.207.193.20",
														   "127.0.0.1"};
	
	public static Logger logger = Logger.getLogger("myLogger");
	
	public static boolean allowedClientIP(String IPaddr)
	{
		for (int i = 0; i < whitelistedClientIPs.length; i++) {
			if (IPaddr.equals(whitelistedClientIPs[i])) {
				// IP address is among the whitelisted IPs
				return true;
			}
		}
		// if we got here, IP address has not been whitelisted
		return false;
	}

	public static void delay(String delayTime)
	{
		// TODO
		// convert delayTime to int
		// sleep for delayTime seconds
		// long millitime = System.currentTimeMillis();
		try {
			TimeUnit.SECONDS.sleep(Long.parseLong(delayTime));
		} catch (NumberFormatException e) {
			SMSutilities.logger.log(Level.WARNING, "MO Delay command: Received invalid number format for delay time: " +  delayTime + ". Continuing without delay");
			e.printStackTrace();
		} catch (InterruptedException e) {
			SMSutilities.logger.log(Level.WARNING, "MO Delay command: Delay interrupted. Continuing anyway.");
			e.printStackTrace();
		}
		return;
	}
	
	public static Integer logHTTPRequest(HttpServletRequest req)
	{		
		String xmlString = "";
		
		byte[] buff = new byte[4096];
		int len = 0;
		
		String requestHeader = "";

		// collect header fields		
		Enumeration<String> headerNames = req.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			requestHeader += headerName + ": " + req.getHeader(headerName) + ", ";
		}
		logger.log(Level.INFO, "Received header: " + requestHeader);
					
		try {
			ServletInputStream inStr = req.getInputStream();

			do {
				len = inStr.read(buff);
				xmlString += new String (buff, 0, len, "UTF-8");
			}
			while (len == 4096);
			
			// Must decode string to get normal XML prior to processing
			
			logger.log(Level.INFO, "Received raw: " + xmlString);
			
			xmlString = URLDecoder.decode(xmlString, "UTF-8");
			logger.log(Level.INFO, "Received decode: " + xmlString);
		}
		catch (IOException e) {
			System.out.print("EXCEPTION CAUGHT:\n" + e);
		}
		
		return 1;
	}
	
	public static String removeTags(String XMLString)
	{
		// Removes <Tags>, </Tags>, and everything in between, from XML
		Integer tagStart, tagEnd;
		tagStart = XMLString.indexOf("<Tags>");
		
		if (tagStart >= 0) {
			tagEnd = XMLString.indexOf("</Tags>");
			return XMLString.substring(0, tagStart)
					+ XMLString.substring(tagEnd + 7, XMLString.length());
		}
		else {
			return XMLString;			
		}
	}
	private static Integer parseFields(Document dDoc)
	{
		// determine message type:
		// - notification request result (response)
		// - notification service (DR)
		// - response service (MO)
		messageType = 0;
		
		String messageTypeString = dDoc.getDocumentElement().getNodeName();
		if (messageTypeString.equals(MT_STRING)) {
			messageType = MT;
		} else if (messageTypeString.equals(RESULT_STRING)) {
			messageType = RESULT;
		} else if (messageTypeString.equals(DR_STRING)) {
			messageType = DR;
		} else if (messageTypeString.equals(MO_STRING)) {
			messageType = MO;
		}
			
	    NodeList nList = dDoc.getElementsByTagName("*");
	    
	    int nListLen = nList.getLength();
		for (int nodeNum = 0; nodeNum < nListLen; nodeNum++) {
			 Node nNode = nList.item(nodeNum);
			 String name = nNode.getNodeName();
			 String value = nNode.getTextContent();
			 
			 if (name.equals("Partner")) {
				 username = value;
			 } else if (name.equals("Password")) {
				 password = value;
			 } else if (name.equals("ServiceID")) {        			 
			 } else if (name.equals("TransactionID")) {       			 
			 } else if (name.equals("OriginatingNumber")) {
				 msisdn = value;
			 } else if (name.equals("SubscriberNumber")) {        			 
				 destinationAddr = value;
				 // msisdn = value;
			 } else if (name.equals("Status")) {
				 status = value;
			 } else if (name.equals("Time")) {
			 } else if (name.equals("Data")) {
				 textBody = value;
			 } else if (name.equals("Deliverer")) {        			 
			 } else if (name.equals("Destination")) {
				 shortCode = value;
				 destinationAddr = value;
			 } else if (name.equals("Operator")) {
				 operatorID = value; 
			 } else if (name.equals("Tariff")) {        			 
			 } else if (name.equals("SessionId")) {        			 
			 } else if (name.equals("SubscriberResultCode")) {
				 resultCode = value;
			 } else if (name.equals("SubscriberResultText")) {
				 resultText = value;
			 } else if (name.equals("TimeStamp")) {
				 timeStamp = value;
			 } else if (name.equals("MsgReference")) {
				 msgReference = value;
			 } else if (name.equals("Reason")) {
				 reason = value;
			 }
		} // end for loop
		
		return messageType;
	}
	
	public static Integer buildResult(String XMLresult)
			throws SAXException, IOException {

		Document dDoc;
		DocumentBuilder builder;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		// First, remove leading and trailing whitespace
		XMLresult = XMLresult.trim();
		
		// Next, attempt to convert to XML document (Document type) so it can be traversed 
		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.log(Level.SEVERE, "Unable to parse XML document.");
			e.printStackTrace();
			return -1;
		}
		try {
			InputSource inSrc = new InputSource(new StringReader(XMLresult));
			dDoc = builder.parse(inSrc);

            // see http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
            // for a good way to parse when looking for a specific node at a specific
            // location
            // dDoc.getDocumentElement().normalize();

			messageType = parseFields(dDoc);

		} catch (SAXException e) {
			// This exception is common. It happens any time there are <Tag> fields in the XML. Currently,
			// these fields are not used and can be stripped out. 
			// We will suppress this exception the first time, attempt to clean up XML string, and try again.
			// Should we also call trim() on the XML string to remove leading and trailing whitespace?
			XMLresult = removeTags(XMLresult);
			InputSource inSrc = new InputSource(new StringReader(XMLresult));
			dDoc = builder.parse(inSrc);
			
			messageType = parseFields(dDoc);
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		
		return messageType;
	}
	
	// Accessors (getters)
	
	public static Integer getMessageType() {
		return messageType;
	}
	public static Integer getResultCode() {
		try {
			return Integer.parseInt(resultCode);
		} catch (NumberFormatException e) {
			return 0;			
		}
	}
	public static String getResultText() {
		return resultText;
	}
	public static String getDestinationAddr() {
		return destinationAddr;
	}
	public static String getShortCode() {
		return shortCode;
	}
	
	public static String getBody() {
		return textBody;
	}
	public static String getMSISDN() {
		return msisdn;
	}
	public static String getDRReason() {
		return reason;
	}
	public static String getDRStatusCode() {
		return status;
	}	
	public static String getUsername() {
		return username;
	}
	/*
	public static void setUsername(String user) {
		username = user;
	}
	*/
	public static String getPassword() {
		return password;
	}

	/*
	 public static String getBatchID() {
		return batchID;
	}
	*/
	public static Integer getBatchID() {
		try {
			return Integer.parseInt(batchID);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static String getOperatorID() {
		return operatorID;
	}
}