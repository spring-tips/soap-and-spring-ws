package com.example.ws;


import io.spring.guides.gs_producing_web_service.Country;
import io.spring.guides.gs_producing_web_service.Currency;
import io.spring.guides.gs_producing_web_service.GetCountryRequest;
import io.spring.guides.gs_producing_web_service.GetCountryResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.HashMap;
import java.util.Map;


@SpringBootApplication
public class WsApplication {

		public static void main(String[] args) {
				SpringApplication.run(WsApplication.class, args);
		}
}

@Repository
class CountryRepository implements InitializingBean {

		private final Map<String, Country> countries = new HashMap<>();

		@Override
		public void afterPropertiesSet() {
				Country spain = new Country();
				spain.setName("Spain");
				spain.setCapital("Madrid");
				spain.setCurrency(Currency.EUR);
				spain.setPopulation(46704314);

				countries.put(spain.getName(), spain);

				Country poland = new Country();
				poland.setName("Poland");
				poland.setCapital("Warsaw");
				poland.setCurrency(Currency.PLN);
				poland.setPopulation(38186860);

				countries.put(poland.getName(), poland);

				Country uk = new Country();
				uk.setName("United Kingdom");
				uk.setCapital("London");
				uk.setCurrency(Currency.GBP);
				uk.setPopulation(63705000);

				countries.put(uk.getName(), uk);
		}

		Country findCountry(String name) {
				Assert.isTrue(this.countries.containsKey(name), "the country '" + name + "' does not exist in the repository!");
				return this.countries.get(name);
		}
}

@Endpoint
class CountryEndpoint {

		private static final String NAMESPACE_URI = "http://spring.io/guides/gs-producing-web-service";

		private CountryRepository countryRepository;

		public CountryEndpoint(CountryRepository countryRepository) {
				this.countryRepository = countryRepository;
		}

		@PayloadRoot(namespace = NAMESPACE_URI, localPart = "getCountryRequest")
		@ResponsePayload
		public GetCountryResponse getCountry(@RequestPayload GetCountryRequest request) {
				GetCountryResponse response = new GetCountryResponse();
				response.setCountry(countryRepository.findCountry(request.getName()));

				return response;
		}
}

@EnableWs
@Configuration
class WebServiceConfig extends WsConfigurerAdapter {

		@Bean
		ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
			ApplicationContext applicationContext) {
				MessageDispatcherServlet servlet = new MessageDispatcherServlet();
				servlet.setApplicationContext(applicationContext);
				servlet.setTransformWsdlLocations(true);
				return new ServletRegistrationBean<>(servlet, "/ws/*");
		}

		@Bean(name = "countries")
		DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema countriesSchema) {
				DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
				wsdl11Definition.setPortTypeName("CountriesPort");
				wsdl11Definition.setLocationUri("/ws");
				wsdl11Definition.setTargetNamespace("http://spring.io/guides/gs-producing-web-service");
				wsdl11Definition.setSchema(countriesSchema);
				return wsdl11Definition;
		}

		@Bean
		XsdSchema countriesSchema() {
				return new SimpleXsdSchema(new ClassPathResource("countries.xsd"));
		}
}
