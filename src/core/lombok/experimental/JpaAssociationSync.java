/*
 * Copyright (C) 2024 The Project Lombok Authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.experimental;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @JpaAssociationSync} is a Lombok annotation used to facilitate the synchronization
 * of JPA associations, particularly in the context of bidirectional relationships.
 * <p>
 *     This annotation can be applied at both the class and field level. When applied, it generates
 *     helper methods to manage JPA associations, ensuring that both sides of a bidirectional
 *     relationship remain consistent.
 * </p>
 * <p>
 *     The methods is generated according to <a href="https://vladmihalcea.com/jpa-hibernate-synchronize-bidirectional-entity-associations/">Vlad Mihalcea article</a>
 * </p>
 *
 * <h2>Example 1</h2>
 * <pre>
 * &#64;Entity
 * public class ExampleEntity {
 *     &#64;JpaAssociationSync
 *     &#64;OneToMany(mappedBy = "exampleEntities")
 *     private List&lt;RelatedEntity&gt; relatedEntity;
 *
 *     &#64;JpaAssociationSync
 *     &#64;JpaAssociationSync.Extra(paramName = "someRelatedField")
 *     &#64;OneToOne(mappedBy = "exampleEntity")
 *     private AnotherRelatedEntity anotherRelatedEntity;
 *
 *     // other fields, getters, and setters
 * }
 * </pre>
 *
 * <h2>Example 2</h2>
 * <pre>
 * &#64;JpaAssociationSync
 * &#64;Entity
 * public class ExampleEntity {
 *     &#64;OneToOne(mappedBy = "exampleEntity")
 *     private RelatedEntity relatedEntity;
 *
 *     // other fields, getters, and setters
 * }
 * </pre>
 *
 * @see lombok.experimental.JpaAssociationSync.Extra
 *
 * @see jakarta.persistence.OneToMany
 * @see jakarta.persistence.OneToOne
 * @see jakarta.persistence.ManyToMany
 * @see javax.persistence.OneToMany
 * @see javax.persistence.OneToOne
 * @see javax.persistence.ManyToMany
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface JpaAssociationSync {

    /**
     * {@code @JpaAssociationSync.Extra} is a nested annotation that provides additional
     * configuration options for the {@code @JpaAssociationSync} annotation when applied at the field level.
     * <p>
     *     This annotation allows for specifying the parameter name for generated methods and the name of
     *     the field on the inverse side of a bidirectional relationship.
     * </p>
     *
     * <h2>Parameters</h2>
     * <ul>
     * <li><b>paramName</b>: Specifies the name of the parameter used in the generated methods.
     * If not specified, the default is the name of the field's type.</li>
     * <li><b>inverseSideFieldName</b>: Necessary on the owning side of bidirectional {@code @OneToOne} or
     * {@code @ManyToMany} associations. Indicates the name of the field on the inverse side of the relationship.
     * On the inverse side, this parameter is redundant if the {@code mappedBy} attribute is used on the
     * association annotation.</li>
     * </ul>
     *
     * <h2>Example</h2>
     * <pre>
     * &#64;JpaAssociationSync
     * &#64;Entity
     * public class ExampleEntity {
     *     &#64;JpaAssociationSync.Extra(paramName = "entity", inverseSideFieldName = "exampleEntities")
     *     &#64;ManyToMany
     *     private Set&lt;RelatedEntity&gt; relatedEntities;
     *
     *     // other fields, getters, and setters
     * }
     * </pre>
     */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Extra {
        /**
         * @return Tells lombok the name of the parameter for jpa association sync methods,
         * by default - the name of the type. Also affects the method names
         */
        String paramName() default "";

        /**
         * Necessary only on the owning side of bidirectional {@code @OneToOne} or {@code @ManyToMany} associations.
         * On the inverse side the similar parameter is redundant,
         * because the parameter {@code mappedBy} on the association annotation is enough.
         *
         * @return Tells lombok the name of the field on the inverse side.
         */
        String inverseSideFieldName() default "";
    }

}
