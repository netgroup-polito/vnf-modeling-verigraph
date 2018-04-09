package it.polito.nfdev.lib;

import java.util.ArrayList;
import java.util.List;

import it.polito.nfdev.lib.Packet.PacketField;

public class Interface {

	public static final String INTERNAL_ATTR = "INTERNAL";
	public static final String EXTERNAL_ATTR = "EXTERNAL";


	public enum Type {
		INTERNAL, EXTERNAL, STD_IN, STD_OUT, NF_IDS, NF_FW
	};

	public String IP_ADRESS;
	private Type interfaceType;
	private final Integer id;

	private List<String> attributes;

	public Interface() {
		this.id = 1;
		this.interfaceType = Type.STD_OUT;
		this.attributes = new ArrayList<>();
	}
	public Interface(Integer id, Type type) {
		this.id = id;
		this.interfaceType = type;
		this.attributes = new ArrayList<>();
	}

	public Type getInterfaceType() {
		return interfaceType;
	}
	public Integer getId() {
		return id;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public void addAttribute(String attribute) {
		this.attributes.add(attribute);
	}

	public boolean isInternal() {
		if (interfaceType == Type.INTERNAL)
			return true;

		return false;
	}

	// MODIFY_02: Add method
	public boolean equalsField(PacketField field, Type value) {

		Type temp = this.interfaceType;
		if (temp != null) {
			if (value.compareTo(temp) == 0)
				return true;
		}

		return false;
	}

	// MODIFY_03: Add method
	public boolean notEqualsField(PacketField field, Type value) {

		Type temp = this.interfaceType;
		if (temp != null) {
			if (value.compareTo(temp) != 0)
				return true;
		}

		return false;
	}

}
