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
package name.bindul.bls.dm.core.spi.export;

import java.util.List;
import java.util.ServiceLoader;

import name.bindul.bls.dm.core.api.ServiceFactory;
import name.bindul.bls.dm.core.spi.repository.Repository;

public interface DataExportManager {
	
	static List<DataExportManager> availableExportManagers () {
		ServiceLoader<DataExportManager> serviceLoader = ServiceLoader.load(DataExportManager.class);
		return serviceLoader.stream().map(p -> p.get()).toList();
	}

	DataExportType supportedExportType();
	
	boolean canExportTo (DataExportLocation location);
	
	void export(DataExportLocation location, ServiceFactory serviceFactory, Repository repository) throws DataExportException;
}
