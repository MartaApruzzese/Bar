package it.polito.tdp.bar.model;

public class Model {
	private Simulator sim;
	
	public Model() {
		sim= new Simulator();
	}
	
	public void simula() {
		sim.init();
		sim.run();
	}
}
