/*-
 * #%L
 * Amazon Athena Query Federation SDK
 * %%
 * Copyright (C) 2019 - 2020 Amazon Web Services
 * %%
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
 * #L%
 */
package com.amazonaws.athena.connector.lambda.serde.v4;

import com.amazonaws.athena.connector.lambda.domain.predicate.expression.FederationExpression;
import com.amazonaws.athena.connector.lambda.domain.predicate.expression.VariableExpression;
import com.amazonaws.athena.connector.lambda.serde.TypedDeserializer;
import com.amazonaws.athena.connector.lambda.serde.TypedSerializer;
import com.amazonaws.athena.connector.lambda.serde.VersionedSerDe;
import com.amazonaws.athena.connector.lambda.serde.v2.ArrowTypeSerDe;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.arrow.vector.types.pojo.ArrowType;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

public final class VariableExpressionSerDeV4
{
    private static final String COLUMN_NAME_FIELD = "columnName";
    private static final String TYPE_FIELD = "type";

    private VariableExpressionSerDeV4() {}

    public static final class Serializer extends TypedSerializer<FederationExpression> implements VersionedSerDe.Serializer<FederationExpression>
    {
        private final ArrowTypeSerDe.Serializer arrowTypeSerializer;

        public Serializer(ArrowTypeSerDe.Serializer arrowTypeSerializer)
        {
            super(FederationExpression.class, VariableExpression.class);
            this.arrowTypeSerializer = requireNonNull(arrowTypeSerializer, "arrowTypeSerializer is null");
        }

        @Override
        protected void doTypedSerialize(FederationExpression federationExpression, JsonGenerator jgen, SerializerProvider provider)
                throws IOException
        {
            VariableExpression variableExpression = (VariableExpression) federationExpression;

            jgen.writeStringField(COLUMN_NAME_FIELD, variableExpression.getColumnName());

            jgen.writeFieldName(TYPE_FIELD);
            arrowTypeSerializer.serialize(variableExpression.getType(), jgen, provider);
        }
    }

    public static final class Deserializer extends TypedDeserializer<FederationExpression> implements VersionedSerDe.Deserializer<FederationExpression>
    {
        private final ArrowTypeSerDe.Deserializer arrowTypeDeserializer;

        public Deserializer(ArrowTypeSerDe.Deserializer arrowTypeDeserializer)
        {
            super(FederationExpression.class, VariableExpression.class);
            this.arrowTypeDeserializer = requireNonNull(arrowTypeDeserializer, "arrowTypeDeserializer is null");
        }

        @Override
        protected FederationExpression doTypedDeserialize(JsonParser jparser, DeserializationContext ctxt)
                throws IOException
        {
           String columnName = getNextStringField(jparser, COLUMN_NAME_FIELD);

            assertFieldName(jparser, TYPE_FIELD);
            ArrowType type = arrowTypeDeserializer.deserialize(jparser, ctxt);

            return new VariableExpression(columnName, type);
        }
    }
}
