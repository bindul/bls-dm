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
import name.bindul.bls.dm.core.spi.export.DataExportException;
import name.bindul.bls.dm.core.spi.export.DataExportLocation;
import name.bindul.bls.dm.core.spi.export.DataExportManager;
import name.bindul.bls.dm.core.spi.export.DataExportType;
import name.bindul.bls.dm.core.spi.repository.Repository;
import name.bindul.bls.dm.core.spi.repository.RepositoryException;
import name.bindul.bls.dm.core.spi.repository.RepositoryLocation;
import name.bindul.bls.dm.core.spi.repository.RepositoryLocationType;
import name.bindul.bls.dm.core.spi.repository.RepositoryProvider;

public class BowlingLeagueStatsImpl extends BowlingLeagueStats implements ServiceImplementationSupport {

	private final EventListenerSupport<BlsStateChangeListener> stateChangeListenerSupport = EventListenerSupport
			.create(BlsStateChangeListener.class);
	private final EventListenerSupport<EntityChangeListener> entityChangeListenerSupport = EventListenerSupport
			.create(EntityChangeListener.class);
	
	private final List<RepositoryProvider> repositoryProviders;
	private final List<DataExportManager> dataExportManagers;
	private final ServiceFactoryImpl serviceFactory = new ServiceFactoryImpl();
	
	private Repository repository;
	
	public BowlingLeagueStatsImpl () {
		this.repositoryProviders = RepositoryProvider.availableProviders();
		this.dataExportManagers = DataExportManager.availableExportManagers();
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
	public List<DataExportType> getSupportedDataExportTypes() {
		return dataExportManagers.stream().map(DataExportManager::supportedExportType).toList();
	}

	@Override
	public void exportData(DataExportLocation exportLocation) throws DataExportException {
		final DataExportManager dem = dataExportManagers.stream()
				.filter(t -> t.canExportTo(exportLocation))
				.findFirst()
				.orElseThrow(() -> new DataExportException("Unable to find a export proider for the location selected"));
		dem.export(exportLocation, getServiceFactory(), getRepository());
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
