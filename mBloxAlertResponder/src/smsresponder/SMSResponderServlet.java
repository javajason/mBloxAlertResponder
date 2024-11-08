package smsresponder;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Level;
import org.xml.sax.SAXException;

import com.google.appengine.labs.repackaged.com.google.common.io.CharStreams;

import javax.servlet.http.*;
import javax.servlet.ServletInputStream;
import java.util.logging.*;

@SuppressWarnings("serial")
public class SMSResponderServlet extends HttpServlet {
	static Integer BUFFSIZE = 4096; // 4K read buffer
	static Integer US_CHAR_LIMIT = 160;
	
	/*
	public static String SERVICE_ID_TMO = "27433";
	public static String SERVICE_ID_VZW = "10293";
	public static String SERVICE_ID_OTHER = null;
	public static String SERVICE_ID_MT = "49884";
	*/
	public static final String CARRIER_ID_TMO = "31004";
	public static final String CARRIER_ID_VZW = "31003";
	// public static final String KEYWORD = "GOOGLE";
	
	public static final Integer VALID_MO = 0;
	public static final Integer INVALID_USERNAME = -1;
	public static final Integer INVALID_PASSWORD = -2;
	public static final Integer INVALID_SHORTCODE = -3;
	public static final Integer INVALID_ORIGINATOR = -4;
	public static final Integer NULL_ORIGINATOR = -5;
	public static final String BLANK_MESSAGE = "blank";
	public static final String UNRECOGNIZED_MESSAGE = "unrec";
	public static final String STANDARD_RESPONSE = "standard";
	public static final String KEYWORD = "keyword";
	public static final String CMD_PREFIX = "cmd";
	public static final String DELAY = "DELAY";
	
/*
	public static void main(String[] args)
	{
		InstanceConfig conf = new InstanceConfig();
		String motd = conf.getProperty("motd");
	}
*/
	
	private Integer populateXMLmessage(HttpServletRequest req)
	{		
		String xmlString = "";
		Integer postType = -1;
		
		byte[] buff = new byte[BUFFSIZE];
		int len = 0;
			
		try {
			ServletInputStream inStr = req.getInputStream();

			/*
			len = inStr.read(buff);			
			String chars = new String (buff, 0, len, "UTF-8");
			SMSutilities.logger.log(Level.INFO, URLDecoder.decode(chars, "UTF-8"));
			// xmlString = CharStreams.toString(req.getReader());
			*/

			do {
				len = inStr.read(buff);
				xmlString += new String (buff, 0, len, "UTF-8");
			}
			while (len == BUFFSIZE);
			
			// Must decode string to get normal XML prior to processing
			xmlString = URLDecoder.decode(xmlString, "UTF-8");
			SMSutilities.logger.log(Level.INFO, "XML POST (MO or DR) received: " + xmlString);

			// Strip out beginning label, "XMLDATA=". String will be "XMLDATA=<?xml version=...".
			if (xmlString.startsWith("XMLDATA=")) {
				xmlString = xmlString.substring(8);
			}
		}
		catch (IOException e) {
			System.out.print("EXCEPTION CAUGHT:\n" + e);
		}
		
		try {
			try {
				// This is where we parse xmlString and do something with it
				postType = SMSutilities.buildResult(xmlString);
			}
			catch (SAXException e) {
				// Suppress exception, clean up string, and try again.
				xmlString = xmlString.trim();
				xmlString = SMSutilities.removeTags(xmlString);
				postType = SMSutilities.buildResult(xmlString);
			}
		}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		SMSutilities.logger.log(Level.INFO, "XML POST type: "
				+ (postType == SMSutilities.DR ?
						"Delivery Receipt, status " + SMSutilities.getDRStatusCode() + " (" + SMSutilities.getDRReason() + ")"
						: (postType == SMSutilities.MO ?
								"Mobile Originated, from " + SMSutilities.getMSISDN()
								: "Other" + postType.toString())));
		return postType;
	}
	
