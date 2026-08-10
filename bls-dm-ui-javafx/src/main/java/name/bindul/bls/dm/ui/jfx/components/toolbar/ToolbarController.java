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
package name.bindul.bls.dm.ui.jfx.components.toolbar;

import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToolBar;
import lombok.Setter;
import name.bindul.bls.dm.core.api.BowlingLeagueStats;
import name.bindul.bls.dm.ui.jfx.components.entityeditors.EntityEditorContainer;
import name.bindul.bls.dm.ui.jfx.components.entityeditors.RefDataViewController;

public class ToolbarController {
	
	@FXML
	private ResourceBundle resources; // Field name MUST be 'resources'

	@FXML
	private ToolBar toolbar;
	
	@Setter
	private EntityEditorContainer editorContainer;
	
	@FXML
	public void initialize () {
		Platform.runLater(() -> 
			BowlingLeagueStats.getInstance().addStateChangeListener(e -> 
				toolbar.setDisable(!e.isRepositoryLoaded())
			)
		);
	}
	
	@FXML
	public void loadRefData (ActionEvent actionEvent) {
		final RefDataViewController refDataViewController = new RefDataViewController(editorContainer, resources);
		refDataViewController.load();
	}
}
