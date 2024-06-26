/*
 * Copyright 2020 VicTools.
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

package com.github.victools.jsonschema.module.jackson;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.victools.jsonschema.generator.CustomDefinition;
import com.github.victools.jsonschema.generator.CustomDefinitionProviderV2;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the {@link CustomDefinitionProviderV2} interface for treating object types based on a {@link JsonValue} annotation
 * being present with {@code value = true} on exactly one argument-free method. If no such annotations exist, no custom definition will be returned;
 * thereby falling back on whatever is defined in a following custom definition (e.g. from one of the standard generator {@code Option}s).
 */
public class JsonValueDefinitionProvider implements CustomDefinitionProviderV2 {

    @Override
    public CustomDefinition provideCustomSchemaDefinition(ResolvedType javaType, SchemaGenerationContext context) {
        ResolvedMethod jsonValueAnnotatedMethod = getJsonValueAnnotatedMethod(javaType, context);
        if (jsonValueAnnotatedMethod == null) {
            return null;
        }
        return new CustomDefinition(context.createDefinition(jsonValueAnnotatedMethod.getType()));
    }

    /**
     * Look-up the single {@link JsonValue} annotated method with {@code value = true} and no expected arguments.
     *
     * @param javaType targeted type to look-up serialization method for
     * @param context generation context providing access to type resolution context
     * @return single method with {@link JsonValue} annotation
     */
    protected ResolvedMethod getJsonValueAnnotatedMethod(ResolvedType javaType, SchemaGenerationContext context) {
        ResolvedMethod[] memberMethods = context.getTypeContext().resolveWithMembers(javaType).getMemberMethods();
        Set<ResolvedMethod> jsonValueAnnotatedMethods = Stream.of(memberMethods)
                .filter(method -> method.getArgumentCount() == 0)
                .filter(method -> Optional.ofNullable(method.getAnnotations().get(JsonValue.class)).map(JsonValue::value).orElse(false))
                .collect(Collectors.toSet());
        if (jsonValueAnnotatedMethods.size() == 1) {
            return jsonValueAnnotatedMethods.iterator().next();
        }
        return null;
    }

}
