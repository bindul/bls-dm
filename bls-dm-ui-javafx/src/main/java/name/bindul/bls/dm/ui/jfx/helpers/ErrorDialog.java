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

import java.lang.Thread.UncaughtExceptionHandler;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Window;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ErrorDialog implements UncaughtExceptionHandler {

	public static void showErrorDialog (String errorTitle, String errorMessage, Throwable e, Window owner) {
		final Alert errorAlert = new Alert(AlertType.ERROR);
		if (null != owner) {
			errorAlert.initOwner(owner);
			errorAlert.initModality(Modality.WINDOW_MODAL);
		} else {
			errorAlert.initModality(Modality.APPLICATION_MODAL);
		}
		
		errorAlert.setHeaderText(errorTitle);
		
		StringBuilder em = new StringBuilder();
		em.append(errorMessage);
		if (null != e) {
			em.append(": ").append(e.getLocalizedMessage());
		}
		errorAlert.setContentText(em.toString());
		
		errorAlert.show();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.warn("Uncaught Error: " + e.getMessage(), e);
		if (Platform.isFxApplicationThread()) {
			ErrorDialog.showErrorDialog("Unknown error in BLS:DM", e.getMessage(), e, null);
		}
	}
}
