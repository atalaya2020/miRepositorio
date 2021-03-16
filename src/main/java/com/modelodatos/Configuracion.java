package com.modelodatos;

import java.util.ArrayList;

public class Configuracion {

	
	private String nombre;
	private String descripcion;
	private ArrayList<Parametro> parametros;
	
	public Configuracion(String nombre, String descripcion, ArrayList<Parametro> parametros) {
		super();
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.parametros = parametros;
		
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public ArrayList<Parametro> getParametros() {
		return parametros;
	}

	public void setParametros(ArrayList<Parametro> parametros) {
		this.parametros = parametros;
	}
	
	
}
