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

import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileAccessPermission;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static org.gradle.internal.nativeintegration.filesystem.FileSystem.DEFAULT_DIR_MODE;
import static org.gradle.internal.nativeintegration.filesystem.FileSystem.DEFAULT_FILE_MODE;

public class DefaultFileAccessPermissions extends AbstractReadOnlyFileAccessPermissions implements FileAccessPermissionsInternal {

    public static int getDefaultUnixNumeric(boolean isDirectory) {
        return isDirectory ? DEFAULT_DIR_MODE : DEFAULT_FILE_MODE;
    }

    private final FileAccessPermissionInternal user;

    private final FileAccessPermissionInternal group;

    private final FileAccessPermissionInternal other;

    @Inject
    public DefaultFileAccessPermissions(ObjectFactory objectFactory, int unixNumeric) {
        this.user = objectFactory.newInstance(DefaultFileAccessPermission.class, getUserMask(unixNumeric));
        this.group = objectFactory.newInstance(DefaultFileAccessPermission.class, getGroupMask(unixNumeric));
        this.other = objectFactory.newInstance(DefaultFileAccessPermission.class, getOtherMask(unixNumeric));
    }

    @Override
    public FileAccessPermission getUser() {
        return user;
    }

    @Override
    public void user(Action<? super FileAccessPermission> configureAction) {
        configureAction.execute(user);
    }

    @Override
    public FileAccessPermission getGroup() {
        return group;
    }

    @Override
    public void group(Action<? super FileAccessPermission> configureAction) {
        configureAction.execute(group);
    }

    @Override
    public FileAccessPermission getOther() {
        return other;
    }

    @Override
    public void other(Action<? super FileAccessPermission> configureAction) {
        configureAction.execute(other);
    }

    @Override
    public void all(Action<? super FileAccessPermission> configureAction) {
        configureAction.execute(user);
        configureAction.execute(group);
        configureAction.execute(other);
    }

    @Override
    public void unix(String permissions) {
        try {
            if (permissions == null) {
                throw new IllegalArgumentException("A value must be specified.");
            }
            String trimmedPermissions = permissions.trim();
            if (trimmedPermissions.length() == 3) {
                int unixNumeric = toUnixNumericPermissions(trimmedPermissions);
                fromUnixNumeric(unixNumeric);
            } else if (trimmedPermissions.length() == 9) {
                fromUnixSymbolic(trimmedPermissions);
            } else {
                throw new IllegalArgumentException("Trimmed length must be either 3 (for numeric notation) or 9 (for symbolic notation).");
            }

        } catch (IllegalArgumentException cause) {
            throw new InvalidUserDataException((permissions == null ? "Null" : "'" + permissions + "'") + " isn't a proper Unix permission. " + cause.getMessage());
            //TODO: is this the right type to throw?
        }
    }

    @Override
    public void fromUnixNumeric(int unixNumeric) {
        user.fromUnixNumeric(getUserMask(unixNumeric));
        group.fromUnixNumeric(getGroupMask(unixNumeric));
        other.fromUnixNumeric(getOtherMask(unixNumeric));
    }

    @Override
    public void fromUnixSymbolic(String unixSymbolic) {
        user.fromUnixSymbolic(unixSymbolic.substring(0, 3));
        group.fromUnixSymbolic(unixSymbolic.substring(3, 6));
        other.fromUnixSymbolic(unixSymbolic.substring(6, 9));
    }

    private static int toUnixNumericPermissions(String permissions) {
        try {
            return Integer.parseInt(permissions, 8);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Can't be parsed as octal number.");
        }
    }
}
