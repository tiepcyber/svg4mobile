package com.grub.svg4mobile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * Clase que parsea el XMLcon la información extra.
 */
public class ExtraInfoParser {
	
	public static ExtraInfoParser instance = null;

	private int contador=0;
	private Document dom;
	private Vector<ExtraInfo> elementos;

	/**
	 * Crea una instancia del ExtraInfoParser.
	 */
	private synchronized static void createInstance() {
		if (instance == null) {
			instance = new ExtraInfoParser();
	    }
	}

	/**
	 * Obtiene la instancia del ExtraInfoParser.
	 * @return Devuelve la instancia del ExtraInfoPArser.  
	 */
	public static ExtraInfoParser getInstance() {
		if (instance == null)
			createInstance();
	    return instance;
	}

	/**
	 * Constructor de la clase genérico
	 */
	public ExtraInfoParser() {
		elementos = new Vector<ExtraInfo>();
	}
	
	/**
	 * Método privado encargado de ir parseando el archivo SVG e insertando
	 * los elementos en la correspondiente lista.
	 */
	public void parseXML (String path){
		
		String path2 = path.substring(0,(path.length()-3));
		path2 = path2 + "s4m";
		
		File file = new File(path2);
		elementos = new Vector<ExtraInfo>();
		if( file.exists()){
		
			String tempdir = System.getProperty("java.io.tmpdir")+"/tmp/";
			new File(tempdir).mkdir();
		
			try{
				ZipFile zipFile = new ZipFile(path2);
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while(entries.hasMoreElements()) {
			        ZipEntry entry = (ZipEntry)entries.nextElement();
			        copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(tempdir + entry.getName())));
				}				
				 zipFile.close();

			 } catch (IOException ioe) {
				 Log.e("svg4mobile", " zipfail:  " + ioe);
		    }	 
			 
		path = path.substring(0,(path.length()-3));
		path = path.substring(path.lastIndexOf("/"),path.length());
		path = tempdir + path + "inf";
	
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			//Inicializamos el documento.
			dom = db.parse(new File(path));
		}catch(ParserConfigurationException pce) {
			Log.e("svg4mobile", "pce " + pce);
		}catch(SAXException se) {
			Log.e("svg4mobile", "se " + se);
		}catch(IOException ioe) {
			Log.e("svg4mobile", "ioe " + ioe);
		}

		
		Element root = dom.getDocumentElement();
		NodeList lista = root.getChildNodes();
		int i = 0;
		while (i++ < (lista.getLength()-1)){
            if(lista.item(i).getNodeType() == 1){
				Element nodo = (Element) lista.item(i);
				if (nodo.getTagName().compareToIgnoreCase("pin")==0){
					String x = nodo.getAttribute("x");
					String y = nodo.getAttribute("y");
					String w = nodo.getAttribute("width");
					String h = nodo.getAttribute("height");
					String title = nodo.getAttribute("title");
					String description = nodo.getAttribute("description"); 
					String image = nodo.getAttribute("image"); 
					String notes = nodo.getAttribute("notes"); 
					String rgb = nodo.getAttribute("rgb");
					String tag = nodo.getAttribute("tags");	
					String[] tags = tag.split(",");

					image = tempdir + image;
					
					//Log.d("svg4mobile", " image:  " + image  + " x:  " + x+ " y: " + y+ " tag1  " + tags[1] +" h:  " + h);

					ExtraInfo info = new ExtraInfo(Float.parseFloat(x),Float.parseFloat(y),Float.parseFloat(w), Float.parseFloat(h), title, description, image, notes, rgb, tags);
					elementos.add(info);

				}
			}
		}		
	 } else {
		 // no existe s4m
	 }
	}

	/**
	 * Método0 que lee el contenido de un fichero ZIP y lo guarda en un buffer
	 * @param in Fichero ZIP de origen.
	 * @param out Buffer de salida
	 * @throws IOException Errores de E/S durante el proceso.
	 */
	 private static final void copyInputStream(InputStream in, OutputStream out) throws IOException
	  {
	    byte[] buffer = new byte[1024];
	    int len;

	    while((len = in.read(buffer)) >= 0)
	      out.write(buffer, 0, len);

	    in.close();
	    out.close();
	  }

	
	/**
	 * Método que devuelve el puntero al primer elemento.
	 */
	public void First() {
		contador = 0;
	}

	/**
	 * Método que indica al usuario si hay más elementos en la lista.
	 * Implementación del patrón Iterator.
	 * @return Devuelve true si quedan más elementos en el iterador.
	 */
	public Boolean hasNext() {
		if ((contador +1) <=elementos.size())
			return true;
		else return false;
	}

	/**
	 * Método que usará el usuario para obtener el siguiente elemento
	 * de la lista e incrementar el contador.
	 * @return Devuelve el siguiente elemento.
	 */
	public ExtraInfo next() {
		//Log.d("svg4mobile", "next  " + String.valueOf(contador) + " " + String.valueOf(elementos.size()));
		return elementos.get(contador++);
	}
	
	/**
	 * Devuelve el tamaño de la lista de elementos.
	 * @return Tamaño de la lista.
	 */
	public int getSize() {
		return elementos.size();
	}
}
