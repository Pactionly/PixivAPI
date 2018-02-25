package Main.PixivAPI;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Vector;

public class Bookmarks
{
    final private String sessionID;
    final private String ID;

    private String order = "";
    private int page = -1;

    Bookmarks(String sessionID, String ID)
    {
        this.sessionID = sessionID;
        this.ID = ID;
    }

    public Bookmarks page(int page)
    {
        this.page = page;
        return this;
    }

    public Bookmarks order(BookmarkOrder order)
    {
        switch (order)
        {
            case ASC:
                this.order = "asc";
                break;
            case DESC:
                this.order = "desc";
                break;
            case DATE_ASC:
                this.order = "date";
                break;
            case DATE_DESC:
                this.order = "date_d";
                break;
            default:
                this.order = "";
                break;
        }
        return this;
    }

    public Vector<Work> get() throws RuntimeException
    {
        Document html = null;
        Vector<Work> results;
        int totalNumber;
        Connection connect;

        try
        {
            connect = Jsoup.connect("https://www.pixiv.net/bookmark.php?id=" + ID + "&order=" + order +
                "&p=" + page);
            html = connect.cookie("PHPSESSID", sessionID).execute().parse();
        }
        catch(Exception e)
        {
            throw new RuntimeException("Bad Request");
        }

        String temp = html.getElementsByClass("count-badge").first().text();
        temp = temp.substring(0, temp.length() - 7);
        totalNumber = Integer.parseInt(temp);

        if(page != -1)
        {
            results = new Vector<Work>(20);
        }
        else
        {
            results = new Vector<Work>(totalNumber);
        }

        Elements images = html.getElementsByClass("image-item");
        for(Element ele : images)
        {
            temp = ele.child(0).attr("href");
            temp = temp.substring(temp.indexOf("illust_id=") + 10);
            results.add(new Work(sessionID, temp));
        }
        if(page != -1)
        {
            return results;
        }

        for(int i = 2; (i * 20) <= totalNumber; i++)
        {
            try
            {
                connect = Jsoup.connect("https://www.pixiv.net/bookmark.php?id=" + ID + "&order=" +
                        order + "&p=" + i);
                html = connect.cookie("PHPSESSID", sessionID).execute().parse();
            }
            catch(Exception e)
            {
                throw new RuntimeException("Bad Request");
            }
            images = html.getElementsByClass("image-item");
            for(Element ele : images)
            {
                temp = ele.child(0).attr("href");
                temp = temp.substring(temp.indexOf("illust_id=") + 10);
                results.add(new Work(sessionID, temp));
            }
        }

        return results;
    }
}
