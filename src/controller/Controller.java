package controller;

import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Scanner;

import model.data_structures.*;
import view.MovingViolationsManagerView;

@SuppressWarnings("unused")
public class Controller {
	
	/*
	 * Atributos
	 */
	public static final String[] movingViolationsFilePaths = new String[] {"data/Moving_Violations_Issued_in_January_2018.csv", "data/Moving_Violations_Issued_in_February_2018.csv", "data/Moving_Violations_Issued_in_March_2018.csv","data/Moving_Violations_Issued_in_April_2018.csv"};

	private static MovingViolationsManagerView view;

	private static IArregloDinamico<VOMovingViolation> movingVOLista;
	
	private static int semestreCargado;
	
	/*
	 * Constructor
	 */
	public Controller() {
		view = new MovingViolationsManagerView();

		movingVOLista = null;
	}
	
	/*
	 * Metodos para requerimientos
	 */
	public IArregloDinamico<Integer> loadMovingViolations(){
		return loadMovingViolations(1);
	}
	/**
	 * Carga los datos del semestre indicado indicado
	 * @param n Numero de semestre del anio (entre 1 y 2)
	 * @return Cola con el numero de datos cargados por mes del semestre
	 */
	public IArregloDinamico<Integer> loadMovingViolations(int n)
	{
		IArregloDinamico<Integer> numeroDeCargas = new ArregloDinamico<>();
		if(n == 1)
		{
			numeroDeCargas = loadMovingViolations(new String[] {"Moving_Violations_Issued_in_January_2018.csv", 
					    	     "Moving_Violations_Issued_in_February_2018.json",
					    	     "Moving_Violations_Issued_in_March_2018.json",
					    	     "Moving_Violations_Issued_in_April_2018.json",
					    	     "Moving_Violations_Issued_in_May_2018.json",
					    	     "Moving_Violations_Issued_in_June_2018.json"
					    	     });
			semestreCargado = 1;
		}
		else if(n == 2)
		{
			numeroDeCargas = loadMovingViolations(new String[] {"Moving_Violations_Issued_in_July_2018.json",
								 "Moving_Violations_Issued_in_August_2018.json",
								 "Moving_Violations_Issued_in_September_2018.json", 
								 "Moving_Violations_Issued_in_October_2018.json",
								 "Moving_Violations_Issued_in_November_2018.json",
								 "Moving_Violations_Issued_in_December_2018.json"
								 });
			semestreCargado = 2;
		}
		else
		{
			throw new IllegalArgumentException("No existe ese semestre en un annio.");
		}
		return numeroDeCargas;
	}

	/**
	 * Metodo ayudante
	 * Carga la informacion sobre infracciones de los archivos a una pila y una cola ordenadas por fecha.
	 * Dado un arreglo con los nombres de los archivos a cargar
	 * @returns ArregloDinamico con el numero de datos cargados por mes del cuatrimestre
	 */
	private IArregloDinamico<Integer> loadMovingViolations(String[] movingViolationsFilePaths){
		// TODO
		CSVReader reader = null;
		IQueue<Integer> numeroDeCargas =new Queue<>();
		
		int contadorInf; // Cuenta numero de infracciones en cada archivo
		try {
			movingVOLista = new ArregloDinamico<VOMovingViolations>(500000);

			for (String filePath : movingViolationsFilePaths) {
				reader = new CSVReader(new FileReader("data/"+filePath));
				
				contadorInf = 0;
				// Deduce las posiciones de las columnas que se reconocen de acuerdo al header
				String[] headers = reader.readNext();
				int[] posiciones = new int[VOMovingViolations.EXPECTEDHEADERS.length];
				for (int i = 0; i < VOMovingViolations.EXPECTEDHEADERS.length; i++) {
					posiciones[i] = buscarArray(headers, VOMovingViolations.EXPECTEDHEADERS[i]);
				}
				
				// Lee linea a linea el archivo para crear las infracciones y cargarlas a la lista
				for (String[] row : reader) {
					movingVOLista.agregar(new VOMovingViolations(posiciones, row));
					contadorInf += 1;
				}
				// Se agrega el numero de infracciones cargadas en este archivo al resultado 
				numeroDeCargas.enqueue(contadorInf);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}

		}
		return numeroDeCargas;
	}
	
	// TODO
	public Object requerimiento1(Object o) {
		return null;
	}
	
	public void run() {
		int nDatos = 0; // Para saber el numero total de datos cargados
		int nMuestra = 0; // En caso de generarse una muestra, variable donde se guarda el tamanio dado por el cliente
		long[] tiempos;// Para calcular tiempos de agregacion y eliminacion de elementos

		Scanner sc = new Scanner(System.in);
		boolean fin = false;

		while(!fin)
		{
			view.printMenu();

			int option = sc.nextInt();
			
			switch(option)
			{
			case 0:
				// Cargar infracciones
				view.printMovingViolationsLoadInfo(this.loadMovingViolations(1));
				break;

			case 1:
				// Requerimiento 1
				// TODO
				view.printMovingViolationsReq1(requerimiento1(null));
				break;

			case 10:	
				fin=true;
				sc.close();
				break;
			}
		}
	}
	
	
	/**
	 * Metodo para buscar strings en un array de strings, usado para deducir la posicion
	 * de las columnas esperadas en cada archivo.
	 * @param array
	 * @param string
	 * @return
	 */
	private int buscarArray(String[] array, String string) {
		int i = 0;

		while (i < array.length) {
			if (array[i].equalsIgnoreCase(string)) return i;
			i += 1;
		}
		return -1;
	}

	/*
	 * Metodos ayudantes
	 */
	/**
	 * Convertir fecha a un objeto LocalDate
	 * @param fecha fecha en formato dd/mm/aaaa con dd para dia, mm para mes y aaaa para agno
	 * @return objeto LD con fecha
	 */
	private static LocalDate convertirFecha(String fecha)
	{
		return LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}

	/**
	 * Convertir fecha y hora a un objeto LocalDateTime
	 * @param fecha fecha en formato yyyy-MM-dd'T'HH:mm:ss'.000Z' con dd para dia, mm para mes y yyy para agno, HH para hora, mm para minutos y ss para segundos
	 * @return objeto LDT con fecha y hora integrados
	 */
	private static LocalDateTime convertirFecha_Hora_LDT(String fechaHora)
	{
		return LocalDateTime.parse(fechaHora, DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH:mm:ss"));

    }
}