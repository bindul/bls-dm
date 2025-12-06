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

import java.util.Optional;
import java.util.ResourceBundle;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import lombok.RequiredArgsConstructor;
import name.bindul.bls.dm.core.api.BowlingLeagueStats;
import name.bindul.bls.dm.core.spi.export.DataExportLocation.LocalDirectoryExportLocation;
import name.bindul.bls.dm.core.spi.export.DataExportType;
import name.bindul.bls.dm.ui.jfx.components.menu.dialogs.ExportDirectoryDialog;
import name.bindul.bls.dm.ui.jfx.context.ApplicationContext;

@RequiredArgsConstructor
public class DataExportAction implements EventHandler<ActionEvent> {
	
	private final BowlingLeagueStats bls;
	private final DataExportType exportType;
	private final ResourceBundle resources;
	private final Parent parent;

	@Override
	public void handle(ActionEvent event) {
		
		if (!exportType.isLocalDirectory()) {
			throw new UnsupportedOperationException("Get error string from resources");
		}
		
		final Optional<LocalDirectoryExportLocation> exportLocation = new ExportDirectoryDialog(
				parent.getScene().getWindow(), resources)
				.showAndWait();
		
		exportLocation.ifPresent(location -> {
			final Task<Void> exportDataTask = createExportTask(location);
			ApplicationContext.getInstance().getExecutorService().execute(exportDataTask);
		});
	}
	
	private Task<Void> createExportTask(final LocalDirectoryExportLocation exportLocation) {
		final Task<Void> exportTask = new Task<>() {
			@Override
			protected Void call() throws Exception {
				updateMessage(resources.getString("action.export.local.task.in-progress") + exportLocation.locationDisplayValue());
				bls.exportData(exportLocation);
				updateMessage(resources.getString("action.export.local.task.completed") + exportLocation.locationDisplayValue());
				return null;
			}
		};
		
		ActionUtils.decorateRepositoryTask(exportTask, "action.export.local", resources, parent);
		return exportTask;
	}

}
