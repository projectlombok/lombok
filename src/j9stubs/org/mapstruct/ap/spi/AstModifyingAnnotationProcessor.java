/**
 *  Copyright 2012-2017 Gunnar Morling (http://www.gunnarmorling.de/)
 *  and/or other contributors as indicated by the @authors tag. See the
 *  copyright.txt file in the distribution for a full listing of all
 *  contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mapstruct.ap.spi;

import javax.lang.model.type.TypeMirror;

/**
 * A contract to be implemented by other annotation processors which - against the design philosophy of JSR 269 - alter
 * the types under compilation.
 * <p>
 * This contract will be queried by MapStruct when examining types referenced by mappers to be generated, most notably
 * the source and target types of mapping methods. If at least one AST-modifying processor announces further changes to
 * such type, the generation of the affected mapper(s) will be deferred to a future round in the annnotation processing
 * cycle.
 * <p>
 * Implementations are discovered via the service loader, i.e. a JAR providing an AST-modifying processor needs to
 * declare its implementation in a file {@code META-INF/services/org.mapstruct.ap.spi.AstModifyingAnnotationProcessor}.
 *
 * @author Gunnar Morling
 */
//@org.mapstruct.util.Experimental
public interface AstModifyingAnnotationProcessor {

    /**
     * Whether the specified type has been fully processed by this processor or not (i.e. this processor will amend the
     * given type's structure after this invocation).
     *
     * @param type The type of interest
     * @return {@code true} if this processor has fully processed the given type, {@code false} otherwise.
     */
    boolean isTypeComplete(TypeMirror type);
}