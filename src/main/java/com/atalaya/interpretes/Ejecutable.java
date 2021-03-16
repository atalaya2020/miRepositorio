package com.atalaya.interpretes;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import com.modelodatos.Analisis;
import com.modelodatos.Configuracion;
import com.modelodatos.Evento;
import com.modelodatos.Indicador;
import com.modelodatos.Parametro;

//import redis.clients.jedis.Jedis;

import org.slf4j.Logger;

/** 
 * Esta clase abstracta que define los atributos y metodos comunes de los ejecutables que componen un analisis (analisis,criterio,indicador y evento) 
 * Atributos del ejecutable. Son comunes a todos los ejecutables que pueden componer un analisis
 */
public abstract class Ejecutable {
	
	public final static String ESTADO_NOEJEUCTADO = "NOEJECUTADO";		//Estado inical del ciclo de vida de un ejecutable
	public final static String ESTADO_EJECUTANDO = "EJECUTANDO";		//Estado de paso del ciclo de vida de un ejecutable
	public final static String ESTADO_VALIDANDO = "VALIDANDO";
	public final static String ESTADO_EJECUTADO = "EJECUTADO";			//Estado final del ciclo de vida de un ejecutable
	
	public final static String FIN_OK = "FIN OK";								//Esta descripción indica una ejecución satisfactoria sin error ni parada forzada
	public final static String FIN_OK_SIN_RESULTADO = "FIN OK SIN RESULTADOS";	//Esta descripción indica una ejecución satisfactoria sin error ni parada forzada
	public final static String FIN_FORZADO = "FIN FORZADO";						//Esta descripción indica una ejecución satisfactoria sin error ni parada forzada
	public final static String FIN_KO_VOLCADO = "FIN KO VOLCADO";						//Esta descripción indica una ejecución satisfactoria sin error ni parada forzada
	
	public final static int minHilos = 2;
	public final static int minHilosAtalaya = 100;
	public final static int tiempo_max_def = 30000;
	public final static String VOLCADO_HTML = "HTML";
		
	private volatile String estado;								//Almacena el estado en el que se encuentra el analisis
	private volatile String descripcion_estado;					//Almacena la descripcion del estado en el que se encuentra el analisis
	private long crono;											//Almacena el momento en el que comienza la interpretacion un ejecutable

	protected static int numHilosAtalaya = minHilosAtalaya;		//Define el numero de hilos definido para Atalaya
	int numHilos = minHilos;
	protected int tiempo_max = tiempo_max_def;					//Define el tiempo maximo de ejecucion de un ejecutable
	
	protected static Logger log;								//log
	private static long count = 0;								//Almacena un contador de instancias para la generacion del codigo hash del objeto
	private long hashCode;										//Almacena el codigo hash creado para la instancia del objeto

	protected static Thread[] hilos;												//Almacena los hilos lanzados por los analisis (se trata de un atributo de clase)
	protected static Hashtable<Long,Hashtable<String,IndicadorProxy>> indicadores;	//Almacena los indicadores de un alias, los ejecutables manejan la informacion obtenida en los indicadores
	private Vector<Object[]> resultadoEjecucion;									//Almacena el resultado
	
	protected static Properties confFuentes = null;
	//private static Jedis redis = null;
	

	public int hashCode()
	{
		count++;
		int hash = (count + this.getClass().getSimpleName()).hashCode(); 
		return  hash;
	}
	
	public Hashtable<Long,Hashtable<String,IndicadorProxy>> getIndicadores() {
		if (indicadores == null ) {
			indicadores = new Hashtable<Long,Hashtable<String,IndicadorProxy>>();
		}
		return indicadores;
	}
	
	public synchronized String getEstado() {
		return this.estado;
	}

	public synchronized void setEstado(String estado) {
		this.estado = estado;
	}
	
	public boolean noejecutado() {
		boolean noejecutado = false;
		if (this.getEstado().equals(ESTADO_NOEJEUCTADO))
			noejecutado=true;
		
		return noejecutado;
	}
	
