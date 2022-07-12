package com.telelanguage.video.service;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

//import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
//import org.jose4j.jwe.JsonWebEncryption;
//import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
//import org.jose4j.keys.PbkdfKey;
//import org.jose4j.lang.JoseException;

public class BlueStreamService {
	private static String bluestreamkey = "52718fe04227871031df8fe04b5f4ccf88dab4753277eb8ce55f2dbe32400fb7";
	
	public static String getBluestreamJWT() {
	
	//public static void main(String[] args) {
//		byte[] bluestreambytes = new BigInteger(bluestreamkey,16).toByteArray();
//		Key key = new PbkdfKey(bluestreamkey);
//		JsonWebEncryption jwe = new JsonWebEncryption();
//		jwe.setPayload("{\r\n" + 
//				"        \"mode\": \"API_UPGRADE\",\r\n" + 
//				"        \"callCenterId\":\"fd625a9e-2819-4a00-8835-e12c1c16eda3\",\r\n" + 
//				"        \"facilityId\": \"3f67456a-1720-4169-848a-207ba6f92a2a\",\r\n" + 
//				"        \"loginId\": \"fd625a9e-2819-4a00-8835-e12c1c16eda3\",\r\n" + 
//				"        \"iss\": \"telelanguage\",\r\n" + 
//				"        \"aud\": \"Bluestream\",\r\n" + 
//				"        \"iat\": 1556703435,\r\n" + 
//				"        \"nbf\": 1556703435,\r\n" + 
//				"        \"exp\": 1556713435\r\n" + 
//				"}");
//		jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.PBES2_HS256_A128KW);
//		jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512);
//		jwe.setKey(key);
		
		String payload = "{ 'mode': 'API_UPGRADE', 'callCenterId':'fd625a9e-2819-4a00-8835-e12c1c16eda3','facilityId': '3f67456a-1720-4169-848a-207ba6f92a2a','loginId': 'fd625a9e-2819-4a00-8835-e12c1c16eda3','iss': 'telelanguage','aud':'Bluestream','iat': 1556703435,'nbf': 1556703435,'exp: 1556713435}";
		
		try {
		    Algorithm algorithm = Algorithm.HMAC256(bluestreamkey);
		    Date date = new Date();

		    String token = JWT.create()
		        .withClaim("mode", "API_UPGRADE")
		        .withClaim("callCenterId", "fd625a9e-2819-4a00-8835-e12c1c16eda3")
		        .withClaim("facilityId", "3f67456a-1720-4169-848a-207ba6f92a2a")
		        .withClaim("loginId", "fd625a9e-2819-4a00-8835-e12c1c16eda3")
		        .withClaim("iss", "telelanguage")
		        .withClaim("aud", "Bluestream")
		        .withClaim("iat", NumericDate.fromMilliseconds(date.getTime()).getValue())
		        .withClaim("nbf", NumericDate.fromMilliseconds(date.getTime()).getValue())
		        .withClaim("exp", NumericDate.fromMilliseconds(date.getTime()+100000).getValue())
		        .sign(algorithm);

			System.out.println("Serialized key: "+token);
			
			BlueStreamJWTRequest jwtRequest = new BlueStreamJWTRequest();
			BlueStreamJWTRequestJwt bsjwt = new BlueStreamJWTRequestJwt();
			jwtRequest.Body = bsjwt;
			bsjwt.jwt = token;
			
			BlueStreamClient bsclient = new BlueStreamClient("https://telelanguage.bluestreamhealth.com/api/");
			BlueStreamJWTResponse resp = bsclient.jwtUpgrade(bsjwt);
			System.out.println(resp);
			if (resp == null) throw new RuntimeException("resp is null");
			if (resp.code == 200) {
				return resp.payload.jwt;
			} else {
				throw new RuntimeException("Unable to place call "+resp.code);
			}
		} catch (JWTCreationException exception){
		    //Invalid Signing configuration / Couldn't convert Claims.
			exception.printStackTrace();
			throw new RuntimeException("Unable to place call "+exception.getMessage());
		}
	}
}
