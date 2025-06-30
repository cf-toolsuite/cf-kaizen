package org.cftoolsuite.cfapp;

import org.cftoolsuite.cfapp.service.ai.HooverService;
import org.cftoolsuite.cfapp.service.ai.PageableHooverService;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallbackProvider;
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
	public ToolCallbackProvider hooverTools(
		HooverService hooverService,
		PageableHooverService pageableHooverService) {
		return
			ToolCallbackProvider
				.from(List.of(
					ToolCallbacks.from(
						hooverService,
						pageableHooverService
					)
				));
	}

}
