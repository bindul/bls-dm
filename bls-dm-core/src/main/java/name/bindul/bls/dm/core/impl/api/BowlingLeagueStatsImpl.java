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

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.event.EventListenerSupport;

import name.bindul.bls.dm.core.api.BlsStateChangeEvent;
import name.bindul.bls.dm.core.api.BlsStateChangeListener;
import name.bindul.bls.dm.core.api.BowlingLeagueStats;
import name.bindul.bls.dm.core.api.EntityChangeEvent;
import name.bindul.bls.dm.core.api.EntityChangeListener;
import name.bindul.bls.dm.core.api.ReferenceDataService;
import name.bindul.bls.dm.core.api.ServiceFactory;
import name.bindul.bls.dm.core.spi.Repository;
import name.bindul.bls.dm.core.spi.RepositoryException;
import name.bindul.bls.dm.core.spi.RepositoryLocation;
import name.bindul.bls.dm.core.spi.RepositoryLocationType;
import name.bindul.bls.dm.core.spi.RepositoryProvider;

public class BowlingLeagueStatsImpl extends BowlingLeagueStats implements ServiceImplementationSupport {

	private final EventListenerSupport<BlsStateChangeListener> stateChangeListenerSupport = EventListenerSupport
			.create(BlsStateChangeListener.class);
	private final EventListenerSupport<EntityChangeListener> entityChangeListenerSupport = EventListenerSupport
			.create(EntityChangeListener.class);
	
	private final List<RepositoryProvider> repositoryProviders;
	private final ServiceFactoryImpl serviceFactory = new ServiceFactoryImpl();
	
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
	public void newRepository(RepositoryLocation newBlsStore) throws RepositoryException {
		final Optional<RepositoryProvider> prov = findRepositoryProvider(newBlsStore);
		if (prov.isPresent()) {
			this.repository = prov.get().create(newBlsStore);
			fireRepositoryLoadedOrUnloadedEvent(newBlsStore);
		}
	}

	@Override
	public void openRepository(RepositoryLocation existingBlsStore) throws RepositoryException {
		final Optional<RepositoryProvider> prov = findRepositoryProvider(existingBlsStore);
		if (prov.isPresent()) {
			this.repository = prov.get().open(existingBlsStore);
			fireRepositoryLoadedOrUnloadedEvent(existingBlsStore);
		}
	}
	
	@Override
	public void closeRepository() throws RepositoryException {
		if (isRepositoryConnected()) {
			this.repository.close();
			this.repository = null;
			fireRepositoryLoadedOrUnloadedEvent(null);
		}
	}

	private Optional<RepositoryProvider> findRepositoryProvider(RepositoryLocation blsStore) {
		return repositoryProviders.stream().filter(rp -> rp.canOpenOrCreate(blsStore)).findFirst();
	}
	
	private void fireRepositoryLoadedOrUnloadedEvent(RepositoryLocation location) {
		stateChangeListenerSupport.fire()
			.repositoryLoadedOrUnloaded(
					new BlsStateChangeEvent(this, 
							isRepositoryConnected(), 
							(null != location) ? location.locationDisplayValue() : null));
	}

	@Override
	public void fireEntityChangeEvent(EntityChangeEvent event) {
		entityChangeListenerSupport.fire().entityChanged(event);
	}

	@Override
	public Repository getRepository() {
		return repository;
	}

	@Override
	public void addStateChangeListener(BlsStateChangeListener listener) {
		stateChangeListenerSupport.addListener(listener);
	}

	@Override
	public void removeStateChangeListener(BlsStateChangeListener listener) {
		stateChangeListenerSupport.removeListener(listener);
	}

	@Override
	public void addEntityChangeListener(EntityChangeListener listener) {
		entityChangeListenerSupport.addListener(listener);
	}

	@Override
	public void removeEntityChangeListener(EntityChangeListener listener) {
		entityChangeListenerSupport.removeListener(listener);
	}

	@Override
	public ServiceFactory getServiceFactory() {
		return serviceFactory;
	}

	// Services
	private class ServiceFactoryImpl implements ServiceFactory {

		private ReferenceDataService refDataSvc = new ReferenceDataServiceImpl(BowlingLeagueStatsImpl.this);
		
		@Override
		public ReferenceDataService getReferenceDataService() {
			return refDataSvc;
		}
		
	}
}
