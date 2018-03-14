package it.polito.nfdev.ipv4InIpv6;

import java.util.List;

import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.RoutingResult;
import it.polito.nfdev.lib.Table;
import it.polito.nfdev.lib.TableEntry;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.parser.Constants;

public class IPv4Access extends NetworkFunction{

	private Table ipTable;
	private String accessIp;
	private String exitIp;
	
	public IPv4Access(List<Interface> interfaces,  String accessIp, String exitIp) {
		super(interfaces);
		this.accessIp = accessIp;
		this.exitIp = exitIp;
		this.ipTable = new Table(1,0);
		this.ipTable.setTypes(Table.TableTypes.Ip);
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
	/*	TableEntry entry = ipTable.matchEntry(packet.getField(PacketField.IP_DST));
		if(entry!=null){    // if the ip_dst is an IPv6 address, then the IPv6 gateway can forward the packet directly.
			return new RoutingResult(Action.FORWARD, p, iface);
		}
		else{*/
			if(!packet.equalsField(PacketField.ENCRYPTED, String.valueOf(true)) && packet.equalsField(PacketField.INNER_SRC,String.valueOf(null)) && packet.equalsField(PacketField.INNER_DEST,String.valueOf(null)))
			{	
				p.setField(PacketField.IP_SRC, accessIp);
				p.setField(PacketField.IP_DST, exitIp);
				p.setField(PacketField.INNER_DEST, packet.getField(PacketField.IP_DST));
				p.setField(PacketField.INNER_SRC, packet.getField(PacketField.IP_SRC));
				p.setField(PacketField.INNER_SRC, Constants.NOTNULL);
				p.setField(PacketField.ENCRYPTED,  String.valueOf(true));

				//p.equalsField(PacketField.INNER_SRC, exitIp)=false;
				this.addInternalAddress(p.getField(PacketField.INNER_SRC));
				return new RoutingResult(Action.FORWARD, p, iface);
			}
	 if(packet.equalsField(PacketField.IP_SRC,exitIp) && packet.equalsField(PacketField.IP_DST,accessIp) && packet.equalsField(PacketField.ENCRYPTED, String.valueOf(true)) )
		{
			
			p.setField(PacketField.IP_DST, packet.getField(PacketField.INNER_DEST));
			p.setField(PacketField.IP_SRC, packet.getField(PacketField.INNER_SRC));
			p.setField(PacketField.INNER_SRC, Constants.NULL);
			p.setField(PacketField.INNER_DEST, Constants.NULL);
			p.setField(PacketField.ENCRYPTED, String.valueOf(false));
			
			this.addInternalAddress(p.getField(PacketField.IP_DST));
			return new RoutingResult(Action.FORWARD, p, iface);
			
		}
	//	}
		return new RoutingResult(Action.DROP, null, null);
	}
}
