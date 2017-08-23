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

public class TestPreAuth {

	static {
		// Bypass SSL : Use for testing only
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

			public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {

				if (hostname.equals("accounts.mail.go.th") || hostname.equals("203.150.62.10")) {
					return true;
				} else if (hostname.equals("203.150.62.191")) {
					return true;
				} else if (hostname.equals("192.168.243.149")) {
					return true;
				} else {
					return false;
				}
			}
		});
	}

	public static void main(String[] args) {
//		final String WEB_MAIL_URL = "https://accounts.mail.go.th/service/soap";
//		final String PREAUTH_KEY = "105ef8fb544dfd45bf2c770554aa42992f741285d20873316984cb8ee9afd4e5";
//		final String ACCOUNT = "thaiairways_admin@api.mail.go.th";
		
//		final String WEB_MAIL_URL = "https://203.150.62.191/service/soap";
//		final String PREAUTH_KEY = "105ef8fb544dfd45bf2c770554aa42992f741285d20873316984cb8ee9afd4e5";
//		final String ACCOUNT = "test002@test.thaiairways.com";
		
//		final String WEB_MAIL_URL = "https://203.150.62.191/service/soap";
//		final String PREAUTH_KEY = "c51dea711e61820f47f55cf77292062470062b83ff62ac34df63663ad6e9920a";
//		final String ACCOUNT = "test002@mgt.com";
	
		final String WEB_MAIL_URL = "https://192.168.243.149/service/soap";
		final String PREAUTH_KEY = "645870e43919374eb6495b94feb504ac85723d7f6742c56cdb3947a9beaba333";
		final String ACCOUNT = "admin@mail.centos7.lan";

		String preauthValue = "";

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("account", ACCOUNT);
		params.put("by", "name");
		String ts = String.valueOf((new Date()).getTime());
		params.put("timestamp", ts);
		params.put("expires", "0");
		preauthValue = PreAuth.computePreAuth(params, PREAUTH_KEY);
		
		System.out.println("timestamp: "+ts);
		System.out.printf("PreAuthen: %s\n", preauthValue	);
		System.out.println("Url: "+WEB_MAIL_URL.split("soap")[0]+"preauth?account="+ACCOUNT+"&expires=0&timestamp="+ts+"&preauth="+preauthValue);
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
				
			System.out.println("Response :"+response);
			String authToken = response.split("<authToken>")[1].split("</authToken>")[0];
			System.out.println("Token : "+ authToken);

		} catch (Exception e) {
			System.out.println("Error while " + e.getMessage());
			e.printStackTrace();

		}
		
		
	}

}