	public boolean ejecutado() {
		boolean ejecutado = false;
		if (this.getEstado().equals(ESTADO_EJECUTADO))
			ejecutado=true;
		
		return ejecutado;
	}
	
	public boolean ejecutando() {
		boolean ejecutando = false;
		if (this.getEstado().equals(ESTADO_EJECUTANDO))
			ejecutando=true;
		
		return ejecutando;
	}
	
	public synchronized String getDescripcionEstado() {
		
		if (this.descripcion_estado!=null)
			return this.descripcion_estado;
		else
			return "SIN INFORMAR";
	}

	public synchronized void setDescripcionEstado(String desc_estado) {
		this.descripcion_estado = desc_estado;
	}
	
	public long getCrono() {
		return crono;
	}

	public void setCrono(long crono) {
		this.crono = crono;
	}
	
	public long getHashCode() {
		return hashCode;
	}
	
	public void setHashCode(long hash) {
		this.hashCode = hash;
	}
	
	public Vector<Object[]> getResultadoEjecucion() {
		return resultadoEjecucion;
	}

	public void setResultadoEjecucion(Vector<Object[]> resultadoEjecucion) {
		
		this.resultadoEjecucion = resultadoEjecucion;
	}
	
	public void setIndicadores(Hashtable<Long,Hashtable<String,IndicadorProxy>> indicadores) {
		Ejecutable.indicadores = indicadores;
	}

	public static Properties getConfFuentes() {
		return confFuentes;
	}

	public static void setConfFuentes(Properties confFuentes) {
		Ejecutable.confFuentes = confFuentes;
	}
	
//	public static Jedis getRedis() {
//		return redis;
//	}
//
//	public static void setRedis() {
//		
//		if (redis==null)
//			redis = new Jedis("localhost",6379);
//	}

	
	
	
	public abstract boolean ejecutar();

