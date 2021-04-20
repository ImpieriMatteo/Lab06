package it.polito.tdp.meteo.model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	private MeteoDAO dao;
	private List<Citta> citta;
	private List<Rilevamento> risultatoMigliore;
	private int costoSoluzioneMigliore;
	
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;

	public Model() {
		dao = new MeteoDAO();
		citta = new ArrayList<>();
		for(String s : dao.getAllLocalita()) {
			Citta temp = new Citta(s);
			citta.add(temp);
		}
		
	}

	// of course you can change the String output with what you think works best
	public List<String> getUmiditaMedia(int mese) {
		List<String> result = new ArrayList<String>();
		DecimalFormat df = new DecimalFormat("#.###");
		
		for(Citta c : citta) {
			List<Rilevamento> rilevamenti = dao.getAllRilevamentiLocalitaMese(mese, c.getNome());
			String temp = c.getNome()+" - "+df.format(getMedia(rilevamenti));
			result.add(temp);
		}
		
		return result;
	}
	
	// of course you can change the String output with what you think works best
	public List<String> trovaSequenza(int mese) {
		List<Rilevamento> parziale = new ArrayList<>();
		Citta ultimaCittaVisitata = null;
		
		this.risultatoMigliore = new ArrayList<>();
		this.costoSoluzioneMigliore = 100000000;
		for(Citta c : citta) {
			c.setRilevamenti(dao.getAllRilevamentiLocalitaMese(mese, c.getNome()));
			c.setCounter(0);
		}
		
		this.cerca(parziale, ultimaCittaVisitata, 0);
		
		List<String> result = new ArrayList<>();
		for(Rilevamento r : risultatoMigliore) {
			result.add(r.getLocalita());
		}
		
		return result;
	}
	
	private void cerca(List<Rilevamento> parziale, Citta ultimaCittaVisitata, Integer livello) {
		
		if(livello==NUMERO_GIORNI_TOTALI) {
			int costo = this.getCosto(parziale);
			if(costo<this.costoSoluzioneMigliore) {
				this.risultatoMigliore = new ArrayList<>(parziale);
				this.costoSoluzioneMigliore = costo;
			}
			return;
		}
		else {
			for(Citta c : citta) {
				if(!c.equals(ultimaCittaVisitata)) {
					if(ultimaCittaVisitata==null) {
						Rilevamento r = c.getRilevamenti().get(livello);
						parziale.add(r);
						ultimaCittaVisitata = c;
						c.increaseCounter();
						cerca(parziale, ultimaCittaVisitata, livello+1);
						parziale.remove(r);
						ultimaCittaVisitata = null;
						c.decreaseCounter();

					}
					else if(ultimaCittaVisitata.getCounter()>=NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN) {
						Citta temp = ultimaCittaVisitata;
						Rilevamento r = c.getRilevamenti().get(livello);
						parziale.add(r);
						ultimaCittaVisitata = c;
						c.increaseCounter();
						cerca(parziale, ultimaCittaVisitata, livello+1);
						parziale.remove(r);
						ultimaCittaVisitata = temp;
						c.decreaseCounter();

					}
				}
				//con il controllo sul numero max di giorni trascorribili in una città controllo automaticamente che vengano visitate tutte e 3 le città
				//(solo con questa particolare struttura dati, ovvero 3 città, max 6 gg trascorribili e 15 giorni su cui eseguire l'algoritmo) 
				//	---> dovrò per forza visitare tutte e 3 le città durante l'arco di 15 gg con un max di 6 giorni per città!! 
				else if(c.getCounter()<NUMERO_GIORNI_CITTA_MAX) {
					Rilevamento r = c.getRilevamenti().get(livello);
					parziale.add(r);
					c.increaseCounter();
					cerca(parziale, ultimaCittaVisitata, livello+1);
					parziale.remove(r);
					c.decreaseCounter();
				}
			}
		}
	}
	
	private double getMedia(List<Rilevamento> rilevamenti) {
		double somma = 0.0;
		for(Rilevamento r : rilevamenti) {
			somma += (double)r.getUmidita();
		}
		
		return somma/rilevamenti.size();
	}
	
	private int getCosto(List<Rilevamento> parziale) {
		int costo = 0; 
		for(int i=0; i<parziale.size(); i++) {
			if(i==0) 
				//supponiamo che non ci sia il costo di spostamento tra le città con la prima misurazione nella prima città
				costo += parziale.get(i).getUmidita();
			else if(!parziale.get(i).getLocalita().equals(parziale.get(i-1).getLocalita()))
				costo += COST+parziale.get(i).getUmidita();
			else
				costo += parziale.get(i).getUmidita();
		}
		
		return costo;
	}
}
