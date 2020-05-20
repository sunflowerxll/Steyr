/**
 * Copyright © 2016-2020 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sunflower.server.dao.model.sqlts.dictionary;

import lombok.Data;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import static com.sunflower.server.dao.model.ModelConstants.KEY_COLUMN;
import static com.sunflower.server.dao.model.ModelConstants.KEY_ID_COLUMN;

@Data
@Entity
@Table(name = "ts_kv_dictionary")
@IdClass(TsKvDictionaryCompositeKey.class)
public final class TsKvDictionary {

    @Id
    @Column(name = KEY_COLUMN)
    private String key;

    @Column(name = KEY_ID_COLUMN, unique = true, columnDefinition="int")
    @Generated(GenerationTime.INSERT)
    private int keyId;

}