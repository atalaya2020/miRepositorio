package com.atalaya.interpretes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import com.modelodatos.Analisis;
import com.modelodatos.Parametro;

/** 
 * Esta clase representa a un componente Analisis ejecutable, contiene la definicion del analisis y los atributos y metodos necesarios para 
 * su interpretacion. 
 */
public class AnalisisProxy extends Ejecutable implements Runnable {

	private Analisis analisis;									//Almacena la definicion del analisis recuperado de bbdd
	
	private ArrayList<CriterioProxy> criterios;					//Almacena los criterios de un analisis
	private ArrayList<IndicadorProxy> eventos;					//Almacena los eventos de un analisis
	private ArrayList<Parametro> parametros;					//Almacena los parametros a utilizar para interpretar un analisis

	String cabeceralog;
	
	public ArrayList<CriterioProxy> getCriterios() {
		return criterios;
	}
	
	public void setCriterios(ArrayList<CriterioProxy> criterios) {
		this.criterios = criterios;
	}	
	
	public ArrayList<IndicadorProxy> getEventos() {
		return eventos;
	}
	
	public void setEventos(ArrayList<IndicadorProxy> eventos) {
		this.eventos = eventos;
	}	
	
	public ArrayList<Parametro> getParametros() {
		return parametros;
	}
	public void setParametros(ArrayList<Parametro> parametros) {
		this.parametros = parametros;
	}
	
	public Analisis getAnalisis() {
		return analisis;
	}
	
	public AnalisisProxy (Analisis analisis, ArrayList<Parametro> parametros) 
	{
		setHashCode(hashCode());
		
		log = LoggerFactory.getLogger(AnalisisProxy.class);
		
		this.parametros = parametros;
		this.analisis = analisis;
		
		setEstado(ESTADO_NOEJEUCTADO);
		
		cargarAnalisisProxy(analisis);
		cabeceralog = "Analisis "+ this.getAnalisis().getNombre() + "|" + this.getHashCode() + ":";
		log.info(cabeceralog + "Cargando configuracion");
		obtenerConfiguracion(analisis, "Threads");
		obtenerConfiguracion(analisis, "Tiempos");
		
		hilos = new Thread[numHilos];
	}
	
	public AnalisisProxy (Analisis analisis, ArrayList<Parametro> parametros, Properties confFuentes) 
	{
		setConfFuentes(confFuentes);
		//setRedis();
		setHashCode(hashCode());
		
		log = LoggerFactory.getLogger(AnalisisProxy.class);
		
		this.parametros = parametros;
		this.analisis = analisis;
		
		setEstado(ESTADO_NOEJEUCTADO);
		
		cargarAnalisisProxy(analisis);
		cabeceralog = "Analisis "+ this.getAnalisis().getNombre() + "|" + this.getHashCode() + ":";
		log.info(cabeceralog + "Cargando configuracion");
		obtenerConfiguracion(analisis, "Threads");
		obtenerConfiguracion(analisis, "Tiempos");
		
		hilos = new Thread[numHilos];
	}
	
/*	public AnalisisProxy (Analisis analisis, ArrayList<Parametro> parametros, Properties confFuentes, RedisRepository redis) 
	{
		setConfFuentes(confFuentes);
		setRedis(redis);

		setHashCode(hashCode());
		
		log = LoggerFactory.getLogger(AnalisisProxy.class);
		
		this.parametros = parametros;
		this.analisis = analisis;
		
		setEstado(ESTADO_NOEJEUCTADO);
		
		cargarAnalisisProxy(analisis);
		cabeceralog = "Analisis "+ this.getAnalisis().getNombre() + "|" + this.getHashCode() + ":";
		log.info(cabeceralog + "Cargando configuracion");
		obtenerConfiguracion(analisis, "Threads");
		obtenerConfiguracion(analisis, "Tiempos");
		
		hilos = new Thread[numHilos];
	}*/
	
