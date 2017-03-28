/*
 * This file is part of the JDrupes non-blocking HTTP Codec
 * Copyright (C) 2016  Michael N. Lipp
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package org.jdrupes.httpcodec;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Optional;

import org.jdrupes.httpcodec.Codec.ProtocolSwitchResult;

/**
 * An engine that can be used as a server. It has an associated
 * request decoder and response encoder.
 * 
 * @param <Q> the message header type handled by the decoder (the request)
 * @param <R> the message header type handled be the encoder (the response)
 */
public class ServerEngine<Q extends MessageHeader, R extends MessageHeader>
	extends Engine {

	private Decoder<?, ?> requestDecoder;
	private Encoder<?> responseEncoder;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param requestDecoder the decoder for the request
	 * @param responseEncoder the encoder for the response
	 */
	public ServerEngine(
			Decoder<Q, R> requestDecoder, Encoder<R> responseEncoder) {
		this.requestDecoder = requestDecoder;
		this.responseEncoder = responseEncoder;
	}

	/**
	 * Returns the request decoder.
	 * 
	 * @return the request decoder
	 */
	@SuppressWarnings("unchecked")
	public Decoder<Q, R> requestDecoder() {
		return (Decoder<Q, R>)requestDecoder;
	}

	/**
	 * Returns the response encoder.
	 * 
	 * @return the response encoder
	 */
	@SuppressWarnings("unchecked")
	public Encoder<R> responseEncoder() {
		return (Encoder<R>)responseEncoder;
	}

	/**
	 * Decodes a request sent to the server.
	 * 
	 * @param in the data to decode
	 * @param out the decoded data
	 * @param endOfInput {@code true} if this invocation finishes the message
	 * @return the result
	 * @throws ProtocolException if the input violates the protocol
	 */
	@SuppressWarnings("unchecked")
	public Decoder.Result<R> decode(
		ByteBuffer in, Buffer out, boolean endOfInput)
			throws ProtocolException {
		return (Decoder.Result<R>)requestDecoder.decode(in, out, endOfInput);
	}

	/**
	 * Encodes a response generated by the server. This method must be used
	 * instead of the encoder's method if the encoder and decoder should adapt
	 * to a protocol switch automatically.
	 * 
	 * @param messageHeader
	 *            the message header
	 * @see Encoder#encode(MessageHeader)
	 */
	@SuppressWarnings("unchecked")
	public void encode(R messageHeader) {
		((Encoder<R>)responseEncoder).encode(messageHeader);
	}

	/**
	 * Invokes the encoder's encode method. This method must be used instead of
	 * encoder's method if the encoder and decoder should adapt to a protocol
	 * switch automatically.
	 * 
	 * @param out
	 *            the decoded data
	 * @return the result
	 * @see Encoder#encode(ByteBuffer)
	 */
	public Encoder.Result encode(
	        ByteBuffer out) {
		return encode(Codec.EMPTY_IN, out, true);
	}

	/**
	 * Invokes the encoder's encode method. This method must be used
	 * instead of decoding the encoder's method directly to allow derived
	 * server classes to adapt to any information contained in the message.
	 * 
	 * @param in the data to encode
	 * @param out the encoded data
	 * @param endOfInput {@code true} if this invocation finishes the message
	 * @return the result
	 * @see Encoder#encode(Buffer, ByteBuffer, boolean)
	 */
	public Encoder.Result encode(
	        Buffer in, ByteBuffer out, boolean endOfInput) {
		Encoder.Result result = responseEncoder.encode(in, out, endOfInput);
		if (result instanceof ProtocolSwitchResult) {
			ProtocolSwitchResult res = (ProtocolSwitchResult)result;
			if (res.newProtocol() != null) {
				requestDecoder = res.newDecoder();
				responseEncoder = res.newEncoder();
			}
		}
		return result;
	}

	/**
	 * Returns the last fully decoded request if it exists.
	 * 
	 * @return the request
	 */
	@SuppressWarnings("unchecked")
	public Optional<Q> currentRequest() {
		return (Optional<Q>)requestDecoder.header();
	}
	
}
