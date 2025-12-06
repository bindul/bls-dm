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
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import name.bindul.bls.dm.core.api.BowlingLeagueStats;
import name.bindul.bls.dm.core.spi.repository.RepositoryLocation;
import name.bindul.bls.dm.core.spi.repository.RepositoryLocationType;
import name.bindul.bls.dm.core.spi.repository.RepositoryLocation.LocalFileRepositoryLocation;
import name.bindul.bls.dm.ui.jfx.context.ApplicationContext;

public class NewRepositoryAction extends RepositoryLocationActionSupport implements EventHandler<ActionEvent> {
	
	protected final RepositoryLocationType repositoryLocType;
	
	public NewRepositoryAction(BowlingLeagueStats bls, RepositoryLocationType repositoryLocType,
			ResourceBundle resources, Parent parent) {
		super(bls, resources, parent);
		this.repositoryLocType = repositoryLocType;
	}

	@Override
	public void handle(ActionEvent event) {
		if (!repositoryLocType.isLocalFile()) {
			throw new UnsupportedOperationException(resources.getString("action.new.remote.unsupported"));
		}
		newLocalFile();
	}

	private void newLocalFile() {
		final File chosenFile = createFileChooser(repositoryLocType).showSaveDialog(parent.getScene().getWindow());
		
		if (null != chosenFile) {
			// Confirm overwrite
			if (chosenFile.exists()) {
				final Alert overwriteConfirmation = new Alert(AlertType.CONFIRMATION);
				overwriteConfirmation.setTitle(resources.getString("action.new.local.overwrite.title"));
				overwriteConfirmation.setHeaderText(resources.getString("action.new.local.overwrite.header") + chosenFile.getPath());
				overwriteConfirmation.setContentText(resources.getString("action.new.local.overwrite.content"));
				
				final Optional<ButtonType> overwriteResult = overwriteConfirmation.showAndWait();
				if (overwriteResult.isPresent() && overwriteResult.get() != ButtonType.OK) {
					// User did not confirm overwrite. We could show the file chooser again, but let them start over
					return;
				}
			}
			
			final RepositoryLocation repoLocation = LocalFileRepositoryLocation.builder().location(chosenFile).build();
			final Task<Void> newRepositoryTask = createNewRepositoryTask(repoLocation);
			
			addToRecentFiles(repositoryLocType, chosenFile);
			ApplicationContext.getInstance().getExecutorService().execute(newRepositoryTask);
		}
	}

	private Task<Void> createNewRepositoryTask(final RepositoryLocation repoLocation) {
		final Task<Void> repositoryTask = new Task<>() {
			@Override
			protected Void call() throws Exception {
				updateTitle(resources.getString("action.new.local.task.title"));
				updateMessage(resources.getString("action.new.local.task.in-progress") + repoLocation.locationDisplayValue());
				bls.newRepository(repoLocation);
				updateMessage(resources.getString("action.new.local.task.completed") + repoLocation.locationDisplayValue());
				return null;
			}
		};
		
		ActionUtils.decorateRepositoryTask(repositoryTask, "action.new.local", resources, parent);
		return repositoryTask;
	}
}