	//Este metodo tendria mas sentido si le metemos la validacion de cada uno de los elementos del analisis
	private void cargarAnalisisProxy(Analisis analisis) {
	Hashtable<String,IndicadorProxy> indicadoresAnalisis;
		
		this.criterios = new ArrayList<CriterioProxy>();	
		for (int i = 0; i < analisis.getCriterios().size(); i++) {
			CriterioProxy criProxy = new CriterioProxy(analisis.getCriterios().get(i));
			criProxy.setAnalisis(this.getHashCode());
			this.criterios.add(i, criProxy);
		}
		
//		setIndicadores(new Hashtable<String,IndicadorProxy>());
		indicadoresAnalisis = new Hashtable<String,IndicadorProxy>();
		for (int i = 0; i < analisis.getIndicadores().size(); i++) {
			IndicadorProxy indProxy = new IndicadorProxy(analisis.getIndicadores().get(i));
			indProxy.setAnalisis(this.getHashCode());
			
			//Recorro los parametros del indicador para identificar aquellos que hacen referencia a un parametro de entrada #PARAM.NOMBRE_PARAMETRO
			ArrayList<Parametro> parametros = indProxy.getIndicador().getParametros();
			for(int j=0;j<parametros.size();j++)
			{
				//Si el parametro es del tipo #PARAM debo darle el valor recibido como parametro
				if (parametros.get(j).getValor().startsWith("#PARAM"))
				{
					String[] arValor = parametros.get(j).getValor().split("\\.");
					if (this.parametros!=null)
					{
						for (int k=0;k<this.parametros.size();k++)
						{
							if (this.parametros.get(k).getNombre().equals(arValor[1]))
								parametros.get(j).setValor(this.parametros.get(k).getValor());
						}
					}
				}
			}
			indicadoresAnalisis.put(indProxy.getIndicador().getNombre(), indProxy);
			
//			super.getIndicadores().put(analisis.getIndicadores().get(i).getNombre(), indProxy);
		}
		
		super.getIndicadores().put(this.getHashCode(), indicadoresAnalisis);
		
		this.eventos = new ArrayList<IndicadorProxy>();
		for (int i = 0; i < analisis.getEventos().size(); i++) 
		{
			IndicadorProxy eveProxy = new IndicadorProxy(analisis.getEventos().get(i));
			eveProxy.setAnalisis(this.getHashCode());
			this.eventos.add(i, eveProxy);
		}
		
	}
	
	public void run()
	{
		ejecutar();
	}
	
