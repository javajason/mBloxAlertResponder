package smsresponder;
// Adapted from http://www.java-samples.com/java/POST-toHTTPS-url-free-java-sample-program.htm
import java.io.*;
import java.net.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.logging.*;

public class httppost {
	// Alvin - xml, qsv-rhgw42
	// Jannsen - xml4, qsv-rhgw265

	//  POST to http://xml.us.mblox.com:8180/send
	//  or to http://qsv-rhgw265:8180/send
	// public static String DEFAULT_DESTINATION_URL = new String("http://xml.us.mblox.com");
	// public static String DESTINATION_URL = new String("http://qsv-rhgw42");
	public static String DEFAULT_DESTINATION_URL = new String("http://qsv-rhgw265");

	public static final String PORT = new String("8180");
	public static final String URLPATH = new String("/send");
	
	public static final String OPERATOR_ID_TMO = "31004";
	public static final String OPERATOR_ID_VZW = "31003";
	public static final String OPERATOR_ID_ATT = "31002";
	public static final String OPERATOR_ID_SPRINT = "31005";
	public static final String OPERATOR_ID_BOOST1 = "31008";
	public static final String OPERATOR_ID_BOOST2 = "31011";
	public static final String OPERATOR_ID_VIRGIN = "31010";
	
	// public static String [] sureRouteCarriers = {OPERATOR_ID_TMO, OPERATOR_ID_SPRINT, OPERATOR_ID_BOOST1, OPERATOR_ID_BOOST2, OPERATOR_ID_VIRGIN};
	public static final String [] sureRouteCarriers = {OPERATOR_ID_TMO};
	
	public static final String DEFAULT_USER = "AlvintestUS";
	public static final String DEFAULT_PASS = "LameLame";
	public static final String DEFAULT_SERVICE_ID_TMO = "27433";
	public static final String DEFAULT_SERVICE_ID_VZW = "10293";
	public static final String DEFAULT_SERVICE_ID_OTHER = "";
	// public static final String DEFAULT_SYSTEMTYPE = "redhot_31310a";
	
	public static final String DEFAULT_USER_MT = "AlvinTestMT";
	public static final String DEFAULT_PASS_MT = "asdfqwer";
	public static final String DEFAULT_SERVICE_ID_MT = "49884";
	public static final String DEFAULT_PROFILE_ID_MT = "32319";
	
	//public static String USER = "JanssenMT";
	// public static String PASS = "KPgybhX92";
	// public static String SERVICE_ID = "60061";
	
	public static final String DEFAULT_SHORT_CODE = "28444";
	
	public static final String DEST_NUMBER = "16504776910";
	
	public static String TEXTBODY = "Testing";
	// public static Boolean useSureRoute = false;
	
	/*
	private static String buildXMLbody()
	{
		String subscriberNumber = DEST_NUMBER; //"12063802788";
		String messageBody = TEXTBODY; //"Test message from mblox!";
		return buildXMLbody(subscriberNumber, messageBody);
	}
	*/
	/*
	private static String buildXMLbody(String subscriberNumber, String messageBody)
	{
		String username = DEFAULT_USER;
		String userpass = DEFAULT_PASS;
		String serviceID = DEFAULT_SERVICE_ID_MT;
		return buildXMLbody(username, userpass, subscriberNumber, serviceID, messageBody);
	}
	*/
	
	/*
	private static String buildXMLbody(String subscriberNumber, String operatorID, String messageBody)
	{
		String username = DEFAULT_USER;
		String userpass = DEFAULT_PASS;
		String serviceID = DEFAULT_SERVICE_ID_MT;
		String shortcode = DEFAULT_SHORT_CODE;
		return buildXMLbody(username, userpass, shortcode, subscriberNumber, serviceID, messageBody, operatorID);
	}
	*/
	
