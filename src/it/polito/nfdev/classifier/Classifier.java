package it.polito.nfdev.classifier;

import it.polito.nfdev.lib.Table;
import it.polito.nfdev.lib.TableEntry;

import java.util.ArrayList;
import java.util.List;

import it.polito.nfdev.lib.Interface;
import it.polito.nfdev.lib.NetworkFunction;
import it.polito.nfdev.lib.Packet;
import it.polito.nfdev.lib.Packet.PacketField;
import it.polito.nfdev.lib.RoutingResult.Action;
import it.polito.nfdev.lib.RoutingResult;

/**
 * @author s211483
 * 
 *         #TRAFFIC CLASSIFIER# +The Constructor of the class received as param
 *         one List of interfaces. +The Classifier forwarding depend to the
 *         classifier Table: The table has 3 col: <Priority> <Application>
 *         <Interface> type: -Generic - Proto -Interface ex: -1 -POP3_REQUEST
 *         -if_2 +Behavior: if the packet has a Proto that match on the table
 *         than it can be forwarding through the interface indicate on the
 *         table.
 */
public class Classifier extends NetworkFunction {

	private Table classifierTable;
	private Integer interfaceIdSend; // It is used to generate the rules

	public Classifier(List<Interface> ifout) {
		super(new ArrayList<Interface>(ifout));

		this.classifierTable = new Table(3, 0);
		this.classifierTable.setTypes(Table.TableTypes.Generic, Table.TableTypes.Proto, Table.TableTypes.Generic);
	}

	@Override
	public RoutingResult onReceivedPacket(Packet packet, Interface iface) {
		TableEntry entry = classifierTable.matchEntry(packet.getField(PacketField.PROTO));
		if (entry != null) {
			Interface ifSend = (Interface) entry.getValue(2);
			if (ifSend != null && ifSend != iface) {
				setInterfaceIdSend(ifSend.getId());
				return new RoutingResult(Action.FORWARD, packet, ifSend);
			}
		}
		// DROP packet if is Unclassified:
		return new RoutingResult(Action.DROP, null, null);
	}

	public boolean addClassifierRule(int priority, String app, Interface ifctrl) {
		TableEntry entry = new TableEntry(3);
		entry.setValue(0, priority);
		entry.setValue(1, app);
		entry.setValue(2, ifctrl);

		return classifierTable.storeEntry(entry);
	}

	public boolean removeClassifierRule(int rule) {
		TableEntry entry = classifierTable.matchEntry(rule);
		return classifierTable.removeEntry(entry);
	}

	public void clearClassifierTable() {
		classifierTable.clear();
	}

	/*
	 * Getters and Setters
	 */
	public Table getClassifierTable() {
		return classifierTable;
	}

	public void setClassifierTable(Table classifierTable) {
		this.classifierTable = classifierTable;
	}

	public Integer getInterfaceIdSend() {
		return interfaceIdSend;
	}

	public void setInterfaceIdSend(Integer interfaceIdSend) {
		this.interfaceIdSend = interfaceIdSend;
	}

}
