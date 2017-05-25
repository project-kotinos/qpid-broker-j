/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.server.protocol.converter.v0_8_v1_0;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.qpid.server.bytebuffer.QpidByteBuffer;
import org.apache.qpid.server.message.mimecontentconverter.ObjectToMimeContentConverter;
import org.apache.qpid.server.message.mimecontentconverter.MimeContentConverterRegistry;
import org.apache.qpid.server.protocol.v0_8.AMQShortString;
import org.apache.qpid.server.protocol.v0_8.transport.BasicContentHeaderProperties;
import org.apache.qpid.server.protocol.v0_8.transport.ContentHeaderBody;
import org.apache.qpid.server.protocol.v0_8.FieldTable;
import org.apache.qpid.server.protocol.v0_8.transport.MessagePublishInfo;
import org.apache.qpid.server.model.NamedAddressSpace;
import org.apache.qpid.server.plugin.MessageConverter;
import org.apache.qpid.server.plugin.PluggableService;
import org.apache.qpid.server.protocol.v0_8.AMQMessage;
import org.apache.qpid.server.protocol.v0_8.MessageMetaData;
import org.apache.qpid.server.protocol.v1_0.MessageConverter_from_1_0;
import org.apache.qpid.server.protocol.v1_0.MessageMetaData_1_0;
import org.apache.qpid.server.protocol.v1_0.Message_1_0;
import org.apache.qpid.server.store.StoredMessage;

@PluggableService
public class MessageConverter_1_0_to_v0_8 implements MessageConverter<Message_1_0, AMQMessage>
{
    private static final int BASIC_CLASS_ID = 60;


    public Class<Message_1_0> getInputClass()
    {
        return Message_1_0.class;
    }

    @Override
    public Class<AMQMessage> getOutputClass()
    {
        return AMQMessage.class;
    }

    @Override
    public AMQMessage convert(Message_1_0 serverMsg, NamedAddressSpace addressSpace)
    {
        return new AMQMessage(convertToStoredMessage(serverMsg), null);
    }

    @Override
    public void dispose(final AMQMessage message)
    {

    }

    private StoredMessage<MessageMetaData> convertToStoredMessage(final Message_1_0 serverMsg)
    {
        Object bodyObject = MessageConverter_from_1_0.convertBodyToObject(serverMsg);

        final ObjectToMimeContentConverter converter = MimeContentConverterRegistry.getBestFitObjectToMimeContentConverter(bodyObject);

        final byte[] messageContent = converter == null ? new byte[] {} : converter.toMimeContent(bodyObject);
        final String mimeType = converter == null ? null  : converter.getMimeType();
        final MessageMetaData messageMetaData_0_8 = convertMetaData(serverMsg,
                                                                    mimeType,
                                                                    messageContent.length);
        final int metadataSize = messageMetaData_0_8.getStorableSize();

        return new StoredMessage<MessageMetaData>()
        {
            @Override
            public MessageMetaData getMetaData()
            {
                return messageMetaData_0_8;
            }

            @Override
            public long getMessageNumber()
            {
                return serverMsg.getMessageNumber();
            }

            @Override
            public Collection<QpidByteBuffer> getContent(final int offset, final int length)
            {
                return Collections.singleton(QpidByteBuffer.wrap(messageContent, offset, length));
            }

            @Override
            public int getContentSize()
            {
                return messageContent.length;
            }

            @Override
            public int getMetadataSize()
            {
                return metadataSize;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isInMemory()
            {
                return true;
            }

            @Override
            public boolean flowToDisk()
            {
                return false;
            }

            @Override
            public void reallocate()
            {

            }
        };
    }

    private MessageMetaData convertMetaData(final Message_1_0 serverMsg, final String bodyMimeType, final int size)
    {

        final MessageMetaData_1_0.MessageHeader_1_0 header = serverMsg.getMessageHeader();
        String key = header.getTo();
        if(key == null)
        {
            key = header.getSubject();
        }

        MessagePublishInfo publishInfo = new MessagePublishInfo(null, false, false, AMQShortString.valueOf(key));


        final BasicContentHeaderProperties props = new BasicContentHeaderProperties();
        props.setAppId(serverMsg.getMessageHeader().getAppId());
        props.setContentType(bodyMimeType);
        props.setCorrelationId(serverMsg.getMessageHeader().getCorrelationId());
        props.setDeliveryMode(serverMsg.isPersistent() ? BasicContentHeaderProperties.PERSISTENT : BasicContentHeaderProperties.NON_PERSISTENT);
        props.setExpiration(serverMsg.getExpiration());
        props.setMessageId(serverMsg.getMessageHeader().getMessageId());
        props.setPriority(serverMsg.getMessageHeader().getPriority());
        props.setReplyTo(serverMsg.getMessageHeader().getReplyTo());
        props.setTimestamp(serverMsg.getMessageHeader().getTimestamp());
        props.setUserId(serverMsg.getMessageHeader().getUserId());

        Map<String,Object> headerProps = new LinkedHashMap<String, Object>();

        if(header.getSubject() != null)
        {
            headerProps.put("qpid.subject", header.getSubject());
        }

        for(String headerName : serverMsg.getMessageHeader().getHeaderNames())
        {
            headerProps.put(headerName, MessageConverter_from_1_0.convertValue(serverMsg.getMessageHeader().getHeader(headerName)));
        }

        props.setHeaders(FieldTable.convertToFieldTable(headerProps));

        final ContentHeaderBody chb = new ContentHeaderBody(props);
        chb.setBodySize(size);

        return new MessageMetaData(publishInfo, chb, serverMsg.getArrivalTime());
    }


    @Override
    public String getType()
    {
        return "v1-0 to v0-8";
    }


}