	// This is the version of buildXMLbody that currently gets called directly.
	private static String buildXMLbody(String subscriberNumber, String operatorID, String serviceID, String messageBody)
	{
		String submittedUsername = SMSutilities.getUsername();
		String username;
		String userpass;

		if (operatorID == null)
		{			
			//username = DEFAULT_USER_MT;
			//userpass = DEFAULT_PASS_MT;
			
			// Need to switch to MT user account
			// username = ServletContextClass.getValue(submittedUsername + "_" + SMSutilities.USERNAMEMTKEY);
			username = submittedUsername; // TODO change later?
			userpass = ServletContextClass.getValue(submittedUsername + "_" + SMSutilities.PASSWORDMTKEY);
		} else {
			//username = DEFAULT_USER;
			username = submittedUsername;
			//userpass = DEFAULT_PASS;
			userpass = ServletContextClass.getValue(submittedUsername + "_" + SMSutilities.PASSWORDKEY); 
		}
		// String shortcode = DEFAULT_SHORT_CODE; //"28444";
		String shortcode = SMSutilities.getShortCode();
		return buildXMLbody(username, userpass, shortcode, subscriberNumber, serviceID, messageBody, operatorID);
	}
	
	/*
	private static String buildXMLbody(String username, String userpass, String subscriberNumber, String serviceID, String messageBody)
	{
		String shortcode = DEFAULT_SHORT_CODE; //"28444";
		return buildXMLbody(username, userpass, shortcode, subscriberNumber, serviceID, messageBody, null);
	}
	*/
	
	private static String buildXMLbody(String username, String userpass, String shortcode, String subscriberNumber, String serviceID, String messageBody, String operatorID)
	{
		String profileID;
		String systemType = null;
		
		if (operatorID == null)
		{	// Use SureRoute account
			
			// profileID = DEFAULT_PROFILE_ID_MT;
			profileID = ServletContextClass.getValue(username + "_" + SMSutilities.PROFILEIDMTKEY);
			userpass = ServletContextClass.getValue(username + "_" + SMSutilities.PASSWORDMTKEY);
			username = ServletContextClass.getValue(username + "_" + SMSutilities.USERNAMEMTKEY);
			systemType = ServletContextClass.getValue(username + "_" + SMSutilities.SYSTEMTYPEMTKEY);
		} else {
			profileID = "-1";
			systemType = ServletContextClass.getValue(username + "_" + SMSutilities.SYSTEMTYPEKEY);
		}
				
		return buildXMLbody(username, userpass, profileID, shortcode, subscriberNumber, serviceID, messageBody, operatorID);
		// TODO: return buildXMLbody(username, userpass, profileID, shortcode, subscriberNumber, serviceID, messageBody, operatorID, systemType);
	}

	private static String buildXMLbody(String username, String userpass, String profileID, String shortcode, String subscriberNumber, String serviceID, String messageBody, String operatorID)
	// TODO private static String buildXMLbody(String username, String userpass, String profileID, String shortcode, String subscriberNumber, String serviceID, String messageBody, String operatorID, String systemType)
	{
		String body =
		"<NotificationRequest Version=\"3.5\">" +
		"<NotificationHeader>" +
		"  <PartnerName>" + username + "</PartnerName>" +
		"  <PartnerPassword>" + userpass + "</PartnerPassword>" +
		// TODO:
		// (systemType != null ?
		//		"  <Username>" + DEFAULT_SYSTEMTYPE + "</Username>"
		//		: "") +
		"</NotificationHeader>" +
		"<NotificationList BatchID=\"10\">" +
		"<Notification SequenceNumber=\"1\" MessageType=\"SMS\">" +
		"  <Message>" + messageBody + "</Message>" +
		"  <Profile>" + profileID + "</Profile>" +
		"  <SenderID Type=\"Shortcode\">" + shortcode + "</SenderID>" +
		(operatorID != null ?
				"  <Operator>" + operatorID + "</Operator>" +
				"  <Tariff>0</Tariff>"
				: "") +
		"  <Subscriber>" +
		"     <SubscriberNumber>" + subscriberNumber + "</SubscriberNumber>" +
		"  </Subscriber>" +
		(operatorID != null ?
				"  <Tags>" +
				"    <Tag Name=\"Program\">stdrt</Tag>" +
				"  </Tags>"
				: "") +
		(serviceID != null ?
				"  <ServiceId>" + serviceID + "</ServiceId>"
				: "") +
		"</Notification>" +
		"</NotificationList>" +
		"</NotificationRequest>";
		
		return body;
	}
	
