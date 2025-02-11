package org.cftoolsuite.cfapp;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
@EnableFeignClients
public class CfKaizenButlerMcpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CfKaizenButlerMcpServerApplication.class, args);
	}

	@Bean
	public List<ToolCallback> butlerTools(ButlerService butlerService) {
		return List.of(ToolCallbacks.from(butlerService));
	}

}
