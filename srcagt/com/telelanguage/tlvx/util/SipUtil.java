package com.telelanguage.tlvx.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SipUtil
 */
public class SipUtil 
{
	private static final Pattern p = Pattern.compile("sip\\:([a-zA-Z0-9]+)@(.*)");
	private static final Pattern np = Pattern.compile("[0-9]+");
	private static final Pattern tp = Pattern.compile("([0-9]{3})-([0-9]{3})-([0-9]{4})");
	
	/**
	 * return the user part of the SIP URI as the phone number
	 * 
	 * @param uri
	 * @return
	 */
	public static String getPhoneNumber (String uri)
	{
		if (uri!=null)
		{
			Matcher matcher = p.matcher(uri);
			if (matcher.matches())
			{
				return formatNumber(matcher.group(1));
			}
			else
			{
				return formatNumber(uri);
			}
		}
		return uri;
	}
	
	public static String getUnformattedPhoneNumberFromSipUri(String uri) {
		if (uri!=null)
		{
			Matcher matcher = p.matcher(uri);
			if (matcher.matches())
			{
				return matcher.group(1);
			}
			else
			{
				return uri;
			}
		}
		return uri;
	}
	
	public static boolean validateNumber(String number) {
		return (np.matcher(number).matches() || tp.matcher(number).matches());
	}
	
	/**
	 * try and format a number with area code brackets, etc
	 * 
	 * @param number
	 * @return
	 */
	public static String formatNumber (String number)
	{
		if (number!=null)
		{
			if (np.matcher(number).matches())
			{
				if (number.length()==10)
				{
					return "(" + number.substring(0,3) + ") " + number.substring(3,6) + "-" + number.substring(6);
				}
				if (number.length()==7)
				{
					return number.substring(0,3) + "-" + number.substring(3);
				}
			}
			else
			{
				Matcher m = tp.matcher(number);
				if (m.matches())
				{
					return "(" + m.group(1) + ") "+ m.group(2) + "-" + m.group(3);
				}
			}
		}
		
		return number;
	}
	
	public static void main (String args[]) throws Exception
	{
		System.err.println(getPhoneNumber("sip:foo@foo.bar"));
		System.err.println(getPhoneNumber("sip:12345@foo.bar"));
		System.err.println(getPhoneNumber("sip:555@foo.bar"));
		System.err.println(getPhoneNumber("sip:4045551212@foo.bar"));
		System.err.println(getPhoneNumber("sip:5551212@foo.bar"));
		System.err.println(getPhoneNumber("5551212"));
		System.err.println(getPhoneNumber("404-555-1212"));
		System.err.println(getUnformattedPhoneNumberFromSipUri("sip:foo@foo.bar"));
		System.err.println(getUnformattedPhoneNumberFromSipUri("sip:12345@foo.bar"));
		System.err.println(getUnformattedPhoneNumberFromSipUri("sip:555@foo.bar"));
		System.err.println(getUnformattedPhoneNumberFromSipUri("sip:4045551212@foo.bar"));
		System.err.println(getUnformattedPhoneNumberFromSipUri("sip:5551212@foo.bar"));
		System.err.println(getUnformattedPhoneNumberFromSipUri("5551212"));
		System.err.println(getUnformattedPhoneNumberFromSipUri("404-555-1212"));
	}
}
