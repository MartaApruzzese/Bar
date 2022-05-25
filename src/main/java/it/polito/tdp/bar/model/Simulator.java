package it.polito.tdp.bar.model;
import java.time.Duration;
import java.util.*;

import it.polito.tdp.bar.model.Event.EventType;

public class Simulator {
	//Modello
		private List<Tavolo> tavoli;
		
		//Parametri della simulazione 
		private int NUM_EVENTI = 2000;
		private int T_ARRIVO_MAX = 10;
		private int NUM_PERSONE_MAX = 10;
		private int DURATA_MIN = 60;
		private int DURATA_MAX = 120;
		private double TOLLERANZA_MAX = 0.9;
		private double OCCUPAZIONE_MAX = 0.5;
		
		//Coda degli eventi
		private PriorityQueue<Event> queue;
		
		//Statistiche
		private Statistiche statistiche;
		
		public void init() {
			this.queue= new PriorityQueue<>();
			this.statistiche= new Statistiche();
			
			creaTavoli();
			creaEventi();
			
		}

		private void creaEventi() {
			Duration arrivo= Duration.ofMinutes(0);
			for(int i=0; i<this.NUM_EVENTI; i++) {
				int nPersone= (int)(Math.random()*this.NUM_PERSONE_MAX+1);
				Duration durata= Duration.ofMinutes(this.DURATA_MIN+
						(int)(Math.random()*(this.DURATA_MAX-this.DURATA_MIN +1)));
				double tolleranza= Math.random()+this.TOLLERANZA_MAX;
				
				Event e= new Event(arrivo,EventType.ARRIVO_GRUPPO_CLIENTI,nPersone,durata, tolleranza, null );
				this.queue.add(e);
				
				arrivo=arrivo.plusMinutes((int)(Math.random()*this.T_ARRIVO_MAX+1));
			}
			
		}

		private void creaTavolo(int qta, int dimensione) {
			for(int i=0; i<qta; i++) {
				this.tavoli.add(new Tavolo(dimensione, false));
			}
		}
		
		private void creaTavoli() {
			creaTavolo(2,10);
			creaTavolo(4,8);
			creaTavolo(4,6);
			creaTavolo(5,4);
			
			Collections.sort(this.tavoli, new Comparator<Tavolo>() {
			
				public int compare(Tavolo o1, Tavolo o2) {
					return o1.getPosti()-o2.getPosti();
				}
			});
		}
		
		
		public void run() {
			while(!this.queue.isEmpty()) {
				Event e= queue.poll();
				processEvent(e);
			}
		}

		private void processEvent(Event e) {
			switch(e.getType()) {
			case ARRIVO_GRUPPO_CLIENTI:
				this.statistiche.incrementaClienti(e.getnPersone());//Conto clienti totali
				
				//Vedo se c'è un tavolo
				Tavolo tavolo=null;
				for(Tavolo t: tavoli) {
					if(!t.isOccupato() && t.getPosti()>=e.getnPersone() &&
							t.getPosti()*this.OCCUPAZIONE_MAX<= e.getnPersone()) {
						tavolo=t;
						break;
					}
				}
				if(tavolo!=null) {
					System.out.println("Trovato un tavolo");
					e.setTavolo(tavolo);
					tavolo.setOccupato(true);
					statistiche.incrementaSoddisfatti(e.getnPersone());
					//Dopo un po i clienti se ne vanno
					queue.add(new Event(e.getTime().plus(e.getDurata()), EventType.TAVOLO_LIBERATO, e.getnPersone(), e.getDurata(), e.getTolleranza(), tavolo));
					
				}else {
					//C'è il bancone
					double bancone= Math.random();
					if(bancone<=e.getTolleranza()) {
						//Si fermano al bancone
						statistiche.incrementaSoddisfatti(e.getnPersone());
					}else {
						//Non si fermao al bancone.
						statistiche.incrementaInsoddisfatti(e.getnPersone());
					}
					
				}
				
				break;
			case TAVOLO_LIBERATO:
				e.getTavolo().setOccupato(false);
				break;
			}
			
		}

		public Statistiche getStatistiche() {
			return statistiche;
		}
		
		
}
