package com.evolveum.midpoint.studio.impl.trace;

import java.util.HashSet;
import java.util.Set;

public class Options {
	private final Set<OpType> typesToShow = new HashSet<>();
	private final Set<PerformanceCategory> categoriesToShow = new HashSet<>();
	private boolean showAlsoParents;
	private boolean showPerformanceColumns;
	private boolean showReadWriteColumns;
	
	public boolean isShowAlsoParents() {
		return showAlsoParents;
	}
	public void setShowAlsoParents(boolean showAlsoParents) {
		this.showAlsoParents = showAlsoParents;
	}
	public Set<OpType> getTypesToShow() {
		return typesToShow;
	}
	public Set<PerformanceCategory> getCategoriesToShow() {
		return categoriesToShow;
	}
	public boolean isShowPerformanceColumns() {
		return showPerformanceColumns;
	}
	public void setShowPerformanceColumns(boolean showPerformanceColumns) {
		this.showPerformanceColumns = showPerformanceColumns;
	}
	public boolean isShowReadWriteColumns() {
		return showReadWriteColumns;
	}
	public void setShowReadWriteColumns(boolean showReadWriteColumns) {
		this.showReadWriteColumns = showReadWriteColumns;
	}
	
}
