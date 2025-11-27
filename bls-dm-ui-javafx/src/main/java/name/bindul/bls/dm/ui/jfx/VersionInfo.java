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
package name.bindul.bls.dm.ui.jfx;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@Log4j2 @UtilityClass
public class VersionInfo {

	private static final String VERSION_INFO_FILE = "/name/bindul/bls/dm/ui/jfx/version.properties";
	
	public static String getVersion() {
		final Properties versionProps = new Properties();
		try (final InputStream versionPropsStream = VersionInfo.class.getResourceAsStream(VERSION_INFO_FILE)) {
			versionProps.load(versionPropsStream);
			return versionProps.getProperty("version");
		} catch (IOException e) {
			e.printStackTrace();
			log.warn("Unable to load version info", e);
		}
		return "UNABLE TO RETRIEVE VERSION";
	}
}
