/**
 * Copyright © 2016-2022 The Thingsboard Authors
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
package org.thingsboard.server.service.edge.rpc.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.EdgeUtils;
import org.thingsboard.server.common.data.edge.EdgeEvent;
import org.thingsboard.server.common.data.edge.EdgeEventActionType;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.gen.edge.v1.CustomerUpdateMsg;
import org.thingsboard.server.gen.edge.v1.DownlinkMsg;
import org.thingsboard.server.gen.edge.v1.UpdateMsgType;
import org.thingsboard.server.queue.util.TbCoreComponent;

@Component
@Slf4j
@TbCoreComponent
public class CustomerEdgeProcessor extends BaseEdgeProcessor {

    public DownlinkMsg processCustomerToEdge(EdgeEvent edgeEvent, UpdateMsgType msgType, EdgeEventActionType action) {
        CustomerId customerId = new CustomerId(edgeEvent.getEntityId());
        DownlinkMsg downlinkMsg = null;
        switch (action) {
            case ADDED:
            case UPDATED:
                Customer customer = customerService.findCustomerById(edgeEvent.getTenantId(), customerId);
                if (customer != null) {
                    CustomerUpdateMsg customerUpdateMsg =
                            customerMsgConstructor.constructCustomerUpdatedMsg(msgType, customer);
                    downlinkMsg = DownlinkMsg.newBuilder()
                            .setDownlinkMsgId(EdgeUtils.nextPositiveInt())
                            .addCustomerUpdateMsg(customerUpdateMsg)
                            .build();
                }
                break;
            case DELETED:
                CustomerUpdateMsg customerUpdateMsg =
                        customerMsgConstructor.constructCustomerDeleteMsg(customerId);
                downlinkMsg = DownlinkMsg.newBuilder()
                        .setDownlinkMsgId(EdgeUtils.nextPositiveInt())
                        .addCustomerUpdateMsg(customerUpdateMsg)
                        .build();
                break;
        }
        return downlinkMsg;
    }

}
