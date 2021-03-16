package com.atalaya.evaluador;
//Clase para representar cada una de las condiciones simpres de una condicion compleja. 


public class Condicion {

	private Operando operando1;		// Primer operando de la condicion
	private Operando operando2;		// Segundo operando de la condicion
	private String operador;		// Operador de la condicion
	private boolean resultado;		// Resultado de la condicion
	private boolean negacion;		// Indica si la condicion es negada
	private boolean evaluada;		// Flaga que indica si la condicion se ha evaluado ya o no. Es un campo intero, no tiene métodos get y set
	
	public Operando getOperando1() {
		return operando1;
	}
	public void setOperando1(Operando operando1) {
		this.operando1 = operando1;	
	}
	
	public Operando getOperando2() {
		return operando2;
	}
	public void setOperando2(Operando operando2) {
		this.operando2 = operando2;			
	}
	
	public String getOperador() {
		return operador;
	}
	public void setOperador(String operador) {
		this.operador = operador;
	}
	
	public boolean getResultado() {
		return resultado;
	}
	public void setResultado(boolean resultado) {
		this.resultado = resultado;
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
	public void Condicion() {
		this.resultado = false;
		this.negacion = false;
	}
	
}