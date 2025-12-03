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

import java.util.EventObject;

import lombok.Getter;

@Getter
public class EntityChangeEvent extends EventObject {

	private static final long serialVersionUID = 4116299154469826772L;

	public enum ChangeType { CREATED, UPDATED, DELETED };
	
	private final ChangeType changeType;
	private final String entityType;
	private final String entityId;
	
	public EntityChangeEvent(Object source, ChangeType changeType, String entityType, String entityId) {
		super(source);
		this.changeType = changeType;
		this.entityType = entityType;
		this.entityId = entityId;
	}
}
