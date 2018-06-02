package chatroom;

import chatroom.util.User;

import java.awt.BorderLayout;
import java.awt.Color;  
import java.awt.GridLayout;  
import java.awt.Toolkit;  
import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;  
import java.awt.event.WindowAdapter;  
import java.awt.event.WindowEvent;  
import java.io.BufferedReader;  
import java.io.IOException;  
import java.io.InputStreamReader;  
import java.io.PrintWriter;  
import java.net.BindException;  
import java.net.ServerSocket;  
import java.net.Socket;  
import java.util.ArrayList;  
import java.util.StringTokenizer;  
  
import javax.swing.DefaultListModel;  
import javax.swing.JButton;  
import javax.swing.JFrame;  
import javax.swing.JLabel;  
import javax.swing.JList;  
import javax.swing.JOptionPane;  
import javax.swing.JPanel;  
import javax.swing.JScrollPane;  
import javax.swing.JSplitPane;  
import javax.swing.JTextArea;  
import javax.swing.JTextField;  
import javax.swing.border.TitledBorder;  
  
public class Server {  
  
    private JFrame frame;  
    private JTextArea contentArea;  
    private JTextField txt_message;  
    private JTextField txt_max;  
    private JTextField txt_port;  
    private JButton btn_start;  
    private JButton btn_stop;  
    private JButton btn_send;  
    private JPanel northPanel;  
    private JPanel southPanel;  
    private JScrollPane rightPanel;  
    private JScrollPane leftPanel;  
    private JSplitPane centerSplit;  
    private JList userList;  
    private DefaultListModel listModel;  
  
    private ServerSocket serverSocket;  
    private ServerThread serverThread;  
    private ArrayList<ClientThread> clients;  
  
    private boolean isStart = false;  
  
    public static void main(String[] args) {  
        new Server();  
    }  
  
    public void send() {  
        if (!isStart) {  
            JOptionPane.showMessageDialog(frame, "Server not initialized yet!", "ERROR",  
                    JOptionPane.ERROR_MESSAGE);  
            return;  
        }  
        if (clients.size() == 0) {  
            JOptionPane.showMessageDialog(frame, "No users online yet!", "ERROR",  
                    JOptionPane.ERROR_MESSAGE);  
            return;  
        }  
        String message = txt_message.getText().trim();  
        if (message == null || message.equals("")) {  
            JOptionPane.showMessageDialog(frame, "Message is empty!", "ERROR",  
                    JOptionPane.ERROR_MESSAGE);  
            return;  
        }  
        sendServerMessage(message);
        contentArea.append("Server：" + txt_message.getText() + "\r\n");  
        txt_message.setText(null);  
    }  
  
