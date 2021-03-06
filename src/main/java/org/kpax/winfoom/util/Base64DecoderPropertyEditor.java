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

package org.kpax.winfoom.util;

import org.apache.commons.lang3.*;

import java.beans.*;
import java.util.*;

/**
 * Decode the properties of {@code encoded(value)} format.
 */
public class Base64DecoderPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String source) {
        if (StringUtils.isNotEmpty(source)) {
            if (source.startsWith("encoded(")) {
                String encoded = source.substring("encoded(".length(), source.length() - 1);
                setValue(new String(Base64.getDecoder().decode(encoded)));
                return;
            }
        }
        setValue(source);
    }

}