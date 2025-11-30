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
package name.bindul.bls.dm.ui.jfx.components.actions;

import java.util.ResourceBundle;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import name.bindul.bls.dm.core.api.BowlingLeagueStats;
import name.bindul.bls.dm.ui.jfx.context.ApplicationContext;

public class CloseRepositoryAction extends RepositoryLocationActionSupport implements EventHandler<ActionEvent> {

	public CloseRepositoryAction(ResourceBundle resources, Parent parent) {
		super(resources, parent);
	}

	@Override
	public void handle(ActionEvent event) {
		final Task<Void> closeRepositoryTask = createCloseRepositoryTask();
		ApplicationContext.getInstance().getExecutorService().execute(closeRepositoryTask);
	}

	private Task<Void> createCloseRepositoryTask() {
		final BowlingLeagueStats bls = BowlingLeagueStats.getInstance();
		final Task<Void> repositoryTask = new Task<>() {
			@Override
			protected Void call() throws Exception {
				updateTitle(resources.getString("action.close.task.title"));
				updateMessage(resources.getString("action.close.task.in-progress"));
				bls.closeRepository();
				updateMessage(resources.getString("action.close.task.completed"));
				return null;
			}
		};
		
		decorateRepositoryTask(repositoryTask, "action.close");
		return repositoryTask;
	}

}
