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

public abstract class AbstractReadOnlyFileAccessPermissions implements ReadOnlyFileAccessPermissionsInternal {

    @Override
    public int toUnixNumeric() {
        return toUnixNumericPermissions(
            ((ReadOnlyFileAccessPermissionInternal) getUser()).toUnixNumeric(),
            ((ReadOnlyFileAccessPermissionInternal) getGroup()).toUnixNumeric(),
            ((ReadOnlyFileAccessPermissionInternal) getOther()).toUnixNumeric());
    }

    @SuppressWarnings("OctalInteger")
    protected static int getUserMask(int unixNumeric) { //todo: rename, not mask, but masked value
        return (unixNumeric & 0_700) >> 6;
    }

    @SuppressWarnings("OctalInteger")
    protected static int getGroupMask(int unixNumeric) { //todo: rename, not mask, but masked value
        return (unixNumeric & 0_070) >> 3;
    }

    @SuppressWarnings("OctalInteger")
    protected static int getOtherMask(int unixNumeric) { //todo: rename, not mask, but masked value
        return unixNumeric & 0_007;
    }

    private static int toUnixNumericPermissions(int user, int group, int other) {
        return 64 * user + 8 * group + other;
    }

}
