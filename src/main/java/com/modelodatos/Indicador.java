package com.modelodatos;


import java.util.ArrayList;

public class Indicador {

	public Indicador() {
		super();
		// TODO Auto-generated constructor stub
	}

	private String nombre;
	private String descripcion;
	private String fuente;
	private String destino;
	private String tipo;
	private String comando;
	private ArrayList<Parametro> parametros;
	private String[] resultado;
	private ArrayList<Configuracion> configuraciones;
	private boolean stopper;



	public Indicador(String nombre, String descripcion, String fuente, String destino, String tipo, String comando,
			ArrayList<Parametro> parametros, String[] resultado, ArrayList<Configuracion> configuraciones, boolean stopper) {
		super();
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.fuente = fuente;
		this.destino = destino;
		this.tipo = tipo;
		this.comando = comando;
		this.parametros = parametros;
		this.resultado = resultado;
		this.configuraciones = configuraciones;
		this.stopper = stopper;

	}
	
	
	public String getDestino() {
		return destino;
	}

	public void setDestino(String destino) {
		this.destino = destino;
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
	public String getFuente() {
		return fuente;
	}
	public void setFuente(String fuente) {
		this.fuente = fuente;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getComando() {
		return comando;
	}
	public void setComando(String comando) {
		this.comando = comando;
	}
	public ArrayList<Parametro> getParametros() {
		return parametros;
	}
	public void setParametros(ArrayList<Parametro> parametros) {
		this.parametros = parametros;
	}
	public String[] getResultado() {
		return resultado;
	}
	public void setResultado(String[] resultado) {
		this.resultado = resultado;
	}

	public ArrayList<Configuracion> getConfiguraciones() {
		return configuraciones;
	}
	public void setConfiguraciones(ArrayList<Configuracion> configuraciones) {
		this.configuraciones = configuraciones;
	}
	public boolean getStopper(){
		return this.stopper;
	}
	public void setStopper(boolean stopper){
		this.stopper = stopper;
	}

}
