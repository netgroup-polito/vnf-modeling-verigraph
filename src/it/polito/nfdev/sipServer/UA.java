package it.polito.nfdev.sipServer;

import java.util.List;

import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.RoutingResult;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.nat.PortPool;

public class UA extends NetworkFunction {
	
	public static final String  SIP_INVITE = "Sip_Invite";
	public static final String  SIP_INVITE_OK = "Sip_Invite_OK";
	public static final String  SIP_REGISTE_OK = "Sip_Registe_OK";
	public static final String  SIP_REGISTE = "Sip_Registe";
	public static final String  SIP_END = "Sip_End";
	
	private String num;
	private String ip_sipServer;
	private String ip_caller;
	private String domain;
	private PortPool portPool;
	protected Interface initialForwardingInterface;
	
	public UA(List<Interface> interfaces,String ip_caller, String num,String ip_sipServer, String domain) {
		super(interfaces);
		
		this.ip_caller = ip_caller;
		this.num = num;
		this.domain = domain;
		this.ip_sipServer = ip_sipServer;
	    this.portPool = new PortPool(10000, 1024);
	    initialForwardingInterface = interfaces.get(0);
	}
	
	public RoutingResult defineSendingPacket() {
		Packet p = new Packet();
		
		Integer new_port = portPool.getAvailablePort();
		if(new_port == null)
			return new RoutingResult(Action.DROP, null, null);
		
		p.setField(PacketField.IP_SRC, ip_caller);
		p.setField(PacketField.IP_DST, ip_sipServer);
		p.setField(PacketField.BODY, num);
		p.setField(PacketField.URL, domain);
		p.setField(PacketField.PROTO, SIP_REGISTE);
		
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
		
	/*	if(packet.equalsField(PacketField.PROTO, SIP_INVITE)){
		
			p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
			p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
			p.setField(PacketField.PROTO, SIP_INVITE_OK);	
			return new RoutingResult(Action.FORWARD,p,iface);		
		}
		if(packet.equalsField(PacketField.PROTO, SIP_INVITE_OK)){
			
			p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
			p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
			p.setField(PacketField.PROTO, SIP_END);	
			return new RoutingResult(Action.FORWARD,p,iface);	
		}
	*/
		return new RoutingResult(Action.DROP,null,null);

	}
	

}
