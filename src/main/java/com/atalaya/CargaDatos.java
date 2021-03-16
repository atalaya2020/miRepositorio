package com.atalaya;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class CargaDatos {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		int registrosCarga = 2000;
		
		if (args!=null && args.length>0)
		{
			registrosCarga = Integer.parseInt((String)args[0]);
		}
		else
		{	
			String [] nombres = new String [] {"ALBERTO", "ANA", "BLAS", "BELEN", "CARLOS", "CARMEN", "DAVID", "DIANA", "EDUARDO", "ELVIRA", "FRANCISCO", "FATIMA", 
					"GABRIEL", "GLORIA", "HUGO", "HERMINIA", "IGNACIO", "ISABEL", "JAIME", "JIMENA", "LUIS", "LAURA", "MANUEL", "MARIA", "NESTOR", "NOELIA", "OSCAR", "OLGA", 
					"PABLO", "PALOMA", "RAUL", "RAQUEL", "SERGIO", "SUSANA", "TELMO", "TERESA", "ULISES", "URSULA", "VICTOR", "VERONICA", "YAGO", "YOLANDA", "ERNESTO", "MARCO", "PEDRO", 
					"JUAN", "LORENZO", "LUCAS", "ARTURO", "ROSA", "MARTA", "ALICIA", "JULIA", "EVA", "PILAR", "INES", "ANDRES", "BEATRIZ", "ANTONIO", "PATRICIA"};
				
			String [] apellidos1 = new String [] {"ALVAREZ", "BENITEZ", "CORONAS", "DIAZ", "ESCOBAR", "FERNANDEZ", "GARCIA", "HERAS", "IBANEZ", "JUAREZ", "LOPEZ", 
				"MUNOZ", "NUNEZ", "OJEDA", "PONTE", "RODRIGUEZ", "SANCHEZ", "TELLEZ", "UCEDA", "VAZQUEZ", "YANGUAS", "ZARZALEJO", "RUBIO", "GONZALEZ", "CABALLERO", "CASTRO", "REYES", 
				"ASENSIO", "SILVA", "MATA", "VILLANUEVA", "CUESTA", "ALCANTARA", "RUIZ", "ABELLAN", "DELGADO", "CORREDOR", "NADAL", "RIVERO", "FUENTES", "CASTAÑO", "FIGUEROA", "VALLEJO", "VERDU"};
			
			String [] apellidos2 = new String [] {"ALONSO", "BLANCO", "CALVO", "DIEZ", "ESCUDERO", "FLORES", "GOMEZ", "HERNANDEZ", "HEREDIA", "ISLA", "JIMENEZ", "LEDESMA", 
				"MARTINEZ", "NAVAS", "OLIVA", "PEREZ", "RIESCO", "SOLIS", "TAPIAS", "URRA", "VELASCO", "YANEZ", "ZURITA", "MORENO", "GUTIERREZ", "PASO", "ROS", 
				"SAMPER", "RAMOS", "COSTA", "CRESPO", "CAMARA", "ARIAS", "MATEOS", "CAMPOS", "SORDO", "CARRERAS", "NOVOA", "MOLINA", "ROMERO", "RIVAS", "MONTERO", "SAURA", "TORRES"};
				
			String [] provincias = new String [] {"CORUNA", "LUGO", "ORENSE", "PONTEVEDRA", "ASTURIAS", "CANTABRIA", "VIZCAYA", "GUIPUZCOA", "ALAVA", "NAVARRA", 
					"HUESCA", "ZARAGOZA", "TERUEL", "LERIDA", "GERONA", "BARCELONA", "TARRAGONA", "LEON", "PALENCIA", "BURGOS", "RIOJA", "ZAMORA", "SORIA", "SALAMANCA", 
					"VALLADOLID", "AVILA", "SEGOVIA", "MADRID", "CASTELLON", "VALENCIA", "ALICANTE", "BALEARES", "MURCIA", "CACERES", "BADAJOZ", "GUADALAJARA", "TOLEDO", "CUENCA", 
					"CIUDAD REAL", "ALBACETE", "JAEN", "CORDOBA", "SEVILLA", "HUELVA", "CADIZ", "MALAGA", "ALMERIA", "GRANADA", "TENERIFE", "LAS PALMAS", "CEUTA", "MELILLA"};
			
			String[] asignaturas = new String [] {"ATALAYA", "DOCKER", "HAPROXY", "CLOUD", "MYSQL", "MONGO", "SPRING_BOOT", "JAVA", "INGLES", "FRANCES", "ALEMAN"};
			
			int identificador = 0;
			int registros = 0;
			String nombre;
			String apellidos;
			String provincia;
			int iniNom = 0;		
			int iniApe1 = 0;
			int iniApe2 = 0;
			int n = 0;
			int a1 = 0;
			int a2 = 0;		
			PreparedStatement maxId = null;
			PreparedStatement nuevoAlumno = null;
			PreparedStatement nuevaNota = null;
			String insAlumno = "INSERT INTO ALUMNADO (ID, NOMBRE, APELLIDOS, PROVINCIA) VALUES (?, ?, ?, ?)";
			String insNota = "INSERT INTO CALIFICACIONES (ID,ASIGNATURA,CALIFICACION) VALUES (?, ?, ?);";
			boolean finCarga = false;		
					
			Connection conexion;
	
			try {
				conexion = DriverManager.getConnection(
						"jdbc:mysql://localhost:3306/alumnadodb?useServerPrepStmts=true&useSSL=false&allowPublicKeyRetrieval=true",
	//							"jdbc:mysql://alumnadodb:3306/alumnadodb?useServerPrepStmts=true&useSSL=false&allowPublicKeyRetrieval=true",
							"root", "atalaya"); 			
				maxId = conexion.prepareStatement("SELECT IFNULL(ID, 0) ID, NOMBRE, APELLIDOS, PROVINCIA FROM ALUMNADO WHERE ID = (SELECT MAX(ID) FROM ALUMNADO)");
				// Para continuar con la carga, obtiene el último registro creado. 
				// Localiza el nombre y apellidos del alumno en as tablas nombre, apellido1 y apellido2. 
				
				ResultSet rs = maxId.executeQuery();
				if (rs.next()) {
					identificador = rs.getInt("ID");
					nombre = rs.getString("NOMBRE");
					String ultApellidos[] = rs.getString("APELLIDOS").split(" ");
					for (int i = 0; i < nombres.length; i++) {
						if (nombres[i].equals(nombre)) {
							iniNom = i;
							break;
						}
					}
					for (int i = 0; i < apellidos1.length; i++) {
						if (apellidos1[i].equals(ultApellidos[0])) {
							iniApe1 = i ;
							break;
						}
						if (apellidos2[i].equals(ultApellidos[0])) {
							iniApe2 = i ;
							break;
						}					
					}				
					if (ultApellidos.length == 2) {					
						for (int i = 0; i < apellidos2.length; i++) {
							if (apellidos1[i].equals(ultApellidos[1])) {
								iniApe1 = i;
								break;
							}						
							if (apellidos2[i].equals(ultApellidos[1])) {
								iniApe2 = i;
								break;
							}
						}	
					} else {
						// Si no ha encontrado el nombre y los apellidos, inicia la carga.
						iniNom = 0;
						iniApe1 = 0;
						iniApe2 = 0;
					}				
				}	
				
				// Si la carga ya se está inicida, se posicina en el siguiente elemento para reiniciar con él la carga de datos.
				if (!(iniNom == 0 && iniApe1 == 0 && iniApe2 == 0)) {
					if (iniApe2 == (apellidos2.length - 1)) {
						iniApe2 = 0;
						iniApe1++;
						if (iniApe1 == (apellidos1.length - 1)) {
							iniApe1 = 0;
							iniApe2 = 0;
							iniNom++;
							if (iniNom == (nombres.length - 1)) {
								finCarga = true;
							} else {				
								iniNom++;				
							}	
						} else {				
							iniApe1++;				
						}				
					} else {				
						iniApe2++;					
					}				
				}
	
				// Recorre anidadamente las tablas de nombres, apellido1 y apellido2. Cada una de las combinaciones posibles de estas tablas será el nombre de un alumno.
				// A cada alumno, le asignará aleatoriamente una provincia.
				for (n = iniNom; n < nombres.length && !finCarga; n++) {
					for (a1 = iniApe1; a1 < apellidos1.length && !finCarga; a1++ ) {
						for (a2 = iniApe2; a2 < apellidos2.length && !finCarga; a2++ ) {
							identificador++;
	
							if (((int) Math.floor(Math.random() * 2) != 0)) {
								apellidos = apellidos1[a1].trim() + " " + apellidos2[a2].trim();
							} else {
								apellidos = apellidos2[a2].trim() + " " + apellidos1[a1].trim();
							}
							provincia = provincias[(int) Math.floor(Math.random() * provincias.length)];
							nuevoAlumno = conexion.prepareStatement(insAlumno);
							nuevoAlumno.setInt(1, identificador);
							nuevoAlumno.setString(2, nombres[n]);
							nuevoAlumno.setString(3, apellidos);
							nuevoAlumno.setString(4, provincia);						
							nuevoAlumno.execute();						
							// Una vez creado el alumno, se crean sus calificaciones en las asignaturas con una calificación calculada aletoriamente.
							// La decisión de crear o no una calificacion para se alumno se toma mediante una función aleatoria descompensada para favorecer que en la mayoría de los casos, se cree la calificación 
							// para el alumno en esa asignatura.						
							for (int a = 0; a < asignaturas.length; a++) {
								if (((int) Math.floor(Math.random() * 4)) != 0) {
									nuevaNota = conexion.prepareStatement(insNota);
									nuevaNota.setInt(1, identificador);
									nuevaNota.setString(2, asignaturas[a]);						
									nuevaNota.setInt(3, (int) Math.floor(Math.random() * 11));				
									nuevaNota.execute();
									nuevaNota.close();
								}
							}			
  							nuevoAlumno.close();	

  							registros++; 
							if (registros == registrosCarga) {
								finCarga = true;
							}
						}
						iniApe2 = 0;
					}				
					iniApe1 = 0;
				}			
			
				conexion.commit();
				
				try {
					conexion.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}		
			} catch (SQLException e1) {
			
				e1.printStackTrace();
			}
			
			System.out.println("Insertados " + registros + " alumnos");
		}
		
	}

}
