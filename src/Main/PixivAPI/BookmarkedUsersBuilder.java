package Main.PixivAPI;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Vector;

public class BookmarkedUsersBuilder
{
    final private String sessionID;
    final private String ID;

    private int pageNumber;

    BookmarkedUsersBuilder(String sessionID, String ID)
    {
        this.sessionID = sessionID;
        this.ID = ID;
        this.pageNumber = 1;
    }

    public BookmarkedUsersBuilder page(int page)
    {
        if(page < 1)
        {
            throw new IllegalArgumentException("pageNumber cannot be less than 1");
        }
        pageNumber = page;
        return this;
    }

    public List<User> get()
    {
        Vector<User> results = new Vector<>(48);
        Document html;
        Elements userList;
        Connection con;
        try
        {
            con = Jsoup.connect("https://www.pixiv.net/bookmark.php?id=" + ID + "&type=user" +
                    "&p=" + pageNumber);
            html = con.cookie("PHPSESSID", sessionID).execute().parse();
        }
        catch (java.io.IOException e)
        {
            throw new RuntimeException("Bad Request");
        }
        userList = html.getElementsByClass("userdata");
        for(Element ele : userList)
        {
            results.add(new User(sessionID, ele.child(0).attr("data-user_id")));
        }

        return results;
    }

    public List<User> getAll()
    {
        Vector<User> results;
        Document html;
        Elements userList;
        Connection con;
        int currentPage = 1;
        int totalBookmarks;
        try
        {
            con = Jsoup.connect("https://www.pixiv.net/bookmark.php?id=" + ID + "&type=user" +
                    "&p=" + currentPage);
            html = con.cookie("PHPSESSID", sessionID).execute().parse();
        }
        catch (java.io.IOException e)
        {
            throw new RuntimeException("Bad Request");
        }

        Elements ele;
        ele = html.getElementsByClass("count-badge");
        String temp;
        temp = ele.first().text();
        totalBookmarks = Integer.parseInt(temp);
        results = new Vector<>(totalBookmarks);

        userList = html.getElementsByClass("userdata");
        for(Element e : userList)
        {
            results.add(new User(sessionID, e.child(0).attr("data-user_id")));
        }

        while(totalBookmarks > results.size())
        {
            currentPage++;
            try
            {
                con = Jsoup.connect("https://www.pixiv.net/bookmark.php?id=" + ID + "&type=user" +
                        "&p=" + currentPage);
                html = con.cookie("PHPSESSID", sessionID).execute().parse();
            }
            catch (java.io.IOException e)
            {
                throw new RuntimeException("Bad Request");
            }
            userList = html.getElementsByClass("userdata");
            for(Element e : userList)
            {
                results.add(new User(sessionID, e.child(0).attr("data-user_id")));
            }
        }
        return results;
    }
}
