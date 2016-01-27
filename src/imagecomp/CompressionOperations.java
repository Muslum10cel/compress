/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imagecomp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;

/**
 *
 * @author muslumoncel
 */
public class CompressionOperations {

    public static File[] selectedFiles;

    private final String file_path = "//Users//muslumoncel//Desktop//";
    private String file_name = "";
    private File compressed_file;
    private final String extension = ".zip";
    private FileOutputStream fileOutputStream;
    private ZipOutputStream gZIPOutputStream;
    private ServerSocket serverSocket;
    private FileInputStream fileInputStream = null;
    private BufferedInputStream bufferedInputStream = null;
    private OutputStream outputStream = null;
    private Socket socket;
    private volatile boolean sent = false;

    /**
     * This method is written for sending compressed file via Internet. If
     * compressed file does not exist; warn user and return back. Inside a
     * Thread a ServerSocket is created and waiting for connection of Client
     * side. After connection establishing, file will be written to socket and
     * it will be sent.
     */
    @SuppressWarnings("empty-statement")
    public void sendOverChannel() {
        if (!compressed_file.exists()) { // if compressed_file does not exist
            JOptionPane.showMessageDialog(null, "Please compress files first"); // warn user
            return; // return back
        }
        Thread thread = new Thread(() -> { //create a thread
            try {
                serverSocket = new ServerSocket(13267); // create a server socket with specified port number
                serverSocket.setSoTimeout(5000); // set Time out for client socket
                socket = serverSocket.accept(); // wait for connection of client
                int count; // counter
                byte[] temp = new byte[(int) compressed_file.length()]; // create a byte array with length of compressed_file length 
                fileInputStream = new FileInputStream(compressed_file); // gettting bytes from specified file
                bufferedInputStream = new BufferedInputStream(fileInputStream);
                outputStream = socket.getOutputStream(); // getting output streams of client socket
                while ((count = bufferedInputStream.read(temp)) > 0) { // read bytes from file
                    outputStream.write(temp, 0, count); // write bytes into socket
                    outputStream.flush(); // flush oustput stream
                }
                socket.close(); // close client socket
                sent = true; // true is send process is successfull
            } catch (IOException | NumberFormatException e) {
                Logger.getLogger(Compress.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                try {
                    if (!Objects.equals(bufferedInputStream, null)) {
                        bufferedInputStream.close();
                    }
                    if (!Objects.equals(outputStream, null)) {
                        outputStream.close();
                    }
                    if (!Objects.equals(serverSocket, null)) {
                        serverSocket.close();
                    }
                    if (!Objects.equals(socket, null)) {
                        if (!socket.isClosed()) {
                            socket.close();
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Compress.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        thread.start(); // start thread
        while (thread.isAlive()); // wait for process of thread
        if (sent) { // if sending process is successful or not; warn user
            JOptionPane.showMessageDialog(null, "Datas were sent succesfully!");
        } else {
            JOptionPane.showMessageDialog(null, "Sent is unsuccessful!");
        }
    }

    /**
     * This method preparation of compression process. If selectedFiles[] is
     * empty; warn user and return back. Inside infinite loop file name will be
     * taken from user. After entering file name a private method will be
     * invoked.
     *
     * @throws java.io.FileNotFoundException
     */
    public void compress() throws FileNotFoundException, IOException {
        if (!Objects.equals(compressed_file, null)) {
            JOptionPane.showMessageDialog(null, "File is already compressed");
            return;
        }
        if (Objects.equals(CompressionOperations.selectedFiles, null)) { // if user has not selected files from directory
            JOptionPane.showMessageDialog(null, "Please choose files first"); // warn user
            return; //return back
        }
        for (;;) { // infinite loop
            file_name = JOptionPane.showInputDialog(null, "Please enter a name for compressed file!"); // user will enter file name
            if (!Objects.equals(file_name, null)) { // if user has not yet entered file name continue until entering a file name
                break; // if user entered file name break infinite loop
            }
        }
        if (!Objects.equals(file_name.length(), 0)) { // if file name entered
            doCompress(); //invoke doCompress() method
            return;
        }
        JOptionPane.showMessageDialog(null, "Files are not compressed!");
    }

    /**
     * This method compress chosen files. After given file name, compressed_file
     * will be created with specified path and file extension. fileOutputStream,
     * gZIPOutputStream will be created. If compressed_file does not exist, a
     * new file will be created. A byte buffer is created. Within a loop
     * selected files are compressed. Finally streams are closed.
     *
     * This method is not visible for created object from this class.
     *
     * @throws IOException
     */
    private void doCompress() throws IOException {
        compressed_file = new File(file_path + file_name + extension); //compressed_file creation
        fileOutputStream = new FileOutputStream(compressed_file); //fileOutputStream is created for writing datas to zip file
        gZIPOutputStream = new ZipOutputStream(fileOutputStream);
        if (!compressed_file.exists()) { //if compressed_file does not exist
            compressed_file.createNewFile(); //new file will be created
        }
        byte[] buffer = new byte[6000]; // a byte buffer array creation with length of 6000
        int len; // length
        for (File f : selectedFiles) { //getting selected files that selected by user
            gZIPOutputStream.putNextEntry(new ZipEntry(f.getName())); // //putting entry
            fileInputStream = new FileInputStream(f); //reads streams from selected file
            while ((len = fileInputStream.read(buffer)) > 0) { // while readed byte from selected file is greater tahn 0
                gZIPOutputStream.write(buffer, 0, len); // write streams to compressed_file 
            }
        }
        fileInputStream.close(); //close fileInputStream
        gZIPOutputStream.closeEntry(); //close entry
        gZIPOutputStream.close(); //close zip streams
        JOptionPane.showMessageDialog(null, "File saved to your Desktop as " + file_name + extension); // show user saving
    }
}
