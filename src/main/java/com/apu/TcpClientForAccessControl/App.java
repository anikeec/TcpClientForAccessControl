package com.apu.TcpClientForAccessControl;

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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Hello world!
 *
 */
public class App 
{
    private static Socket socket; 
    private static final int CONNECTION_PORT = 65530;
    private static final String CONNECTION_HOST = "127.0.0.1";
    private static final int SOCKET_RECEIVE_TIMEOUT = 100;
    
    public static void main( String[] args )
    {
        OutputStream os = null;
        InputStream is = null;
        
        try {
            socket = new Socket(CONNECTION_HOST, CONNECTION_PORT);
//            socket.setSoTimeout(SOCKET_RECEIVE_TIMEOUT);
            os = socket.getOutputStream();
            is = socket.getInputStream();
        
            RawPacket packet = new InfoPacket();
            packet.setDeviceId(15);            
            
            byte[] packetBytes;            
            int packetNumber = 0;
            
            while(!socket.isClosed()) {
                
                packet.setPacketNumber(packetNumber++);
                
                packetBytes = serializePacket(packet);                
                
                os.write(packetBytes);
                os.write("\r\n".getBytes());
                os.flush();

                List<Byte> bytes = new ArrayList<>();
                byte receiveByte = 0;
                while(receiveByte != -1) {
                    receiveByte = (byte)is.read();                    
                    if(receiveByte != -1) {
                        bytes.add(receiveByte);
                        if((bytes.size() > 2) &&
                           (bytes.get(bytes.size() - 2) == '\r') &&
                           (bytes.get(bytes.size() - 1) == '\n')) {
                            break;
                        }
                    }
                }
                
                bytes = bytes.subList(0, bytes.size() - 2);
                packetBytes = new byte[bytes.size()];
                for(int i=0; i<bytes.size(); i++) {
                    packetBytes[i] = bytes.get(i);
                }

                try {
                    RawPacket resultPacket = deserializePacket(packetBytes);
                    System.out.println(resultPacket);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                try {
                    Thread.sleep(500);
                } catch(InterruptedException ex) {
                    break;
                }
            }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if(is != null)
                try {
                    is.close();
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(os!= null)
                try {
                    os.close();
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
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
        }
        return resultPacket;
    }
    
}
