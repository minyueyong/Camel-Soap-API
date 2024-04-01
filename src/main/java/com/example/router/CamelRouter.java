/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.router;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;

import org.apache.camel.builder.RouteBuilder;

import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.example.builder.GetWordRequestBuilder;
import com.example.generated.NumberConversion;
import com.example.generated.NumberConversionSoapType;
import com.example.generated.NumberToWords;
import com.example.generated.NumberToWordsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.apache.camel.model.rest.RestParamType.body;
import static org.apache.camel.model.rest.RestParamType.path;

import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.camel.component.cxf.DataFormat;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.dataformat.soap.SoapDataFormat;
import org.apache.camel.dataformat.soap.name.ServiceInterfaceStrategy;
import org.apache.camel.language.xpath.XPathBuilder;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints
 * to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto-detect this route when starting.
 */
@Component
public class CamelRouter extends RouteBuilder {

	@Autowired
	private CamelContext camelContext;
	@Autowired
	private ProducerTemplate template;

	
	ObjectMapper objectMapper = new ObjectMapper();
	/*
	 * @Override public void configure() throws Exception {
	 * 
	 * ------------------------------SOAP to
	 * XML-------------------------------------
	 * 
	 * // JAXB is used to map Java classes to XML representations and vice versa
	 * 
	 * SoapDataFormat xmlDataFormat = new SoapDataFormat("com.example.generated",
	 * new ServiceInterfaceStrategy(NumberConversionSoapType.class, false)); //
	 * SoapJaxbDataFormat xmlDataFormat = new //
	 * SoapJaxbDataFormat(NumberToWords.class.getPackage().getName(), new //
	 * ServiceInterfaceStrategy(null, false) );
	 * xmlDataFormat.setContextPath(NumberToWords.class.getPackage().getName());
	 * 
	 * JAXBContext con =
	 * JAXBContext.newInstance(NumberToWords.class.getPackage().getName(),
	 * NumberToWords.class.getClassLoader());
	 * 
	 * 
	 * CxfEndpoint endpoint = createCxfEndpoint(
	 * "https://www.dataaccess.com/webservicesserver/NumberConversion.wso",
	 * NumberConversionSoapType.class, "NumberConversionSoap", "NumberConversion",
	 * "src/main/resources/wsdl/NumberConversion.wsdl");
	 * 
	 * from("direct:soap") .log("start sending SOAP ")
	 * 
	 * 
	 * .unmarshal(xmlDataFormat) // unmarshal into the JAXB annotated class which is
	 * NumberToWordsRequest .log("Unmarshal Successfully").process(exchange -> { //
	 * Extract ubiNum from the response NumberToWords request =
	 * exchange.getIn().getBody(NumberToWords.class); // after unmarshal, get the //
	 * request body BigInteger ubiNum = request.getUbiNum();
	 * 
	 * System.out.println(request.getUbiNum());
	 * 
	 * })
	 * 
	 * .setHeader(CxfConstants.OPERATION_NAME, constant("NumberToWords"))
	 * 
	 * .to("requestEndpoint");
	 * 
	 * }
	 */

	@Override
	public void configure() throws Exception {

		// Add route to send SOAP request using ProducerTemplate
		from("direct:soap") // Trigger the route once
				.process(exchange -> {
					// Create an instance of the web service client
					NumberConversion service = new NumberConversion();
					NumberConversionSoapType port = service.getNumberConversionSoap();

					System.out.println(exchange.getMessage().getBody());

					String requestData = extractElementFromSoapRequest(exchange.getMessage().getBody(String.class));
					System.out.println("request Data " + requestData);
					
					String result = port.numberToWords(new BigInteger(requestData)); // Example input
                       
					// Set the result as the body of the message
					exchange.getMessage().setBody(result);
					
					 // Create a map to hold the response with a specific key
			        Map<String, String> responseMap = new HashMap<>();
			        responseMap.put("ubiNum", result);

			        // Set the map as the body of the message
			        exchange.getMessage().setBody(responseMap);
					
			        
			        //This one is to get the SOAP response
					  NumberToWordsResponse response = new NumberToWordsResponse();
					 response.setNumberToWordsResult(result);

					   exchange.getContext().createProducerTemplate()
		                .sendBody("direct:marshal", response);
				

				})  .marshal().json(JsonLibrary.Jackson)    //convert the map into JSON format
				.log("Response from web service (JSON): ${body}");;// Marshaling to JSON format
		
				
		//Convert NumberToWordResponse into XML format
		from("direct:marshal")
	    .process(exchange -> {
	        // Get the response from the previous route
	        String response = exchange.getIn().getBody(String.class);

	        // Set the body of the message to the response received from the previous route
	        exchange.getIn().setBody(response);
	    })
	    // Marshal the object to XML using JAXB
	    .marshal().jaxb()
	    // Log the marshaled XML
	    .log("Marshaled XML: ${body}");


	}

	private String extractElementFromSoapRequest(String soapRequest) throws Exception {
		// Define the XPath expression to extract the element
		String xpathExpression = "//*[local-name()='ubiNum']/text()";

		// Use XPath to extract the element from the SOAP request
		// You can also use namespaces if required
		return XPathBuilder.xpath(xpathExpression).evaluate(camelContext, soapRequest);
	}

	public CxfEndpoint createCxfEndpoint(String address, Class<?> serviceClass, String portName, String serviceName,
			String wsdUrl) {
		CxfEndpoint cxfEndpoint = new CxfEndpoint();
		cxfEndpoint.setCamelContext(camelContext);
		cxfEndpoint.setAddress(address);
		cxfEndpoint.setServiceClass(serviceClass);
		cxfEndpoint.setPortName(portName);
		cxfEndpoint.setServiceName(serviceName);

		// cxfEndpoint.setDataFormat(DataFormat.MESSAGE);
		cxfEndpoint.setWsdlURL(wsdUrl);

		return cxfEndpoint;
	}

	@Override
	public void addTemplatedRoutesToCamelContext(CamelContext context) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> updateRoutesToCamelContext(CamelContext context) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}