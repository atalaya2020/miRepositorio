package com.atalaya.interpretes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.atalaya.evaluador.Comunes;
import com.modelodatos.Indicador;


public class IndicadorProxy extends Ejecutable implements Runnable  {
	
	private Indicador indicador;
	
	private int indice; 							//Exclusivo de indicadores bucle, define con que posicion del indicador padre está relacionado
	private volatile int countEjecutado;			//Exclusivo de indicadores bucle, define cuantos de los indicadores bucle pasado a estado EJECUTADO 
	protected ArrayList<IndicadorProxy> listaIndicadoresHijos = null; //Exclusivo de indicadores bucle, almacena el listado de indicadores hijos
	
	private boolean autoGenerado = false;
	private String nombreIndicadorPadre = null; //nombre del indicador donde queremos alamcenar el resultado de este indicador, solo utilizado por los indicadores autogenerados
	private long analisis; 
	
	public final String TIPO_QUERY = "Query"; 	//Este tipo de indicador define una query con bind variables que pueden tomar valores fijos o dinamicos
	public final String TIPO_BUCLE = "Bucle";	//Este tipo de alias define una relacion entre alias tipo para cada elemento del alias "a" aplica el alias "b" 
	public final String TIPO_WS = "Ws"; 		//Este tipo de indicador define un recurso tipo WebService
	
	public void setAnalisis(long analisis) {
		this.analisis = analisis;
	}
	
	public long getAnalisis() {
		return this.analisis;
	}	
	
	String cabeceralog;

	public IndicadorProxy(Indicador ind)
	{
		setEstado(ESTADO_NOEJEUCTADO);

		countEjecutado = 0;
		indicador = ind;
		
		setHashCode(hashCode());
		
		
			
		
		cabeceralog = "Indicador " + ind.getNombre() + "|" + this.getHashCode() + ":";
	}
	
	//Este constructor crea objetos IndicadorProxy no almacenados en el atributo de clase indicadoresProxy, no son visibles por el resto de indicadores
	public IndicadorProxy(Indicador ind, boolean autoGenerado, String nombreIndicadorPadre)
	{
		setEstado(ESTADO_NOEJEUCTADO);
		
		indice = 0;
		indicador = ind;
		
		this.autoGenerado = autoGenerado;
		this.nombreIndicadorPadre = nombreIndicadorPadre;

		setHashCode(hashCode());
		cabeceralog = "Indicador auto " + ind.getNombre() + "|" + this.getHashCode() + ":";
	}
	
	public Indicador getIndicador() {
		return indicador;
	}

	public void setIndicador(Indicador indicador) {
		this.indicador = indicador;
	}
		
	public void setIndice(int indice) {
		this.indice = indice;
	}
	
	public int getIndice() {
		return this.indice;
	}
	
	public boolean isAutoGenerado() {
		return autoGenerado;
	}

	public void setAutoGenerado(boolean autoGenerado) {
		this.autoGenerado = autoGenerado;
	}
	
	public String getNombreIndicadorPadre() {
		return nombreIndicadorPadre;
	}

	public void setNombreIndicadorPadre(String nombreIndicadorPadre) {
		this.nombreIndicadorPadre = nombreIndicadorPadre;
	}
	
	public synchronized int getCountEjecutado() {
		return this.countEjecutado;
	}

