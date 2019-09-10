/*
 * Copyright (c) 2019 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.client.java.query;

import java.util.ArrayList;
import java.util.List;

import com.couchbase.client.core.annotation.Stability;
import com.couchbase.client.core.error.DecodingFailedException;
import com.couchbase.client.core.msg.query.QueryChunkHeader;
import com.couchbase.client.core.msg.query.QueryChunkRow;
import com.couchbase.client.core.msg.query.QueryChunkTrailer;
import com.couchbase.client.java.codec.JsonSerializer;
import com.couchbase.client.java.codec.Serializer;
import com.couchbase.client.java.json.JsonObject;

/**
 * The result of a N1QL query, including rows and associated metadata.
 *
 * @since 3.0.0
 */
public class QueryResult {

    /**
     * Stores the encoded rows from the query response.
     */
    private final List<QueryChunkRow> rows;

    /**
     * The header holds associated metadata that came back before the rows streamed.
     */
    private final QueryChunkHeader header;

    /**
     * The trailer holds associated metadata that came back after the rows streamed.
     */
    private final QueryChunkTrailer trailer;

    /**
     * Creates a new QueryResult.
     *
     * @param header the query header.
     * @param rows the query rows.
     * @param trailer the query trailer.
     */
    QueryResult(final QueryChunkHeader header, final List<QueryChunkRow> rows, final QueryChunkTrailer trailer) {
        this.rows = rows;
        this.header = header;
        this.trailer = trailer;
    }

    /**
     * Returns all rows, converted into {@link JsonObject}s.
     *
     * @throws DecodingFailedException if any row could not be successfully deserialized.
     */
    public List<JsonObject> rowsAsObject() {
        return rowsAs(JsonObject.class);
    }

    /**
     * Returns all rows, converted into the target class, and using the default serializer.
     *
     * @param target the target class to deserialize into.
     * @throws DecodingFailedException if any row could not be successfully deserialized.
     */
    public <T> List<T> rowsAs(final Class<T> target) {
        return rowsAs(target, JsonSerializer.INSTANCE);
    }

    /**
     * Returns all rows, converted into the target class, and using a custom serializer.
     *
     * @param target the target class to deserialize into.
     * @param serializer the custom serializer to use.
     * @throws DecodingFailedException if any row could not be successfully deserialized.
     */
    public <T> List<T> rowsAs(final Class<T> target, final Serializer serializer) {
        final List<T> converted = new ArrayList<T>(rows.size());
        for (QueryChunkRow row : rows) {
            converted.add(serializer.deserialize(target, row.data()));
        }
        return converted;
    }

    /**
     * Returns the {@link QueryMetaData} giving access to the additional metadata associated with this query.
     */
    public QueryMetaData metaData() {
        return QueryMetaData.from(header, trailer);
    }

    @Override
    public String toString() {
        return "QueryResult{" +
            "rows=" + rows +
            ", header=" + header +
            ", trailer=" + trailer +
            '}';
    }
}