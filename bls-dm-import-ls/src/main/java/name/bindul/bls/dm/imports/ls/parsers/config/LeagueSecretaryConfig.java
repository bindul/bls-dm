/*
 * Copyright (c) 2026. Bindul Bhowmik
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
package name.bindul.bls.dm.imports.ls.parsers.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Shared League Secretary configuration accessor.
 */
public final class LeagueSecretaryConfig {

    public static final String DEFAULT_CONFIG_FILE = "league-secretary-import.properties";

    private final String source;
    private final Properties properties;

    private LeagueSecretaryConfig(String source, Properties properties) {
        this.source = source;
        this.properties = properties;
    }

    public static LeagueSecretaryConfig load() {
        return load(DEFAULT_CONFIG_FILE);
    }

    public static LeagueSecretaryConfig load(String configFile) {
        String file = Objects.requireNonNull(configFile, "configFile cannot be null").trim();
        if (file.isBlank()) {
            throw new IllegalArgumentException("configFile cannot be blank");
        }

        Properties properties = new Properties();
        try (InputStream input = LeagueSecretaryConfig.class.getClassLoader().getResourceAsStream(file)) {
            if (input == null) {
                throw new IllegalStateException("Required config file not found on classpath: " + file);
            }
            properties.load(input);
            return new LeagueSecretaryConfig(file, properties);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read " + file, ex);
        }
    }

    public String requireString(String key) {
        String keyName = normalizeKey(key);
        String value = Objects.requireNonNull(properties.getProperty(keyName), "Missing required property: " + keyName).trim();
        if (value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + keyName);
        }
        return value;
    }

    public boolean requireBoolean(String key) {
        String keyName = normalizeKey(key);
        String value = requireString(keyName);
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new IllegalStateException("Property " + keyName + " must be true or false");
        }
        return Boolean.parseBoolean(value);
    }

    public String source() {
        return source;
    }

    private static String normalizeKey(String key) {
        String keyName = Objects.requireNonNull(key, "key cannot be null").trim();
        if (keyName.isBlank()) {
            throw new IllegalArgumentException("key cannot be blank");
        }
        return keyName;
    }
}
