/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.file;

import org.gradle.api.file.FileAccessPermission;
import org.gradle.api.file.FileAccessPermissions;

public interface FileAccessPermissionInternal extends FileAccessPermission, ReadOnlyFileAccessPermissionInternal {

    /**
     * Sets the user permission from a numeric Unix permission.
     * See {@link FileAccessPermissions#unix(String)} for details,
     * input value is equivalent to one of the three octal digits.
     */
    void fromUnixNumeric(int unixNumeric);

    /**
     * Sets the user permission from a symbolic Unix permission.
     * See {@link FileAccessPermissions#unix(String)} for details,
     * input value is equivalent to one of the three sets of symbol triplets.
     */
    void fromUnixSymbolic(String unixSymbolic);

}
