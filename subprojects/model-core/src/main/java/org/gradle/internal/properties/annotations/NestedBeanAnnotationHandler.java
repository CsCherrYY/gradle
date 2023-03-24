/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.internal.properties.annotations;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.gradle.api.tasks.Nested;
import org.gradle.internal.properties.PropertyValue;
import org.gradle.internal.properties.PropertyVisitor;
import org.gradle.internal.reflect.problems.ValidationProblemId;
import org.gradle.internal.reflect.validation.TypeValidationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;

import static org.gradle.internal.reflect.validation.Severity.ERROR;

public class NestedBeanAnnotationHandler extends AbstractPropertyAnnotationHandler {
    public NestedBeanAnnotationHandler(Collection<Class<? extends Annotation>> allowedModifiers) {
        super(Nested.class, Kind.OTHER, ImmutableSet.copyOf(allowedModifiers));
    }

    @Override
    public boolean isPropertyRelevant() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validatePropertyMetadata(PropertyMetadata propertyMetadata, TypeValidationContext validationContext) {
        if (Map.class.isAssignableFrom(propertyMetadata.getDeclaredType().getRawType())) {
            TypeToken<?> keyType = extractNestedType((TypeToken<Map<?, ?>>) propertyMetadata.getDeclaredType(), Map.class, 0);
            validateKeyType(propertyMetadata, validationContext, keyType);
        }
    }

    @Override
    public void visitPropertyValue(String propertyName, PropertyValue value, PropertyMetadata propertyMetadata, PropertyVisitor visitor) {
    }

    private static <T> TypeToken<?> extractNestedType(TypeToken<T> beanType, Class<? super T> parameterizedSuperClass, int typeParameterIndex) {
        ParameterizedType type = (ParameterizedType) beanType.getSupertype(parameterizedSuperClass).getType();
        return TypeToken.of(type.getActualTypeArguments()[typeParameterIndex]);
    }

    private static void validateKeyType(PropertyMetadata propertyMetadata, TypeValidationContext validationContext, TypeToken<?> keyType) {
        if (!keyType.getRawType().isAssignableFrom(String.class)) {
            validationContext.visitPropertyProblem(problem ->
                problem.withId(ValidationProblemId.NESTED_MAP_UNSUPPORTED_KEY_TYPE)
                    .reportAs(ERROR)
                    .forProperty(propertyMetadata.getPropertyName())
                    .withDescription(() -> "where key of @Nested Map is of type '" + keyType.getRawType().getName() + "'")
                    .happensBecause("Key of @Nested Map must be of type 'String'")
                    .addPossibleSolution("Change type of key to 'String'")
                    .documentedAt("validation_problems", "unsupported_key_type_of_nested_map")
            );
        }
    }
}
