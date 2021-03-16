package com.atalaya.evaluador;

public class Operando {
	private String nombre;
	private int tipo;
	private int tipoValor;
	private boolean negado;
	private Object resultado;
	private boolean ejecutado;
	private String indicador;
	
	
//	public void Operando () {	
//		this.tipo = 0;
//		this.negado = false;
//	}
//	
//	public void Operando (String nombre) {
//		this.nombre = nombre;
//		this.tipo = 0;
//		this.negado = false;
//	}
	
	public String getNombre() {		
		return nombre;
	}
	public void setNombre(String nombre) {
		this.negado = operandoNegado(nombre);
		this.nombre = nombre.replaceAll("NOT ", "").trim();	
	}

	public int getTipo() {		
		return tipo;
	}
	
	public void setTipo(int tipo) {
		this.tipo = tipo;			
	}	
	
	public int getTipoValor() {		
		return tipoValor;
	}
	public void setTipoValor(int tipoValor) {
		this.tipoValor = tipoValor;			
	}		
	
	public boolean getNegado() {		
		return negado;
	}
	public void setNegado(boolean negado) {
		this.negado = negado;			
	}	
	
	public boolean getEjecutado() {		
		return ejecutado;
	}
	public void setEjecutado(boolean ejecutado) {
		this.ejecutado = ejecutado;			
	}
	
	public Object getResultado() {		
		return resultado;
	}
	public void setResultado(Object resultado) {
		this.resultado = resultado;			
	}
	
	public void setIndicador(String nombreIndicador) {
		this.indicador = nombreIndicador;
	}
	
	public String getIndicador() {
		return indicador;
	}
	
	
	
	private boolean operandoNegado(String operando) {
		boolean negado = false;
		if (operando.trim().startsWith("NOT")) {
			negado = true;
		}		
		return negado;
	}	
		
}