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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernatePersistenceConfiguration;
import org.hibernate.tool.schema.Action;
import org.sqlite.JDBC;
import org.sqlite.javax.SQLiteConnectionPoolDataSource;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSetStatus;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.log4j.Log4j2;
import name.bindul.bls.dm.core.spi.Repository;
import name.bindul.bls.dm.core.spi.RepositoryException;
import name.bindul.bls.dm.core.spi.accessors.ReferenceDataAccessor;
import name.bindul.bls.dm.store.sqlite.accessors.ReferenceDataAccessorImpl;

@Log4j2
public class SqlliteRepository implements Repository {
	
	private static final String DB_CHANGE_LOG_INDEX = "/bls-sqlite-db/changeset-index.xml";

	private final File repositoryLocation;
	private final String jdbcUrl;
	private final boolean newRepository;
	
	private SQLiteConnectionPoolDataSource dataSource;

	private SessionFactory sessionFactory;
	
	public SqlliteRepository (File repositoryLocation, boolean newRepository) {
		this.repositoryLocation = repositoryLocation;
		this.newRepository = newRepository;
		
		this.jdbcUrl = JDBC.PREFIX + repositoryLocation.getPath();
		log.info("Will open repository at: {}", jdbcUrl);
	}
	
	public void connect() throws RepositoryException, SQLException {
		if (null == dataSource) {
			dataSource = new SQLiteConnectionPoolDataSource();
			dataSource.setUrl(jdbcUrl);
			try {
				if (newRepository) {
					updateSchema();
				} else if (hasSchemaChanges()) {
					backupRepositoryFile();
					updateSchema();
				}
			} catch (LiquibaseException e) {
				log.warn("Error executing liquibase changeset: {} {}", e.getDetails(), e.getMessage(), e);
				throw new RepositoryException("Error executing liquibase DB changesets: " + e.getMessage(), e);
			}
		}
		setupHibernate();
	}

	@Override
	public void close() throws RepositoryException {
		if (null != dataSource) {
			// Does not seem to have a close function!
			dataSource = null;
		}
		if (null != sessionFactory) {
			sessionFactory.close();
			sessionFactory = null;
		}
	}
		
	@Override
	public ReferenceDataAccessor getReferenceDataAccessor() {
		return new ReferenceDataAccessorImpl(sessionFactory);
	}
	
	private void setupHibernate () {
		sessionFactory = new HibernatePersistenceConfiguration("BLS", getClass())
				.jdbcUrl(jdbcUrl)
				.schemaToolingAction(Action.VALIDATE)
				.showSql(true, true, true) // TODO Externalize it
				.createEntityManagerFactory();
	}

	private boolean hasSchemaChanges () throws SQLException, LiquibaseException {
		final List<ChangeSetStatus> changeSetStatuses = executeLiquibaseAction(
				liquibase -> liquibase.getChangeSetStatuses(new Contexts(), new LabelExpression()));
		return changeSetStatuses.stream().anyMatch(ChangeSetStatus::getWillRun);
	}
	
	private void updateSchema() throws SQLException, LiquibaseException {
		executeLiquibaseAction(liquibase -> {liquibase.update(); return null;});
	}
	
	private <R> R executeLiquibaseAction(LiquibaseOperation<R> liquibaseAction) throws SQLException, LiquibaseException {
		try (Connection connection = dataSource.getConnection()) {
			final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
			try (final Liquibase liquibase = new Liquibase(DB_CHANGE_LOG_INDEX, new ClassLoaderResourceAccessor(), database)) {
				return liquibaseAction.apply(liquibase);
			}
		}
	}
	
	private void backupRepositoryFile() throws RepositoryException {
		final String backupTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		final String backupFileLoc = new StringBuilder(repositoryLocation.getPath()).append(".").append(backupTimestamp).append(".backup").toString();
		
		try {
			Files.copy(repositoryLocation.toPath(), Paths.get(backupFileLoc), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RepositoryException("Error backing up repository file before updating: " + e.getMessage(), e);
		}
	}
	
	@FunctionalInterface
	interface LiquibaseOperation<R> {
		R apply (Liquibase liquibase) throws LiquibaseException;
	}
}
