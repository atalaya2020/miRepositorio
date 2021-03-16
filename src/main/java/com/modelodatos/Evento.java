package com.modelodatos;

import java.util.ArrayList;

public class Evento extends Indicador {

	public Evento() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Evento(String nombre, String descripcion, String fuente, String destino, String tipo, String comando,
			ArrayList<Parametro> parametros, String[] resultado, ArrayList<Configuracion> configuraciones, boolean stopper) {
		super(nombre, descripcion, fuente, destino, tipo, comando, parametros, resultado, configuraciones, stopper);
		// TODO Auto-generated constructor stub
	}

}
