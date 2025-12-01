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
package name.bindul.bls.dm.ui.jfx.pref;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import name.bindul.bls.dm.ui.jfx.BlsDmApp;

@Log4j2
@UtilityClass
public class ApplicationPreferences {

	private static final int MAX_RECENT_FILES = 4;
	private static final String RECENT_REPOSITORIES_KEY = "RECENT_REPOSITORIES";
	
	private Preferences getApplicationPreferences () {
		return Preferences.userNodeForPackage(BlsDmApp.class);
	}
	
	public static Optional<List<RecentRepository>> getRecentRepositories () {
		final Preferences prefs = getApplicationPreferences();
		return recentRepositories0(prefs);
	}

	private static Optional<List<RecentRepository>> recentRepositories0(final Preferences prefs) {
		final String serRecentRepos = prefs.get(RECENT_REPOSITORIES_KEY, null);
		if (null != serRecentRepos) {
			try {
				return Optional.of(new ObjectMapper().readValue(serRecentRepos, new TypeReference<List<RecentRepository>>(){}));
			} catch (JsonProcessingException e) {
				log.warn("Error deserializing recent repositories [{}] : {}", serRecentRepos, e.getMessage(), e);
			}
		}
		return Optional.empty();
	}
	
	public static void addRecentRepository (RecentRepository recentRepo) {
		final Preferences prefs = getApplicationPreferences();
		
		final List<RecentRepository> repoList = recentRepositories0(prefs).orElse(new ArrayList<>());
		boolean hasChanges = false;
		
		// Check if the repo already exists in list
		final int currentLoc = repoList.indexOf(recentRepo);
		if (currentLoc != 0) { // 0 indicates it is already the top, don't need changes
			if (currentLoc > 0) {
				// It already exists in the list, remove it and add it to the beginning
				repoList.remove(currentLoc);
			}
			repoList.addFirst(recentRepo);
			while (repoList.size() > MAX_RECENT_FILES) {
				repoList.removeLast();
			}
			hasChanges = true;
		}
		
		if (hasChanges) {
			try {
				prefs.put(RECENT_REPOSITORIES_KEY, new ObjectMapper().writeValueAsString(repoList));
			} catch (JsonProcessingException e) {
				log.warn("Error serializing recent repositories [{}] : {}", repoList, e.getMessage(), e);
			}
		}
	}
}
