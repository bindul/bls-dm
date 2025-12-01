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
package name.bindul.bls.dm.ui.jfx.components.actions;

import java.io.File;
import java.util.ResourceBundle;

import javafx.scene.Parent;
import name.bindul.bls.dm.core.api.BowlingLeagueStats;
import name.bindul.bls.dm.core.spi.RepositoryLocation;
import name.bindul.bls.dm.core.spi.RepositoryLocation.LocalFileRepositoryLocation;
import name.bindul.bls.dm.core.spi.RepositoryLocationType;

public class OpenRecentRepositoryAction extends OpenRepositoryAction {

	private final String repositoryLocation;

	public OpenRecentRepositoryAction(BowlingLeagueStats bls, RepositoryLocationType repositoryLocType,
			ResourceBundle resources, Parent parent, String repositoryLocation) {
		super(bls, repositoryLocType, resources, parent);
		this.repositoryLocation = repositoryLocation;
	}

	@Override
	protected RepositoryLocation chooseRepositoryLocation() {
		// We will go with the local file assumption for now, in the future may have to change repository location to a URL
		final File chosenFile = new File(repositoryLocation);
		return LocalFileRepositoryLocation.builder().location(chosenFile).build();
	}
}
