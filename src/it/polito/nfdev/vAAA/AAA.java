package it.polito.nfdev.vAAA;

import java.util.ArrayList;

import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.RoutingResult;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.nat.NatTableEntry;
import it.polito.nfdev.lib.Table;
import it.polito.nfdev.lib.TableEntry;

public class AAA extends NetworkFunction {

	public static final String ACCOUNTING_RESPONSE = "Accounting_Response";
	public static final String ACCESS_REJECT = "Access_Reject";
	public static final String AUTHORIZATION_RESPONSE = "Authorization_Response";  // including the network configurations
	
	public static final String AUTHENTICATION_PORT_1812 = "1812";
	public static final String ACCOUNTING_PORT_1813 = "1813";
	
	private Table userTable;
	
	public AAA() {
		super(new ArrayList<Interface>());
		
		this.userTable = new Table(1,0);   // (authentication data including userName and passWord)
		this.userTable.setTypes(Table.TableTypes.ApplicationData);
		
	}

	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {

		Packet p = null;
		try {
			p = packet.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		if(packet.equalsField(PacketField.PORT_DST, AUTHENTICATION_PORT_1812)){ 
			 	// if it's a Authentication Packet from NAS Client, CZ authentication port is 1812
		/*	String Uname_Pwd = packet.getField(PacketField.L7DATA);
			String[] parts = Uname_Pwd.split(":");   // assume L7DATA is 'Username:Password'
			String userName = parts[0];
			String passWord = parts[1];
		*/	
			TableEntry entry = userTable.matchEntry(packet.getField(PacketField.L7DATA));
			
			if(entry==null){
				p.setField(PacketField.L7DATA, ACCESS_REJECT);				
				
				p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
				p.setField(PacketField.PORT_SRC, packet.getField(PacketField.PORT_DST));
				p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
				p.setField(PacketField.PORT_DST, packet.getField(PacketField.PORT_SRC));
			
				return new RoutingResult(Action.FORWARD,p,iface);
			}else{
				p.setField(PacketField.L7DATA, AUTHORIZATION_RESPONSE);
				
				p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
				p.setField(PacketField.PORT_SRC, packet.getField(PacketField.PORT_DST));
				p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
				p.setField(PacketField.PORT_DST, packet.getField(PacketField.PORT_SRC));
			
				return new RoutingResult(Action.FORWARD,p,iface);
			}
			
			
		}
		else if(packet.equalsField(PacketField.PORT_DST,ACCOUNTING_PORT_1813 )){
					// if it's a Accounting Packet from NAS Client , CZ accounting port is 1813
			
		//	String Uname_Pwd = packet.getField(PacketField.L7DATA);
			
			// assume L7DATA is 'Username:	Periodic number of Bytes'
		/*	String[] parts = Uname_Pwd.split(":");   
			String userName = parts[0];*/
		//	Integer byteCount = Integer.parseInt(parts[1]);
			
			TableEntry entry = userTable.matchEntry(packet.getField(PacketField.L7DATA));
			
			if(entry==null){
				return new RoutingResult(Action.DROP, null, null);
			}
			else{		// Update the total number of Bytes
			/*	int totalBytes = byteCount + Integer.parseInt((String)entry.getValue(2));
				TableEntry e = new NatTableEntry(3);
				e.setValue(0, (String)entry.getValue(0));  // Username
				e.setValue(1, (String)entry.getValue(1));  // Password
				e.setValue(2, (new Integer(totalBytes)).toString());		   // ByteCount
				userTable.removeEntry(entry);
				userTable.storeEntry(e);
			*/	
				p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
				p.setField(PacketField.PORT_SRC, packet.getField(PacketField.PORT_DST));
				p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
				p.setField(PacketField.PORT_DST, packet.getField(PacketField.PORT_SRC));
				p.setField(PacketField.L7DATA, ACCOUNTING_RESPONSE);
				return new RoutingResult(Action.FORWARD,p,iface);
			}
		}
		else{
			return new RoutingResult(Action.DROP,null,null);
		}
		
	}
	
	
	public boolean addUserInfo(String authenticationData){
		
		TableEntry e = userTable.matchEntry(authenticationData);
		
		if(e!=null)    
			return false;
		
		TableEntry entry = new TableEntry(3);
		entry.setValue(0, authenticationData);
			
		return userTable.storeEntry(entry);
	}
	
	public boolean removeUserInfo(String authenticationData){
		TableEntry entry  = userTable.matchEntry(authenticationData);
		
		return userTable.removeEntry(entry);  // if return false, --> entry is empty
	}
	
	
	public void clearUserTable() {
		
		this.userTable.clear();
	}

}
