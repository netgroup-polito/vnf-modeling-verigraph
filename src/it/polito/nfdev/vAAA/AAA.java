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
		this.userTable.setTypes(Table.TableTypes.BodyData);
		
	}

	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {

		Packet p = null;
		try {
			p = packet.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		if(packet.equalsField(PacketField.PORT_DST, AUTHENTICATION_PORT_1812))
		{ 
			TableEntry entry = userTable.matchEntry(packet.getField(PacketField.BODY));
			
			if(entry==null){
				p.setField(PacketField.BODY, ACCESS_REJECT);				
				
				p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
				p.setField(PacketField.PORT_SRC, packet.getField(PacketField.PORT_DST));
				p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
				p.setField(PacketField.PORT_DST, packet.getField(PacketField.PORT_SRC));
			
				return new RoutingResult(Action.FORWARD,p,iface);
			}else{
				p.setField(PacketField.BODY, AUTHORIZATION_RESPONSE);
				
				p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
				p.setField(PacketField.PORT_SRC, packet.getField(PacketField.PORT_DST));
				p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
				p.setField(PacketField.PORT_DST, packet.getField(PacketField.PORT_SRC));
			
				return new RoutingResult(Action.FORWARD,p,iface);
			}						
		}
		if(packet.equalsField(PacketField.PORT_DST,ACCOUNTING_PORT_1813 ))
		{
			TableEntry entry = userTable.matchEntry(packet.getField(PacketField.BODY));
			
			if(entry!=null){
				p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
				p.setField(PacketField.PORT_SRC, packet.getField(PacketField.PORT_DST));
				p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
				p.setField(PacketField.PORT_DST, packet.getField(PacketField.PORT_SRC));
				p.setField(PacketField.BODY, ACCOUNTING_RESPONSE);
				return new RoutingResult(Action.FORWARD,p,iface);			
			}
					
		}
			return new RoutingResult(Action.DROP,null,null);
	
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
