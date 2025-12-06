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
package name.bindul.bls.dm.core.api;

import java.util.List;

import name.bindul.bls.dm.core.impl.api.BowlingLeagueStatsImpl;
import name.bindul.bls.dm.core.spi.export.DataExportException;
import name.bindul.bls.dm.core.spi.export.DataExportLocation;
import name.bindul.bls.dm.core.spi.export.DataExportType;
import name.bindul.bls.dm.core.spi.repository.RepositoryException;
import name.bindul.bls.dm.core.spi.repository.RepositoryLocation;
import name.bindul.bls.dm.core.spi.repository.RepositoryLocationType;

public abstract class BowlingLeagueStats {
	
	// For now hard-coding the implementation
	private static class SingletonHelper {
		private static final BowlingLeagueStatsImpl INSTANCE = new BowlingLeagueStatsImpl();
	}
	
	public static BowlingLeagueStats getInstance() {
		return SingletonHelper.INSTANCE;
	}
	
	public abstract List<RepositoryLocationType> getSupportedRepositoryLocationTypes();
	
	public abstract boolean isRepositoryConnected();
	
	public abstract void newRepository (RepositoryLocation newBlsStore) throws RepositoryException;
	
	public abstract void openRepository (RepositoryLocation existingBlsStore) throws RepositoryException;
	
	public abstract void closeRepository () throws RepositoryException;
	
	public abstract List<DataExportType> getSupportedDataExportTypes();
	
	public abstract void exportData(DataExportLocation exportLocation) throws DataExportException;

	public abstract void addStateChangeListener (BlsStateChangeListener listener);
	
	public abstract void removeStateChangeListener (BlsStateChangeListener listener);
	
	public abstract void addEntityChangeListener (EntityChangeListener listener);
	
	public abstract void removeEntityChangeListener (EntityChangeListener listener);
	
	public abstract ServiceFactory getServiceFactory();
	
}
