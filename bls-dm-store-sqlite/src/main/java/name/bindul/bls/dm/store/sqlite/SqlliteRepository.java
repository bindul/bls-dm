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
package name.bindul.bls.dm.store.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import name.bindul.bls.dm.core.spi.Repository;
import name.bindul.bls.dm.core.spi.RepositoryException;

@Log4j2
@RequiredArgsConstructor
public class SqlliteRepository implements Repository {

	private final String jdbcUrl;
	private final boolean newRepository;
	
	private Connection connection;
	
	public void connect() throws SQLException {
		// No point pooling connections here: https://stackoverflow.com/questions/15822778/sqlite-connection-pool-in-java-locked-database
		if (null == connection) {
			connection = DriverManager.getConnection(jdbcUrl);
		}
	}

	@Override
	public void close() throws RepositoryException {
		if (null != connection) {
			try {
				connection.close();
				connection = null;
				log.debug("Closed repository connection");
			} catch (SQLException e) {
				throw new RepositoryException("Error closing connection to repository.", e);
			}
		}
	}
}