	public boolean ejecutar() {
		
		if (this.noejecutado())
		{
			this.setEstado(ESTADO_EJECUTANDO);
			setCrono(Calendar.getInstance().getTimeInMillis());
			log.info(cabeceralog + "Ejecutando analizador...");
			log.info(cabeceralog + this.getAnalisis().getDescripcion());
			
			//Bucle para el control del ciclo de vida del hilo
			while(!this.ejecutado())
			{	
				long ahora = Calendar.getInstance().getTimeInMillis();
				
				if ((ahora - this.getCrono()) > this.tiempo_max) //TAREA añadir nuevo parametro de ejecucion a nivel de indicador TIEMPO MAX DE EJECUCION
				{
					this.detener();
					log.info(cabeceralog+"Parado el analisis:" +this.getAnalisis().getNombre()+ " por sobrepasar el tiempo maximo de ejecucion "+ this.tiempo_max);
				}
				
				//Validar si se han superado los tiempos maximos de espera en los indicadores
				Enumeration<String> enumIndicadores = this.getIndicadores().get(this.getHashCode()).keys();
//				Enumeration<String> enumIndicadores = this.getIndicadores().keys();
				
				//Recorremos todos los indicadores lanzados para este Analisis
				while(enumIndicadores.hasMoreElements())
				{
					String nombreIndicador = (String)enumIndicadores.nextElement();
					//Paramos aquellos indicadores que han superado el tiempo maximo de ejecucion
//					if (this.getIndicadores().get(nombreIndicador).ejecutando())
					IndicadorProxy indicador = super.getIndicadorNombre(this.getHashCode(), nombreIndicador);
					if (indicador.ejecutando())
					{
//						if ((ahora - this.getIndicadores().get(nombreIndicador).getCrono()) > tiempo_max) //TAREA añadir nuevo parametro de ejecucion a nivel de indicador TIEMPO MAX DE EJECUCION
						int tiempo_max_indicador = indicador.tiempo_max;
						if (indicador.tiempo_max==Ejecutable.tiempo_max_def)
							tiempo_max_indicador = this.tiempo_max;
						if ((ahora - indicador.getCrono()) > tiempo_max_indicador) //TAREA añadir nuevo parametro de ejecucion a nivel de indicador TIEMPO MAX DE EJECUCION
						{
							//Forzamos su parada por haber superado el tiempo maximo de ejeucion
//							this.getIndicadores().get(nombreIndicador).detener();
							indicador.detener();
							log.info(cabeceralog+"Parado el indicador:" +nombreIndicador+ " por sobrepasar el tiempo maximo de ejecucion "+ tiempo_max_indicador);
						}
					}
				}
					
				int numCriEjecutados = 0;
				int numCriEjecutadosOk = 0;
				int numCriEjecutadosFinForzado = 0;

				//Para interpretar un analisis el punto de partida son los criterios donde se definen las condiciones de evalucion para finalmente lanzar o no los eventos correspondientes
				//Recorro los criterios para ejecutarlos				
				for (int c = 0; c < analisis.getCriterios().size(); c++) 
				{	
					CriterioProxy criterio = this.getCriterios().get(c);
					
					//Recorro todos los criterios para validar si todos han sido ejecutados
					if (criterio.ejecutado())
						numCriEjecutados++;
					if (criterio.getDescripcionEstado()==FIN_OK)
					{
						numCriEjecutadosOk++;
						this.lanzarEventos(criterio);
					}
					else if (criterio.getDescripcionEstado()==FIN_FORZADO)
						numCriEjecutadosFinForzado++;
					
					//Si se han ejecutado todos los criterios damos por ejecutado el analisis
					if (numCriEjecutados==this.getCriterios().size())
					{
						this.setEstado(ESTADO_EJECUTADO);
						if (numCriEjecutadosOk==this.getCriterios().size())
							this.setDescripcionEstado(FIN_OK);
						else if (numCriEjecutadosFinForzado>0)
							this.setDescripcionEstado(FIN_FORZADO);
						else
							this.setDescripcionEstado(FIN_OK_SIN_RESULTADO);
						
						break;
					}
					
					//Paramos aquellos criterios que han superado el tiempo maximo de ejecucion
					
					int tiempo_max_criterio = criterio.tiempo_max;
					if (criterio.tiempo_max==Ejecutable.tiempo_max_def)
						tiempo_max_criterio = this.tiempo_max;
					if ((ahora - criterio.getCrono()) > tiempo_max_criterio) //TAREA añadir nuevo parametro de ejecucion a nivel de indicador TIEMPO MAX DE EJECUCION
					{
						//Forzamos su parada por haber superado el tiempo maximo de ejeucion
						if (criterio.ejecutando())
						{
							criterio.detener();
							log.info(cabeceralog+"Parado el criterio:" +criterio.getCriterio().getNombre()+ " por sobrepasar el tiempo maximo de ejecucion "+ tiempo_max_criterio);
							continue;
						}
					}
					
					if (criterio.noejecutado())
					{
						//Ejecuto el criterio
						if (numHilos<=minHilos)
							criterio.ejecutar();
						else
							super.nuevoHilo(criterio);
					}
				}
			
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
			
			log.info(cabeceralog + "Finalizada la ejeucion del analisis.");
			// Se borran de la lista estática de indicadores de Ejecutable los indicadores de este análisis
			Enumeration<String> enumIndicadores = this.getIndicadores().get(this.getHashCode()).keys();
			while(enumIndicadores.hasMoreElements()) {
				String nombreIndicador = (String)enumIndicadores.nextElement();
				// falta comprobar en la configuración si el indicador es persisntente o no para decidir si se borra o no;
				super.getIndicadores().get(this.getHashCode()).remove(nombreIndicador);
				// Si la lista de indicadores del análisis está vacía (se han borrado todos sus indicadores), se borra el análisis de la lista de la clase Ejecutable
				if (super.getIndicadores().get(this.getHashCode()).isEmpty()) {
					super.getIndicadores().remove(this.getHashCode());
				}
			}			
		}
		
		return true;
	}
	
	public String volcadoResultado(String modo)
	{
		String volcado = "";
		
		volcado = volcado + "Nombre: " + this.getAnalisis().getNombre() + "\n";
		volcado = volcado + "Estado: " + this.getEstado() + "\n";
		volcado = volcado + "Descripcion del estado: " + this.getDescripcionEstado() + "\n";
		volcado = volcado + super.volcadoResultado(modo);
		log.info("Volcado:");
		log.info(volcado);
		return volcado;
	}
	
	public IndicadorProxy lanzarEventos(CriterioProxy criProxy)
	{
		IndicadorProxy evento = null;
		
		//Recorro la lista de eventos asociados al cumplimento de un criterio
		for (int le = 0; le < criProxy.getCriterio().getEventos().length; le++) 
		{
			String nombreEvento = criProxy.getCriterio().getEventos()[le];
			
			//Recorro la lista de eventos para encontrar el nombre de evento
			for(int e = 0; e < this.getEventos().size(); e++)
			{
				if (this.getEventos().get(e).getIndicador().getNombre().equalsIgnoreCase(nombreEvento))
				{	
					evento = this.getEventos().get(e);
					evento.parametrosIndicador();
					evento.ejecutar();
				}
			}
		}	
		return evento;
	}
	
}
