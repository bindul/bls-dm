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
package name.bindul.bls.dm.ui.jfx.components.menu;

import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import name.bindul.bls.dm.core.api.BowlingLeagueStats;
import name.bindul.bls.dm.core.spi.export.DataExportType;
import name.bindul.bls.dm.ui.jfx.components.ParentNodeAware;
import name.bindul.bls.dm.ui.jfx.components.dialogs.ErrorDialog;
import name.bindul.bls.dm.ui.jfx.components.menu.actions.DataExportAction;

public class ExportMenuController implements ParentNodeAware {
	
	@FXML
	private ResourceBundle resources; // Field name MUST be 'resources'
	
	@FXML
	private Menu exportMenu;
	
	private Parent parent;

	@Override
	public void setParent(Parent parent) {
		this.parent = parent;
	}

	@FXML
	public void initialize() {
		Platform.runLater(() -> {
			try {
				final BowlingLeagueStats bls = BowlingLeagueStats.getInstance();
				final List<DataExportType> supportedExportTypes = bls.getSupportedDataExportTypes();
				// Set up menus
				supportedExportTypes.forEach(type -> {
					final String label = resources.getString("export.type." + type.typeCode());
					final MenuItem exportDataMenuItem = new MenuItem(label);
					exportDataMenuItem.setOnAction(new DataExportAction(bls, type, resources, parent));
					exportMenu.getItems().add(exportDataMenuItem);
				});
				
				flipDisabled(bls.isRepositoryConnected());
				bls.addStateChangeListener(event -> flipDisabled(event.isRepositoryLoaded()));
			} catch (Exception e) {
				ErrorDialog.showErrorDialogP(resources.getString("menu.init.error.title"), 
						resources.getString("menu.init.error.message"), e, parent);
			}
		});
	}
	
	private void flipDisabled (boolean repositoryLoaded) {
		exportMenu.setDisable(!repositoryLoaded);
	}
}
