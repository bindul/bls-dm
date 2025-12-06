/*
 * Copyright (c) 2025. Bindul Bhowmik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package name.bindul.bls.dm.export.site;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;

import lombok.extern.log4j.Log4j2;
import name.bindul.bls.dm.core.api.ServiceFactory;
import name.bindul.bls.dm.core.spi.export.DataExportException;
import name.bindul.bls.dm.core.spi.export.DataExportLocation;
import name.bindul.bls.dm.core.spi.export.DataExportLocation.LocalDirectoryExportLocation;
import name.bindul.bls.dm.core.spi.export.DataExportManager;
import name.bindul.bls.dm.core.spi.export.DataExportType;
import name.bindul.bls.dm.core.spi.repository.Repository;
import name.bindul.bls.dm.core.spi.repository.RepositoryException;
import name.bindul.bls.dm.export.site.model.League;
import name.bindul.bls.dm.export.site.model.LeaguesIndexSchema;
import name.bindul.bls.dm.export.site.model.Location;
import name.bindul.bls.dm.export.site.model.Season;
import name.bindul.bls.dm.export.site.model.Team;

@Log4j2
@AutoService(DataExportManager.class)
public class SiteJsonDataExportManager implements DataExportManager {
	
	private final DataExportType supportedExportType = new DataExportType("local.directory.site.json", true);

	@Override
	public DataExportType supportedExportType() {
		return supportedExportType;
	}

	@Override
	public boolean canExportTo(DataExportLocation location) {
		return location instanceof LocalDirectoryExportLocation;
	}

	@Override
	public void export(DataExportLocation locationIn, ServiceFactory serviceFactory, Repository repository)
			throws DataExportException {
		
		final LocalDirectoryExportLocation location = (LocalDirectoryExportLocation) locationIn;
		final File destDir = location.getDirectory();
		
		if (!destDir.exists()) {
			try {
				Files.createDirectories(destDir.toPath());
			} catch (IOException e) {
				throw new DataExportException("Unable to create directory to export files. " + e.getMessage(), e);
			}
		}
		
		if (!destDir.isDirectory() || !destDir.canWrite()) {
			throw new DataExportException("Selected location is either not a directory or cannot be written to");
		}
		
		cleanDirectory(location);
		
		createLeagueFile(destDir, serviceFactory);

	}

	private void createLeagueFile(File destDir, ServiceFactory serviceFactory) throws DataExportException {
		LeaguesIndexSchema leagueSchema = new LeaguesIndexSchema();
		List<Season> seasons = new ArrayList<>();
		leagueSchema.setSeasons(seasons);
		
		// TODO Temporary code, change it
		try {
			serviceFactory.getReferenceDataService().getBowlingCenters().forEach(bc -> {
				final Season season = new Season();
				season.setId("2025-26");
				
				final League league = new League();
				season.setLeagues(Arrays.asList(league));
				league.setId("2526-ARAPBC-Beer-Winter");
				league.setName("Summer Beer");
				
				final Location location = new Location();
				location.setId(bc.getId());
				location.setName(bc.getName());
				league.setLocation(location);
				
				final Team team = new Team();
				team.setId("BEER-234");
				team.setName("Pins Go Boom!");
				league.setTeams(Arrays.asList(team));
				
				seasons.add(season);
			});
		} catch (RepositoryException e) {
			throw new DataExportException("Error getting data for league file: " + e.getMessage(), e);
		}
		
		File destinFile = new File(destDir, "leagues.json");
		
		final ObjectMapper om = new ObjectMapper();
		try {
			om.writer().writeValue(destinFile, leagueSchema);
		} catch (IOException e) {
			throw new DataExportException("Error writing destination file: " + e.getMessage(), e);
		}
	}

	protected void cleanDirectory(final LocalDirectoryExportLocation location)
			throws DataExportException {
		if (location.isEmptyDirectory()) {
			// TODO Move the glob to a configuration somewhere
			final PathMatcher jsonPathMatcher = FileSystems.getDefault().getPathMatcher("glob:*.json");
			final Path pathToClean = location.getDirectory().toPath();
			try (Stream<Path> walk = Files.walk(pathToClean)) {
				walk.sorted(Comparator.reverseOrder())
					.filter(path -> shouldDeleteFileOrDirectory(path, pathToClean, jsonPathMatcher))
					.forEach(path -> {
						try {
							Files.delete(path);
						} catch (IOException e) {
							ExceptionUtils.rethrow(e);
						}
					});
			} catch (IOException e) {
				throw new DataExportException("Error cleaning output directory: " + e.getMessage(), e);
			}
		}
	}

	private boolean shouldDeleteFileOrDirectory(Path candidateToDelete, final Path pathToClean,
			final PathMatcher jsonPathMatcher) {
		try {
			if (candidateToDelete.equals(pathToClean)) {
				return false;
			} else if (Files.isDirectory(candidateToDelete) && Files.list(candidateToDelete).findAny().isEmpty()) {
				return true;
			} else if (Files.isRegularFile(candidateToDelete) && jsonPathMatcher.matches(candidateToDelete.getFileName())) {
				return true;
			}
		} catch (IOException e) {
			log.info("Ignoring file {} that could not be checked: {}", candidateToDelete, e.getMessage(), e);
		}
		return false;
	}

}
