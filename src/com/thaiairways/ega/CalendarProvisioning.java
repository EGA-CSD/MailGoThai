package com.thaiairways.ega;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CalendarProvisioning {

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

	public static String readICS(String filename) {
		File file = new File(filename);
		String file_ics = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				file_ics += line + "\n";
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(file_ics);
		return file_ics;
	}

	public static LinkedList<String> getUsers( String filename) {
		File file = new File(filename);
		LinkedList<String> users = new LinkedList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				users.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return users;
	}
	
	public static String getAuthToken(HttpURLConnection httpConn, String url, String account, String password,
			String preauth) {
		String authToken = "";
		String preauthValue = "";

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("account", account);
		params.put("by", "name");
		String ts = String.valueOf((new Date()).getTime());
		params.put("timestamp", ts);
		params.put("expires", "0");
		preauthValue = PreAuth.computePreAuth(params, preauth);
		try {
			String xmlInput = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">" 
					+ "<soap:Header>"
					+ "<context xmlns=\"urn:zimbra\"/>" 
					+ "</soap:Header>" 
					+ "<soap:Body>"
					+ "<AuthRequest xmlns=\"urn:zimbraAccount\">" 
					+ "<account by=\"name\">" 
					+ account 
					+ "</account>"
					+ "<preauth timestamp=\"" 
					+ ts + "\" expires=\"0\">"
					+ preauthValue 
					+ "</preauth>"
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
				System.out.println("Requeset :" + out.toString());
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
			System.out.println("Token : " + authToken);

		} catch (Exception e) {
			System.out.println("Error while getOutputStream: " + e.getMessage());
			e.printStackTrace();
		}
		return authToken;
	}

	public static boolean sendAppointment(String webURL, String token, String ics_filename) {
		try {
			URL url = new URL(webURL);
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			String str_ics = readICS(ics_filename);
			String soapRequest = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">"
					+ "<soap:Header>" 
					+ "<context xmlns=\"urn:zimbra\">" 
					+ "<format type=\"xml\"/>"
					+ "<authToken>"
					+ token
					+ "</authToken>" 
					+ "</context>" 
					+ "</soap:Header>" 
					+ "<soap:Body>"
					+ "<ImportAppointmentsRequest xmlns=\"urn:zimbraMail\" ct=\"text/calendar\">" 
					+ " <content>" 
					+ str_ics
					+ "</content>" 
					+ "</ImportAppointmentsRequest>" 
					+ "</soap:Body>" 
					+ "</soap:Envelope>";

			httpConn.setRequestProperty("Content-Length", String.valueOf(soapRequest.length()));
			httpConn.setRequestProperty("Content-Type", "application/soapxml");
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buffer = new byte[soapRequest.length()];
			buffer = soapRequest.getBytes();
			bout.write(buffer);
			byte[] b = bout.toByteArray();

			OutputStream out = null;
			try {
				out = httpConn.getOutputStream();
				out.write(b);
				System.out.println("Requeset :" + out.toString());
				out.close();
			} catch (Exception e) {
				System.out.println("Error while getOutputStream: " + e.getMessage());
				e.printStackTrace();
				return false;
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

			System.out.println("Response :" + response);

		} catch (Exception e) {
			System.out.println("Error while " + e.getMessage());
			e.printStackTrace();

		}
		return true;
	}

	public static boolean sendAppointment2(String webURL, String token, String ics) {
		try {
			URL url = new URL(webURL);
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			// String str_ics =
			// readICS("C:\\Users\\narongsak.mala\\Downloads\\Calendar-2017-08-22-153754.ics");
			String soapRequest = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">"
					+ "<soap:Header>" 
					+ "<context xmlns=\"urn:zimbra\">" 
					+ "<format type=\"xml\"/>"
					+ "<authToken>"
					+ token
					+ "</authToken>" 
					+ "</context>" 
					+ "</soap:Header>" 
					+ "<soap:Body>"
					+ "<ImportAppointmentsRequest xmlns=\"urn:zimbraMail\" ct=\"text/calendar\">" 
					+ " <content>" 
					+ ics
					+ "</content>" 
					+ "</ImportAppointmentsRequest>" 
					+ "</soap:Body>" 
					+ "</soap:Envelope>";

			httpConn.setRequestProperty("Content-Length", String.valueOf(soapRequest.length()));
			httpConn.setRequestProperty("Content-Type", "application/soapxml");
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buffer = new byte[soapRequest.length()];
			buffer = soapRequest.getBytes();
			bout.write(buffer);
			byte[] b = bout.toByteArray();

			OutputStream out = null;
			try {
				out = httpConn.getOutputStream();
				out.write(b);
				System.out.println("Requeset :" + out.toString());
				out.close();
			} catch (Exception e) {
				System.out.println("Error while getOutputStream: " + e.getMessage());
				e.printStackTrace();
				return false;
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

			System.out.println("Response :" + response);

		} catch (Exception e) {
			System.out.println("Error while " + e.getMessage());
			e.printStackTrace();

		}
		return true;
	}

	public static void main(String[] args) {
		// final String WEB_MAIL_URL = "https://accounts.mail.go.th/service/soap";
		// final String PREAUTH_KEY =
		// "105ef8fb544dfd45bf2c770554aa42992f741285d20873316984cb8ee9afd4e5";
		// final String ACCOUNT = "thaiairways_admin@api.mail.go.th";

//		 final String WEB_MAIL_URL = "https://203.150.62.191/service/soap";
//		 final String PREAUTH_KEY = "105ef8fb544dfd45bf2c770554aa42992f741285d20873316984cb8ee9afd4e5";
//		 final String ACCOUNT = "user01@test.thaiairways.com";

		final String WEB_MAIL_URL = "https://192.168.243.146/service/soap";
		final String PREAUTH_KEY = "645870e43919374eb6495b94feb504ac85723d7f6742c56cdb3947a9beaba333";
		final String ACCOUNT = "admin@mail.centos7.lan";
		final String PASSWORD = "";

		LinkedList<String> users = getUsers("example\\users.txt");
		Iterator<String> iterator = users.iterator();
		while( iterator.hasNext() ) {
			String account = iterator.next();
			System.out.println("Sending appointment to "+account);
			try {
				URL url = new URL(WEB_MAIL_URL);
				URLConnection connection = url.openConnection();
				HttpURLConnection httpConn = (HttpURLConnection) connection;
				String authToken = getAuthToken(httpConn, WEB_MAIL_URL, account, PASSWORD, PREAUTH_KEY);
				/* Reuse connection
				connection = url.openConnection();
				httpConn = (HttpURLConnection) connection;
				authToken = getAuthToken(httpConn, WEB_MAIL_URL, ACCOUNT, PASSWORD, PREAUTH_KEY);
				/**********************************************************************************/
				sendAppointment( WEB_MAIL_URL, authToken, "C:\\Users\\narongsak.mala\\Downloads\\Calendar-2017-08-22-153754.ics");
			} catch (Exception e) {
				System.out.println("Error while " + e.getMessage());
				e.printStackTrace();
	
			}

		}
	}

}
