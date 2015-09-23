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
package com.rapidminer.operator.visualization;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.DummyPortPairExtender;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.PortPairExtender;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

import java.util.ArrayList;
import java.util.List;


/**
 * This operator transforms the data generated by a ProcessLog operator into an ExampleSet which can
 * then be used by other operators.
 * 
 * @author Ingo Mierswa
 */
public class ProcessLog2ExampleSet extends Operator {

	public static final String PARAMETER_LOG_NAME = "log_name";

	private final OutputPort exampleSetOutput = getOutputPorts().createPort("exampleSet");
	private final PortPairExtender dummyPorts = new DummyPortPairExtender("through", getInputPorts(), getOutputPorts());

	public ProcessLog2ExampleSet(OperatorDescription description) {
		super(description);
		ExampleSetMetaData newEMD = new ExampleSetMetaData();
		newEMD.attributesAreSuperset();
		newEMD.setNumberOfExamples(0);
		newEMD.getNumberOfExamples().increaseByUnknownAmount();
		getTransformer().addRule(new GenerateNewMDRule(exampleSetOutput, newEMD));

		dummyPorts.start();

		getTransformer().addRule(dummyPorts.makePassThroughRule());
	}

	@Override
	public void doWork() throws OperatorException {
		DataTable table = null;
		if (isParameterSet(PARAMETER_LOG_NAME)) {
			String dataTableName = getParameterAsString(PARAMETER_LOG_NAME);
			table = getProcess().getDataTable(dataTableName);
		} else {
			if (getProcess().getDataTables().size() > 0) {
				table = getProcess().getDataTables().iterator().next();
				logNote("No log name was specified, using first data table found...");
			}
		}

		// check
		if (table == null) {
			throw new UserError(this, 939);
		}

		// create attributes
		List<Attribute> attributes = new ArrayList<Attribute>();
		for (int i = 0; i < table.getNumberOfColumns(); i++) {
			String name = table.getColumnName(i);
			if (table.isDate(i)) {
				attributes.add(AttributeFactory.createAttribute(name, Ontology.DATE));
			} else if (table.isDateTime(i)) {
				attributes.add(AttributeFactory.createAttribute(name, Ontology.DATE_TIME));
			} else if (table.isNumerical(i)) {
				attributes.add(AttributeFactory.createAttribute(name, Ontology.REAL));
			} else {
				attributes.add(AttributeFactory.createAttribute(name, Ontology.NOMINAL));
			}
		}

		// create table
		MemoryExampleTable exampleTable = new MemoryExampleTable(attributes);
		for (int r = 0; r < table.getNumberOfRows(); r++) {
			DataTableRow row = table.getRow(r);
			double[] data = new double[attributes.size()];
			for (int i = 0; i < table.getNumberOfColumns(); i++) {
				if (table.isDate(i)) {
					data[i] = row.getValue(i);
				} else if (table.isDateTime(i)) {
					data[i] = row.getValue(i);
				} else if (table.isNumerical(i)) {
					data[i] = row.getValue(i);
				} else {
					Attribute attribute = attributes.get(i);
					String value = table.getValueAsString(row, i);
					data[i] = attribute.getMapping().mapString(value);
				}
			}
			exampleTable.addDataRow(new DoubleArrayDataRow(data));
		}

		// create and return example set
		exampleSetOutput.deliver(exampleTable.createExampleSet());
		dummyPorts.passDataThrough();
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(
				PARAMETER_LOG_NAME,
				"The name of the ProcessLog operator which generated the log data which should be transformed (empty: use first found data table).",
				true, false));
		return types;
	}
}
