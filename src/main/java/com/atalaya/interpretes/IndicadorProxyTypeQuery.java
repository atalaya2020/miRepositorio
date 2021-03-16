package com.atalaya.interpretes;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Hashtable;
import java.util.Vector;

import com.modelodatos.Indicador;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class IndicadorProxyTypeQuery implements IIndicadorProxyType {
	
	private static IndicadorProxyTypeQuery instance;
	
	private String modoVolcado = "memoria";
	private int numFilasParaVolcarFichero = 100;
	private int numFilasParaLeer = 1000;
	private String ficheroVolcado = "c://users/0015814/Desktop/volcado.txt";
	
	private static Hashtable<String,HikariDataSource>almacenPool; 
	//statement.setFetchSize(Integer.MIN_VALUE);
	
	/** 
	 * Constructor.
	 */
	protected IndicadorProxyTypeQuery(){}
	

	public static IndicadorProxyTypeQuery getInstance()
	{
		//Si el atributo instance no tiene valor significa que no existe ninguna instancia de la clase
		if (instance==null)
		{
			//Da valor al atributo instance, creando una instancia de la clase
			instance = new IndicadorProxyTypeQuery();
		}
		//Devuelve la instancia unica de la clase AliasTypeLoop
		return instance;
	}
	
	public boolean ejecutar(IndicadorProxy indicador) {
		
		boolean exec = false;
		
		try
		{			
			HikariDataSource pool = null;
			
			if (almacenPool==null || (almacenPool!=null && almacenPool.size()==0))
				almacenPool = new Hashtable<String,HikariDataSource>();
			
			if (almacenPool.containsKey(indicador.getIndicador().getFuente()))
				pool = almacenPool.get(indicador.getIndicador().getFuente());
			
			
			if (!almacenPool.containsKey(indicador.getIndicador().getFuente()))
			{
				if (indicador.getIndicador().getFuente().equals("Alumnos"))
				{
					HikariConfig config = new HikariConfig();
					config.setJdbcUrl("jdbc:mysql://localhost:3306/alumnadodb?useServerPrepStmts=true&useSSL=false&allowPublicKeyRetrieval=true");
					//config.setJdbcUrl("jdbc:mysql://alumnadodb:3306/alumnadodb?useServerPrepStmts=true&useSSL=false&allowPublicKeyRetrieval=true");
					config.setUsername("root");
					config.setPassword("atalaya");
					config.addDataSourceProperty("cachePrepStmts", "true");
					config.addDataSourceProperty("prepStmtCacheSize", "250");
					config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
					config.addDataSourceProperty("maximum-pool-size", "20");
	
					pool = new HikariDataSource(config);
					almacenPool.put(indicador.getIndicador().getFuente(), pool);
				}
			}
			
			if (pool!=null)
			{
				Connection conexion = null;
				ResultSet rs = null;
				
				PrintWriter fichero = null;
				
				if (modoVolcado.equals("memoria"))
					indicador.setResultadoEjecucion(new Vector<Object[]>());
				else
					fichero = new PrintWriter(new File(ficheroVolcado));
				
				try
				{
					String sComando = indicador.getIndicador().getComando();
				
					conexion = pool.getConnection();
					
					Indicador def_indicador = indicador.getIndicador();
					
					PreparedStatement pstmt = conexion.prepareStatement(sComando);
					pstmt.setFetchSize(numFilasParaLeer);
					
					for(int i = 0; i< def_indicador.getParametros().size(); i++) {
						if(def_indicador.getParametros().get(i).getTipo().equalsIgnoreCase("String")) {
							pstmt.setString(i+1, def_indicador.getParametros().get(i).getValor());
						}
						else if(def_indicador.getParametros().get(i).getTipo().equalsIgnoreCase("Entero")) {
							pstmt.setInt(i+1,Integer.parseInt(def_indicador.getParametros().get(i).getValor()));
						}
						else if(def_indicador.getParametros().get(i).getTipo().equalsIgnoreCase("BigDecimal")) {
							pstmt.setBigDecimal(i+1,new BigDecimal(def_indicador.getParametros().get(i).getValor()));
						}
						else if(def_indicador.getParametros().get(i).getTipo().equalsIgnoreCase("Date")) {						
							pstmt.setString(i+1, def_indicador.getParametros().get(i).getValor());
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
	
					if (def_indicador.getResultado() == null ||  def_indicador.getResultado().length == 0)
					{
						num_columnas_extraccion = num_columnas;
						def_indicador.setResultado(new String[num_columnas]);
						vacio = true;
					}
					else
					{
						num_columnas_extraccion = def_indicador.getResultado().length;
					}
	
					int[] column_types = new int[num_columnas];
					
					for(int i=0; i<num_columnas_extraccion; i++)
					{
						if(vacio) 
						{
							try
							{
								String column_name = metadata.getColumnName(i+1);
								def_indicador.getResultado()[i]=column_name;
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
								if (column_name.equalsIgnoreCase(def_indicador.getResultado()[i]))
								{
									column_types[i] = coltype;
									break;
								}
							}
						}
					}
					
					int numRows = 0;
					
					while(rs.next())
					{
						++numRows;
						
						Object[] row = new Object[num_columnas_extraccion];
						for(int i=0;i<num_columnas_extraccion;i++)
						{
							Object value = new Object();
							if (def_indicador.getResultado()[i] == null)
							{
								value=null;
								continue;
							}
							
							if (column_types[i] == Types.TIMESTAMP)
							{
								try
								{
									value = new Timestamp(rs.getTimestamp(def_indicador.getResultado()[i]).getTime());
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
									value  = rs.getInt(def_indicador.getResultado()[i]);
	
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
									value  = rs.getString(def_indicador.getResultado()[i]);
	
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
									value = rs.getObject(def_indicador.getResultado()[i]);
								} catch (SQLException e)
								{
	
									value = null;
								}
							}
							
							row[i] = value;	
						}
						
						if (modoVolcado.equals("memoria"))
						{
							if (indicador.getResultadoEjecucion()!=null)
								indicador.getResultadoEjecucion().add(row);
						}
						else
						{
							fichero.println(row.toString());
							if(numRows % numFilasParaVolcarFichero == 0)
						        fichero.flush();
						}
						indicador.setDescripcionEstado(Ejecutable.FIN_OK);
					}
					
					indicador.setEstado(Ejecutable.ESTADO_EJECUTADO);
					exec = true;
	
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
					indicador.setEstado(Ejecutable.ESTADO_EJECUTADO);
					indicador.setDescripcionEstado(Ejecutable.FIN_OK_SIN_RESULTADO);
				}
				finally
				{
					if (rs!=null)
						rs.close();
					
					if (conexion!=null)
						conexion.close();
				}
			}
			else
			{
				indicador.setEstado(Ejecutable.ESTADO_EJECUTADO);
				indicador.setDescripcionEstado(Ejecutable.FIN_OK_SIN_RESULTADO);
			}
			
		}
		catch (Exception e)
		{
			indicador.setEstado(Ejecutable.ESTADO_EJECUTADO);
			indicador.setDescripcionEstado(Ejecutable.FIN_OK_SIN_RESULTADO);
		}
		
		return exec;
	}
}
