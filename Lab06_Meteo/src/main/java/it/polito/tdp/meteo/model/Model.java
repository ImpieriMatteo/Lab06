package it.polito.tdp.meteo.model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	MeteoDAO dao;
	
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;

	public Model() {
		dao = new MeteoDAO();
	}

	// of course you can change the String output with what you think works best
	public List<String> getUmiditaMedia(int mese) {
		List<String> result = new ArrayList<String>();
		DecimalFormat df = new DecimalFormat("#.###");
		
		for(String s : dao.getAllLocalita()) {
			List<Rilevamento> rilevamenti = dao.getAllRilevamentiLocalitaMese(mese, s);
			String temp = s+" - "+df.format(getMedia(rilevamenti));
			result.add(temp);
		}
		
		return result;
	}
	
	// of course you can change the String output with what you think works best
	public String trovaSequenza(int mese) {
		return "TODO!";
	}
	
	private double getMedia(List<Rilevamento> rilevamenti) {
		double media = 0.0;
		for(Rilevamento r : rilevamenti) {
			media += (double)r.getUmidita();
		}
		
		return media/rilevamenti.size();
	}
}
