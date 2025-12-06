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
package name.bindul.bls.dm.core.spi.repository.accessors;

import java.util.List;

import name.bindul.bls.dm.core.model.BowlingCenter;
import name.bindul.bls.dm.core.spi.repository.RepositoryException;

public interface ReferenceDataAccessor {

	public List<BowlingCenter> getBowlingCenters() throws RepositoryException;
	
	public void createBowlingCenter (BowlingCenter bowlingCenter) throws RepositoryException;
	
	public void updateBowlingCenter (BowlingCenter bowlingCenter) throws RepositoryException;
	
	public void deleteBowlingCenter (String id) throws RepositoryException;
}
