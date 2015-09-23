/**
 * Copyright (C) 2001-2015 by RapidMiner and the contributors
 *
 * Complete list of developers available at our web site:
 *
 *      http://rapidminer.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.gui.properties.celleditors.value;

import com.rapidminer.gui.properties.MatrixPropertyDialog;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterTypeMatrix;
import com.rapidminer.tools.math.StringToMatrixConverter;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;


/**
 * A cell editor with a button that opens a {@link MatrixPropertyDialog}. Values generated by this
 * operator are matrices of String pairs.
 * 
 * @see com.rapidminer.gui.properties.MatrixPropertyDialog
 * @author Helge Homburg
 */
public class MatrixValueCellEditor extends AbstractCellEditor implements PropertyValueCellEditor {

	private static final long serialVersionUID = 0L;

	private ParameterTypeMatrix type;

	private Operator operator;

	private JButton button = new JButton(new ResourceAction(true, "matrix.edit") {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			MatrixPropertyDialog dialog = new MatrixPropertyDialog(type, matrix, operator);
			dialog.setVisible(true);
			if (dialog.isOk()) {
				matrix = dialog.getMatrix();
				fireEditingStopped();
				setButtonText();
			} else {
				fireEditingCanceled();
			}
		}

	});

	private double[][] matrix;

	public MatrixValueCellEditor(ParameterTypeMatrix type) {
		this.type = type;
		button.setMargin(new java.awt.Insets(0, 0, 0, 0));
		button.setToolTipText(type.getDescription());
		setButtonText();
	}

	@Override
	public void setOperator(final Operator operator) {
		this.operator = operator;
	}

	@Override
	public Object getCellEditorValue() {
		return StringToMatrixConverter.createMatlabString(matrix);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
		try {
			this.matrix = StringToMatrixConverter.parseMatlabString((String) value);
		} catch (Exception e) {
			// TODO: do nothing?
		}
		setButtonText();
		return button;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		return getTableCellEditorComponent(table, value, isSelected, row, column);
	}

	@Override
	public boolean useEditorAsRenderer() {
		return true;
	}

	private void setButtonText() {
		if (matrix != null) {
			button.setText("Edit Matrix (" + matrix.length + " x " + matrix[0].length + ")...");
		} else {
			button.setText("Edit Matrix...");
		}
	}

	@Override
	public boolean rendersLabel() {
		return false;
	}
}
