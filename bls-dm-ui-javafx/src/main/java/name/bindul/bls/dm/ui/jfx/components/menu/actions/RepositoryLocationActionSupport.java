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
package name.bindul.bls.dm.ui.jfx.components.menu.actions;

import java.io.File;
import java.util.ResourceBundle;

import javafx.scene.Parent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import name.bindul.bls.dm.core.api.BowlingLeagueStats;
import name.bindul.bls.dm.core.spi.repository.RepositoryLocationType;
import name.bindul.bls.dm.ui.jfx.pref.ApplicationPreferences;
import name.bindul.bls.dm.ui.jfx.pref.RecentRepository;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class RepositoryLocationActionSupport {

	protected final BowlingLeagueStats bls;
	protected final ResourceBundle resources;
	protected final Parent parent;

	protected FileChooser createFileChooser(RepositoryLocationType repositoryLocType) {
		final FileChooser fileChooser = new FileChooser();
		
		final String resourceTypeName = resources.getString("repo.type." + repositoryLocType.typeCode());
		fileChooser.setTitle(resources.getString("action.new.local.title") + " " + resourceTypeName);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.setInitialFileName("bls.db");
		if (!repositoryLocType.localFileExtensions().isEmpty()) {
			fileChooser.getExtensionFilters().add(new ExtensionFilter(resourceTypeName + " (" 
							+ String.join(", ", repositoryLocType.localFileExtensions()) + ")",
					repositoryLocType.localFileExtensions().toArray(new String[0])));
		}
		
		return fileChooser;
	}
	
	protected void addToRecentFiles(RepositoryLocationType repositoryLocType, File chosenFile) {
		final RecentRepository recentRepo = new RecentRepository(repositoryLocType.typeCode(), chosenFile.getPath());
		ApplicationPreferences.addRecentRepository(recentRepo);
	}
}
