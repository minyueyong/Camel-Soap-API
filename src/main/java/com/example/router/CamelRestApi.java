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
public class CamelRestApi extends RouteBuilder {

	@Autowired
	private CamelContext camelContext;
	@Autowired
	private ProducerTemplate template;
	
	
	@Autowired
	private Environment env;

	@Value("${camel.servlet.mapping.context-path}")
	private String contextPath;


	
	ObjectMapper objectMapper = new ObjectMapper();
	

	@Override
	public void configure() throws Exception {

		
		
		  restConfiguration()
          .component("servlet")
          //.bindingMode(RestBindingMode.json)   //global configuration, incoming and outgoing request are json
          .dataFormatProperty("prettyPrint", "true")
          .enableCORS(true)
          .port(env.getProperty("server.port", "8080"))
          .contextPath(contextPath.substring(0, contextPath.length() - 2))   //change it to /api
          // turn on openapi api-doc
          .apiContextPath("/api-doc")
          .apiProperty("api.title", "Products API")
          .apiProperty("api.version", "1.0.0");

      rest().description("Actor REST service")
          
          .post("/soap")
          .to("direct:RestSoap");
          
		// Add route to send SOAP request using ProducerTemplate
		from("direct:RestSoap") // Trigger the route once
		 .log("Route triggered successfully")
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
					
					 // Create a map to hold the response with a specific key   //change into POJO then into JSON
			        Map<String, String> responseMap = new HashMap<>();
			        responseMap.put("ubiNum", result);

			        // Set the map as the body of the message
			        exchange.getMessage().setBody(responseMap);
					
			        
			        //This one is to get the SOAP response
					  NumberToWordsResponse response = new NumberToWordsResponse();
					 response.setNumberToWordsResult(result);

					   exchange.getContext().createProducerTemplate()
		                .sendBody("direct:Restmarshal", response);
				

				})  .marshal().json(JsonLibrary.Jackson) 
				.log("Response from web service (JSON): ${body}");;// Marshaling to JSON format
		
		//not need
		from("direct:Restmarshal")
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

}