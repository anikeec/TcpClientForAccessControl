/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apu.TcpClientForAccessControl.converter;

import com.apu.TcpServerForAccessControlAPI.packet.AccessPacket;
import com.apu.TcpServerForAccessControlAPI.packet.EventType;
import com.apu.TcpServerForAccessControlAPI.packet.MessageType;
import com.apu.TcpServerForAccessControlAPI.packet.RawPacket;
import com.google.gson.JsonSyntaxException;
import java.util.Date;
import junit.framework.TestCase;

/**
 *
 * @author apu
 */
public class JsonSerializerTest extends TestCase {
    
    private final int TEST_DEVICE_NUMBER = 10;
    private final int TEST_PACKET_NUMBER = 5;
    private final String TEST_CARD_NUMBER = "12344231";
    private final int TEST_EVENT_ID = 6;
    private final EventType TEST_EVENT_TYPE = EventType.EXIT_QUERY;
    private final long TEST_TIME = 1538292073916l;
    private final String TEST_TIME_STR = "Sep 30, 2018 10:21:13 AM";
    private final String TEST_JSON_ACCESS =
                            "{\"mt\":\"ACCESS\","
                            + "\"cn\":\"" + TEST_CARD_NUMBER + "\","
                            + "\"et\":\"" + TEST_EVENT_TYPE.toString() + "\","
                            + "\"ei\":" + TEST_EVENT_ID + ","
                            + "\"dn\":" + TEST_DEVICE_NUMBER + ","
                            + "\"pn\":" + TEST_PACKET_NUMBER + ","
                            + "\"t\":\"" + TEST_TIME_STR + "\"}";
    
    public JsonSerializerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of deserialize method, of class JsonDeserializer.
     */
    public void testDeserialize() {
        System.out.println("deserialize"); 
        
        AccessPacket expResult = new AccessPacket();
        expResult.setDeviceNumber(TEST_DEVICE_NUMBER);
        expResult.setPacketNumber(TEST_PACKET_NUMBER);
        expResult.setCardNumber(TEST_CARD_NUMBER);
        expResult.setEventId(TEST_EVENT_ID);
        expResult.setEventType(TEST_EVENT_TYPE);
        expResult.setTime(new Date(TEST_TIME));
        
        String inputJson = TEST_JSON_ACCESS;
        JsonSerializer instance = new JsonSerializer();

        RawPacket result = instance.deserialize(inputJson);
        assertTrue(result instanceof AccessPacket);
        AccessPacket apResult = (AccessPacket)result;
        assertTrue(
            (apResult.getDeviceNumber().equals(expResult.getDeviceNumber())) &&
            (apResult.getPacketNumber().equals(expResult.getPacketNumber())) && 
            (apResult.getCardNumber().equals(expResult.getCardNumber())) && 
            (apResult.getEventId().equals(expResult.getEventId())) && 
            (apResult.getEventType().equals(expResult.getEventType())) && 
            (apResult.getMessageType().equals(expResult.getMessageType())) 
        );

        try {
            instance.deserialize(null);
            fail();
        } catch (NullPointerException e) { } 
        
    }

    /**
     * Test of serialize method, of class JsonDeserializer.
     */
    public void testSerialize() {
        System.out.println("serialize");
        
        AccessPacket inputPkt = new AccessPacket();
        inputPkt.setDeviceNumber(TEST_DEVICE_NUMBER);
        inputPkt.setPacketNumber(TEST_PACKET_NUMBER);
        inputPkt.setCardNumber(TEST_CARD_NUMBER);
        inputPkt.setEventId(TEST_EVENT_ID);
        inputPkt.setEventType(TEST_EVENT_TYPE);
        inputPkt.setTime(new Date(TEST_TIME));
        JsonSerializer instance = new JsonSerializer();
        String expResult = TEST_JSON_ACCESS;
        String result = instance.serialize(inputPkt);
        assertEquals(expResult, result);
        
        expResult = "null";
        result = instance.serialize(null);       
        assertEquals(expResult, result);
    }
    
}
