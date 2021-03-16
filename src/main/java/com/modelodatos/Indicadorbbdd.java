package com.modelodatos;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="indicador")
public class Indicadorbbdd {
	@Id
	private String id;
	
	private String name;
	private String fuente;
	private String comando;
	
	public Indicadorbbdd() {
		// TODO Auto-generated constructor stub
	}
	
	public Indicadorbbdd(String name, String fuente, String comando) {
		
		setName(name);
		setFuente(fuente);
		setComando(comando);
		// TODO Auto-generated constructor stub
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFuente() {
		return fuente;
	}
	public void setFuente(String fuente) {
		this.fuente = fuente;
	}
	public String getComando() {
		return comando;
	}
	public void setComando(String comando) {
		this.comando = comando;
	}
}
