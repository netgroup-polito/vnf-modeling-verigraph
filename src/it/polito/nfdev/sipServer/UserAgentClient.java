package it.polito.nfdev.sipServer;

import java.util.List;

import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.RoutingResult;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.nat.PortPool;

public class UserAgentClient extends NetworkFunction {
	
	public static final String  SIP_INVITE = "Sip_Invitation";
	public static final String  SIP_OK = "Sip_OK";
	public static final String  SIP_REGISTER = "Sip_Register";
	public static final String  SIP_ENDING = "Sip_Ending";
	
	private String num_Callee;
	private String ip_sipServer;
	private PortPool portPool;
	protected Interface initialForwardingInterface;
	
	public UserAgentClient(List<Interface> interfaces, String num_Callee,String ip_sipServer) {
		super(interfaces);
		
		this.num_Callee = num_Callee;
		this.ip_sipServer = ip_sipServer;
	    this.portPool = new PortPool(10000, 1024);
	    initialForwardingInterface = interfaces.get(0);
	}
	
	public RoutingResult defineSendingPacket() {
		Packet p = new Packet();
		
		Integer new_port = portPool.getAvailablePort();
		if(new_port == null)
			return new RoutingResult(Action.DROP, null, null);
		
		p.setField(PacketField.IP_DST, ip_sipServer);
		p.setField(PacketField.BODY, num_Callee);
		p.setField(PacketField.BODY, p.getField(PacketField.ORIG_BODY));
		p.setField(PacketField.APPLICATION_PROTOCOL, SIP_REGISTER);
		
		return new RoutingResult(Action.FORWARD,p,initialForwardingInterface);
	}
	
	
	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {  
	
		Packet p = null;
		try {
			p = packet.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		if(packet.equalsField(PacketField.APPLICATION_PROTOCOL, SIP_INVITE)){
		
			p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
			p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
			p.setField(PacketField.PORT_SRC,packet.getField(PacketField.PORT_DST));
			p.setField(PacketField.PORT_DST, packet.getField(PacketField.PORT_SRC));
			p.setField(PacketField.APPLICATION_PROTOCOL, SIP_OK);	
			return new RoutingResult(Action.FORWARD,p,iface);		
		}
		if(packet.equalsField(PacketField.APPLICATION_PROTOCOL, SIP_OK)){
			
			p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
			p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
			p.setField(PacketField.PORT_SRC,packet.getField(PacketField.PORT_DST));
			p.setField(PacketField.PORT_DST, packet.getField(PacketField.PORT_SRC));
			p.setField(PacketField.APPLICATION_PROTOCOL, SIP_ENDING);	
			return new RoutingResult(Action.FORWARD,p,iface);	
		}
	
		return new RoutingResult(Action.DROP,null,null);

	}
	

}
