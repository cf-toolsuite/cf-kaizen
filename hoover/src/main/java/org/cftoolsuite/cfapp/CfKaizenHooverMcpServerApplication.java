package org.cftoolsuite.cfapp;

import org.cftoolsuite.cfapp.service.ai.HooverService;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
@EnableFeignClients
public class CfKaizenHooverMcpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CfKaizenHooverMcpServerApplication.class, args);
	}

	@Bean
	public List<ToolCallback> hooverTools(HooverService hooverService) {
		return List.of(ToolCallbacks.from(hooverService));
	}

}
