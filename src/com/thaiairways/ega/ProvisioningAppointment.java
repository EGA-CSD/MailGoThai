package com.thaiairways.ega;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;

public class ProvisioningAppointment {

	static {
		// Bypass SSL : Use for testing only
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

			public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {

				if (hostname.equals("accounts.mail.go.th") || hostname.equals("203.150.62.10")) {
					return true;
				} else if (hostname.equals("203.150.62.191")) {
					return true;
				} else if (hostname.equals("192.168.243.129")) {
					return true;
				} else {
					return false;
				}
			}
		});
	}

	public static String getAuthToken(HttpURLConnection httpConn, String url, String account, String password, String preauth){
		String authToken = "";
		String preauthValue = "";

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("account", account);
		params.put("by", "name");
		String ts = String.valueOf((new Date()).getTime());
		params.put("timestamp", ts);
		params.put("expires", "0");
		preauthValue = PreAuth.computePreAuth(params, preauth);
		try{
			String xmlInput = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">" 
					+ "<soap:Header>"
					+ "<context xmlns=\"urn:zimbra\"/>" 
					+ "</soap:Header>"
					+ "<soap:Body>" 
					+ "<AuthRequest xmlns=\"urn:zimbraAccount\">"
					+ "<account by=\"name\">"+account+"</account>"
					+ "<preauth timestamp=\""+ts+"\" expires=\"0\">"+preauthValue+"</preauth>" 
					+ "</AuthRequest>" 
					+ "</soap:Body>" 
					+ "</soap:Envelope>";

			httpConn.setRequestProperty("Content-Length", String.valueOf(xmlInput.length()));
			httpConn.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");
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
			
			authToken = response.split("<authToken>")[1].split("</authToken>")[0];
			System.out.println("Token : "+ authToken);
		
		}catch (Exception e) {
			System.out.println("Error while getOutputStream: " + e.getMessage());
			e.printStackTrace();
		}
		return authToken;
	}
	
	public static void main(String[] args) {
//		final String WEB_MAIL_URL = "https://accounts.mail.go.th/service/soap";
//		final String PREAUTH_KEY = "105ef8fb544dfd45bf2c770554aa42992f741285d20873316984cb8ee9afd4e5";
//		final String ACCOUNT = "thaiairways_admin@api.mail.go.th";
		
//		final String WEB_MAIL_URL = "https://203.150.62.191/service/soap";
//		final String PREAUTH_KEY = "105ef8fb544dfd45bf2c770554aa42992f741285d20873316984cb8ee9afd4e5";
//		final String ACCOUNT = "user01@test.thaiairways.com";
		
		final String WEB_MAIL_URL = "https://192.168.243.129/service/soap";
		final String PREAUTH_KEY = "a2e7b7c513e2472c49d83d9b62598c6f2c3c966abe823d0b989105c92781a536";
		final String ACCOUNT = "admin@mail.centos7.lan";
		final String PASSWORD = "Acho20mkr";

		try {
			URL url = new URL(WEB_MAIL_URL);
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			String authToken = getAuthToken(httpConn, WEB_MAIL_URL, ACCOUNT, PASSWORD, PREAUTH_KEY);
			connection = url.openConnection();
			httpConn = (HttpURLConnection) connection;
			authToken = getAuthToken(httpConn, WEB_MAIL_URL, ACCOUNT, PASSWORD, PREAUTH_KEY);
		} catch (Exception e) {
			System.out.println("Error while " + e.getMessage());
			e.printStackTrace();

		}
		
		
	}

}
