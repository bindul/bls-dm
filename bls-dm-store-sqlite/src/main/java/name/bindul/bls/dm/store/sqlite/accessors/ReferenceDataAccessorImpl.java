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
package name.bindul.bls.dm.store.sqlite.accessors;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import name.bindul.bls.dm.core.model.BowlingCenter;
import name.bindul.bls.dm.core.spi.RepositoryException;
import name.bindul.bls.dm.core.spi.accessors.ReferenceDataAccessor;
import name.bindul.bls.dm.store.sqlite.daos.BowlingCenterDao;

public class ReferenceDataAccessorImpl implements ReferenceDataAccessor {
	
	private final DataSource ds;
	private final BowlingCenterDao dao;
	
	public ReferenceDataAccessorImpl (DataSource ds) {
		this.ds = ds;
		this.dao = new BowlingCenterDao();
	}

	@Override
	public List<BowlingCenter> getBowlingCenters() throws RepositoryException {
		try (Connection con = ds.getConnection()) {
			return dao.list(con);
		} catch (SQLException e) {
			throw new RepositoryException(e.getMessage(), e);
		}
	}

	@Override
	public void createBowlingCenter(BowlingCenter bowlingCenter) throws RepositoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateBowlingCenter(BowlingCenter bowlingCenter) throws RepositoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteBowlingCenter(String id) throws RepositoryException {
		// TODO Auto-generated method stub

	}

}
