package com.huaxun.more.bean;

import java.io.Serializable;


public class Column implements Serializable {
	private static final long serialVersionUID = 1L;

	public int columnId;
	public String columnName;
	
	public Column() {
	}
	
	public Column(int columnId, String columnName) {
		this.columnId = columnId;
		this.columnName = columnName;
	}

	@Override
	public int hashCode() {
		return columnId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (!(obj instanceof Column)) {
			return false;
		}
		if (this == obj)
			return true;
		
		Column other = (Column) obj;
		if (this.columnId == other.columnId)
			return true;
		
		return false;
	}

}
