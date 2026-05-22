package org.cftoolsuite.cfapp.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class Http {

	@Bean
	public WebClientCustomizer webClientCustomizer() {
		return webClientBuilder -> {
			HttpClient httpClient = HttpClient.create()
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) Duration.ofMinutes(10).toMillis())
					.responseTimeout(Duration.ofMinutes(10));
			webClientBuilder.defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate")
					.clientConnector(new ReactorClientHttpConnector(httpClient));
		};
	}
}
