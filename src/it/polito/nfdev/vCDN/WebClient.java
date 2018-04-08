package it.polito.nfdev.vCDN;

import java.util.List;

import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.RoutingResult;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.nat.PortPool;

public class WebClient extends NetworkFunction {

	public static final String  REQUESTED_URL = "Requested_url";
	
	private String ip_EndHost;
	private String ip_localDns;
	private PortPool portPool;
	protected Interface initialForwardingInterface;
	
	public WebClient(List<Interface> interfaces, String ip_EndHost, String ip_localDns) {
		super(interfaces);
		
		this.ip_EndHost = ip_EndHost;
	    this.ip_localDns = ip_localDns;
	    this.portPool = new PortPool(10000, 1024);
	    initialForwardingInterface = interfaces.get(0);
	}
	
	public RoutingResult defineSendingPacket() {
		Packet p = new Packet();
		
		Integer new_port = portPool.getAvailablePort();
		if(new_port == null)
			return new RoutingResult(Action.DROP, null, null);
		
		p.setField(PacketField.IP_SRC, ip_EndHost);
		p.setField(PacketField.IP_DST, ip_localDns);
		p.setField(PacketField.PROTO, Packet.DNS_REQUEST);
		p.setField(PacketField.URL, REQUESTED_URL);
		
		return new RoutingResult(Action.FORWARD,p,initialForwardingInterface);
	}
	
	
	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {
		// Assume that this EndHost received a DNS Response with the ip of cache node  
	
		Packet p = null;
		try {
			p = packet.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		if(packet.equalsField(PacketField.PROTO, Packet.DNS_RESPONSE) && !packet.equalsField(PacketField.INNER_DEST,String.valueOf(null))){
		
			p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
			p.setField(PacketField.IP_DST, packet.getField(PacketField.INNER_DEST));  // INNER_DEST is the Cache IP
			p.setField(PacketField.PORT_DST, Packet.HTTP_PORT_80);
			p.setField(PacketField.PROTO, Packet.HTTP_REQUEST);
		//	p.setField(PacketField.URL, REQUESTED_URL);
		
			return new RoutingResult(Action.FORWARD,p,iface);
		
		}
		
		return new RoutingResult(Action.DROP,null,null);

	}
	

}
