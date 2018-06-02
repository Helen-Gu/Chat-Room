package chatroom.util;

public class User
{
    private String name;
    private String ip;

    public User(String name, String ip)
    {
        this.name = name;
        this.ip = ip;
    }

    public String getName()
    {
        return name;
    }

    public String getIp()
    {
        return ip;
    }
}  
