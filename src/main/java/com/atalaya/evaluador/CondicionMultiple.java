package com.atalaya.evaluador;

import java.util.ArrayList;
// Clase para representar cada una de las condiciones compuestas que forman parte de una condición compleja
public class CondicionMultiple {
	private String tipo;				// Tipo de relación, AND u OR, entre las condiciones simples que forman la condición compuesta
	private boolean resultado;			// Resultado de la condición
	private Integer idCondicion;		// Identificador de la condición
	private Integer madre;				// Identificador de la condición de nivel inferior de la que forma parte la condición múltiple			
	private String texto;				// Texto que representa la condición
	private Integer nivel;				// Nivel de la condición. Las condiciones más simples tendrás los valores más de nivel.
	private Condicion condicion;		// Elementos de la condición. Si la condición es simple, tendrá contenido, si se trata de una condición compuesta, estará a null
	private boolean negacion;			// Indica que la condición es negada. En este caso, el resultado de la condición será el inverso del obtenido en su evaluación.
	private boolean evaluada;			// Flaga que indica si la condición se ha evaluado ya o no. Es un campo intero, no tiene métodos get y set
	
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public boolean getResultado() {
		return resultado;
	}
	public void setResultado(boolean resultado) {
		this.resultado = resultado;
	}
	public String getTexto() {
		return texto;
	}
	public void setTexto(String texto) {
		this.texto = texto;
	}	
	public Integer getMadre() {
		return madre;
	}
	public void setMadre(Integer madre) {
		this.madre = madre;
	}		
	public Integer getIdCondicion() {
		return idCondicion;
	}
	public void setIdCondicion(Integer idCondicion) {
		this.idCondicion = idCondicion;
	}		
	public Integer getNivel() {
		return nivel;
	}
	public void setNivel(Integer nivel) {
		this.nivel = nivel;
	}		
	public Condicion getCondicion() {
		return condicion;
	}
	public void setCondicion(Condicion condicion) {
		this.condicion = condicion;
	}	
	public boolean getNegacion() {
		return negacion;
	}
	public void setNegacion(boolean negacion) {
		this.negacion = negacion;
	}	
	public boolean getEvaluada() {
		return evaluada;
	}
	public void setEvaluada(boolean evaluada) {
		this.evaluada = evaluada;
	}		
	public CondicionMultiple () {
		this.madre = null;
		this.condicion = null;
		this.tipo = null;
		this.tipo = null;
		this.negacion = false;
	}		

}
