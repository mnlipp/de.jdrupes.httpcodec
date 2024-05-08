/*
 * This file is part of the JDrupes non-blocking HTTP Codec
 * Copyright (C) 2024  Michael N. Lipp
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

package org.jdrupes.httpcodec.test.http;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jdrupes.httpcodec.protocols.http.HttpConstants.HttpProtocol;
import org.jdrupes.httpcodec.protocols.http.HttpRequest;

import static org.junit.Assert.*;
import org.junit.Test;

public class FormUrlencoderTests {

    @Test
    public void testNormal() throws UnsupportedEncodingException {
        Map<String, List<String>> data = new TreeMap<>(Map.of(
            "first", List.of("value1.1", "value1.2"),
            "second", List.of("value2"),
            "third", List.of("välue3")));
        var enc = HttpRequest.wwwFormUrlencode(data);
        assertEquals("first=value1.1&first=value1.2"
            + "&second=value2&third=v%C3%A4lue3", enc);
    }

    @Test
    public void testSimple() throws UnsupportedEncodingException {
        Map<String, String> data = new TreeMap<>(Map.of(
            "first", "&value 1",
            "second", "value2",
            "third", "välue3"));
        var enc = HttpRequest.simpleWwwFormUrlencode(data);
        assertEquals("first=%26value+1"
            + "&second=value2&third=v%C3%A4lue3", enc);
    }

    @Test
    public void testRequestNormal()
            throws UnsupportedEncodingException, URISyntaxException {
        HttpRequest req = new HttpRequest("GET",
            new URI("http://test.com/path?k=v#there"), HttpProtocol.HTTP_1_1,
            false);
        Map<String, List<String>> data = new TreeMap<>(Map.of(
            "first", List.of("value1.1", "value1.2"),
            "second", List.of("value2"),
            "third", List.of("välue3")));
        req.setQueryData(data);
        assertEquals("http://test.com/path?first=value1.1&first=value1.2"
            + "&second=value2&third=v%C3%A4lue3#there",
            req.requestUri().toString());
    }

    @Test
    public void testRequestSimple()
            throws UnsupportedEncodingException, URISyntaxException {
        HttpRequest req = new HttpRequest("GET",
            new URI("http://test.com/path?k=v#there"), HttpProtocol.HTTP_1_1,
            false);
        Map<String, String> data = new TreeMap<>(Map.of(
            "first", "&value 1",
            "second", "value2",
            "third", "välue3"));
        req.setSimpleQueryData(data);
        assertEquals("http://test.com/path"
            + "?first=%26value+1&second=value2&third=v%C3%A4lue3#there",
            req.requestUri().toString());
    }

}
