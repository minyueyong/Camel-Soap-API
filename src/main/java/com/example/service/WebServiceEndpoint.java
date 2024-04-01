package com.example.service;


import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

import org.apache.cxf.binding.soap.SoapBindingConstants;
import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.Endpoint;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.env.Environment;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Bean;

@Service
public class WebServiceEndpoint {
	
	CamelContext context = new DefaultCamelContext();
	
	
	
	/*
	 * @Bean Endpoint requestEndpoint() throws Exception { HttpComponent
	 * httpComponent = context.getComponent("https4", HttpComponent.class);
	 * 
	 * //httpComponent.setSocketTimeout(setSocketTimeout());
	 * 
	 * //return httpComponent.createEndpoint(
	 * "http4://localhost:8082/aov-financing-insight/response" + return
	 * httpComponent.createEndpoint(
	 * "https4://www.dataaccess.com/webservicesserver/NumberConversion.wso"
	 * 
	 * );
	 * 
	 * }
	 */
}