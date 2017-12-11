package it.polito.nfdev.vCDN;

import java.util.List;

import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.RoutingResult;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.nat.PortPool;

public class EndHost extends NetworkFunction {

	public static final String  REQUESTED_URL = "Requested_url";
	
	private String ip_EndHost;
	private String ip_WebServer;
	private PortPool portPool;
	protected Interface initialForwardingInterface;
	
	public EndHost(List<Interface> interfaces, String ip_EndHost, String ip_WebServer) {
		super(interfaces);
		
		this.ip_EndHost = ip_EndHost;
	    this.ip_WebServer = ip_WebServer;
	    this.portPool = new PortPool(10000, 1024);
	    initialForwardingInterface = interfaces.get(0);
	}
	
	public RoutingResult defineSendingPacket() {
		Packet p = new Packet();
		
		Integer new_port = portPool.getAvailablePort();
		if(new_port == null)
			return new RoutingResult(Action.DROP, null, null);
		
		p.setField(PacketField.IP_SRC, ip_EndHost);
		p.setField(PacketField.PORT_SRC, String.valueOf(new_port));
		p.setField(PacketField.IP_DST, ip_WebServer);
		p.setField(PacketField.OLD_DST, ip_WebServer);
		p.setField(PacketField.OLD_SRC, ip_EndHost);
		p.setField(PacketField.PORT_DST, Packet.HTTP_PORT_80);
		p.setField(PacketField.APPLICATION_PROTOCOL, Packet.HTTP_REQUEST);
		p.setField(PacketField.L7DATA, REQUESTED_URL);
		
		return new RoutingResult(Action.FORWARD,p,initialForwardingInterface);
	}
	
	
	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {
		// Assume that this EndHost received a DNS Response with the ip of cache node  
	
	/*	Packet p = null;
		try {
			p = packet.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		if(packet.equalsField(PacketField.APPLICATION_PROTOCOL, Packet.HTTP_RESPONSE)){
		
			p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
			p.setField(PacketField.PORT_SRC, packet.getField(PacketField.PORT_DST));
			p.setField(PacketField.IP_DST, packet.getField(PacketField.L7DATA));  // L7DATA is the Cache IP
			p.setField(PacketField.PORT_DST, Packet.HTTP_PORT_80);
			p.setField(PacketField.APPLICATION_PROTOCOL, Packet.HTTP_REQUEST);
			p.setField(PacketField.L7DATA, REQUESTED_URL);
		
			return new RoutingResult(Action.FORWARD,p,iface);
		
		}
		*/
		return new RoutingResult(Action.DROP,null,null);

	}
	

}
