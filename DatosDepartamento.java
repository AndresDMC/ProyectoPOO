/**
     * Lee los datos de la empresa desde un archivo y los almacena en las listas y matriz proporcionadas.
     */
package Documentos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatosDepartamento {

	public static void leerDatosEmpresa(String rutaArchivo, List<String> meses, List<String> departamentos,
			int[][] ventas) {
		try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
			String linea;
			int fila = 0;
			while ((linea = br.readLine()) != null) {
				String[] datos = linea.split(",");
				if (fila == 0) {
					// Si es la primera fila, se extraen los nombres de los meses
					for (int i = 1; i < datos.length; i++) {
						meses.add(datos[i]);
					}
					// Se inicializa la matriz de ventas con el tamaño adecuado
					ventas = new int[datos.length - 1][meses.size()];
				} else {
					// Si es una fila de datos de departamento, se extrae el nombre del departamento
					departamentos.add(datos[0]);
					// Se recorren los valores de ventas por mes y se almacenan en la matriz
					for (int i = 1; i < datos.length; i++) {
						ventas[fila - 1][i - 1] = Integer.parseInt(datos[i]);
					}
				}
				fila++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
