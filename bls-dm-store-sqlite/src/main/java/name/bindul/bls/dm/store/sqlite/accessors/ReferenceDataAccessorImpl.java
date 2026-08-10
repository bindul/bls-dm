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

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.mapstruct.factory.Mappers;

import name.bindul.bls.dm.core.model.BowlingCenter;
import name.bindul.bls.dm.core.spi.repository.RepositoryException;
import name.bindul.bls.dm.core.spi.repository.accessors.ReferenceDataAccessor;
import name.bindul.bls.dm.store.sqlite.orm.BeanMapper;
import name.bindul.bls.dm.store.sqlite.orm.BowlingCenterRepository;
import name.bindul.bls.dm.store.sqlite.orm.BowlingCenterRepository_;
import name.bindul.bls.dm.store.sqlite.orm.BowlingCenterTO;

public class ReferenceDataAccessorImpl implements ReferenceDataAccessor {
	
	private final SessionFactory sessionFactory;
	private final BeanMapper mapper = Mappers.getMapper(BeanMapper.class);
	
	public ReferenceDataAccessorImpl (SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public List<BowlingCenter> getBowlingCenters() throws RepositoryException {
		try (StatelessSession session = sessionFactory.openStatelessSession()) {
			final BowlingCenterRepository bcr = new BowlingCenterRepository_(session);
			return bcr.findAll().map(mapper::fromBowlingCenterStore).toList();
		}
	}

	@Override
	public void createBowlingCenter(BowlingCenter bowlingCenter) throws RepositoryException {
		sessionFactory.inStatelessTransaction(session -> session.insert(mapper.toBowlingCenterStore(bowlingCenter)));
	}

	@Override
	public void updateBowlingCenter(BowlingCenter bowlingCenter) throws RepositoryException {
		sessionFactory.inStatelessTransaction(session -> session.update(mapper.toBowlingCenterStore(bowlingCenter)));
	}

	@Override
	public void deleteBowlingCenter(String id) throws RepositoryException {
		sessionFactory.inStatelessTransaction(session -> {
			final BowlingCenterTO bcs = session.get(BowlingCenterTO.class, id);
			session.delete(bcs);
		});
	}

}
