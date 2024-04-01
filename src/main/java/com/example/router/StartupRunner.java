package com.example.router;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

	@Autowired
	private ProducerTemplate producerTemplate;

	@Override
	public void run(String... args) throws Exception {
		try {
			
			
			
			  producerTemplate.sendBody("direct:soap",
			  "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
			  "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
			  "<soap:Body>" +
			  "<NumberToWords xmlns=\"http://www.dataaccess.com/webservicesserver/\">" +
			  "<ubiNum>500</ubiNum>" + "</NumberToWords>" + "</soap:Body>" +
			  "</soap:Envelope>");
			 
			 
			 

				/*
				 * producerTemplate.sendBody("direct:soap", "    <NumberToWords>\n" +
				 * "      <ubiNum>500</ubiNum>\n" + "    </NumberToWords>\n");
				 */

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
