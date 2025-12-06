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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import name.bindul.bls.dm.core.api.BowlingLeagueStats;
import name.bindul.bls.dm.core.spi.repository.RepositoryLocationType;
import name.bindul.bls.dm.ui.jfx.components.ParentNodeAware;
import name.bindul.bls.dm.ui.jfx.components.dialogs.ErrorDialog;
import name.bindul.bls.dm.ui.jfx.components.menu.actions.CloseRepositoryAction;
import name.bindul.bls.dm.ui.jfx.components.menu.actions.NewRepositoryAction;
import name.bindul.bls.dm.ui.jfx.components.menu.actions.OpenRecentRepositoryAction;
import name.bindul.bls.dm.ui.jfx.components.menu.actions.OpenRepositoryAction;
import name.bindul.bls.dm.ui.jfx.helpers.ApplicationOnCloseHandler;
import name.bindul.bls.dm.ui.jfx.pref.ApplicationPreferences;

public class FileMenuController implements ParentNodeAware {
	
	@FXML
	private ResourceBundle resources; // Field name MUST be 'resources'
	
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
		Platform.runLater(() -> {
			try {
				final BowlingLeagueStats bls = BowlingLeagueStats.getInstance();
				final List<RepositoryLocationType> supportedLocationTypes = bls.getSupportedRepositoryLocationTypes();
				// Set up menus
				supportedLocationTypes.forEach(type -> {
					final String label = resources.getString("repo.type." + type.typeCode());
					if (type.supportsCreateNew()) {
						final MenuItem newRepoMenu = new MenuItem(label);
						newRepoMenu.setOnAction(new NewRepositoryAction(bls, type, resources, parent));
						newMenu.getItems().add(newRepoMenu);
					}
					final MenuItem openRepoMenu = new MenuItem(label);
					openRepoMenu.setOnAction(new OpenRepositoryAction(bls, type, resources, parent));
					openMenu.getItems().add(openRepoMenu);
				});
				
				// Recent files
				setupRecentReposMenu();
				
				flipDisabled(bls.isRepositoryConnected());
				bls.addStateChangeListener(event -> {
					setupRecentReposMenu();
					flipDisabled(event.isRepositoryLoaded());
				});
			} catch (Exception e) {
				ErrorDialog.showErrorDialogP(resources.getString("menu.init.error.title"), 
						resources.getString("menu.init.error.message"), e, parent);
			}
		});
	}

	private void setupRecentReposMenu() {
		final BowlingLeagueStats bls = BowlingLeagueStats.getInstance();
		final List<RepositoryLocationType> supportedLocationTypes = bls.getSupportedRepositoryLocationTypes();
		
		recentRepositoriesMenu.getItems().clear();
		ApplicationPreferences.getRecentRepositories()
			.ifPresent(recentRepos -> recentRepos.forEach(
				recent -> supportedLocationTypes.stream()
					.filter(slt -> slt.typeCode().equals(recent.repositoryLocationTypeCode()))
					.findFirst()
					.ifPresent(slt -> {
						final MenuItem recentItem = new MenuItem(recent.location());
						recentItem.setOnAction(new OpenRecentRepositoryAction(bls, slt, resources, parent, recent.location()));
						recentRepositoriesMenu.getItems().add(recentItem);
					})
			)
		);
	}
	
	private void flipDisabled (boolean repositoryLoaded) {
		newMenu.setDisable(repositoryLoaded);
		openMenu.setDisable(repositoryLoaded);
		recentRepositoriesMenu.setDisable(repositoryLoaded);
		closeRepository.setDisable(!repositoryLoaded);
	}

	@FXML
	protected void handleCloseRepository(ActionEvent e) {
		new CloseRepositoryAction(BowlingLeagueStats.getInstance(), resources, parent).handle(e);
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
