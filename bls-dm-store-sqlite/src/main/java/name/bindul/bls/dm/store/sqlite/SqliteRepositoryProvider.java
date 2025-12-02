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

import java.sql.SQLException;
import java.util.Arrays;

import lombok.extern.log4j.Log4j2;
import name.bindul.bls.dm.core.spi.Repository;
import name.bindul.bls.dm.core.spi.RepositoryException;
import name.bindul.bls.dm.core.spi.RepositoryLocation;
import name.bindul.bls.dm.core.spi.RepositoryLocation.LocalFileRepositoryLocation;
import name.bindul.bls.dm.core.spi.RepositoryLocationType;
import name.bindul.bls.dm.core.spi.RepositoryProvider;

@Log4j2
public class SqliteRepositoryProvider extends RepositoryProvider {
	
	private static final RepositoryLocationType SUPPORTED_LOC_TYPE = RepositoryLocationType.builder()
			.typeCode("local.file.sqllite")
			.isLocalFile(true)
			.localFileExtensions(Arrays.asList("*.sqlite", "*.sqlite3", "*.db", "*.db3"))
			.supportsCreateNew(true)
			.requiresCredentials(false)
			.build();

	public SqliteRepositoryProvider () throws RepositoryException {
		// Validate we have JDBC driver
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new RepositoryException("SQLite JDBC driver not found.", e);
		}
	}
	
	@Override
	public RepositoryLocationType supportedLocationType() {
		return SUPPORTED_LOC_TYPE;
	}

	@Override
	public boolean canOpenOrCreate(RepositoryLocation location) {
		return location instanceof LocalFileRepositoryLocation;
	}

	@Override
	public Repository open(RepositoryLocation location) throws RepositoryException {
		return openOrCreateRepository((LocalFileRepositoryLocation) location, false);
	}

	@Override
	public Repository create(RepositoryLocation location) throws RepositoryException {
		return openOrCreateRepository((LocalFileRepositoryLocation) location, true);
	}

	private SqlliteRepository openOrCreateRepository(LocalFileRepositoryLocation location, boolean create) throws RepositoryException {
		if (!(location instanceof LocalFileRepositoryLocation)) {
			throw new RepositoryException("This implementation can only open local file repositories");
		}
		try {
			final SqlliteRepository repository = new SqlliteRepository(location.getLocation(), create);
			repository.connect();
			return repository;
		} catch (SQLException e) {
			throw new RepositoryException("Error opening and validating the repository: " + e.getMessage(), e);
		}
	}
}
