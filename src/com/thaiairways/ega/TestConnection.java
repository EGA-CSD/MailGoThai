package com.thaiairways.ega;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class TestConnection {

	static {
		// Bypass SSL : Use for testing only
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

			public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {

				if (hostname.equals("accounts.mail.go.th") || hostname.equals("203.150.62.10")) {
					return true;
				} else if (hostname.equals("203.150.62.191")) {
					return true;
				} else if (hostname.equals("192.168.243.146")) {
					return true;
				} else {
					return false;
				}
			}
		});
	}

	public static void main(String[] args) {
//		final String WEB_MAIL_URL = "https://accounts.mail.go.th/service/soap";
//		final String ACCOUNT = "thaiairways_admin@api.mail.go.th";
//		final String PASSWORD = "yQR1kcD3";
		
//		final String WEB_MAIL_URL = "https://203.150.62.191/service/soap";
//		final String ACCOUNT = "user01@test.thaiairways.com";
//		final String PASSWORD = "yQR1kcD3";
		
//		final String WEB_MAIL_URL = "https://203.150.62.191/service/soap";
//		final String ACCOUNT = "test002@test.thaiairways.com";
//		final String PASSWORD = "Acho20mkr";
		
//		final String WEB_MAIL_URL = "https://203.150.62.191/service/soap";
//		final String ACCOUNT = "test002@mgt.com";
//		final String PASSWORD = "Acho20mkr";
//		
		final String WEB_MAIL_URL = "https://192.168.243.146/service/soap";
		final String ACCOUNT = "pepsi3@mgt.com";
		final String PASSWORD = "Acho20mkr";

		try {
			URL url = new URL(WEB_MAIL_URL);
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;

			String xmlInput = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">" 
							+ "<soap:Header>"
							+ "<context xmlns=\"urn:zimbra\"/>" 
							+ "</soap:Header>"
							+ "<soap:Body>" 
							+ "<AuthRequest xmlns=\"urn:zimbraAccount\">"
							+ "<account by=\"name\">"+ACCOUNT+"</account>"
							+ "<password>"+PASSWORD+"</password>" 
							+ "</AuthRequest>" 
							+ "</soap:Body>" 
							+ "</soap:Envelope>";

			httpConn.setRequestProperty("Content-Length", String.valueOf(xmlInput.length()));
			httpConn.setRequestProperty("Content-Type", "application/soapxml");
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buffer = new byte[xmlInput.length()];
			buffer = xmlInput.getBytes();
			bout.write(buffer);
			byte[] b = bout.toByteArray();

			OutputStream out = null;
			try {
				out = httpConn.getOutputStream();
				out.write(b);
				System.out.println("Requeset :"+out.toString());
				out.close();
			} catch (Exception e) {
				System.out.println("Error while getOutputStream: " + e.getMessage());
				e.printStackTrace();
			}

			InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
			BufferedReader in = new BufferedReader(isr);

			String response = "";
			String readBuffer = "";

			while ((readBuffer = in.readLine()) != null) {
				System.out.println(response);
				System.out.println("Reading");
				response = response + readBuffer;
			}
			
			System.out.println("Response :"+response);
			String authToken = response.split("<authToken>")[1].split("</authToken>")[0];
			System.out.println("Token : "+ authToken);
			System.out.println("Url: "+WEB_MAIL_URL.split("soap")[0]+"preauth?isredirect=1&authtoken="+authToken);
			//https://server/service/preauth?isredirect=1&authtoken={...}
		} catch (Exception e) {
			System.out.println("Error while " + e.getMessage());
			e.printStackTrace();

		}
		
		
	}

}
