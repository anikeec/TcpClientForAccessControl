package com.apu.TcpClientForAccessControl;

import com.apu.TcpClientForAccessControl.converter.JsonSerializer;
import com.apu.TcpServerForAccessControlAPI.packet.AccessPacket;
import com.apu.TcpServerForAccessControlAPI.packet.EventType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.apu.TcpServerForAccessControlAPI.packet.InfoPacket;
import com.apu.TcpServerForAccessControlAPI.packet.RawPacket;
import com.apu.TcpServerForAccessControlAPI.packet.ServicePacket;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;


/**
 * Hello world!
 *
 */
public class App 
{
    public static boolean TEST_MODE = false;
    
    private static final Logger logger = LogManager.getLogger(App.class);
    
    private static Socket socket; 
    private static final int CONNECTION_PORT = 65530;
    private static final String CONNECTION_HOST = "127.0.0.1";
    private static final int SOCKET_RECEIVE_TIMEOUT = 100;
    
    public static void main( String[] args )
    {
        int deviceNumber;
        if(TEST_MODE == false) {
//        System.setOut(new PrintStream(new LoggingOutputStream(LogManager.getLogger("outLog"), Level.ALL), true));
        
            if(args.length == 0) {
                System.out.print("Enter device number");
                return;
            }


            try {
                deviceNumber = Integer.parseInt(args[0]);
            } catch(NumberFormatException ex) {
                System.out.print("Error device number");
                return;
            }        
        } else {
            deviceNumber = 15;
        }
        
//        deviceNumber = 15;
        
        
        OutputStream os = null;
        InputStream is = null;
        
        JsonSerializer serializer = new JsonSerializer();
        
        try {
            socket = new Socket(CONNECTION_HOST, CONNECTION_PORT);
//            socket.setSoTimeout(SOCKET_RECEIVE_TIMEOUT);
            os = socket.getOutputStream();
            is = socket.getInputStream();
        
            AccessPacket packet = new AccessPacket();
            packet.setEventId(EventType.ENTER_QUERY.getIndex());
            packet.setDeviceNumber(deviceNumber); 
            packet.setCardNumber("11111111");            
            
            byte[] packetBytes;
            byte[] packetBytesForSend;            
            int packetNumber = 25;
            RawPacket resultPacket;
            long timeStart;
            long timeFinish;
            while(!socket.isClosed()) {
                
                packet.setPacketNumber(packetNumber++);
                packet.setTime(new Date());
                
                if(TEST_MODE == false) {
                    packetBytes = serializer.serializeBytes(packet);                
                } else {
                    packetBytes = new byte[]{1,2,3,4,5};
                }
                timeStart = System.nanoTime();
                packetBytesForSend = new byte[packetBytes.length + 2];
                int i = 0;
                for(i=0; i<packetBytes.length; i++) {
                    packetBytesForSend[i] = packetBytes[i];
                }
                packetBytesForSend[i++] = '\r';
                packetBytesForSend[i++] = '\n';
                os.write(packetBytesForSend);
//                os.write("\r\n".getBytes());
                os.flush();

                List<Byte> bytes = new ArrayList<>();
                byte receiveByte = 0;
                int intRead = 0;
                while(intRead != -1) {
                    intRead = is.read();
                    receiveByte = (byte)intRead;                    
                    if(intRead != -1) {
                        bytes.add(receiveByte);
                        if((bytes.size() > 2) &&
                           (bytes.get(bytes.size() - 2) == '\r') &&
                           (bytes.get(bytes.size() - 1) == '\n')) {
//                            System.out.println("break");
                            break;
                        }
                    }
                }
                timeFinish = System.nanoTime();
                bytes = bytes.subList(0, bytes.size() - 2);
                packetBytes = new byte[bytes.size()];
                for(i=0; i<bytes.size(); i++) {
                    packetBytes[i] = bytes.get(i);
                }

                try {
                    
                    if(TEST_MODE == false) {
                        resultPacket = serializer.deserializeBytes(packetBytes);
                    } else {
                        resultPacket = new RawPacket();
                    }
                    
//                    System.out.println("TimeStart: " + timeStart + " ms.");
//                    System.out.print("Received bytes: " + packetBytes.length);                    
//                    System.out.println("TimeFinish: " + timeFinish + " ms.");
                    System.out.println(resultPacket + "; time: " + ((timeFinish - timeStart)/1000) + " us.");
                } catch (Exception ex) {
                    logger.error(ExceptionUtils.getStackTrace(ex));
                    for(byte b:bytes) {
                        System.out.print(b);
                    }
                    System.out.println();
                } finally {
                }
//                System.out.println();
                try {
                    Thread.sleep(500);
                } catch(InterruptedException ex) {
                    break;
                }
            }
        } catch (UnknownHostException ex) {
            logger.error(ExceptionUtils.getStackTrace(ex));
        } catch (IOException ex) {
            logger.error(ExceptionUtils.getStackTrace(ex));
        } finally {
            if(is != null)
                try {
                    is.close();
            } catch (IOException ex) {
                logger.error(ExceptionUtils.getStackTrace(ex));
            }
            if(os!= null)
                try {
                    os.close();
            } catch (IOException ex) {
                logger.error(ExceptionUtils.getStackTrace(ex));
            }
        }
        
    }
    
    private static byte[] serializePacket(RawPacket srcPacket) {   
        byte[] resultBytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);   
            out.writeObject(srcPacket);
            out.flush();
            resultBytes = bos.toByteArray();
        } catch (IOException ex) {
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
            }
        }        
        return resultBytes;
    }
    
    private static RawPacket deserializePacket(byte[] srcPacketStr) throws ClassNotFoundException, IOException {
        RawPacket resultPacket = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(srcPacketStr);
        ObjectInput in = null;
        try {
          in = new ObjectInputStream(bis);
          Object o = in.readObject(); 
          if(o instanceof RawPacket) {
              if(o instanceof ServicePacket) {
                  resultPacket = (ServicePacket)o;
              } else if(o instanceof InfoPacket) {
                  resultPacket = (InfoPacket)o;
              } else {
                  resultPacket = (RawPacket)o;
              }
          }
        } finally {
                if (in != null) {
                  in.close();
                }
                bis.close();
        }
        return resultPacket;
    }
    
}
