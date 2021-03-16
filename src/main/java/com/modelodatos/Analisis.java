package com.modelodatos;

import java.util.ArrayList;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="analisis")
public class Analisis {
	private String nombre;
	private String descripcion;

	private ArrayList<Indicador> indicadores;
	private ArrayList<Criterio> criterios;
	private ArrayList<Evento> eventos;
	private ArrayList<Configuracion> configuraciones;
	
	public Analisis(String nombre, String descripcion , ArrayList<Indicador> indicadores, ArrayList<Criterio> criterios, ArrayList<Evento> eventos, ArrayList<Configuracion> configuraciones) {
		super();
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.indicadores = indicadores;
		this.criterios = criterios;
		this.eventos = eventos;
		this.configuraciones = configuraciones;
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

	public ArrayList<Indicador> getIndicadores() {
		return indicadores;
	}

	public void setIndicadores(ArrayList<Indicador> indicadores) {
		this.indicadores = indicadores;
	}

	public ArrayList<Criterio> getCriterios() {
		return criterios;
	}

	public void setCriterios(ArrayList<Criterio> criterios) {
		this.criterios = criterios;
	}

	public ArrayList<Evento> getEventos() {
		return eventos;
	}

	public void setEventos(ArrayList<Evento> eventos) {
		this.eventos = eventos;
	}

	public ArrayList<Configuracion> getConfiguraciones() {
		return configuraciones;
	}

	public void setConfiguraciones(ArrayList<Configuracion> configuraciones) {
		this.configuraciones = configuraciones;
	}

	
	
}