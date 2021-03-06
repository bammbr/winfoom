/*
 * Copyright (c) 2020. Eugen Covaci
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 */

package org.kpax.winfoom.config;

import java.util.*;
import java.util.stream.*;

/**
 * Provide information about the current operating system, Spring's active profiles.
 */
public class SystemContext {

    /**
     * The name of the current operating system.
     */
    public static final String OS_NAME = System.getProperty("os.name");

    /**
     * Is Windows the current operating system?
     */
    public static final boolean IS_OS_WINDOWS = OS_NAME.toLowerCase(Locale.ROOT).startsWith("windows");

    /**
     * The list of provided Spring active profiles with the {@code windows} profile appended
     * if the current operating system is Windows.
     */
    public static final List<String> PROFILES =
            Optional.ofNullable(System.getProperty("spring.profiles.active")).
                    map(s -> List.of((s + (IS_OS_WINDOWS ? ",windows" : "")).split(","))).
                    orElse(IS_OS_WINDOWS ? Collections.singletonList("windows") : Collections.emptyList());

    /**
     * Is the application running in graphical mode?
     */
    public static final boolean IS_GUI_MODE = PROFILES.contains("gui");

    /**
     * Apply {@link #PROFILES}'s content to the {@code spring.profiles.active} environment variable.
     */
    public static void setSpringActiveProfiles() {
        System.setProperty("spring.profiles.active", String.join(",", PROFILES));
    }

}
