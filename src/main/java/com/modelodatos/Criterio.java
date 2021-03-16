package com.modelodatos;

public class Criterio {

	private String nombre;
	private String descripcion;
	private String evaluacion;
	private String tipoResultado;
	private Object resultado;
	private String [] eventos;

	public Criterio(String nombre, String descripcion, String evaluacion, String tipoResultado, Object resultado, String[] eventos) {
		super();
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.evaluacion = evaluacion;
		this.tipoResultado = tipoResultado;
		this.resultado = resultado;
		this.eventos = eventos;
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

	public String getEvaluacion() {
		return evaluacion;
	}

	public void setEvaluacion(String evaluacion) {
		this.evaluacion = evaluacion;
	}

	public String getTipoResultado() {
		return tipoResultado;
	}

	public void setTipoResultado(String tipoResultado) {
		this.tipoResultado = tipoResultado;
	}

	public Object getResultado() {
		return resultado;
	}

	public void setResultado(Object resultado) {
		this.resultado = resultado;
	}
	
	public String [] getEventos() {
		return eventos;
	}

	public void setEventos(String [] eventos) {
		this.eventos = eventos;
	}
	
/*	public boolean validar() {
		
		boolean valido = false;
		
		if(this.getNombre().equals("")) {
			valido = false;
			return valido;
			// Falta nombre
		}
		else if(this.getDescripcion().equals("")) {
			valido = false;
			return valido;
			// Falta descripcion
		}
		else if(this.getEvaluacion().equals("")) {
			valido = false;
			return valido;
			// Falta descripcion
		}
		else if(this.getTipoResultado().equals("")) {
			valido = false;
			return valido;
			// Falta descripcion
		}
		else if(this.getResultado() == null) {
			valido = false;
			return valido;
			// Falta descripcion
		}
		else {
			valido = true;
		}
		
		return valido;
	}*/

}
