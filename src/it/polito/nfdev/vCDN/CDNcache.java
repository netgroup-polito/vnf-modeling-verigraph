package it.polito.nfdev.vCDN;

import java.util.List;
import java.net.MalformedURLException;
import java.net.URL;
import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.lib.TableEntry;
import it.polito.nfdev.verification.Verifier;
import it.polito.nfdev.webcache.*;
import it.polito.nfdev.verification.Table;
import it.polito.nfdev.nat.PortPool;

public class CDNcache extends NetworkFunction {

//	private String webServer_Ip;
//	private String client_Ip;
//	private PortPool portPool;
	private Interface internalFace;
	private Interface externalFace;
	
	@Table( fields = {"URL", "CONTENT"} )
	private CacheTable cdnCacheTable;
	
	public CDNcache(List<Interface> interfaces) {
		super(interfaces);
		assert interfaces.size() == 2;
		internalFace = null;
		externalFace = null;
		for(Interface i : interfaces)
		{
			if(i.getAttributes().contains(Interface.INTERNAL_ATTR))
				internalFace = i;
			if(i.getAttributes().contains(Interface.EXTERNAL_ATTR))
				externalFace = i;
		}
		assert internalFace != null;
		assert externalFace != null;
		assert internalFace.getId() != externalFace.getId();
		
		this.cdnCacheTable = new CacheTable(2,0);  // URL, CONTENT
		this.cdnCacheTable.setDataDriven();
		
	}
	
	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {		
		Packet p = null;
		try {
			p = packet.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return new RoutingResult(Action.DROP, null, null);
		}
		
		if(iface.isInternal())
		{
			if(packet.equalsField(PacketField.APPLICATION_PROTOCOL,Packet.HTTP_REQUEST)){
				TableEntry entry = cdnCacheTable.matchEntry(packet.getField(PacketField.L7DATA));				
				
				if(entry != null)   
				{
					p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC));
					p.setField(PacketField.IP_SRC, packet.getField(PacketField.OLD_DST));
					p.setField(PacketField.OLD_DST, packet.getField(PacketField.IP_DST));
					p.setField(PacketField.PORT_DST, packet.getField(PacketField.PORT_SRC));
					p.setField(PacketField.PORT_SRC, packet.getField(PacketField.PORT_DST));
					p.setField(PacketField.APPLICATION_PROTOCOL, Packet.HTTP_RESPONSE);
					p.setField(PacketField.L7DATA, (String)entry.getValue(0));   
					return new RoutingResult(Action.FORWARD, p, internalInterface); 
				
				}
				else 
				{
					p.setField(PacketField.IP_DST, packet.getField(PacketField.OLD_DST));
					p.setField(PacketField.OLD_DST, packet.getField(PacketField.IP_DST));
					
					
					return new RoutingResult(Action.FORWARD, p, externalInterface); 
				}
			}
			return new RoutingResult(Action.DROP, null, null);   
			
		}
		else  
		{		
			if(packet.equalsField(PacketField.APPLICATION_PROTOCOL,Packet.HTTP_RESPONSE)){
				
					Content content;
					try {
						content = new Content(new URL(packet.getField(PacketField.L7DATA)));
					
					CacheTableEntry newEntry = new CacheTableEntry(2);
					newEntry.setValue(0, packet.getField(PacketField.L7DATA));
					newEntry.setValue(1, content);
					cdnCacheTable.storeEntry(newEntry);
					} catch (Exception e) {
						e.printStackTrace();
					}
				/*	p.setField(PacketField.IP_SRC, packet.getField(PacketField.IP_DST));
					p.setField(PacketField.IP_DST, packet.getField(PacketField.IP_SRC)); 
				*/
					return new RoutingResult(Action.FORWARD, p, internalInterface); 
					
				
			}
			return new RoutingResult(Action.DROP, null, null);
			
		}
		
	}
	
	
	/*public boolean addCdnCacheRule(String pagePath, String srcIp, String cacheIp){
		
		TableEntry entry = new TableEntry(3);
		entry.setValue(0, serverName.trim());
		entry.setValue(1, srcIp.trim());
		entry.setValue(2, cacheIp.trim());
			
		return cdnCacheTable.storeEntry(entry);
	}
	
	public boolean removeCDNRule(String serverName, String srcIp, String cacheIp){
		TableEntry entry  = cdnTable.matchEntry(serverName, srcIp, cacheIp);
		
		return cdnTable.removeEntry(entry);  
	}*/

	public void clearCdnCacheTable(){
		cdnCacheTable.clear();
	}

}
