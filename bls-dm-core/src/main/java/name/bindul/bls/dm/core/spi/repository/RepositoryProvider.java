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
package name.bindul.bls.dm.core.spi.repository;

import java.util.List;
import java.util.ServiceLoader;

public abstract class RepositoryProvider {
	
	public static List<RepositoryProvider> availableProviders () {
		ServiceLoader<RepositoryProvider> serviceLoader = ServiceLoader.load(RepositoryProvider.class);
		return serviceLoader.stream().map(p -> p.get()).toList();
	}
	
	public abstract RepositoryLocationType supportedLocationType();
	
	public abstract boolean canOpenOrCreate(RepositoryLocation location);

	public abstract Repository open(RepositoryLocation location) throws RepositoryException;
	
	public abstract Repository create(RepositoryLocation location) throws RepositoryException;
}
