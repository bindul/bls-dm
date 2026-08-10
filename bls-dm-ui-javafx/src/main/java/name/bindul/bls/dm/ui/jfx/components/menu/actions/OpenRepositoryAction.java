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
import java.util.List;
import java.util.ResourceBundle;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import lombok.extern.log4j.Log4j2;
import name.bindul.bls.dm.core.api.BowlingLeagueStats;
import name.bindul.bls.dm.core.model.BowlingCenter;
import name.bindul.bls.dm.core.spi.repository.RepositoryLocation;
import name.bindul.bls.dm.core.spi.repository.RepositoryLocationType;
import name.bindul.bls.dm.core.spi.repository.RepositoryLocation.LocalFileRepositoryLocation;
import name.bindul.bls.dm.ui.jfx.context.ApplicationContext;

@Log4j2
public class OpenRepositoryAction extends RepositoryLocationActionSupport implements EventHandler<ActionEvent> {
	
	protected final RepositoryLocationType repositoryLocType;
	
	public OpenRepositoryAction(BowlingLeagueStats bls, RepositoryLocationType repositoryLocType,
			ResourceBundle resources, Parent parent) {
		super(bls, resources, parent);
		this.repositoryLocType = repositoryLocType;
	}

	@Override
	public void handle(ActionEvent event) {
		if (!repositoryLocType.isLocalFile()) {
			throw new UnsupportedOperationException(resources.getString("action.open.remote.unsupported"));
		}
		openLocalFile();
	}

	private void openLocalFile() {
		final RepositoryLocation repoLocation = chooseRepositoryLocation();
		
		if (null != repoLocation) {
			final Task<Void> openRepositoryTask = createOpenRepositoryTask(repoLocation);
			
			addToRecentFiles(repositoryLocType, ((LocalFileRepositoryLocation) repoLocation).getLocation());
			ApplicationContext.getInstance().getExecutorService().execute(openRepositoryTask);
		}
	}

	protected RepositoryLocation chooseRepositoryLocation() {
		final File chosenFile = createFileChooser(repositoryLocType).showOpenDialog(parent.getScene().getWindow());
		if (null != chosenFile) {
			return LocalFileRepositoryLocation.builder().location(chosenFile).build();
		}
		return null;
	}

	private Task<Void> createOpenRepositoryTask(RepositoryLocation repoLocation) {
		final Task<Void> repositoryTask = new Task<>() {
			@Override
			protected Void call() throws Exception {
				updateTitle(resources.getString("action.open.local.task.title"));
				updateMessage(resources.getString("action.open.local.task.in-progress") + repoLocation.locationDisplayValue());
				bls.openRepository(repoLocation);
				
				// TODO - Delete later
				try {
					List<BowlingCenter> bcs = bls.getServiceFactory().getReferenceDataService().getBowlingCenters();
					log.debug("Loaded bowling centers: {}", bcs);
				} catch (Exception e) {
					log.warn("Error in data access: {}", e.getMessage(), e);
					e.printStackTrace();
					throw e;
				}
				// TODO - End delete later
				
				updateMessage(resources.getString("action.open.local.task.completed") + repoLocation.locationDisplayValue());
				return null;
			}
		};
		
		ActionUtils.decorateRepositoryTask(repositoryTask, "action.open.local", resources, parent);
		return repositoryTask;
	}

}
