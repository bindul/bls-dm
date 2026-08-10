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
package name.bindul.bls.dm.ui.jfx.components;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import name.bindul.bls.dm.ui.jfx.components.entityeditors.EntityEditorContainer;
import name.bindul.bls.dm.ui.jfx.components.toolbar.ToolbarController;

public class ApplicationLayoutController implements EntityEditorContainer {

	// This is a special injection by @FXML. The fx:id in the fxml is toolbar, but the field has 'Controller' 
	// added to it - then it injects the controller rather than the node.
	@FXML
	private ToolbarController toolbarController;
	
	@FXML
	private Pane mainContent;
	
	@FXML
	private Stage stage;
	
	@FXML
	public void initialize() {
		toolbarController.setEditorContainer(this);
	}

	@Override
	public Window getParentWindow() {
		return stage.getOwner();
	}

	@Override
	public void loadEditor(Node editor) {
		// TODO Load FXML heref up
		final ObservableList<Node> children = mainContent.getChildren();
		children.clear();
		children.add(editor);
	}
}