	public synchronized void setCountEjecutado() {
		this.countEjecutado++;
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
			log.info(cabeceralog + "Ejecutando indicador...");
			log.info(cabeceralog + this.getIndicador().getDescripcion());
		
			//Mientras no haya finalizado su ejecucion debo controlar su interrupcion
			while (!this.ejecutado())
			{
				if (this.getIndicador().getTipo().equals(IIndicadorProxyType.tipo_bucle)
					|| this.getIndicador().getTipo().equals(IIndicadorProxyType.tipo_ws)
					|| this.getIndicador().getTipo().equals(IIndicadorProxyType.tipo_fichero_Reader)
					|| this.getIndicador().getTipo().equals(IIndicadorProxyType.tipo_fichero_Writer)
					|| this.getIndicador().getTipo().equals(IIndicadorProxyType.tipo_query))

				{
					IIndicadorProxyType indicadorProxyType = null;
					
					
					if (this.getIndicador().getTipo().equals(IIndicadorProxyType.tipo_fichero_Reader) &&
						this.getIndicador().getDestino().equals("Memoria")){
						log.info("Usamos la información y la tratamos en memoria");
					}else if (this.getIndicador().getTipo().equals(IIndicadorProxyType.tipo_fichero_Reader) &&
						this.getIndicador().getDestino().equals("Directa")) {
						log.info("Tratamos la informacion directamente linea a linea tal y como la recogemos");
					}
					
					try 
					{
						indicadorProxyType = IndicadorProxyType.getInstanceByAliasType(this.getIndicador().getTipo());
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					try 
					{
						indicadorProxyType.ejecutar(this);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				else if (this.getIndicador().getTipo().equals(IIndicadorProxyType.tipo_volcado))
				{
					String comando = this.getIndicador().getComando();
					String[] palabras = comando.split(" ");
					
					String nombreInd = palabras[1].substring(1);
					IndicadorProxy ind = super.getIndicadorNombre(this.getAnalisis(), nombreInd);					
//					IndicadorProxy ind = indicadores.get(this.getHashCode());	
					
					ind.volcadoResultado("trazas");
					this.setEstado(ESTADO_EJECUTADO);
					this.setDescripcionEstado(FIN_OK);
				}
				/*
				else if (this.getIndicador().getTipo().equals(IIndicadorProxyType.tipo_query))
				{
					ResultSet rs = null;
			
					if (this.indicador.getTipo().equalsIgnoreCase("Query")) 
					{
						Connection conexion = null;
						
						try {
			
							conexion = DriverManager.getConnection(
									"jdbc:mysql://localhost:3306/alumnadodb?useServerPrepStmts=true&useSSL=false&allowPublicKeyRetrieval=true",
//									"jdbc:mysql://alumnadodb:3306/alumnadodb?useServerPrepStmts=true&useSSL=false&allowPublicKeyRetrieval=true",
									"root", "atalaya"); 
			 
							PreparedStatement pstmt = conexion.prepareStatement(this.indicador.getComando());
							for(int i = 0; i< this.indicador.getParametros().size(); i++) {
								if(this.indicador.getParametros().get(i).getTipo().equalsIgnoreCase("String")) {
									pstmt.setString(i+1, this.indicador.getParametros().get(i).getValor());
								}
								else if(this.indicador.getParametros().get(i).getTipo().equalsIgnoreCase("Entero")) {
									pstmt.setInt(i+1,Integer.parseInt(this.indicador.getParametros().get(i).getValor()));
								}
								else if(this.indicador.getParametros().get(i).getTipo().equalsIgnoreCase("BigDecimal")) {
									pstmt.setBigDecimal(i+1,new BigDecimal(this.indicador.getParametros().get(i).getValor()));
								}
								else if(this.indicador.getParametros().get(i).getTipo().equalsIgnoreCase("Date")) {						
									pstmt.setString(i+1, this.indicador.getParametros().get(i).getValor());
								}
								else {
									System.out.println("Introduce un tipo válido");
								}
							}
			
							rs = pstmt.executeQuery();
			
							ResultSetMetaData metadata=null;
			
							try
							{
								metadata = rs.getMetaData();
			
			
							} catch (SQLException e)
							{
								return false;
							}
			
							int num_columnas_extraccion;
			
							boolean vacio = false;
			
							int num_columnas = metadata.getColumnCount();
			
							if (this.indicador.getResultado() == null ||  this.indicador.getResultado().length == 0)
							{
								num_columnas_extraccion = num_columnas;
								this.indicador.setResultado(new String[num_columnas]);
								vacio = true;
							}
							else
							{
								num_columnas_extraccion = this.indicador.getResultado().length;
							}
			
							int[] column_types = new int[num_columnas];
							
							for(int i=0; i<num_columnas_extraccion; i++)
							{
								if(vacio) {
									try
									{
										String column_name = metadata.getColumnName(i+1);
										this.indicador.getResultado()[i]=column_name;
										column_types[i] = metadata.getColumnType(i+1);
									}
									catch(SQLException e)
									{
									}
								}
								else {
			
									for (int j=0;j<num_columnas;j++)
									{
										String column_name = metadata.getColumnName(j+1);
										int coltype = metadata.getColumnType(j+1);
										if (column_name.equalsIgnoreCase(this.indicador.getResultado()[i]))
										{
											column_types[i] = coltype;
											break;
										}
									}
								}
							}
							while(rs.next())
							{
								Object[] row = new Object[num_columnas_extraccion];
								for(int i=0;i<num_columnas_extraccion;i++)
								{
									Object value = new Object();
									if (this.indicador.getResultado()[i] == null)
									{
										value=null;
										continue;
									}
									
									if (column_types[i] == Types.TIMESTAMP)
									{
										try
										{
											value = new Timestamp(rs.getTimestamp(this.indicador.getResultado()[i]).getTime());
										}
										catch (NullPointerException e)
										{
											value = null;
											
										} catch (SQLException e)
										{
											value = null;
											return false;
										}
									}
									
									else if (column_types[i] == Types.INTEGER)
									{
										try
										{
											value  = rs.getInt(this.indicador.getResultado()[i]);
			
										}
										catch (NullPointerException e)
										{
											value = null;
										} catch (SQLException e) {
			
											value = null;
											return false;
										}
									}
			
			
									else if (column_types[i] == Types.VARCHAR) 
									{
										try
										{
											value  = rs.getString(this.indicador.getResultado()[i]);
			
										}
										catch (NullPointerException e)
										{
											value = null;
										} catch (SQLException e) {
			
											value = null;
											return false;
										}
									}
									else 
									{
										try
										{
											value = rs.getObject(this.indicador.getResultado()[i]);
										} catch (SQLException e)
										{
			
											value = null;
										}
									}
									
									row[i] = value;
								}
								
								if (this.getResultadoEjecucion()!=null)
									this.getResultadoEjecucion().add(row);
								else
								{
									this.setResultadoEjecucion(new Vector<Object[]>());
									this.getResultadoEjecucion().add(row);
								}
								
								this.setDescripcionEstado(FIN_OK);
							}
							
							this.setEstado(ESTADO_EJECUTADO);
			
						} catch (Exception e) {
							e.printStackTrace();
							this.setEstado(ESTADO_EJECUTADO);
							this.setDescripcionEstado(FIN_OK_SIN_RESULTADO);
						}
						finally
						{
							try {
								conexion.close();
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}*/

				
				//Si se trata de un indice autogenerado, por un bucle, tenemos que incorporar a cada uno de los resultados obtenidos del indicador_a el resultado del indicador_b
				if (this.autoGenerado && this.getIndice()>-1)
				{
					Object[] linea = null;
//					Object[] linea_a = getIndicadores().get(this.getNombreIndicadorPadre()).getResultadoEjecucion().elementAt(this.getIndice());
					Object[] linea_a = super.getIndicadorNombre(this.getAnalisis(), this.getNombreIndicadorPadre()).getResultadoEjecucion().elementAt(this.getIndice());
					
					if (this.getResultadoEjecucion()!=null && this.getResultadoEjecucion().size()>0)
					{
						Object[] linea_b = this.getResultadoEjecucion().elementAt(0);
							
						linea = new Object[linea_a.length+linea_b.length];
						for (int i=0;i<linea_a.length;i++)
							linea[i] = linea_a[i];
							
						for (int i=0;i<linea_b.length;i++)
							linea[linea_a.length+i] = linea_b[i];
					}
					else
						//AGUJERO se deberia crear un objeto con la longitud de campos correspondiente al numero de elementos que el atributo resultado del indicador_b
						linea = linea_a;
					
					
					log.info("Indicador auto finalizado: " + this.getIndicador().getNombre());
//					getIndicadores().get(this.getNombreIndicadorPadre()).getResultadoEjecucion().setElementAt(linea, this.getIndice());
//					getIndicadores().get(this.getNombreIndicadorPadre()).setCountEjecutado();
					super.getIndicadorNombre(this.getAnalisis(), this.getNombreIndicadorPadre()).getResultadoEjecucion().setElementAt(linea, this.getIndice());
					super.getIndicadorNombre(this.getAnalisis(), this.getNombreIndicadorPadre()).setCountEjecutado();
					this.setDescripcionEstado(FIN_OK);
					//TAREA revisar si en este punto cuando damos por finalizado el hilo, tenemos que eliminar el indicador autogenerado o por el contrario se libera con la muerte del thread.
				}
			}
			this.setEstado(ESTADO_EJECUTADO);
		}
		
		return true;

	}
	
	public void detener()
	{
		super.detener();
		
		if (this.listaIndicadoresHijos!=null && this.listaIndicadoresHijos.size()>0)
		{
			for(int i=0;i<this.listaIndicadoresHijos.size();i++)
			{
				IndicadorProxy indHijo = this.listaIndicadoresHijos.get(i);
				//Buscamos los indicadores autogenerados hijos 
				indHijo.detener();
				log.info(cabeceralog+"Parado el indicador hijo:" +indHijo.getIndice()+ " por sobrepasar el tiempo maximo de ejecucion "+ tiempo_max);
			}
		}
	}
	
	public void parametrosIndicador() {
		
		for (int p = 0; p < this.getIndicador().getParametros().size(); p++) {			
			if (this.getIndicador().getParametros().get(p).getValor().startsWith(Comunes.tpMarcaIndicador)) {
				
				String[] tramos = new String [] {};
				tramos = this.getIndicador().getParametros().get(p).getValor().split(Comunes.tpSeparador);
				String nombreIndicador = tramos[0].substring(1);
				
				IndicadorProxy indProxy = super.getIndicadorNombre(this.getAnalisis(), nombreIndicador);
				if (indProxy!=null) 
				{
//					if (getIndicadores().get(nombreIndicador).ejecutar() && (getIndicadores().get(nombreIndicador).getResultadoEjecucion()!=null) && getIndicadores().get(nombreIndicador).getResultadoEjecucion().size()>0)
					if (indProxy.ejecutar() && (indProxy.getResultadoEjecucion() != null) && indProxy.getResultadoEjecucion().size() > 0) 
					{					
						String columna = tramos[1];
						
						Object valParam = new Object();
						
						if (columna.equals("TODO")) {
							valParam = nombreIndicador;
						}else {
							Object[] linea = indProxy.getResultadoEjecucion().elementAt(0);
							int c = 0;
							while (c < indProxy.getIndicador().getResultado().length ) {
								if (columna.equals(indProxy.getIndicador().getResultado()[c])) {
									valParam = linea[c];
									break;
						}
								c++;
//						Object[] linea = getIndicadores().get(nombreIndicador).getResultadoEjecucion().elementAt(0);

							}
						}
						
						this.getIndicador().getParametros().get(p).setValor(valParam.toString());					
					}
				}
			}
		}
	}
	
	public boolean esDependiente()
	{
		boolean esDependiente = false;
		
		//Recorro los parametros del indicador para averiguar si todos su parametros estan interpretados o no
		for (int p = 0; p < this.getIndicador().getParametros().size(); p++) 
		{			
			//Identifico aquellos parametros tipo Indicador, comienzan por #
			if (this.getIndicador().getParametros().get(p).getValor().startsWith(Comunes.tpMarcaIndicador)) 
			{
				String[] tramos = new String [] {};
				tramos = this.getIndicador().getParametros().get(p).getValor().split("\\.");
				String nombreIndicador = tramos[0].substring(1);
				
				//Busco en la lista de indicadores
				IndicadorProxy indProxy = super.getIndicadorNombre(this.getAnalisis(), nombreIndicador);
				
//				if (getIndicadores().containsKey(nombreIndicador))
				if (indProxy!=null)
				{
//					if (getIndicadores().get(nombreIndicador).noejecutado() || getIndicadores().get(nombreIndicador).ejecutando())
					if (super.getIndicadorNombre(this.getAnalisis(), nombreIndicador).noejecutado() || super.getIndicadorNombre(this.getAnalisis(), nombreIndicador).ejecutando())
					{
						esDependiente = true;
						break;
					}
				}
			}
		}
		
		return esDependiente;
	}
	
	//Este metodo recopila en una Lista todos los objetos IndicadorProxy asociados al IndicadorProxy pasado como parametro
	public List<String> ObtenerIndicadorAsociado(List<String> listaInd) {
		
		if (listaInd==null)
			listaInd = new ArrayList<String>();
		
		//Recorro los parametros del indicador
		for (int p = 0; p < this.getIndicador().getParametros().size(); p++) 
		{			
			//Identifico aquellos parametros tipo Indicador, comienzan por #
			if (this.getIndicador().getParametros().get(p).getValor().startsWith(Comunes.tpMarcaIndicador)) 
			{
				
				String[] tramos = null;
				tramos = this.getIndicador().getParametros().get(p).getValor().split("\\.");
				
				String nombreIndicador = tramos[0].substring(1);
				//Busco en la lista de indicadores
				if ((nombreIndicador.equals("CURSOR") || getIndicadorNombre(this.getAnalisis(), nombreIndicador)!=null) && !listaInd.contains(nombreIndicador)) 
				{
					listaInd.add(nombreIndicador);
					log.info(cabeceralog+" Añadimos a la lista de indicadores dependientes:"+nombreIndicador);
					//Llamamos recursivamente a este metodo hasta llegar al nivel de profundidad necesario y almacenando en un lista todos aquellos indicadores que esten asociados al indicador inicial
//					getIndicadores().get(nombreIndicador).ObtenerIndicadorAsociado(listaInd);
					super.getIndicadorNombre(this.getAnalisis(), nombreIndicador).ObtenerIndicadorAsociado(listaInd);
				}
			}
		}
		
		return listaInd;
	}

	public boolean validar() {
		boolean valido = false;
		
		if(this.indicador.getNombre().equals("")) {
			valido = false;
			// Falta nombre
		}
		else if(this.indicador.getDescripcion().equals("")) {
			valido = false;
			// Falta descripcion
		}
		else if(this.indicador.getFuente().equals("")) {
			valido = false;
			// Falta descripcion
		}
		else if(this.indicador.getTipo().equals("")) {
			valido = false;
			// Falta descripcion
		}
		else if(this.indicador.getComando().equals("")) {
			valido = false;
			// Falta descripcion
		}
		else if(this.indicador.getResultado() == null) {
			valido = false;
			// Falta descripcion
		}
		else {
		for (int i = 0; i<this.indicador.getParametros().size(); i++) {
			valido = this.indicador.getParametros().get(i).validar();
			if(valido == false) {
				return valido;
				// un criterio no es valido
			}
		}
		
			valido = true;
		}		
		
		return valido;
	}
}
