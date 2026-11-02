package br.com.fiap.veti;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VetiApplication {

	public static void main(String[] args) {
		// Exportação opcional para o Azure Application Insights (traces, métricas e logs) via connection string
		String appInsights = System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING");
		if (appInsights != null && !appInsights.isBlank()) {
			ApplicationInsights.attach();
		}
		SpringApplication.run(VetiApplication.class, args);
	}

}
