package com.telelanguage.tlvx.service;

import java.security.MessageDigest;

public class TestPassword {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String password = "testuser00120170329022005";
		System.out.println("Input password: "+password);
		System.out.println("SHA-256 output: "+sha256(password));

	}
	
	public static String sha256(String base) {
	    try{
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        byte[] hash = digest.digest(base.getBytes("UTF-8"));
	        StringBuffer hexString = new StringBuffer();

	        for (int i = 0; i < hash.length; i++) {
	            String hex = Integer.toHexString(0xff & hash[i]);
	            if(hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }

	        return hexString.toString();
	    } catch(Exception ex){
	       throw new RuntimeException(ex);
	    }
	}

}
