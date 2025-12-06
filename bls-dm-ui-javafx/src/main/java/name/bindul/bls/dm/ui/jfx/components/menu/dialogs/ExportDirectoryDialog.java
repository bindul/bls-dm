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
package name.bindul.bls.dm.ui.jfx.components.menu.dialogs;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
import name.bindul.bls.dm.core.spi.export.DataExportLocation.LocalDirectoryExportLocation;
import name.bindul.bls.dm.ui.jfx.components.dialogs.ErrorDialog;

// https://stackoverflow.com/questions/64964471/java-fx-create-custom-dialog-with-fxml-file-how-to-set-or-get-result-from-it
public class ExportDirectoryDialog extends Dialog<LocalDirectoryExportLocation> {

	private final ResourceBundle resources;
	
	@FXML
	private TextField exportDirectoryPath;
	
	@FXML
	private CheckBox cleanDirectory;
	
	public ExportDirectoryDialog (Window owner, ResourceBundle resources) {
		this.resources = resources;
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/name/bindul/bls/dm/ui/jfx/components/menu/dialogs/export-directory-selector.fxml"));
			loader.setController(this);
			loader.setResources(this.resources);
			
			DialogPane dialogPane = loader.load();
			
			initOwner(owner);
			initModality(Modality.APPLICATION_MODAL);
			setResizable(true);
			setTitle(this.resources.getString("action.export.dialog.title"));
			setDialogPane(dialogPane);
			
			setOnShowing(event -> Platform.runLater(() -> {
				// https://stackoverflow.com/questions/55190380/javafx-creates-alert-dialog-which-is-too-small
				ExportDirectoryDialog.this.getDialogPane().getScene().getWindow().sizeToScene();
				ExportDirectoryDialog.this.setResizable(false);
			}));
			
			setResultConverter(buttonType -> {
				if (!Objects.equals(ButtonBar.ButtonData.OK_DONE, buttonType.getButtonData())) {
					return null;
				}
				return LocalDirectoryExportLocation.builder()
						.directory(new File(exportDirectoryPath.getText()))
						.emptyDirectory(cleanDirectory.isSelected())
						.build();
			});
			
		} catch (IOException e) {
			ErrorDialog.showErrorDialog(resources.getString("menu.init.error.title"), 
					resources.getString("menu.init.error.message"), e, owner);
		}
	}
	
	@FXML
	private void showDirChooser(ActionEvent actionEvent) {
		final DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle(resources.getString("action.export.dialog.local.dir-chooser.title"));
		
		// TODO Set initial directory from history?
		File selectedDirectory = dirChooser.showDialog(getOwner());
		if (selectedDirectory != null) {
			exportDirectoryPath.setText(selectedDirectory.getPath());
		}
	}
}
