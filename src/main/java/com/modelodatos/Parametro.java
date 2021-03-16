package com.modelodatos;

public class Parametro {
	
	private String nombre;
	private String tipo;
	private String valor;
	
	public Parametro() {
		super();

	}	
	
	public Parametro(String nombre, String tipo, String valor) {
		super();
		this.nombre = nombre;
		this.tipo = tipo;
		this.valor = valor;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getValor() {
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	
public boolean validar() {
		
		boolean valido = false;
		
		if(this.getNombre().equals("")) {
			valido = false;
			return valido;
			// Falta nombre
		}
		else if(this.getTipo() == null) {
			valido = false;
			return valido;
			// Falta descripcion
		}
		else if(this.getValor() == null) {
			valido = false;
			return valido;
			// Falta descripcion
		}
		
		else {
			valido = true;
		}
		
		return valido;
	}

}
