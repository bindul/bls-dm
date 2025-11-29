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

import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import name.bindul.bls.dm.core.api.BowlingLeagueStats;
import name.bindul.bls.dm.ui.jfx.helpers.ApplicationOnCloseHandler;
import name.bindul.bls.dm.ui.jfx.helpers.ParentNodeAware;

public class FileMenuController implements ParentNodeAware {
	
	@FXML
	private ResourceBundle resources;
	
	@FXML
	private Menu newMenu;
	
	@FXML
	private Menu openMenu;
	
	@FXML
	private Menu recentRepositoriesMenu;
	
	@FXML
	private MenuItem closeRepository;
	
	private Parent parent;

	@Override
	public void setParent(Parent parent) {
		this.parent = parent;
	}

	@FXML
	public void initialize() {
		final BowlingLeagueStats bls = BowlingLeagueStats.getInstance();
		
		// Set up menus
		bls.getSupportedRepositoryLocationTypes().forEach(type -> {
			final String label = resources.getString("menu.file.repo.type." + type.getTypeCode());
			if (type.isSupportsCreateNew()) {
				final MenuItem newTypeMenu = new MenuItem(label);
				newTypeMenu.setOnAction(e -> handleNewRepository(type.getTypeCode()));
				newMenu.getItems().add(newTypeMenu);
			}
			final MenuItem openTypeMenu = new MenuItem(label);
			openTypeMenu.setOnAction(e -> handleOpenRepository(type.getTypeCode()));
			openMenu.getItems().add(openTypeMenu);
		});
		flipDisabled(bls.isRepositoryConnected());
		
		bls.addStateChangeListener(event -> flipDisabled(event.isRepositoryLoaded()));
	}
	
	private void flipDisabled (boolean repositoryLoaded) {
		newMenu.setDisable(repositoryLoaded);
		openMenu.setDisable(repositoryLoaded);
		recentRepositoriesMenu.setDisable(repositoryLoaded);
		closeRepository.setDisable(!repositoryLoaded);
	}

	private void handleNewRepository(String typeId) {
		// TODO Implement
		System.out.println("New");
	}
	
	private void handleOpenRepository(String typeId) {
		// TODO Implement
		System.out.println("Open");
	}
	
	@FXML
	protected void handleCloseRepository() {
		// TODO Implement
	}
	
	@FXML
	protected void handleExit(ActionEvent e) {
		
		final Stage parentStage = (Stage) parent.getScene().getWindow();
		final ApplicationOnCloseHandler closeHandler = new ApplicationOnCloseHandler(parentStage);
		closeHandler.createAlert().ifPresentOrElse(
				a -> a.showAndWait()
					.filter(r -> r == ButtonType.OK)
					.ifPresent(r -> Platform.exit()),
				Platform::exit);
	}
}
