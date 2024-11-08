package smsresponder;

// import java.util.EventListener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServletContextClass implements ServletContextListener
{
	// changed getValue() and Strings (below) to static to make all calls static
	
	public static String keyword;
	public static String STOP;
	public static String HELP;
	public static String START;
	public static String keywordresponse;
	public static String motd;

	/*
	public String keyword;
	public String STOP;
	public String HELP;
	public String START;
	public String keywordresponse;
	public String motd;
	*/
	private static InstanceConfig conf = null;
	
	/*
	public void ServletContextClass()
	{
		conf = new InstanceConfig();
	}
	*/

	// deprecate?
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// Initialize settings at server startup
		InstanceConfig conf = new InstanceConfig();
		
		keyword = conf.getProperty("keyword");
				
		keywordresponse = conf.getProperty("blank");
		STOP = conf.getProperty("STOP");
		HELP = conf.getProperty("HELP");
		START = conf.getProperty("START");
		motd = conf.getProperty("motd");
		
		/*
		destURL = http://xml.us.mblox.com
		shortcode = 28444  
		keyword = GOOGLE
		*/
	}
	
	public static String getValue(String key) {
		if (key == null) {
			return "";
		}
		if (conf == null) {
			conf = new InstanceConfig();
		}
		return (conf.getProperty(key.trim()));
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub		
	}
}
