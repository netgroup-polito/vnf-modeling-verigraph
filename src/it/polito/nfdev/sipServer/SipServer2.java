package it.polito.nfdev.sipServer;

import java.util.List;


import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.lib.Table;
import it.polito.nfdev.lib.TableEntry;
import it.polito.nfdev.verification.Verifier;
import it.polito.nfdev.lib.RoutingResult;

public class SipServer2 extends NetworkFunction {
	
	public static final String  SIP_INVITE = "Sip_Invitation";
	public static final String  SIP_OK = "Sip_OK";
	public static final String  SIP_REGISTER = "Sip_Register";
	public static final String  SIP_ENDING = "Sip_Ending";
	
	private Table sipTable;
	private String ip_sipServer;

	public SipServer2(List<Interface> interfaces, String ip_sipServer) {
		super(interfaces);
		this.ip_sipServer = ip_sipServer;
		sipTable = new Table(2,0);
		sipTable.setTypes(Table.TableTypes.ApplicationData, Table.TableTypes.Ip);
		sipTable.setDataDriven();
	}

	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {
		  
	
		Packet p = null;
		try {
			p = packet.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		if(packet.equalsField(PacketField.APPLICATION_PROTOCOL, SIP_REGISTER) && packet.equalsField(PacketField.IP_DST, ip_sipServer))
		{
			
			TableEntry entry = new TableEntry(2);
			entry.setValue(0, packet.getField(PacketField.BODY));
			entry.setValue(1, packet.getField(PacketField.IP_SRC));
			sipTable.storeEntry(entry);
			
			p.setField(PacketField.IP_SRC,packet.getField(PacketField.IP_DST));
			p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
			p.setField(PacketField.PORT_SRC,packet.getField(PacketField.PORT_DST));
			p.setField(PacketField.PORT_DST, packet.getField(PacketField.PORT_SRC));
			p.setField(PacketField.APPLICATION_PROTOCOL, SIP_OK);
			
			return new RoutingResult(Action.FORWARD,p,iface);
		
			
		}
		
		if(packet.equalsField(PacketField.APPLICATION_PROTOCOL, SIP_INVITE) && packet.equalsField(PacketField.IP_DST, ip_sipServer))
		{
			
			TableEntry entry = sipTable.matchEntry(packet.getField(PacketField.BODY), Verifier.ANY_VALUE);
			if(entry != null)
			{
						
			p.setField(PacketField.IP_DST, (String)entry.getValue(1));  // second place is the Callee IP						
			return new RoutingResult(Action.FORWARD,p,iface);
		
			}
		}
		if((packet.equalsField(PacketField.APPLICATION_PROTOCOL, SIP_INVITE) && !packet.equalsField(PacketField.IP_DST, ip_sipServer))
				|| packet.equalsField(PacketField.APPLICATION_PROTOCOL, SIP_OK) || packet.equalsField(PacketField.APPLICATION_PROTOCOL, SIP_ENDING))
		{
			return new RoutingResult(Action.FORWARD,p,iface);
		}
		
		return new RoutingResult(Action.DROP,null,null);

	}
}
