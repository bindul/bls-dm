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
package name.bindul.bls.dm.core.impl.api;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.event.EventListenerSupport;

import name.bindul.bls.dm.core.api.BlsStateChangeListener;
import name.bindul.bls.dm.core.api.BowlingLeagueStats;
import name.bindul.bls.dm.core.spi.Repository;
import name.bindul.bls.dm.core.spi.RepositoryLocation;
import name.bindul.bls.dm.core.spi.RepositoryLocationType;
import name.bindul.bls.dm.core.spi.RepositoryProvider;

public class BowlingLeagueStatsImpl extends BowlingLeagueStats {

	private final EventListenerSupport<BlsStateChangeListener> stateChangeListenerSupport = new EventListenerSupport<>(BlsStateChangeListener.class);
	
	private final List<RepositoryProvider> repositoryProviders;
	
	private Repository repository;
	
	public BowlingLeagueStatsImpl () {
		this.repositoryProviders = RepositoryProvider.availableProviders();
	}
	
	@Override
	public List<RepositoryLocationType> getSupportedRepositoryLocationTypes() {
		return repositoryProviders.stream().map(RepositoryProvider::supportedLocationType).toList();
	}

	@Override
	public boolean isRepositoryConnected() {
		return repository != null;
	}

	@Override
	public void newRepository(RepositoryLocation newBlsStore) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openRepository(RepositoryLocation existingBlsStore) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addStateChangeListener(BlsStateChangeListener listener) {
		stateChangeListenerSupport.addListener(listener);
	}

	@Override
	public void removeStateChangeListener(BlsStateChangeListener listener) {
		stateChangeListenerSupport.removeListener(listener);
	}
}
