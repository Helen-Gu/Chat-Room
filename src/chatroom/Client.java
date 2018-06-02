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
import java.net.Socket;  
import java.util.HashMap;  
import java.util.Map;  
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
  
public class Client{  
  
    private JFrame frame;  
    private JList userList;  
    private JTextArea textArea;  
    private JTextField textField;  
    private JTextField txt_port;  
    private JTextField txt_hostIp;  
    private JTextField txt_name;  
    private JButton btn_start;  
    private JButton btn_stop;  
    private JButton btn_send;  
    private JPanel northPanel;  
    private JPanel southPanel;  
    private JScrollPane rightScroll;  
    private JScrollPane leftScroll;  
    private JSplitPane centerSplit;  
  
    private DefaultListModel listModel;  
    private boolean isConnected = false;  
  
    private Socket socket;  
    private PrintWriter writer;  
    private BufferedReader reader;  
    private MessageThread messageThread;//thread that is responsible for receiving message
    private Map<String, User> onLineUsers = new HashMap<String, User>();//all online users
  
    public static void main(String[] args) {  
        new Client();  
    }  
  
    public void send() {  
        if (!isConnected) {  
            JOptionPane.showMessageDialog(frame, "Not connected to the server yet, you cannot send message!!", "ERROR",  
                    JOptionPane.ERROR_MESSAGE);  
            return;  
        }  
        String message = textField.getText().trim();  
        if (message == null || message.equals("")) {  
            JOptionPane.showMessageDialog(frame, "Message cannot be empty!!", "ERROR",  
                    JOptionPane.ERROR_MESSAGE);  
            return;  
        }  
        sendMessage(frame.getTitle() + "@" + "ALL" + "@" + message);  
        textField.setText(null);  
    }  
  
    //client constructor 
    public Client() {  
        textArea = new JTextArea();  
        textArea.setEditable(false);  
        textArea.setForeground(Color.blue);  
        textField = new JTextField();  
        txt_port = new JTextField("6666");  
        txt_hostIp = new JTextField("127.0.0.1");  
        txt_name = new JTextField("Helen");  
        btn_start = new JButton("Connect");  
        btn_stop = new JButton("DisConnect");  
        btn_send = new JButton("Send");  
        listModel = new DefaultListModel();  
        userList = new JList(listModel);  
  
        northPanel = new JPanel();  
        northPanel.setLayout(new GridLayout(1, 7));  
        northPanel.add(new JLabel("Port"));  
        northPanel.add(txt_port);  
        northPanel.add(new JLabel("Server IP"));  
        northPanel.add(txt_hostIp);  
        northPanel.add(new JLabel("Name"));  
        northPanel.add(txt_name);  
        northPanel.add(btn_start);  
        northPanel.add(btn_stop);  
        northPanel.setBorder(new TitledBorder("Connection Info"));  
  
        rightScroll = new JScrollPane(textArea);  
        rightScroll.setBorder(new TitledBorder("Message Display Field"));  
        leftScroll = new JScrollPane(userList);  
        leftScroll.setBorder(new TitledBorder("Users Online"));  
        southPanel = new JPanel(new BorderLayout());  
        southPanel.add(textField, "Center");  
        southPanel.add(btn_send, "East");  
        southPanel.setBorder(new TitledBorder("Write Message"));  
  
        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll,  
                rightScroll);  
        centerSplit.setDividerLocation(100);  
  
