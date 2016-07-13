package core.runtime;

import org.overture.webide.processor.ProcessingResult;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class RuntimeSocketClient extends Thread {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ServerSocket serverSocket;
    private Socket socket;
    private Process process;

    public RuntimeSocketClient(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public synchronized void run() {
        init();
    }

    private void init() {
        try {
            this.socket = this.serverSocket.accept();
            this.out = new ObjectOutputStream(this.socket.getOutputStream());
            this.in = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized ProcessingResult process(List<File> files) {
        ProcessingResult result = null;
        try {
            this.out.writeObject(files);
            this.out.flush();
            result = (ProcessingResult) this.in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
        }
        return result;
    }

    public synchronized void close() {
        try {
            this.in.close();
            this.out.close();
            this.socket.close();
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean isProcessAlive() {
        return this.process != null && this.process.isAlive();
    }

    public void setProcess(Process process) {
        this.process = process;
    }
}