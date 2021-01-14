/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examen_firma;

/**
 *
 * @author tonis
 */
import java.security.*;
import sun.misc.BASE64Encoder;
import java.io.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import javax.crypto.*;
import com.itextpdf.text.DocumentException;
import com.sun.awt.AWTUtilities;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.awt.Desktop;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.swing.JOptionPane;

public class PDF {
    public void generar(String nombre, String edad, String mensaje) throws FileNotFoundException, DocumentException{
        FileOutputStream archivo = new FileOutputStream("Archivo"+".pdf");
        Document documento = new Document();
        PdfWriter.getInstance(documento, archivo);
        documento.open();
        
        Paragraph parrafo = new Paragraph("Datos");
        parrafo.setAlignment(1);
        documento.add(parrafo);
        documento.add(new Paragraph("Nombre: " + nombre));
        documento.add(new Paragraph("Edad: " + edad));
        documento.add(new Paragraph("Mensaje: " + mensaje));
        
        documento.close();
        JOptionPane.showMessageDialog(null, "Archivo PDF creado exitosamente :)");
    }
    
    /*public void generarpdffirma(String firma) throws FileNotFoundException, DocumentException{
        FileOutputStream archivo = new FileOutputStream("Firma"+".pdf");
        Document documento = new Document();
        PdfWriter.getInstance(documento, archivo);
        documento.open();
        
        Paragraph parrafo = new Paragraph("Firma");
        parrafo.setAlignment(1);
        documento.add(parrafo);
        documento.add(new Paragraph("Nombre: " + firma));
        
        documento.close();
        JOptionPane.showMessageDialog(null, "Archivo PDF creado exitosamente :)");
    }*/
    
    public void firmardocumento(String nombreprikey) throws IOException, FileNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException, SignatureException, DocumentException{
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        PrivateKey llaveprivada = cargarPrivadaKey(nombreprikey);
        Signature firma = Signature.getInstance("SHA1WithRSA", "BC");
        //inicializamos la firma
        firma.initSign(llaveprivada, new SecureRandom());
        
        
        byte[] dato = "no se que poner".getBytes();
        Path pdfPath = Paths.get("Archivo.pdf");
        byte[] pdf = Files.readAllBytes(pdfPath);
        //
        //String s = new String(pdf, StandardCharsets.UTF_8);
        //System.out.println("EL STRING: " + s );
        //CAMBIAMOS DATO POR PDF
        firma.update(pdf);
        
        //firmamos
        byte[] firmabytes = firma.sign();
        
        //imprimimos
        System.out.println("Firma:" + new BASE64Encoder().encode(firmabytes));
        
        String StrFirma = new BASE64Encoder().encode(firmabytes);
        
        FileOutputStream archivo = new FileOutputStream("Firma"+".pdf");
        Document documento = new Document();
        PdfWriter.getInstance(documento, archivo);
        documento.open();
        
        Paragraph parrafo = new Paragraph("Firma");
        parrafo.setAlignment(1);
        documento.add(parrafo);
        documento.add(new Paragraph("Firma: " + StrFirma));
        
        documento.close();
        JOptionPane.showMessageDialog(null, "Archivo PDF creado exitosamente :)");
    } 
    
    public void comprobardoc(String namedoc) throws IOException, FileNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException, SignatureException{
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        PrivateKey llaveprivada = cargarPrivadaKey("privatekey.key");
        PublicKey llavepublica = cargarPublicaKey("publickey.key");
        Signature firma = Signature.getInstance("SHA1WithRSA", "BC");
        //inicializamos la firma
        firma.initSign(llaveprivada, new SecureRandom());
        
        
        byte[] dato = "no se que poner".getBytes();
        Path pdfPath = Paths.get("Archivo.pdf");
        byte[] pdf = Files.readAllBytes(pdfPath);
        Path pdfPatha = Paths.get(""+ namedoc);
        byte[] pdfa = Files.readAllBytes(pdfPatha);
        //
        //String s = new String(pdf, StandardCharsets.UTF_8);
        //System.out.println("EL STRING: " + s );
        //CAMBIAMOS DATO POR PDF
        firma.update(pdf);
        
        //firmamos
        byte[] firmabytes = firma.sign();
        
        //imprimimos
        System.out.println("Firma:" + new BASE64Encoder().encode(firmabytes));
        firma.initVerify(llavepublica);
        
        //CAMBIAMOS DATO POR PDF
        firma.update(pdfa);
        Boolean Hola = firma.verify(firmabytes);
        System.out.println(firma.verify(firmabytes));
        
        System.out.println("Pues aqui está"+Hola);
        if (Hola == true) {
            JOptionPane.showMessageDialog(null, "El archivo es válido uwu");
        } else {
            JOptionPane.showMessageDialog(null, "El archivo es inválido 7m7");
        }
    }
    
    public void generarKeys() throws IOException, NoSuchAlgorithmException, NoSuchProviderException{
        //recordemos que security no tiene soporte con BC
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        //primero hay que hacer la instancia con un nuevo proveedor
        KeyPairGenerator generador = KeyPairGenerator.getInstance("RSA", "BC");
        
        //inicializamos la llave
        generador.initialize(2048, new SecureRandom());
        
        //laves
        KeyPair llaves = generador.genKeyPair();
        //ahora necesitamos la llave publica y la privada
        PublicKey llavepublica = llaves.getPublic();
        PrivateKey llaveprivada = llaves.getPrivate();
        
        //vamos a guardar y cargar un archivo con el contenido de la llave publica
        guardarKey(llavepublica, "publickey.key");
        //llavepublica = cargarPublicaKey("publickey.key");
        
        //vamos a guardar y cargar un archivo con el contenido de la llave privada
        guardarKey(llaveprivada, "privatekey.key");
        
        //llaveprivada = cargarPrivadaKey("privatekey.key");
    }
    
    private static void guardarKey(Key llave, String archivo) throws FileNotFoundException, IOException {
        //generarme un archivo .dat
        byte[] llavepublic = llave.getEncoded();
        FileOutputStream fos = new FileOutputStream(archivo);
        fos.write(llavepublic);
        fos.close();
        
    }

    private static PublicKey cargarPublicaKey(String archivo) throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        /*
        para poder exportar la llave publica es necesario codificarla mediante una codificacion
        certificada por X509 es para la certificacion de la llave
        */
        
        FileInputStream fis = new FileInputStream(archivo);
        //comprobacion si es valida 
        int numBytes = fis.available();
        byte[] bytes = new byte[numBytes];
        fis.read(bytes);
        fis.close();
        
        //para comprobar la llave
        KeyFactory keyfactory = KeyFactory.getInstance("RSA");
        //generar la subllaves
        KeySpec spec = new X509EncodedKeySpec(bytes);
        
        PublicKey llavePublic = keyfactory.generatePublic(spec);
        
        return llavePublic;
        
    }

    private static PrivateKey cargarPrivadaKey(String archivo) throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        
        FileInputStream fis = new FileInputStream(archivo);
        //comprobacion si es valida 
        int numBytes = fis.available();
        byte[] bytes = new byte[numBytes];
        fis.read(bytes);
        fis.close();
        
        /*porque para la comprobacion de la llave privada, es necesario el 
        certificado por parte del estandar PKCS8 el cual nos dice el tipo 
        de codificacion que acepta una llave privada en RSA
        */
         //para comprobar la llave
        KeyFactory keyfactory = KeyFactory.getInstance("RSA");
        KeySpec spec = new PKCS8EncodedKeySpec(bytes);
        PrivateKey llavePrivate = keyfactory.generatePrivate(spec);
        return llavePrivate;
        
    }
    
}
