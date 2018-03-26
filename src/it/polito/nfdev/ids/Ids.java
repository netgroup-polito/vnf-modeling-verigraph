package it.polito.nfdev.ids;

import java.util.ArrayList;
import java.util.List;

import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult;
import it.polito.nfdev.lib.Table;
import it.polito.nfdev.lib.TableEntry;
import it.polito.nfdev.lib.RoutingResult.Action;

public class Ids extends NetworkFunction {
	
	private Table blackList;
	
	public Ids(){
		super(new ArrayList<Interface>());
		
		this.blackList = new Table(1, 0);
		this.blackList.setTypes(Table.TableTypes.URL);
	}
	
	public Ids(List<String> rules){
		this();
		
		for(String rule : rules){
			addIdsRule(rule);
		}
		
	}

	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {
		
		Packet p = null;
		try {
			p = packet.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		if(packet.equalsField(PacketField.PROTO, Packet.HTTP_REQUEST) || packet.equalsField(PacketField.PROTO, Packet.HTTP_RESPONSE)){
			TableEntry e = blackList.matchEntry(packet.getField(PacketField.URL));
			
			if(e!=null)
				return new RoutingResult(Action.DROP,null,null);
			else
				return new RoutingResult(Action.FORWARD,p,iface);
			
			
		}
		return new RoutingResult(Action.DROP,null,null);
	}
	
	public boolean addIdsRule(String rule){
		TableEntry entry = new TableEntry(1);
		entry.setValue(0, rule);
		
		return blackList.storeEntry(entry);
	}
	
	public boolean removeIdsRule(String rule){
		TableEntry entry = blackList.matchEntry(rule);
		
		return blackList.removeEntry(entry);
	}
	
	public void clearIdsTable(){
		blackList.clear();
	}

}
