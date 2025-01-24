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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Martin Lippert
 */
@Component
public class SpringIoApi {

	private RestClient restClient;

	public SpringIoApi(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.baseUrl("https://api.spring.io").build();
	}

	public record ReleasesRoot(ReleasesEmbedded _embedded) {}
	public record ReleasesEmbedded(Release[] releases) {}
	public record Release(String version, String status, boolean current) {}
	
	public record GenerationsRoot(GenerationsEmbedded _embedded) {}
	public record GenerationsEmbedded(Generation[] generations) {}
	public record Generation(String name, String initialReleaseDate, String ossSupportEndDate, String commercialSupportEndDate) {}

	public record UpcomingReleasesRoot(UpcomingReleasesEmbedded _embedded) {}
	public record UpcomingReleasesEmbedded(UpcomingRelease[] releases) {}
	public record UpcomingRelease(boolean allDay, String backgroundColor, LocalDate start, String title, String url) {}

	public Release[] getReleases(String project) {
		ReleasesRoot release = restClient.get()
			.uri("https://api.spring.io/projects/" + project + "/releases")
			.accept(MediaTypes.HAL_JSON)
			.retrieve()
			.body(ReleasesRoot.class);
		
		return release._embedded.releases;
	}

	public Generation[] getGenerations(String project) {
		GenerationsRoot release = restClient.get()
			.uri("https://api.spring.io/projects/" + project + "/generations")
			.accept(MediaTypes.HAL_JSON)
			.retrieve()
			.body(GenerationsRoot.class);
		
		return release._embedded.generations;
	}

	public UpcomingRelease[] getUpcomingReleases() {
		LocalDate start = LocalDate.now();
		LocalDate end = start.plusDays(90L);

		// Convert LocalDate to ISO format and handle timezone
		ZonedDateTime startZdt = start.atStartOfDay(ZoneId.systemDefault());
		ZonedDateTime endZdt = end.atStartOfDay(ZoneId.systemDefault());

		UpcomingReleasesRoot upcoming = restClient.get()
				.uri(uriBuilder -> uriBuilder
						.scheme("https")
						.host("calendar.spring.io")
						.path("/releases")
						.queryParam("start", startZdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
						.queryParam("end", endZdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
						.build())
				.accept(MediaTypes.HAL_JSON)
				.retrieve()
				.body(UpcomingReleasesRoot.class);
		return upcoming._embedded.releases;
	}

	public static void main(String[] args) {
		SpringIoApi springApi = new SpringIoApi(RestClient.builder());
		springApi.getReleases("spring-boot");
		springApi.getGenerations("spring-boot");
		springApi.getUpcomingReleases();
	}

}
