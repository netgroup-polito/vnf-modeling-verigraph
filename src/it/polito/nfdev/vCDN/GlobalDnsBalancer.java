package it.polito.nfdev.vCDN;

import java.util.ArrayList;
import java.util.List;

import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.RoutingResult;
import it.polito.nfdev.lib.Table;
import it.polito.nfdev.lib.TableEntry;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.verification.Verifier;

public class GlobalDnsBalancer extends NetworkFunction {

	private Table balanTable;
	private String ip_url;
	
	public GlobalDnsBalancer(String ip_url) {
		super(new ArrayList<Interface>());
		
		this.balanTable = new Table(2,0);	// Ip_Src_of_Requester, url, IP_of_CDNNode
		this.balanTable.setTypes(Table.TableTypes.Ip, Table.TableTypes.URL);
		this.ip_url = ip_url;
	}

	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {
		Packet P = null;
		try {
			P = packet.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return new RoutingResult(Action.DROP, null, null); 
		}
		
				if (packet.equalsField(PacketField.PROTO, Packet.DNS_REQUEST)){
					TableEntry entry = balanTable.matchEntry(packet.getField(PacketField.IP_SRC), packet.getField(PacketField.URL));
					if(entry != null){
						P.setField(PacketField.INNER_DEST, ip_url);						
						P.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
						P.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
						P.setField(PacketField.PROTO, Packet.DNS_RESPONSE);
						return new RoutingResult(Action.FORWARD,P,iface);
					}
					
				}
		return new RoutingResult(Action.DROP,null,null);
		
		}
	
	}