        frame = new JFrame("Client");  
        frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Client.class.getResource("qq.png")));  
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
  
        //enter key is pressed
        textField.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent arg0) {  
                send();  
            }  
        });  
  
        //btn_send is clicked
        btn_send.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                send();  
            }  
        });  
  
        //btn_start is clicked
        btn_start.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                int port;  
                if (isConnected) {  
                    JOptionPane.showMessageDialog(frame, "Already connected, do not reconnect!!",  
                            "ERROR", JOptionPane.ERROR_MESSAGE);  
                    return;  
                }  
                try {  
                    try {  
                        port = Integer.parseInt(txt_port.getText().trim());  
                    } catch (NumberFormatException e2) {  
                        throw new Exception("Port number has to be integer!");  
                    }  
                    String hostIp = txt_hostIp.getText().trim();  
                    String name = txt_name.getText().trim();  
                    if (name.equals("") || hostIp.equals("")) {  
                        throw new Exception("Name or server IP can not be empty!");  
                    }  
                    boolean flag = connectServer(port, hostIp, name);  
                    if (flag == false) {  
                        throw new Exception("Fail to connect to the server!!");  
                    }  
                    frame.setTitle(name);  
                    JOptionPane.showMessageDialog(frame, "Successfully connected!!");  
                } catch (Exception exc) {  
                    JOptionPane.showMessageDialog(frame, exc.getMessage(),  
                            "ERROR", JOptionPane.ERROR_MESSAGE);  
                }  
            }  
        });  
  
        //btn_stop is clicked
        btn_stop.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                if (!isConnected) {  
                    JOptionPane.showMessageDialog(frame, "Already disconnected, do not redisconnect!!",  
                            "ERROR", JOptionPane.ERROR_MESSAGE);  
                    return;  
                }  
                try {  
                    boolean flag = closeConnection();//break connection
                    if (flag == false) {  
                        throw new Exception("Something wrong with disconnection");  
                    }  
                    JOptionPane.showMessageDialog(frame, "Successfully disconnected!!");  
                } catch (Exception exc) {  
                    JOptionPane.showMessageDialog(frame, exc.getMessage(),  
                            "ERROR", JOptionPane.ERROR_MESSAGE);  
                }  
            }  
        });  
  
        //window is closed
        frame.addWindowListener(new WindowAdapter() {  
            public void windowClosing(WindowEvent e) {  
                if (isConnected) {  
                    closeConnection();  
                }  
                System.exit(0);//program exit
            }  
        });  
    }  
  
    /**  
     * connect to server
     *   
     * @param port  
     * @param hostIp  
     * @param name  
     */  
    public boolean connectServer(int port, String hostIp, String name) {  
        try {  
            socket = new Socket(hostIp, port);//build connection based on port and server IP
            writer = new PrintWriter(socket.getOutputStream());  
            reader = new BufferedReader(new InputStreamReader(socket  
                    .getInputStream()));  
            //send info about client to server
            sendMessage(name + "@" + socket.getLocalAddress().toString());  
            //start a messageThread to receive new message
            messageThread = new MessageThread(reader, textArea);  
            messageThread.start();  
            isConnected = true;//connected to server  
            return true;  
        } catch (Exception e) {  
            textArea.append("Fail to connect tp server with port number: " + port + "  and IP address： " + hostIp  
                    + " !" + "\r\n");  
            isConnected = false;//failed to connect to server
            return false;  
        }  
    }  
  
    /**  
     * send message  
     *   
     * @param message  
     */  
    public void sendMessage(String message) {  
        writer.println(message);  
        writer.flush();  
    }  
  
    /**  
     * Client stopped connection
     */  
    @SuppressWarnings("deprecation")  
    public synchronized boolean closeConnection() {  
        try {  
            sendMessage("CLOSE");// send close request to server
            messageThread.stop();//
            //release resource
            if (reader != null) {  
                reader.close();  
            }  
            if (writer != null) {  
                writer.close();  
            }  
            if (socket != null) {  
                socket.close();  
            }  
            isConnected = false;  
            return true;  
        } catch (IOException e1) {  
            e1.printStackTrace();  
            isConnected = true;  
            return false;  
        }  
    }  
  
    class MessageThread extends Thread {  
        private BufferedReader reader;  
        private JTextArea textArea;  
 
        public MessageThread(BufferedReader reader, JTextArea textArea) {  
            this.reader = reader;  
            this.textArea = textArea;  
        }  
  
        // forced to close
        public synchronized void closeCon() throws Exception {  
            //remove userlist
            listModel.removeAllElements();    
            if (reader != null) {  
                reader.close();  
            }  
            if (writer != null) {  
                writer.close();  
            }  
            if (socket != null) {  
                socket.close();  
            }  
            isConnected = false;
        }  
  
        public void run() {  
            String message = "";  
            while (true) {  
                try {  
                    message = reader.readLine();  
                    StringTokenizer stringTokenizer = new StringTokenizer(  
                            message, "/@");  
                    String command = stringTokenizer.nextToken();//command 
                    if (command.equals("CLOSE"))//server is closed command
                    {  
                        textArea.append("Server closed!\r\n");  
                        closeCon();
                        return;//end thread
                    } else if (command.equals("ADD")) {//update onLineUsers when new user is online 
                        String username = "";  
                        String userIp = "";  
                        if ((username = stringTokenizer.nextToken()) != null  
                                && (userIp = stringTokenizer.nextToken()) != null) {  
                            User user = new User(username, userIp);  
                            onLineUsers.put(username, user);  
                            listModel.addElement(username);  
                        }  
                    } else if (command.equals("DELETE")) {//update onLineUsers when user is offline  
                        String username = stringTokenizer.nextToken();  
                        User user = (User) onLineUsers.get(username);  
                        onLineUsers.remove(user);  
                        listModel.removeElement(username);  
                    } else if (command.equals("USERLIST")) {//load onLineUser list
                        int size = Integer  
                                .parseInt(stringTokenizer.nextToken());  
                        String username = null;  
                        String userIp = null;  
                        for (int i = 0; i < size; i++) {  
                            username = stringTokenizer.nextToken();  
                            userIp = stringTokenizer.nextToken();  
                            User user = new User(username, userIp);  
                            onLineUsers.put(username, user);  
                            listModel.addElement(username);  
                        }  
                    } else if (command.equals("MAX")) {//number of users reached maximum
                        textArea.append(stringTokenizer.nextToken()  
                                + stringTokenizer.nextToken() + "\r\n");  
                        closeCon();//forced to break connection
                        JOptionPane.showMessageDialog(frame, "Server buffer is full!", "ERROR",  
                                JOptionPane.ERROR_MESSAGE);  
                        return;//end thread
                    } else {//just regular message
                        textArea.append(message + "\r\n");  
                    }  
                } catch (IOException e) {  
                    e.printStackTrace();  
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
    }  
}  