	public void detener()
	{
		this.setEstado(ESTADO_EJECUTADO);
		this.setDescripcionEstado(FIN_FORZADO);
		
		try {
			this.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public void obtenerConfiguracion (Object entradaclase, String nombreConf){

		ArrayList<Configuracion> configuraciones = new ArrayList<>(0);
		
		if(entradaclase instanceof Analisis){ //OBTENER CONFIGURACIONES DE ANALISIS
			Analisis entrada = (Analisis)entradaclase;
			configuraciones = entrada.getConfiguraciones();
		}
//		else if (entradaclase instanceof Indicador){ //OBTENER CONFIGURACIONES DE INDICADOR
//			Indicador entrada = (Indicador)entradaclase;
//			configuraciones = entrada.getConfiguraciones();
//		}else if (entradaclase instanceof Evento){ //OBTENER CONFIGURACIONES DE EVENTO
//			Evento entrada = (Evento)entradaclase;
//			configuraciones = entrada.getConfiguraciones();
//		}
		//Si no existe una configuracion se establece por defecto
		if(configuraciones == null || configuraciones.size() == 0){
			log.info("ERROR al cargar configuracion de "+nombreConf+". No se tiene informacion de configuracion");
		}else{
			int i=0;
			while(i<configuraciones.size() && configuraciones.get(i).getNombre() != nombreConf){ //Avanzo configuraciones hasta dar con la  que quiero
				i++;
			}
			if(i == configuraciones.size()){ // No se ha encontrado la configuracion con ese nombre
				log.info("No se ha encontrado configuracion para "+nombreConf);
			}else{
				String nombreConfiguracion = 	  configuraciones.get(i).getNombre();
				//String descripcionConfiguracion = configuraciones.get(i).getDescripcion();
				Parametro parametro = configuraciones.get(i).getParametros().get(0);

				switch (nombreConfiguracion) {
					//CONFIGURACION DE THREADS
					case "Threads":
						if(parametro.getNombre() == "NumeroHilos"){ 
							try {
								int hilos = Integer.parseInt(parametro.getValor());
								if(hilos <= minHilos){ // Si el numero de hilos es menor que el minimo se deja por defecto 
									log.info(entradaclase.getClass().getName() +" EN MODO SECUENCIAL...");
								}else{
									numHilos = hilos;
									log.info(entradaclase.getClass().getName() + " EN MODO MULTIHILO CON  " + numHilos + "  HILOS EN DISPOSICION....");
								}
							} catch (Exception e) {
								log.info("WARNING Valor de hilos no valido. Se deja por defecto");
							}
						}else{
							log.info("ERROR parametro de "+nombreConf+" no valido");
						}	
						break;
					//CONFIGURACION DE TIEMPOS
					case "Tiempos":
						if(parametro.getNombre() == "TiempoEjecucion"){
							try {
								int tiempo = Integer.parseInt(parametro.getValor());
								if(tiempo > 0){
									tiempo_max = tiempo;
									log.info("Definido tiempo maximo para ejecutar analisis en " + tiempo_max);
								}else{
									log.info("WARNING Tiempo de ejecucion no válido. Se deja por defecto");
								}
								
							} catch (Exception e) {
								log.info("WARNING Tiempo de ejecucion no válido. Se deja por defecto");
							}
						}else{
							log.info("ERROR parametro de "+nombreConf+" no valido");
						} 	
						break;

					default:
						log.info("ERROR parametro de "+nombreConf+" no valido");
				}//Se podrian añadir mas casos de configuracion
			}
		}
	}
	
	public String volcadoResultado (String modo)
	{
		StringBuffer volcado = new StringBuffer();
		
		if (this.getResultadoEjecucion()!=null && this.getResultadoEjecucion().size()>0)
		{
			if (modo.equals("html"))
				volcado.append("</br>");
			
			Object[] linea = null;
			for (int i=0;i<this.getResultadoEjecucion().size();i++)
			{
				linea = this.getResultadoEjecucion().get(i);
				
				if (linea!=null && linea.length>0)
				{
					if (modo.equals("html"))
						volcado.append("<br>");
					
					for (int j=0; j<linea.length;j++)
					{
						volcado.append(linea[j]);
						volcado.append("|");
					}
					
					if (modo.equals("html"))
						volcado.append("</br>");
					else
						volcado.append("\n");
				}
			}
		}
		//else 
		//	volcado = volcado.append("Sin Resultados\n");
		
		log.info("Volcado:");
		log.info(volcado.toString());
		
		return volcado.toString();
	}
	
	public synchronized int infoHilos(String opcion)
	{
		int nThreadsOcupados = 0;
		int nPosicionThreadLibre = 0;
		
		if (Ejecutable.hilos!=null && Ejecutable.hilos.length>0)
		{
			nThreadsOcupados = 0;
			for(int j=0; j<Ejecutable.hilos.length;j++)
			{
				if (Ejecutable.hilos[j]!=null && Ejecutable.hilos[j].isAlive())
				{
					nThreadsOcupados++;
				}
				else
					nPosicionThreadLibre = j;
			}
		}
		else
			nThreadsOcupados = 0;
		
		if (opcion==null || opcion.equals("posicionlibre"))
			return nPosicionThreadLibre;
		else
			return nThreadsOcupados;
	}
	
	public synchronized boolean nuevoHilo (Runnable ejecutable)
	{
		boolean nuevoHilo = false;
		
		//Obtenemos los hilos libres
		int nThreadsOcupados = 0;
		
		nThreadsOcupados = infoHilos("ocupados");
		
		//Si existen hilos libres 
		if (numHilos - nThreadsOcupados>0)
		{
			Thread th = new Thread(ejecutable);
			Ejecutable.hilos[infoHilos("posicionlibre")] = th;
			log.info("Arranco nuevo hilo. Quedan:"+(numHilos-nThreadsOcupados));
			th.start();
			
			nuevoHilo = true;
		}
		
		return nuevoHilo;
	}
	
	public IndicadorProxy getIndicadorNombre(long analisis, String nombre) {
		IndicadorProxy indProxy = null;
		
		if (indicadores!=null && indicadores.size()>0)
		{	
			Hashtable<String,IndicadorProxy> lista =indicadores.get(analisis);
			if (lista!=null && lista.size()>0)	
				indProxy = lista.get(nombre);
		}
		
		return indProxy;
	}
}