	public String createMOresponse(String messageBody) {
		
		boolean gotKeyword = false;
		String username = SMSutilities.getUsername();
		String shortCode = SMSutilities.getShortCode();
		String response;
				
		/*
		String keyword = ServletContextClass.keyword;
		String stopResponse = ServletContextClass.STOP;
		String helpResponse = ServletContextClass.HELP;
		// String startResponse = ServletContextClass.START;
		// String startResponse = ServletContextClass.EMPLOYEE;
		*/
		String keyword = ServletContextClass.getValue(username + "_" + KEYWORD);
		
		/*
		if (operatorID.equals(CARRIER_ID_TMO)) {
			// for now, we will use the MT account for TMO
			// serviceID = SERVICE_ID_TMO;
			serviceID = SERVICE_ID_MT;
			operatorID = null;
		} else if (operatorID.equals(CARRIER_ID_VZW)) {
			serviceID = SERVICE_ID_VZW;
		} else {
			serviceID = SERVICE_ID_OTHER;
		}	
		 */
		
		// trim initial message
		messageBody = messageBody.trim();
		String origMessage = messageBody;

		messageBody = messageBody.toUpperCase();
		
		// Parse out keyword and remove from message
		if (messageBody.startsWith(keyword)) {
			gotKeyword = true;
			messageBody = messageBody.substring(keyword.length());
		}
		// trim again, after keyword removed
		messageBody = messageBody.trim();
		
		if (gotKeyword && messageBody.equals("")) {
			response = ServletContextClass.getValue(CMD_PREFIX + username + shortCode + "_" + BLANK_MESSAGE);
		} else {
			// pull out first word
			int firstSpace = messageBody.indexOf(" ");
			if (firstSpace > 0) { // message contains space after non-space text
				// pull out first word
				String firstWord = messageBody.substring(0, firstSpace);
				if (firstWord.equals(DELAY)) {
					/*
					int secondSpace = messageBody.substring(firstSpace + 1, 
					String delayTime = messageBody.substring(firstSpace + 1, 
					*/
					// Hardcode delay value in for now. Fix later (TODO).
					SMSutilities.delay("10");
				}
				response = ServletContextClass.getValue(CMD_PREFIX + username + shortCode + "_" + firstWord);
			} else { // no blank spaces (blank space would not be first since trim() was called)
				response = ServletContextClass.getValue(CMD_PREFIX + username + shortCode + "_" + messageBody);
			}
		}
		if (response == null) {
			// no match found
			response = ServletContextClass.getValue(CMD_PREFIX + username + shortCode + "_" + UNRECOGNIZED_MESSAGE);
		}
		// Handle case with command followed by more text
		
		/*
		} else if (messageBody.startsWith("STOP") // message begins with STOP (or an equivalent), but has more text with no space
				|| messageBody.startsWith("CANCEL")
				|| messageBody.startsWith("END") 
				|| messageBody.startsWith("QUIT")
				|| messageBody.startsWith("UNSUBSCRIBE")) {				
			response = "If you wish to be unsubscribed, please reply with STOP. Please do not put in more stuff. I mean, seriously. (Oh, yeah - msg&amp;data rates may apply.)";
		} else if (messageBody.startsWith("HELP")) { // message begins with HELP, but has more text
			response = "I think you meant HELP. In any case, you&apos;ve reached the mBlox U.S. Onboarding team. Reply STOP to cancel, msg&amp;data rates may apply.";
		} else {
			// Send generic response to unrecognized MO
			String responseBeginning = "You just sent us a completely unrecognized message. Come on! Give us something we can work with here! This is what we got: ";
			if (responseBeginning.length() + origMessage.length() > US_CHAR_LIMIT) {
				response = responseBeginning + origMessage.substring(0, US_CHAR_LIMIT - (responseBeginning.length() + 3)) + "...";
			} else {
				response = responseBeginning + origMessage;
			}
		}
		*/
		return response;
	}
	
