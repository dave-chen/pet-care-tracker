
/**
 *****************************************************************************
 * Copyright (c) 2015 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial Contribution
 *
 *
 * 5/10/2019
 * Cindy Lee - Extended this sample for Pet Care Tracker solution purpose.
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.application;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import com.jayway.jsonpath.JsonPath;


/**
 * This sample shows how to build a scalable applications which will load balance 
 * messages across multiple instances of the application.
 * 
 * Shared subscription allows one to build scalable applications which will load balance 
 * device events across multiple instances of the application by making a few changes to 
 * how the application connects to the IBM Watson IoT Platform. The shared subscription 
 * might be needed if one instance of back-end enterprise application can not keep up with 
 * the number of messages being published to a specific topic space, say for e.g., if many 
 * devices were publishing events that are being processed by a single application. 
 * 
 * The Watson IoT service extends the MQTT 3.1.1 specification to provide support for shared 
 * subscriptions and is limited to non-durable subscriptions only. The IoT doesn�t retain messages 
 * when the non-durable application disconnects from Watson IoT Platform.
 * 
 * In order to enable the shared subscription or scalable application support, Application(s) must 
 * supply a client id of the form A:org_id:app_id while connecting to Watson IoT Platform,
 * 
 *    A indicates the application is a scalable application
 *    org_id is your unique organization ID, assigned when you sign up with the service. It will be a 6 character alphanumeric string.
 *    app_id is a user-defined unique string identifier for this client.
 */

public class SharedSubscriptionSample {

	private final static String PROPERTIES_FILE_NAME = "/application.properties";

    public static final String ACCOUNT_SID = "AC9c80fb982caa830cd11f991aa1e64716";
    public static final String AUTH_TOKEN =  "a698b8df6db3200db17042286dbc47a3";

	public static void main(String[] args) throws Exception {
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(SharedSubscriptionSample.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
		
		ApplicationClient myAppClient = null;
		try {
			//Instantiate the class by passing the properties file
			myAppClient = new ApplicationClient(props);
			myAppClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		myAppClient.setEventCallback(new MyEventCallback());
		myAppClient.subscribeToDeviceEvents();
	}
	
	private static class MyEventCallback implements EventCallback {

		public void processEvent(Event e) {
			System.out.println("Event:: " + e.getDeviceId() + ":" + e.getEvent() + 
					":" + e.getPayload() +" received at time:: "+new Date());


			//Send SMS alert when unusual data is detected
			try{
				//The normal body temperature for dogs is between 101 and 102.5 F
				Double temperature = JsonPath.read(e.getPayload(), "$.d.p");
				Integer sound = JsonPath.read(e.getPayload(), "$.d.s");
				//System.out.println("temperature = " + temperature);
				if (temperature > 90){
					sendSMS("Notification from Smart Pet Care - Unusual temperature " + temperature + " has been detected!");
				}
				if (sound > 500){
					sendSMS("Notification from Smart Pet Care - Unusual sound level " + sound + " has been detected!");
				}
			}catch (Exception ex)
			{
				System.out.println(ex.getMessage());
			}

		}

		public void processCommand(Command cmd) {
			System.out.println("Command " + cmd.getPayload());
		}
	}

    private static void sendSMS(String msg)
    {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message message = Message
                .creator(new PhoneNumber("+13108668380"), // to
                        new PhoneNumber("+14152126896"), // from
                        msg)
                .create();

        System.out.println(message.getSid());

    }
}
