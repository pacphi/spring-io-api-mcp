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

import org.springframework.hateoas.MediaTypes;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * @author Martin Lippert
 */
@Component
public class SpringIoApi {

	private RestClient restClient;

	public SpringIoApi(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.baseUrl("https://api.spring.io").build();
	}

	public record Main(Embedded _embedded) {}
	public record Embedded(Release[] releases) {}
	public record Release(String version, String status, boolean current) {}

	public Release[] getReleases(String project) {
		Main release = restClient.get()
			.uri("https://api.spring.io/projects/" + project + "/releases")
			.accept(MediaTypes.HAL_JSON)
			.retrieve()
			.body(Main.class);
		
		return release._embedded.releases;
	}

	public static void main(String[] args) {
		SpringIoApi springApi = new SpringIoApi(RestClient.builder());
		springApi.getReleases("spring-boot");
	}

}
