package com.atalaya.interpretes;

import java.util.ArrayList;
import java.util.Vector;

import com.atalaya.evaluador.Comunes;
import com.modelodatos.Indicador;

public class IndicadorProxyTypeBucle extends IndicadorProxyType implements IIndicadorProxyType  {
	
	private static IndicadorProxyTypeBucle instance;
	
	/** 
	 * Constructor.
	 */
	protected IndicadorProxyTypeBucle(){}
	
	/** 
	 * Este metodo asegura que exista únicamente una instancia de esta clase. Devuelve el valor del atributo instance en caso de tener valor y si no es
	 * así genera una instancia de la clase y la devuelve como resultado.
	 * @return AliasTypeLoop Instancia única de la clase AliasTypeLoop
	 */
	public static IndicadorProxyTypeBucle getInstance()
	{
		//metemos una linea nueva aquí
		//Si el atributo instance no tiene valor significa que no existe ninguna instancia de la clase
		if (instance==null)
		{
			//Da valor al atributo instance, creando una instancia de la clase
			instance = new IndicadorProxyTypeBucle();
		}
		//Devuelve la instancia unica de la clase AliasTypeLoop
		return instance;
	}
	
	public boolean ejecutar(IndicadorProxy indicador) {
		
		boolean exec = false;
		
		String comando = indicador.getIndicador().getComando();
		String[] arComando = comando.split(" ");
		
		try
		{			
			//Si el bucle es del tipo Bucle, debo recorrer todos los elementos del indicador A 
			if (arComando[0].equals(IIndicadorProxyType.comando_bucle))
			{
				while (!indicador.ejecutado())
				{
					indicador.listaIndicadoresHijos = new ArrayList<IndicadorProxy>();
					
					//Recupero las caracterisiticas del indicador A					
//					IndicadorProxy indicador_a = indicador.getIndicadores().get(arComando[1].substring(1));
					IndicadorProxy indicador_a = indicador.getIndicadorNombre(indicador.getAnalisis(), arComando[1].substring(1));
					//Ejecuto el alias_A
					indicador_a.ejecutar();
					
					//Valido el resultado de ejecutar el indicador A
					//Si hay resultados que recorrer
					if (indicador_a.ejecutado() && 
						indicador_a.getResultadoEjecucion()!=null &&
						indicador_a.getResultadoEjecucion().size()>0) 
					{
						//Recupero las caracterisiticas del indicador B
//						IndicadorProxy indicador_b = indicador.getIndicadores().get(arComando[3].substring(1));
						IndicadorProxy indicador_b = indicador.getIndicadorNombre(indicador.getAnalisis(), arComando[3].substring(1));						
						//Recupero el resultado del indicador A
						Vector<Object[]> resultado_indicador_a = (Vector<Object[]>)indicador_a.getResultadoEjecucion();
						
						int numAuto = 0;
						IndicadorProxy indicador_auto = null;
						
						//Recuperamos el indicador y lo clonamos para utilizarlo como semilla en el bucle
						Indicador ind_b = super.copiaIndicador(indicador_b.getIndicador());
								
						while (numAuto<resultado_indicador_a.size())
						{
							Indicador copia_ind_b = super.copiaIndicador(ind_b);
								
							//creo un indicador hijo con las mismas caracteristicas que el padre
							indicador_auto = new IndicadorProxy(copia_ind_b,true,indicador_a.getIndicador().getNombre());
							indicador_auto.setAnalisis(indicador.getAnalisis());
							//Relacionamos el indicador hijo (su padre es el indicador_b) con el elemento correspondiente del indicador_a 
							indicador_auto.setIndice(numAuto);
							
							//Damos valor a los parametros del indicador_b, que estaran basados en los elementos del indicador_a
							for (int p = 0; p < indicador_auto.getIndicador().getParametros().size(); p++) 
							{
								if (indicador_auto.getIndicador().getParametros().get(p).getValor().startsWith(Comunes.tpMarcaIndicador)) {
									
									String[] tramos = new String [] {};
									tramos = indicador_auto.getIndicador().getParametros().get(p).getValor().split("\\.");
									String nombreIndicador = tramos[0].substring(1);
									
									if (nombreIndicador.equals("CURSOR")) 
									{
										String columna = tramos[1];
										Object valParam = new Object();
										Object[] linea = indicador_a.getResultadoEjecucion().elementAt(numAuto);
										int c = 0;
										while (c < indicador_a.getIndicador().getResultado().length) {
											if (columna.equals(indicador_a.getIndicador().getResultado()[c])) {
												valParam = linea[c];
												break;
											}
											c++;
										} 
										indicador_auto.getIndicador().getParametros().get(p).setValor(valParam.toString());
									}
								}
								//AGUJERO ESTO DEBER EVITARSE EN LA FASE DE CHEQUEO DEL ANALISIS Y EVITAR INDICADORES MAL FORMADOS
							}
							
							indicador.listaIndicadoresHijos.add(indicador_auto);
							numAuto++;
							
							
							//Ejecuto el indicador
							/*if (Ejecutable.numHilos<=Ejecutable.minHilos)
							{
								indicador_auto.ejecutar();
							}*/								
						}
						
						//Una vez copiados e intentado lanzar todos los indicadores_hijos, debemos esperar a que todos los indicadores hijos hayan 
						//sido lanzados y ejecutados o llegue una señal de abandono
						boolean hayHijosSinEjecutar = true;
						boolean hayHijosSinTerminar = true;
						while((hayHijosSinEjecutar || hayHijosSinTerminar) && !indicador.ejecutado())
						{
							if (indicador.listaIndicadoresHijos!=null && indicador.listaIndicadoresHijos.size()>0)
							{
								hayHijosSinEjecutar=false;
								hayHijosSinTerminar=false;
								for(int i=0; i<indicador.listaIndicadoresHijos.size();i++)
								{
									IndicadorProxy indHijo = indicador.listaIndicadoresHijos.get(i);
									
									if (indHijo.noejecutado())
									{
										//linea nueva aqui
										//Ejecuto el indicador
//										if (Ejecutable.numHilos<=Ejecutable.minHilos)
										if (Ejecutable.minHilos<=Ejecutable.minHilos)
										{
											indHijo.ejecutar();
										}
										else
										{
											if (!indicador.nuevoHilo(indHijo))
											{
												hayHijosSinEjecutar=true;
												Thread.sleep(300);
											}
										}
									}
									
									if (!indHijo.ejecutado())
										hayHijosSinTerminar=true;
									Thread.sleep(100);
								}
							}
						}
						
						if (indicador.ejecutado())
						{
							indicador.setEstado(Ejecutable.ESTADO_EJECUTADO);
							indicador.setDescripcionEstado(Ejecutable.FIN_FORZADO);
						}
						else
						{
							//AGUJERO SI NO HAY CASOS HAY QUE CONTROLARLO Y FINALIZAR EL INDICADOR
							indicador.setResultadoEjecucion(indicador_a.getResultadoEjecucion());
							indicador.setEstado(Ejecutable.ESTADO_EJECUTADO);
							indicador.setDescripcionEstado(Ejecutable.FIN_OK);
						}
						
						exec=true;											
					}
				}
			}			
		}
		catch (Exception e)
		{
			indicador.setEstado(Ejecutable.ESTADO_EJECUTADO);
		}
		
		return exec;		
	}

}
