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
package name.bindul.bls.dm.ui.jfx.components.entityeditors;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import lombok.extern.log4j.Log4j2;
import name.bindul.bls.dm.core.api.BowlingLeagueStats;
import name.bindul.bls.dm.core.api.ReferenceDataService;
import name.bindul.bls.dm.core.model.BowlingCenter;
import name.bindul.bls.dm.core.spi.repository.RepositoryException;
import name.bindul.bls.dm.ui.jfx.components.dialogs.ErrorDialog;

@Log4j2
public class RefDataViewController implements EntityEditor {

	@FXML
	private SplitPane view;
	
	@FXML
	private TreeView itemList;
	
	@FXML
	private AnchorPane contentPane;
	
	private final EntityEditorContainer container;
	private final ResourceBundle resources;
	private final BowlingLeagueStats bls;
	
	// TODO move FXML loading to ApplicationLayout controller
	public RefDataViewController (EntityEditorContainer container, ResourceBundle resources) {
		this.container = container;
		this.resources = resources;
		this.bls = BowlingLeagueStats.getInstance();
		
		initScene();
	}
	
	private void initScene() {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/name/bindul/bls/dm/ui/jfx/components/entityeditors/ref-data-view.fxml"));
		loader.setController(this);
		loader.setResources(this.resources);
		
		try {
			loader.load();
		} catch (IOException e) {
			log.warn("Error loading editor: {}", e.getMessage(), e);
			ErrorDialog.showErrorDialogP(resources.getString("app.error.load-data.title"), 
					MessageFormat.format(resources.getString("app.error.load-data.message"), e.getMessage()), e, view.getParent());
		}
	}
	
	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void load() {
		container.loadEditor(view);
		
		Platform.runLater(this::loadContentTree);
	}
	
	private void loadContentTree () {
		final ReferenceDataService refDataService = bls.getServiceFactory().getReferenceDataService();
		
		final TreeItem rootItem = new TreeItem();
		rootItem.setExpanded(true);
		itemList.setRoot(rootItem);
		itemList.setShowRoot(false);
		
		// Bowling Centers
		try {
			final TreeItem bowlingCentersItem = new TreeItem("Bowling Centers");
			final List<BowlingCenter> bowlingCenters = refDataService.getBowlingCenters();
			
			//TODO Need to move this to an observable list or so
			// https://stackoverflow.com/questions/35009982/javafx-treeview-of-multiple-object-types-and-more
			bowlingCenters.forEach(bc -> {
				final TreeItem bcTi = new TreeItem<>(bc);
				bowlingCentersItem.getChildren().add(bcTi);
			});
			
			rootItem.getChildren().add(bowlingCentersItem);
			
		} catch (RepositoryException e) {
			log.warn("Error loading editor content: {}", e.getMessage(), e);
			ErrorDialog.showErrorDialogP(resources.getString("app.error.load-data.title"), 
					MessageFormat.format(resources.getString("app.error.load-data.message"), e.getMessage()), e, view.getParent());
		}
	}
}
