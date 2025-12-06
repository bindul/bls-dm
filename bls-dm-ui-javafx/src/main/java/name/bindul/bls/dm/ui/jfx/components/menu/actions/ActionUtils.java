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

import java.util.ResourceBundle;

import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import lombok.experimental.UtilityClass;
import name.bindul.bls.dm.ui.jfx.components.dialogs.ErrorDialog;
import name.bindul.bls.dm.ui.jfx.context.ApplicationContext;

@UtilityClass
public class ActionUtils {

	public static void decorateRepositoryTask(final Task<Void> repositoryTask, final String resourceKeyPrefix, final ResourceBundle resources, final Parent parent) {
		final StringProperty statusLabel = ApplicationContext.getInstance().getStatusLabel();
		
		if (null != statusLabel) {
			statusLabel.bind(repositoryTask.messageProperty());
			
			repositoryTask.setOnSucceeded(e -> {
				statusLabel.unbind();
				statusLabel.set(null);
			});
		}
			
		repositoryTask.setOnFailed(e -> {
			if (null != statusLabel) {
				statusLabel.unbind();
				statusLabel.set(resources.getString(resourceKeyPrefix + ".task.failed") + repositoryTask.getTitle());
			}
			final Throwable exception = repositoryTask.getException();
			if (null != exception) {
				ErrorDialog.showErrorDialogP(resources.getString(resourceKeyPrefix + ".error-dialog.title"), 
						resources.getString(resourceKeyPrefix + ".error-dialog.message"), exception, parent);
			}
		});
	}
}
