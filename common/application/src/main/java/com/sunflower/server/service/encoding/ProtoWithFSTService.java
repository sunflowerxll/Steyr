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
package com.sunflower.server.service.encoding;

import lombok.extern.slf4j.Slf4j;
import org.nustaq.serialization.FSTConfiguration;
import org.springframework.stereotype.Service;
import com.sunflower.server.common.msg.TbActorMsg;

import java.util.Optional;

@Slf4j
@Service
public class ProtoWithFSTService implements DataDecodingEncodingService {

    private final FSTConfiguration config = FSTConfiguration.createDefaultConfiguration();

    @Override
    public Optional<TbActorMsg> decode(byte[] byteArray) {
        try {
            TbActorMsg msg = (TbActorMsg) config.asObject(byteArray);
            return Optional.of(msg);

        } catch (IllegalArgumentException e) {
            log.error("Error during deserialization message, [{}]", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public byte[] encode(TbActorMsg msq) {
        return config.asByteArray(msq);
    }

}