	public static Integer sendMT(String subscriberNumber, String messageBody)
	{
		return sendMT(subscriberNumber, null, messageBody);
	}
	
	public static Integer sendMT(String subscriberNumber, String operatorID, String messageBody)
	{
		String serviceID = null;
		
		String username = SMSutilities.getUsername();
		String shortCode = SMSutilities.getShortCode();
		
		if (operatorID == null) {
			// If no operator ID, use MT account
			serviceID = ServletContextClass.getValue(username + shortCode + "_" + SMSutilities.SERVICEIDMTKEY);		
		} else {
			// check to make sure operator is not in list of unsupported operators for two-way account (if it is, we'll use MT account)

			// SERVICE_ID_OTHER is the default, but first check if operatorID is on
			// list of operators that we should only reach with SureRoute.
			// (This is a workaround for a current bug. For MOs from Virgin and Boost,
			// our platform is filling in the OperatorID for Sprint instead.)
			for (int oper = 0; oper < sureRouteCarriers.length; oper++) {
				if (operatorID.equals(sureRouteCarriers[oper])) {
					serviceID = ServletContextClass.getValue(username + shortCode + "_" + SMSutilities.SERVICEIDMTKEY);
					//operatorID = null;
					// break;
					return sendMT(subscriberNumber, null, serviceID, messageBody);
				}
			}
		}
		// if we got to this point, operatorID is not null and not in list of unsupported operators. So, continue to look for matching serviceID.
		if (operatorID.equals(OPERATOR_ID_TMO)) { // T-Mobile
			// serviceID = ServletContextClass.getValue(username + "_" + SMSutilities.SERVICEIDMTKEY);
			// operatorID = null;
			serviceID = ServletContextClass.getValue(username + shortCode + shortCode + "_" + SMSutilities.SERVICEIDTMOKEY);
		} else if (operatorID.equals(OPERATOR_ID_VZW)) { // Verizon
			serviceID = ServletContextClass.getValue(username + shortCode + "_" + SMSutilities.SERVICEIDVZWKEY);
		} else { // Other carrier
			serviceID = ServletContextClass.getValue(username + shortCode + "_" + SMSutilities.SERVICEIDOTHERKEY);
		}

		return sendMT(subscriberNumber, operatorID, serviceID, messageBody);
	}
	
	public static Integer sendMT(String urlString, String username, String userpass, String profileID, String shortcode, String subscriberNumber, String operatorID, String serviceID, String messageBody)
	{
		URL url;
		int HTTPResponse = 0;
		
		try {
			url = new URL(urlString);
		}
		catch (MalformedURLException m) {
			// Something wrong with URL string. Instead of using it, just use default URL.
			try {
				url = new URL(DEFAULT_DESTINATION_URL + ":" + PORT + URLPATH);
			}
			catch (MalformedURLException m2) {
				// if it still throws this exception, just give up and return -1.
				System.out.println(m2.toString());
				return -1;
			}
		}
		String xmlSubmit = buildXMLbody(username, userpass, profileID, shortcode, subscriberNumber, serviceID, messageBody, operatorID);

		try {
			HTTPResponse = sendMT(url, xmlSubmit);
			return HTTPResponse;
		}
		catch (IOException ioe) {
			SMSutilities.logger.log(Level.WARNING, "Exception caught when trying to POST XML. Will try once more.\nException: " + ioe.toString());
			// try one more time
			try {
				HTTPResponse = sendMT(url, xmlSubmit);
				return HTTPResponse;
			}
			catch (Exception e) {
				SMSutilities.logger.log(Level.SEVERE, "Unable to post MT. Generic exception caught: " + e.toString());
				HTTPResponse = -1;
			}
		}

		return HTTPResponse;
	}
	
