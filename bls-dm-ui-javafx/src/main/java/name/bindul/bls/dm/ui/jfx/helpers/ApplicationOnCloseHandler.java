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
package name.bindul.bls.dm.ui.jfx.helpers;

import java.util.Optional;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApplicationOnCloseHandler implements EventHandler<WindowEvent> {
	
	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("name.bindul.bls.dm.ui.jfx.ui-resources");
	
	private final Stage parentStage;

	@Override
	public void handle(WindowEvent event) {
		createAlert().ifPresent(a -> a.showAndWait()
			.filter(r -> r != ButtonType.OK) // Only interested if not OK (OK = exit, let the event do it's thing)
			.ifPresent(r -> event.consume()) // Consume the event so it does not propagate
		);
	}
	
	public Optional<Alert> createAlert() {
		final EntityEditorRegistry editorRegistry = (EntityEditorRegistry) parentStage.getProperties()
				.get(EntityEditorRegistry.STAGE_PROPERTIES_KEY);
		if (null != editorRegistry && editorRegistry.hasDirtyEditors()) {
			final Alert exitAlert = new Alert(AlertType.CONFIRMATION);
			exitAlert.initOwner(parentStage);
			exitAlert.initModality(Modality.APPLICATION_MODAL);
			exitAlert.setTitle(resourceBundle.getString("app.exit.alert.title"));
			exitAlert.setHeaderText(null);
			exitAlert.setContentText(resourceBundle.getString("app.exit.alert.content-text"));
			
			return Optional.of(exitAlert);
		}
		return Optional.empty();
	}
}
