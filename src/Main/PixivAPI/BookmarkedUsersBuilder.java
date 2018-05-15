package Main.PixivAPI;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Vector;

public class BookmarkedUsersBuilder
{
    final private String sessionID;
    final private String ID;

    private int pageNumber = -1;

    BookmarkedUsersBuilder(String sessionID, String ID)
    {
        this.sessionID = sessionID;
        this.ID = ID;
    }

    public BookmarkedUsersBuilder page(int page)
    {
        pageNumber = page;
        return this;
    }

    public Vector<User> get()
    {
        Vector<User> results = new Vector<User>(48);
        Document html = null;
        Elements userList = null;
        Connection con;
        int currentPage = (pageNumber == -1) ? 1 : pageNumber;
        do
        {
            try
            {
                con = Jsoup.connect("https://www.pixiv.net/bookmark.php?id=7210261&type=user" +
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
        }while(pageNumber == -1 && userList.size() == 48);

        return results;
    }
}