    public Server() {  
        frame = new JFrame("Server");  
        frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Server.class.getResource("qq.png")));  
        contentArea = new JTextArea();  
        contentArea.setEditable(false);  
        contentArea.setForeground(Color.blue);  
        txt_message = new JTextField();  
        txt_max = new JTextField("30");  
        txt_port = new JTextField("6666");  
        btn_start = new JButton("Start");  
        btn_stop = new JButton("Stop");  
        btn_send = new JButton("Send");  
        btn_stop.setEnabled(false);  
        listModel = new DefaultListModel();  
        userList = new JList(listModel);  
  
        southPanel = new JPanel(new BorderLayout());  
        southPanel.setBorder(new TitledBorder("Write message"));  
        southPanel.add(txt_message, "Center");  
        southPanel.add(btn_send, "East");  
        leftPanel = new JScrollPane(userList);  
        leftPanel.setBorder(new TitledBorder("Online users"));  
  
        rightPanel = new JScrollPane(contentArea);  
        rightPanel.setBorder(new TitledBorder("Message Display Field"));  
  
        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,  
                rightPanel);  
        centerSplit.setDividerLocation(100);  
        northPanel = new JPanel();  
        northPanel.setLayout(new GridLayout(1, 6));  
        northPanel.add(new JLabel("User Limit"));  
        northPanel.add(txt_max);  
        northPanel.add(new JLabel("Port"));  
        northPanel.add(txt_port);  
        northPanel.add(btn_start);  
        northPanel.add(btn_stop);  
        northPanel.setBorder(new TitledBorder("Config info"));  
  
        frame.setLayout(new BorderLayout());  
        frame.add(northPanel, "North");  
        frame.add(centerSplit, "Center");  
        frame.add(southPanel, "South");  
        frame.setSize(600, 400);  
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;  
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;  
        frame.setLocation((screen_width - frame.getWidth()) / 2,  
                (screen_height - frame.getHeight()) / 2);  
        frame.setVisible(true);  
  
        //close window
        frame.addWindowListener(new WindowAdapter() {  
            public void windowClosing(WindowEvent e) {  
                if (isStart) {  
                    closeServer();
                }  
                System.exit(0);
            }  
        });  
  
        //Enter button
        txt_message.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                send();  
            }  
        });  
  
        //Send button
        btn_send.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent arg0) {  
                send();  
            }  
        });  
  
        //start Server  
        btn_start.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                if (isStart) {  
                    JOptionPane.showMessageDialog(frame, "Server initialzed, do not restart",  
                            "ERROR", JOptionPane.ERROR_MESSAGE);  
                    return;  
                }  
                int max;  
                int port;  
                try {  
                    try {  
                        max = Integer.parseInt(txt_max.getText());  
                    } catch (Exception e1) {  
                        throw new Exception("Max number of users should be positive integer!");  
                    }  
                    if (max <= 0) {  
                        throw new Exception("Max number of users should be positive integer!");  
                    }  
                    try {  
                        port = Integer.parseInt(txt_port.getText());  
                    } catch (Exception e1) {  
                        throw new Exception("Port number should be positive integer!");  
                    }  
                    if (port <= 0) {  
                        throw new Exception("Port number should be positive integer!");  
                    }  
                    serverStart(max, port);  
                    contentArea.append("Server initialized with maximum number of users： " + max + ", port：" + port  
                            + "\r\n");  
                    JOptionPane.showMessageDialog(frame, "Server successfully initialized!");  
                    btn_start.setEnabled(false);  
                    txt_max.setEnabled(false);  
                    txt_port.setEnabled(false);  
                    btn_stop.setEnabled(true);  
                } catch (Exception exc) {  
                    JOptionPane.showMessageDialog(frame, exc.getMessage(),  
                            "ERROR", JOptionPane.ERROR_MESSAGE);  
                }  
            }  
        });  
  
        btn_stop.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                if (!isStart) {  
                    JOptionPane.showMessageDialog(frame, "Server not initialized yet, do not stop.", "ERROR",  
                            JOptionPane.ERROR_MESSAGE);  
                    return;  
                }  
                try {  
                    closeServer();  
                    btn_start.setEnabled(true);  
                    txt_max.setEnabled(true);  
                    txt_port.setEnabled(true);  
                    btn_stop.setEnabled(false);  
                    contentArea.append("Server successfully stopped\r\n");  
                    JOptionPane.showMessageDialog(frame, "Server successfully stopped.");  
                } catch (Exception exc) {  
                    JOptionPane.showMessageDialog(frame, "Error in stopping server.", "ERROR",  
                            JOptionPane.ERROR_MESSAGE);  
                }  
            }  
        });  
    }  
  
    public void serverStart(int max, int port) throws java.net.BindException {  
        try {  
            clients = new ArrayList<ClientThread>();  
            serverSocket = new ServerSocket(port);  
            serverThread = new ServerThread(serverSocket, max);  
            serverThread.start();  
            isStart = true;  
        } catch (BindException e) {  
            isStart = false;  
            throw new BindException("Port number is in use, please choose another one.");  
        } catch (Exception e1) {  
            e1.printStackTrace();  
            isStart = false;  
            throw new BindException("Error in starting server.");  
        }  
    }  
  
    @SuppressWarnings("deprecation")  
    public void closeServer() {  
        try {  
            if (serverThread != null)  
                serverThread.stop();
  
            for (int i = clients.size() - 1; i >= 0; i--) {  
                clients.get(i).getWriter().println("CLOSE");  
                clients.get(i).getWriter().flush();  
                clients.get(i).stop();// 
                clients.get(i).reader.close();  
                clients.get(i).writer.close();  
                clients.get(i).socket.close();  
                clients.remove(i);  
            }  
            if (serverSocket != null) {  
                serverSocket.close();
            }  
            listModel.removeAllElements();
            isStart = false;  
        } catch (IOException e) {  
            e.printStackTrace();  
            isStart = true;  
        }  
    }  
  
    // send group text to all clients 
    public void sendServerMessage(String message) {  
        for (int i = clients.size() - 1; i >= 0; i--) {  
            clients.get(i).getWriter().println("Server： " + message + " (for all)");  
            clients.get(i).getWriter().flush();  
        }  
    }  
  
    class ServerThread extends Thread {  
        private ServerSocket serverSocket;  
        private int max;
  
        public ServerThread(ServerSocket serverSocket, int max) {  
            this.serverSocket = serverSocket;  
            this.max = max;  
        }  
  
        public void run() {  
            while (true) {//keep waiting for connection request from client
                try {  
                    Socket socket = serverSocket.accept();  
                    if (clients.size() == max) {//if max user is reached 
                        BufferedReader r = new BufferedReader(  
                                new InputStreamReader(socket.getInputStream()));  
                        PrintWriter w = new PrintWriter(socket  
                                .getOutputStream());  
                       
                        String inf = r.readLine();  
                        StringTokenizer st = new StringTokenizer(inf, "@");  
                        User user = new User(st.nextToken(), st.nextToken());
                        //send a successful connection response
                        w.println("MAX@Server: Sorry, " + user.getName() + ' '+  
                                user.getIp() + ", the server reached its max user limit, please try again later!!");  
                        w.flush();  
                        //release resource
                        r.close();  
                        w.close();  
                        socket.close();  
                        continue;  
                    }  
                    ClientThread client = new ClientThread(socket);  
                    client.start();// start this server thread  
                    clients.add(client);  
                    listModel.addElement(client.getUser().getName());//update user list 
                    contentArea.append(client.getUser().getName()  
                            + client.getUser().getIp() + " is online!\r\n");  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
    }  
  
    class ClientThread extends Thread {  
        private Socket socket;  
        private BufferedReader reader;  
        private PrintWriter writer;  
        private User user;  
  
        public BufferedReader getReader() {  
            return reader;  
        }  
  
        public PrintWriter getWriter() {  
            return writer;  
        }  
  
        public User getUser() {  
            return user;  
        }  
  
        public ClientThread(Socket socket) {  
            try {  
                this.socket = socket;  
                reader = new BufferedReader(new InputStreamReader(socket  
                        .getInputStream()));  
                writer = new PrintWriter(socket.getOutputStream());  
                String inf = reader.readLine();  
                StringTokenizer st = new StringTokenizer(inf, "@");  
                user = new User(st.nextToken(), st.nextToken());  
                writer.println(user.getName() + user.getIp() + " is successfully connected to the server!");  
                writer.flush();  
                if (clients.size() > 0) {  
                    String temp = "";  
                    for (int i = clients.size() - 1; i >= 0; i--) {  
                        temp += (clients.get(i).getUser().getName() + "/" + clients  
                                .get(i).getUser().getIp())  
                                + "@";  
                    }  
                    writer.println("USERLIST@" + clients.size() + "@" + temp);  
                    writer.flush();  
                }  
                //notify all online users about this new online user
                for (int i = clients.size() - 1; i >= 0; i--) {  
                    clients.get(i).getWriter().println(  
                            "ADD@" + user.getName() + user.getIp());  
                    clients.get(i).getWriter().flush();  
                }  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
  
        @SuppressWarnings("deprecation")  
        public void run() {//accept client's message and process  
            String message = null;  
            while (true) {  
                try {  
                    message = reader.readLine();//get client's message
                    if (message.equals("CLOSE"))//be offline
                    {  
                        contentArea.append(this.getUser().getName()  
                                + this.getUser().getIp() + "is offline!\r\n");  
                        //release resource
                        reader.close();  
                        writer.close();  
                        socket.close();  
  
                        //tell other users that this user is offline  
                        for (int i = clients.size() - 1; i >= 0; i--) {  
                            clients.get(i).getWriter().println(  
                                    "DELETE@" + user.getName());  
                            clients.get(i).getWriter().flush();  
                        }  
  
                        listModel.removeElement(user.getName());//update online user
  
                        //delete this server thread
                        for (int i = clients.size() - 1; i >= 0; i--) {  
                            if (clients.get(i).getUser() == user) {  
                                ClientThread temp = clients.get(i);  
                                clients.remove(i);//delete this user thread  
                                temp.stop();
                                return;  
                            }  
                        }  
                    } else {  
                        dispatcherMessage(message); //send to another person
                    }  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
  
        public void dispatcherMessage(String message) {  
            StringTokenizer stringTokenizer = new StringTokenizer(message, "@");  
            String source = stringTokenizer.nextToken();  
            String owner = stringTokenizer.nextToken();  
            String content = stringTokenizer.nextToken();  
            message = source + " :  " + content;  
            contentArea.append(message + "\r\n");  
            if (owner.equals("ALL")) {//group text
                for (int i = clients.size() - 1; i >= 0; i--) {  
                    clients.get(i).getWriter().println(message + "(for all.)");  
                    clients.get(i).getWriter().flush();  
                }  
            }  
        }  
    }  
} 