	public static Integer sendMT(String subscriberNumber, String operatorID, String serviceID, String messageBody)
	{
		URL url;
		String username = SMSutilities.getUsername();
		String destinationURL = ServletContextClass.getValue(username + "_" + SMSutilities.DESTURLKEY);
		Integer HTTPResponse = -1;
		
		try {
			// url = new URL(DESTINATION_URL + ":" + PORT + URLPATH);
			url = new URL(destinationURL);
		}
		catch (MalformedURLException m) {
			// if it still throws this exception, just give up and return -1.
			System.out.println(m.toString());
			return -1;
		}

		String xmlSubmit = buildXMLbody(subscriberNumber, operatorID, serviceID, messageBody);
		try {
			HTTPResponse = sendMT(url, xmlSubmit);
			return HTTPResponse;
		}
		catch (IOException ioe) {
			SMSutilities.logger.log(Level.WARNING, "Exception caught when trying to POST XML. Will try once more.\nException: " + ioe.toString());
			// try one more time
			try {
				HTTPResponse = sendMT(url, xmlSubmit);
				return HTTPResponse;
			}
			catch (Exception e) {
				SMSutilities.logger.log(Level.SEVERE, "Unable to post MT. Generic exception caught: " + e.toString());
				HTTPResponse = -1;
			}
		}

		return HTTPResponse;
	}
	
	// public static Integer _sendMT(String subscriberNumber, String operatorID, String serviceID, String messageBody)
	public static Integer sendMT(URL url, String xmlSubmit) throws IOException
	{
		String hostname = "";
		String fullURL = "";
		String XMLline;
		String XMLdoc = "";
		int HTTPResponse = 0;
		
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			SMSutilities.logger.log(Level.WARNING, "Attempt to post MT failed due to inability to reach host "
				+ hostname
				+ " (" + fullURL + "). Will retry once.");
			try { // second try
				connection = (HttpURLConnection) url.openConnection();
			} catch (IOException e2) {
				SMSutilities.logger.log(Level.SEVERE, "Second attempt to post MT failed, again due to inability to reach host "
						+ hostname + ". Will not retry.");
				e2.printStackTrace();
				return -1;
			}
		}
		if (connection == null) return -1;
			
		// if we got to this point, the connection likely succeeded.
		try {
			fullURL = connection.getURL().toString(); // for use later in logs
			hostname = connection.getURL().getHost(); // for use later in logs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			// connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Type", "application/xml");
			
			String POST_data = "XMLDATA=" + xmlSubmit;
			
			// open up the output stream of the connection 
			DataOutputStream output = new DataOutputStream( connection.getOutputStream() ); 

			// send data on connection 
			output.writeBytes(POST_data);
			SMSutilities.logger.log(Level.INFO, "MT send: " + POST_data);
			
			HTTPResponse = connection.getResponseCode();
			System.out.println("Response:" + HTTPResponse +
					" (" + connection.getResponseMessage() + ")");

			InputStream is = connection.getInputStream();
			output.close();
			
			BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			do
			{
				XMLline = input.readLine();
				XMLdoc += XMLline;
				
			} while (XMLline != null);

			// System.out.println("Returned: " + XMLdoc);
			input.close();
			is.close();
			connection.disconnect();
			SMSutilities.logger.log(Level.INFO, "MT send result: " + XMLdoc);
		} catch (IOException ioe) {
			SMSutilities.logger.log(Level.SEVERE, "Exception caught while attempting to POST XML data to "
				+ hostname);
			throw ioe;
		}
		/*
		} catch (SocketTimeoutException s) {
			// Connectivity problem. Socket timed out.
			SMSutilities.logger.log(Level.SEVERE, "Attempt to post MT failed due to Socket Timeout. Will retry once.");
			s.printStackTrace();
			// Retry logic:
			connection = (HttpURLConnection) url.openConnection();
		}
		*/
		catch(Exception e) 
		{
			SMSutilities.logger.log(Level.SEVERE, "Generic exception caught while attempting to POST XML data to "
					+ hostname + ". Aborting POST...");
			e.printStackTrace();
			return -1;
		}
		finally {
			// For later:
			// capture DR, parse, and return PTT status code
			// SMSutilities.buildResult(XMLdoc);
			// HTTPResponse = 0;
		}		
		return HTTPResponse;
	}
}