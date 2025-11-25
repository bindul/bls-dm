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
package name.bindul.bls.dm.ui.jfx.components.help;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public class HelpMenuController {
	
	@FXML
	protected void showAbout(ActionEvent event) throws IOException {
		
		final FXMLLoader loader = new FXMLLoader(HelpMenuController.class.getResource("/name/bindul/bls/dm/ui/jfx/components/help/about-dialog.fxml"));
		final DialogPane aboutDialogPane = (DialogPane) loader.load();
		aboutDialogPane.autosize();
		
		final Dialog<String> aboutDialog = new Dialog<>();
		aboutDialog.setDialogPane(aboutDialogPane);
		aboutDialog.showAndWait();		
	}
}
