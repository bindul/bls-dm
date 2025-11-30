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
package name.bindul.bls.dm.ui.jfx.context;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import name.bindul.bls.dm.ui.jfx.components.editors.EntityEditor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class EntityEditorRegistry {
	
	private final List<EntityEditor> openEditors = new ArrayList<>();
	
	public void registerEditor(EntityEditor editor) {
		openEditors.add(editor);
	}
	
	public void deRegisterEditor(EntityEditor editor) {
		openEditors.remove(editor);
	}
	
	public boolean hasDirtyEditors () {
		return openEditors.stream().anyMatch(EntityEditor::isDirty);
	}
}
