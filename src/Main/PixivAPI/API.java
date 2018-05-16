package Main.PixivAPI;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.*;


public class API
{
    private final String USER_AGENT = "Mozilla/5.0";
    private String sessionID;

    public API()
    {

    }


    public void login(String username, String password) {
        try {
            Connection con = Jsoup.connect(
                    "https://accounts.pixiv.net/login");
            Connection.Response response = con.userAgent(USER_AGENT).execute();
            String postKey = response.parse().getElementsByAttributeValueContaining("name","post_key")
                    .attr("value");
            String postBody = "pixiv_id=" + username + "&password=" + password + "&post_key=" + postKey;
            sessionID = con.requestBody(postBody)
                    .userAgent(USER_AGENT)
                    .method(Connection.Method.POST)
                    .execute()
                    .cookie("PHPSESSID");

        }catch(IOException i) {
            System.err.println("Login Failed.");
            System.err.println(i.getMessage());
        }
    }

    public User getUser(String id)
    {
        return new User(sessionID,id);
    }

    public Work getWork(String id)
    {
        return new Work(sessionID, id);
    }

    //TODO viewing restriction tests (user set to not show r18, search anyway)
    public RankingWorksBuilder rankingWorks()
    {
        return new RankingWorksBuilder(sessionID);
    }
}
