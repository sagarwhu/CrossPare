package de.ugoe.cs.cpdp.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * Loader for CSV data generated by mynbou. 
 * @author sherbold
 */
public class MynbouDataLoader implements SingleVersionLoader, IBugMatrixLoader {

	/**
	 * the bug matrix
	 */
	private Instances bugMatrix = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ugoe.cs.cpdp.loader.AbstractFolderLoader.SingleVersionLoader#load(
	 * java.io.File)
	 */
	@Override
	public Instances load(File file, boolean binaryClass) {

		final String[] lines;
		try {
			List<String> stringList = Files.readAllLines(file.toPath());
			lines = stringList.toArray(new String[] {});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// configure Instances
		final ArrayList<Attribute> atts = new ArrayList<>();

		String[] lineSplit = lines[0].split(";");
		int index = 1;
		while (!"imports".equals(lineSplit[index])) {
			atts.add(new Attribute(lineSplit[index]));
			index++;
		}
		final int importIndex = index;
		Attribute classAtt;
		if (binaryClass) {
			// add nominal class attribute
			final ArrayList<String> classAttVals = new ArrayList<>();
			classAttVals.add("0");
			classAttVals.add("1");
			classAtt = new Attribute("bug", classAttVals);
		} else {
			// add numeric class attribute
			classAtt = new Attribute("bugs");
		}
		atts.add(classAtt);

		final Instances data = new Instances(file.getName(), atts, 0);
		data.setClass(classAtt);

		// fetch data
		for (int i = 1; i < lines.length; i++) {
			lineSplit = lines[i].split(";");
			double[] values = new double[importIndex];
			for (int j = 0; j < values.length - 1; j++) {
				values[j] = Double.parseDouble(lineSplit[j + 1].trim());
			}
			if (binaryClass) {
				// nominal class value
				values[values.length - 1] = lineSplit[importIndex + 1].trim().equals("0") ? 0 : 1;
			} else {
				// numeric class value
				values[values.length - 1] = Double.parseDouble(lineSplit[importIndex + 1].trim());
			}
			data.add(new DenseInstance(1.0, values));
		}

		// create issue matrix
		final ArrayList<Attribute> issueMatrixAtts = new ArrayList<>();
		index = importIndex + 2;
		lineSplit = lines[0].split(";");
		while (index < lineSplit.length) {
			issueMatrixAtts.add(new Attribute(lineSplit[index]));
			index++;
		}
		bugMatrix = new Instances(file.getName(), issueMatrixAtts, 0);
		for (int i = 1; i < lines.length; i++) {
			lineSplit = lines[i].split(";");
			double[] values = new double[issueMatrixAtts.size()];
			for (int j = 0; j < values.length - 1; j++) {
				values[j] = Double.parseDouble(lineSplit[j + importIndex + 2].trim());
			}
			bugMatrix.add(new DenseInstance(1.0, values));
		}

		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ugoe.cs.cpdp.loader.AbstractFolderLoader.SingleVersionLoader#
	 * filenameFilter(java.lang.String)
	 */
	@Override
	public boolean filenameFilter(String filename) {
		return filename.endsWith(".csv");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ugoe.cs.cpdp.loader.IBugMatrixLoader#getBugMatrix()
	 */
	@Override
	public Instances getBugMatrix() {
		return bugMatrix;
	}

}