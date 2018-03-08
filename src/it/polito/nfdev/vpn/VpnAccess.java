package it.polito.nfdev.vpn;

import java.util.List;

import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.RoutingResult;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;

public class VpnAccess extends NetworkFunction{

	private String accessIp;
	private String exitIp;
	
	public VpnAccess(List<Interface> interfaces,  String accessIp, String exitIp) {
		super(interfaces);
		this.accessIp = accessIp;
		this.exitIp = exitIp;
	}
	
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) 
	{
		Packet p = null;
		try {			
			p = packet.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return new RoutingResult(Action.DROP, null, null); 
		}
		if(iface.isInternal())
		{	
			if(!packet.equalsField(PacketField.ENCRYPTED, String.valueOf(true)) && packet.equalsField(PacketField.INNER_SRC,String.valueOf(null)) && packet.equalsField(PacketField.INNER_DEST,String.valueOf(null)))
			{	
				p.setField(PacketField.IP_SRC, accessIp);
				p.setField(PacketField.IP_DST, exitIp);
				p.setField(PacketField.INNER_DEST, packet.getField(PacketField.IP_DST));
				p.setField(PacketField.INNER_SRC, packet.getField(PacketField.IP_SRC));
				p.setField(PacketField.ENCRYPTED,  String.valueOf(true));
				return new RoutingResult(Action.FORWARD, p, externalInterface);
			}
		}else if(packet.equalsField(PacketField.IP_SRC,exitIp) && packet.equalsField(PacketField.IP_DST,accessIp) && packet.equalsField(PacketField.ENCRYPTED, String.valueOf(true)) ){
			
			p.setField(PacketField.IP_DST, packet.getField(PacketField.INNER_DEST));
			p.setField(PacketField.IP_SRC, packet.getField(PacketField.INNER_SRC));
			p.setField(PacketField.INNER_DEST, String.valueOf(null));
			p.setField(PacketField.INNER_SRC, String.valueOf(null));
			p.setField(PacketField.ENCRYPTED, String.valueOf(false));
			return new RoutingResult(Action.FORWARD, p, internalInterface);
			
		}
		else{
			return new RoutingResult(Action.DROP, null, null);
		}
		return new RoutingResult(Action.DROP, null, null);
}
}
