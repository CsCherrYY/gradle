/*
 * Copyright 2015 the original author or authors.
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
package org.gradle.language.base.internal.resolve;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.internal.DefaultDomainObjectSet;
import org.gradle.api.internal.artifacts.DefaultDependencySet;
import org.gradle.api.internal.artifacts.ResolveContext;
import org.gradle.api.internal.artifacts.configurations.ResolutionStrategyInternal;
import org.gradle.api.internal.artifacts.ivyservice.resolutionstrategy.DefaultResolutionStrategy;
import org.gradle.internal.component.local.model.DefaultLibraryBinaryIdentifier;
import org.gradle.language.base.internal.DependentSourceSetInternal;

public class DependentSourceSetResolveContext implements ResolveContext {
    private final String projectPath;
    private final String componentName;
    private final String variant;
    private final DependentSourceSetInternal sourceSet;
    private final ResolutionStrategyInternal resolutionStrategy = new DefaultResolutionStrategy();

    public DependentSourceSetResolveContext(String projectPath, String componentName, String variant, DependentSourceSetInternal sourceSet) {
        this.projectPath = projectPath;
        this.componentName = componentName;
        this.variant = variant;
        this.sourceSet = sourceSet;
    }

    public DependentSourceSetInternal getSourceSet() {
        return sourceSet;
    }

    @Override
    public String getName() {
        return DefaultLibraryBinaryIdentifier.CONFIGURATION_NAME;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getVariant() {
        return variant;
    }

    @Override
    public ResolutionStrategyInternal getResolutionStrategy() {
        return resolutionStrategy;
    }

    @Override
    public DependencySet getDependencies() {
        DefaultDomainObjectSet<Dependency> backingSet = new DefaultDomainObjectSet<Dependency>(Dependency.class);
        return new DefaultDependencySet(String.format("%s dependencies", this.getName()), backingSet);
    }


    @Override
    public DependencySet getAllDependencies() {
        return new DefaultDependencySet(String.format("%s dependencies", this.getName()), new DefaultDomainObjectSet<Dependency>(Dependency.class));
    }
}
