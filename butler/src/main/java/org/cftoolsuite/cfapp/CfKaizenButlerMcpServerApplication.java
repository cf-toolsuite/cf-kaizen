package org.cftoolsuite.cfapp;

import org.cftoolsuite.cfapp.service.ai.*;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallbackProvider;
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
	public ToolCallbackProvider tools(
			AccountingService accountingService,
			PoliciesService policiesService,
			ProductsService productsService,
			PageableSnapshotService pageableSnapshotService,
			SnapshotService snapshotService) {
		return ToolCallbackProvider.from(List.of(
				ToolCallbacks.from(
						accountingService,
						policiesService,
						productsService,
						pageableSnapshotService,
						snapshotService
				)));
	}

}
