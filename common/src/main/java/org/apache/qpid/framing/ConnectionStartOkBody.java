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

/*
 * This file is auto-generated by Qpid Gentools v.0.1 - do not modify.
 * Supported AMQP version:
 *   8-0
 */

package org.apache.qpid.framing;

import java.io.IOException;

import org.apache.qpid.QpidException;
import org.apache.qpid.bytebuffer.QpidByteBuffer;
import org.apache.qpid.codec.MarkableDataInput;

public class ConnectionStartOkBody extends AMQMethodBodyImpl implements EncodableAMQDataBlock, AMQMethodBody
{

    public static final int CLASS_ID =  10;
    public static final int METHOD_ID = 11;

    // Fields declared in specification
    private final FieldTable _clientProperties; // [clientProperties]
    private final AMQShortString _mechanism; // [mechanism]
    private final byte[] _response; // [response]
    private final AMQShortString _locale; // [locale]

    public ConnectionStartOkBody(
            FieldTable clientProperties,
            AMQShortString mechanism,
            byte[] response,
            AMQShortString locale
                                )
    {
        _clientProperties = clientProperties;
        _mechanism = mechanism;
        _response = response;
        _locale = locale;
    }

    public int getClazz()
    {
        return CLASS_ID;
    }

    public int getMethod()
    {
        return METHOD_ID;
    }

    public final FieldTable getClientProperties()
    {
        return _clientProperties;
    }
    public final AMQShortString getMechanism()
    {
        return _mechanism;
    }
    public final byte[] getResponse()
    {
        return _response;
    }
    public final AMQShortString getLocale()
    {
        return _locale;
    }

    protected int getBodySize()
    {
        int size = 0;
        size += getSizeOf( _clientProperties );
        size += getSizeOf( _mechanism );
        size += getSizeOf( _response );
        size += getSizeOf( _locale );
        return size;
    }

    public void writeMethodPayload(QpidByteBuffer buffer)
    {
        writeFieldTable( buffer, _clientProperties );
        writeAMQShortString( buffer, _mechanism );
        writeBytes( buffer, _response );
        writeAMQShortString( buffer, _locale );
    }

    public boolean execute(MethodDispatcher dispatcher, int channelId) throws QpidException
	{
        return dispatcher.dispatchConnectionStartOk(this, channelId);
	}

    public String toString()
    {
        StringBuilder buf = new StringBuilder("[ConnectionStartOkBodyImpl: ");
        buf.append( "clientProperties=" );
        buf.append(  getClientProperties() );
        buf.append( ", " );
        buf.append( "mechanism=" );
        buf.append(  getMechanism() );
        buf.append( ", " );
        buf.append( "response=" );
        buf.append(  getResponse() == null  ? "null" : java.util.Arrays.toString( getResponse() ) );
        buf.append( ", " );
        buf.append( "locale=" );
        buf.append(  getLocale() );
        buf.append("]");
        return buf.toString();
    }

    public static void process(final MarkableDataInput in, final ServerMethodProcessor dispatcher)
            throws IOException, AMQFrameDecodingException
    {

        FieldTable clientProperties = EncodingUtils.readFieldTable(in);
        AMQShortString mechanism = in.readAMQShortString();
        byte[] response = EncodingUtils.readBytes(in);
        AMQShortString locale = in.readAMQShortString();
        if(!dispatcher.ignoreAllButCloseOk())
        {
            dispatcher.receiveConnectionStartOk(clientProperties, mechanism, response, locale);
        }
        if (clientProperties != null)
        {
            clientProperties.clearEncodedForm();
        }
    }
}
