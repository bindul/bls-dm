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
package name.bindul.bls.dm.store.sqlite.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import name.bindul.bls.dm.core.model.BowlingCenter;

public class BowlingCenterDao implements BlsDao<BowlingCenter, String>{
	
	private static final String COL_CENTER_ID = "CENTER_ID";
	private static final String COL_NAME = "NAME";
	private static final String COL_IS_ACTIVE = "IS_ACTIVE";
	private static final String COL_LOCATION = "LOCATION";
	private static final String TBL_BOWLING_CENTER = "BOWLING_CENTER";
	private static final String SQL_SELECT_ALL = "SELECT " + COL_CENTER_ID + ", " + COL_NAME + ", " + COL_IS_ACTIVE + ", " + COL_LOCATION + " FROM " + TBL_BOWLING_CENTER;

	@Override
	public Optional<BowlingCenter> get(String key, Connection connection) throws SQLException {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public BowlingCenter insert(BowlingCenter entity, Connection connection) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BowlingCenter update(BowlingCenter entity, Connection connection) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(String key, Connection connection) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<BowlingCenter> list(Connection connection) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_ALL);
				ResultSet rs = stmt.executeQuery()) {
			final List<BowlingCenter> centers = new ArrayList<>();
			while (rs.next()) {
				BowlingCenter bc = new BowlingCenter();
				bc.setId(rs.getString(COL_CENTER_ID));
				bc.setName(rs.getString(COL_NAME));
				bc.setLocation(rs.getString(COL_LOCATION));
				final int isActive = rs.getInt(COL_IS_ACTIVE);
				bc.setActive(isActive != 0);
				
				centers.add(bc);
			}
			return centers;
		}
	}
}
