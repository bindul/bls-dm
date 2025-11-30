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
package name.bindul.bls.dm.ui.jfx.components.statusbar;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import name.bindul.bls.dm.core.api.BowlingLeagueStats;
import name.bindul.bls.dm.ui.jfx.context.ApplicationContext;

public class StatusBarController {
	
	@FXML
	private Label repositoryLabel;
	
	@FXML
	private Label statusLabel;
	
	@FXML
	public void initialize() {
		
		ApplicationContext.getInstance().setStatusLabel(statusLabel.textProperty());
		
		Platform.runLater(() -> {
			// File name viewer
			final BowlingLeagueStats bls = BowlingLeagueStats.getInstance();
			// Events may come from worker threads, need to move them to the UI thread to update UI
			bls.addStateChangeListener(e -> Platform.runLater(() -> {
				if (e.isRepositoryLoaded()) {
					repositoryLabel.setText(e.getRepositoryLocation());
				} else {
					repositoryLabel.setText(null);
				}
			}));
		});
	}
}
