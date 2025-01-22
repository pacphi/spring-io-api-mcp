/*
* Copyright 2025 - 2025 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.springframework.ai.mcp.springapi;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.server.McpAsyncServer;
import org.springframework.ai.mcp.server.McpServer;
import org.springframework.ai.mcp.server.transport.StdioServerTransport;
import org.springframework.ai.mcp.server.transport.WebMvcSseServerTransport;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.ai.mcp.spec.ServerMcpTransport;
import org.springframework.ai.mcp.spring.ToolHelper;
import org.springframework.ai.mcp.springapi.SpringIoApi.Release;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Martin Lippert
 */
@Configuration
public class McpConfig {

	private static final Logger logger = LoggerFactory.getLogger(McpConfig.class);

	private SpringIoApi springIoApi;
	
	public McpConfig(SpringIoApi springIoApi) {
		this.springIoApi = springIoApi;
	}

	@Bean
	@ConditionalOnProperty(prefix = "transport", name = "mode", havingValue = "sse")
	WebMvcSseServerTransport webMvcSseServerTransport() {
		return new WebMvcSseServerTransport(new ObjectMapper(), "/mcp/message");
	}

	@Bean
	@ConditionalOnProperty(prefix = "transport", name = "mode", havingValue = "sse")
	RouterFunction<ServerResponse> routerFunction(WebMvcSseServerTransport transport) {
		return transport.getRouterFunction();
	}

	@Bean
	@ConditionalOnProperty(prefix = "transport", name = "mode", havingValue = "stdio")
	StdioServerTransport stdioServerTransport() {
		return new StdioServerTransport();
	}

	public static record GetSpringProjectReleasesInput(String springProjectId) {}
	
	@Bean
	McpAsyncServer mcpServer(ServerMcpTransport transport) {

		var capabilities = McpSchema.ServerCapabilities.builder()
//			.resources(false, true)
			.tools(true)
//			.prompts(true)
			.logging()
			.build();

		var server = McpServer.using(transport)
			.serverInfo("MCP Server for Spring project release information", "1.0.0")
			.capabilities(capabilities)
			.tools(
					ToolHelper.toToolRegistration(
						FunctionCallback.builder()
							.function("getSpringProjectReleaseInformation", (Function<GetSpringProjectReleasesInput, Release[]>) s -> {
								logger.info("get spring project releases for: " + s);
								return springIoApi.getReleases(s.springProjectId());
							})
							.description("Get information about ")
							.inputType(GetSpringProjectReleasesInput.class)						
							.build()))
			.async();
		return server;
	}

}