	public Integer validateMO() {
		
		// Ensure message contains correct username and password
		String receivedUserName = SMSutilities.getUsername();
		String isValidUser = ServletContextClass.getValue(receivedUserName);
		if (isValidUser == null || !isValidUser.equals(SMSutilities.VALIDKEY)) {
			// either user name does not exist in file or is not set to "valid" (is inactive)
			return INVALID_USERNAME;
		}
		String receivedPassword = SMSutilities.getPassword();
		String storedPassword = ServletContextClass.getValue(receivedUserName + "_" + SMSutilities.PASSWORDKEY);
		if (!receivedPassword.equals(storedPassword)) {
		// if (!password.equals(httppost.PASS)) {
			return INVALID_PASSWORD;
		}

		// Validate destination short code
		String receivedShortCode = SMSutilities.getShortCode();
		String isValidCode = ServletContextClass.getValue(receivedUserName + "_" + receivedShortCode);
		if (isValidCode == null || !isValidCode.equals(SMSutilities.VALIDKEY)) {
			// either user name does not exist in file or is not set to "valid" (is inactive)
			return INVALID_SHORTCODE;
		}
		/*
		String storedShortCode = ServletContextClass.getValue(receivedUserName + "_" + SMSutilities.SHORTCODEKEY);
		if (!receivedShortCode.equals(storedShortCode)) {
			return INVALID_SHORTCODE;
		}
		*/
		// Validate originating number
		String subscriberNumber = SMSutilities.getMSISDN();
		if (subscriberNumber == null)
		{
			// Subscriber's number is null. We can't do anything here.
			return NULL_ORIGINATOR;
		}
		if (!subscriberNumber.matches("[0-9]+"))
		{
			return INVALID_ORIGINATOR;		
		}
		return VALID_MO;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String IPaddr = req.getRemoteAddr();
		if (!SMSutilities.allowedClientIP(IPaddr)) {
			// Unauthorized (not whitelisted) IP address. Reject by returning immediately.
			SMSutilities.logger.log(Level.WARNING, "GET received from unauthorized IP address: " + IPaddr.toString() + ". Request will not be processed.");
			return;
		}
		
		resp.setContentType("text/plain");
		resp.getWriter().println("GET received");
		
		String subscriberNumber = "4";
		String messageText = "Hello.";
		Integer pttCode = httppost.sendMT(subscriberNumber, messageText);
	}
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		// ServletContextClass servletContext = new ServletContextClass();  
		Integer postType = -1;
		
		// String motd = ServletContextClass.motd;
		// String motd = servletContext.getValue("motd");
		String motd = "Welcome to the auto-responder.";
		
		String IPaddr = req.getRemoteAddr();
		if (!SMSutilities.allowedClientIP(IPaddr)) {
			// Unauthorized (not whitelisted) IP address. Reject by returning immediately.
			SMSutilities.logger.log(Level.WARNING, "POST received from unauthorized IP address: " + IPaddr + ":" + req.getRemotePort() + ". Request will not be processed.");
			return;
		}
		
		resp.setContentType("text/plain");
		resp.getWriter().println("POST received.  " + motd);
		
		postType = populateXMLmessage(req);
		
		if (postType == SMSutilities.DR) {

			// TODO: process DRs
			String subscriberNumber = SMSutilities.getDestinationAddr();
			String operatorID = SMSutilities.getOperatorID();
			String statusCode = SMSutilities.getDRStatusCode();
			
			// At this point, we can take action based on the statusCode, such as
			// resending. If so, we would determine response text and run this line:
			// 		httppost.sendMT(subscriberNumber, operatorID, response);
			
		} else if (postType == SMSutilities.MO) {
			// Incoming MO message
			
			// First, validate message
			Integer MOvalidCode = validateMO();
			if (MOvalidCode == INVALID_USERNAME || MOvalidCode == INVALID_PASSWORD) {
				SMSutilities.logger.log(Level.WARNING, "Incoming MO contained incorrect username or password. No MT response will be sent.");
				return;
			}
			if (MOvalidCode == INVALID_ORIGINATOR || MOvalidCode == NULL_ORIGINATOR) {
				SMSutilities.logger.log(Level.WARNING, "Subscriber number is null or non-numeric: " + SMSutilities.getMSISDN() + ". Cannot return MT.");
				return;
			}
			if (MOvalidCode == INVALID_SHORTCODE) {
				SMSutilities.logger.log(Level.WARNING, "Destination short code is invalid. " + SMSutilities.getShortCode() + ". Cannot return MT.");
				return;				
			}

			String subscriberNumber = SMSutilities.getMSISDN();
			String operatorID = SMSutilities.getOperatorID();
			// Handle user-entered message body
			String messageBody = SMSutilities.getBody();
			if (messageBody == null) messageBody = "";
			
			String response = createMOresponse(messageBody);
			if (response != null) { // Only send out real responses. Otherwise, just don't reply.
				httppost.sendMT(subscriberNumber, operatorID, response);
			} else { // log this condition and ignore
				SMSutilities.logger.log(Level.INFO, "Unrecognized command in MO, will not be replying with an MT: " + messageBody);
				return;
			}
		}
		// else { handle other types of posts? }
	}
}
